package testgame.ui.main.fight

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainFightRoomBinding
import kotlinx.coroutines.*
import testgame.activities.EntranceActivity
import testgame.activities.MainActivity
import testgame.data.FightAction
import testgame.data.GameApp
import testgame.data.GameApp.Companion.ATTACK_ANIMATION_PLAY_DELAY
import testgame.data.Match
import testgame.ui.main.ProgressBar
import testgame.ui.main.featuresInventory.InventoryItemListener
import testgame.ui.main.featuresInventory.InventoryRecyclerAdapter
import timber.log.Timber

class FightFragment : Fragment() {

    private lateinit var viewModel: FightViewModel
    private lateinit var viewModelFactory: FightViewModelFactory

    private lateinit var playerWarriorImage: ImageView
    private lateinit var enemyWarriorImage: ImageView
    private lateinit var timeTextView: TextView
    private lateinit var myHealthBar: ProgressBar
    private lateinit var enemyHealthBar: ProgressBar

    var mediaPlayer: MediaPlayer? = null

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setMusic()

        val app: GameApp = this.activity?.application as GameApp
        val binding: FragmentMainFightRoomBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_main_fight_room,
                container,
                false
        )

        setUpViewModel(app)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        timeTextView = binding.timeTextView
        myHealthBar = binding.myHealthBar
        enemyHealthBar = binding.enemyHealthBar
        playerWarriorImage = binding.myImageView
        enemyWarriorImage = binding.enemyImageView

        setUpInventory(binding)

        coroutineScope.launch {
            (playerWarriorImage.drawable as AnimationDrawable).start()
            (enemyWarriorImage.drawable as AnimationDrawable).start()
        }

        viewModel.logInfo.observe(viewLifecycleOwner, {
            Timber.i(viewModel.logInfo.value)
        })

        viewModel.action.observe(viewLifecycleOwner, { action ->
            binding.actionTextView.text = action
        })

        viewModel.playerWantToEscape.observe(viewLifecycleOwner, { wantToEscape ->
            if (wantToEscape) {
                val activity = activity as MainActivity
                val dialog = activity.buildOnEscapeDialog().setPositiveButton(R.string.yes) { _, _ ->
                    viewModel.confirmMatchEscape()
                }.create()
                dialog.show()
            }
        })

        viewModel.fightAction.observe(viewLifecycleOwner, { action ->
            handleGameAction(action)
            viewModel.onActionHandled()
        })

        viewModel.matchState.observe(viewLifecycleOwner, { state ->
            handleMatchState(binding, state)
        })

        viewModel.currentFightMenuOption.observe(viewLifecycleOwner, { option ->
            when (option) {
                FightViewModel.FightMenuOption.ATTACK -> {
                    binding.inventoryWindow.visibility = View.GONE
                    binding.skillsWindow.visibility = View.GONE
                    binding.attackWindow.visibility = View.VISIBLE
                }
                FightViewModel.FightMenuOption.INVENTORY -> {
                    binding.attackWindow.visibility = View.GONE
                    binding.skillsWindow.visibility = View.GONE
                    binding.inventoryWindow.visibility = View.VISIBLE
                }
                FightViewModel.FightMenuOption.SKILLS -> {
                    binding.attackWindow.visibility = View.GONE
                    binding.inventoryWindow.visibility = View.GONE
                    binding.skillsWindow.visibility = View.VISIBLE
                }
                else -> {
                    Timber.i("Null enum class of fight menu options")
                }
            }
        })
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        viewModel.confirmMatchEscape()
    }
    private fun setMusic() {
        mediaPlayer = MediaPlayer.create(activity, R.raw.fight_activity_music)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }
    override fun onPause() {
        super.onPause()
        if(mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }
    override fun onResume() {
        super.onResume()
        if(mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    private fun handleGameAction(action: FightAction?) {
        if (action == null) {
            return
        }
        coroutineScope.launch {
            when (action) {
                FightAction.PLAYER_ATTACK -> {
                    (playerWarriorImage.drawable as AnimationDrawable).stop()
                    playerWarriorImage.setImageResource(R.drawable.animation_warrior_attack)
                    val attackPlayerWarrior = playerWarriorImage.drawable as AnimationDrawable
                    attackPlayerWarrior.start()
                    MediaPlayer.create(activity, R.raw.punch_sound).start()
                    delay(ATTACK_ANIMATION_PLAY_DELAY)
                    playerWarriorImage.setImageResource(R.drawable.animation_warrior_idle)
                    (playerWarriorImage.drawable as AnimationDrawable).start()
                }
                FightAction.ENEMY_ATTACK -> {
                    (enemyWarriorImage.drawable as AnimationDrawable).stop()
                    enemyWarriorImage.setImageResource(R.drawable.animation_warrior_attack)
                    val attackPlayerWarrior = enemyWarriorImage.drawable as AnimationDrawable
                    attackPlayerWarrior.start()
                    delay(ATTACK_ANIMATION_PLAY_DELAY)
                    enemyWarriorImage.setImageResource(R.drawable.animation_warrior_idle)
                    (enemyWarriorImage.drawable as AnimationDrawable).start()
                }
            }

        }
    }

    private fun handleMatchState(binding: FragmentMainFightRoomBinding, state: Match.State) {
        val actionsLayout = binding.actionLayout
        when (state) {
            Match.State.MY_TURN -> {
                setViewAndChildrenEnabled(actionsLayout, true)
                val myImageView = binding.myImageView
                myImageView.bringToFront()
            }
            Match.State.ENEMY_TURN -> {
                setViewAndChildrenEnabled(actionsLayout, false)
                val enemyImageView = binding.enemyImageView
                enemyImageView.bringToFront()
            }
            Match.State.NO_MATCH -> {
                activity?.supportFragmentManager?.let { MatchEndDialogFragment.newInstance(viewModel.matchWinner).show(it, MatchEndDialogFragment.TAG) }
                viewModel.confirmMatchRoomExit()
            }
            else -> {
                Timber.i(state.toString())
            }
        }
    }

    private fun setUpViewModel(app: GameApp) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val token = sharedPreferences.getString(getString(R.string.saved_token_key), null)
        if (token == null) {
            val intent = Intent(activity, EntranceActivity::class.java)
            startActivity(intent)
        } else {
            viewModelFactory = FightViewModelFactory(app, token)
            viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(FightViewModel::class.java)
        }
    }

    private fun setUpInventory(binding: FragmentMainFightRoomBinding) {
        try {
            val adapter = InventoryRecyclerAdapter(InventoryItemListener {
                itemId -> Timber.i("Inventory item with $itemId was clicked")
            })
            val manager = GridLayoutManager(activity, 3)
            binding.inventoryRecyclerView.layoutManager = manager
            binding.inventoryRecyclerView.adapter = adapter

            viewModel.inventoryItems.observe(viewLifecycleOwner, {
                it?.let {
                    adapter.submitList(it)
                }
            })
        } catch (exception: IllegalStateException) {
            Timber.i(exception)
        }
    }

    private fun setViewAndChildrenEnabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                setViewAndChildrenEnabled(child, enabled)
            }
        }
    }
}
