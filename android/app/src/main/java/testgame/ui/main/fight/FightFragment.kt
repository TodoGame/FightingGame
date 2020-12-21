package testgame.ui.main.fight

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainFightRoomBinding
import item.ItemType
import kotlinx.coroutines.*
import testgame.activities.EntranceActivity
import testgame.activities.MainActivity
import testgame.data.FightAction
import testgame.data.GameApp.Companion.ATTACK_ANIMATION_PLAY_DELAY
import testgame.data.Match
import testgame.data.Match.playerMaxHealth
import testgame.data.User
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
    private lateinit var playerHealthBar: ProgressBar
    private lateinit var enemyHealthBar: ProgressBar

    var mediaPlayer: MediaPlayer? = null

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val winner = viewModel.matchWinner
            if (winner == User.username.value) {
                val dialog = MainActivity().buildOnEscapeDialog().setPositiveButton(R.string.confirm) { _, _ ->
                    viewModel.confirmMatchEscape()
                }.create()
                dialog.show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setMusic()

        val binding: FragmentMainFightRoomBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_main_fight_room,
                container,
                false
        )

        setUpViewModel()
        binding.viewModel = viewModel
        binding.match = testgame.data.Match
        binding.lifecycleOwner = this

        playerHealthBar = binding.myHealthBar
        enemyHealthBar = binding.enemyHealthBar
        playerWarriorImage = binding.myImageView
        enemyWarriorImage = binding.enemyImageView

        setUpInventory(binding)

        coroutineScope.launch {
            (playerWarriorImage.drawable as AnimationDrawable).start()
            (enemyWarriorImage.drawable as AnimationDrawable).start()
        }

        Match.player.observe(viewLifecycleOwner, { player ->
            Match.playerMaxHealth?.let { playerHealthBar.update(it, player.health) } ?: Timber.i("Null max health")
            playerHealthBar.invalidate()
        })

        Match.enemy.observe(viewLifecycleOwner, { enemy ->
            Match.enemyMaxHealth?.let { enemyHealthBar.update(it, enemy.health) } ?: Timber.i("Null max health")
            playerHealthBar.invalidate()
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
            handleFightMenuOptionChange(binding, option)
        })
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
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
                val action = FightFragmentDirections.actionFightFragmentToLocationsFragment()
                val onConfirmFunction = {
                    NavHostFragment.findNavController(this).navigate(action)
                }
                activity?.supportFragmentManager?.let {
                    MatchEndDialogFragment.newInstance(viewModel.matchWinner, onConfirmFunction)
                            .show(it, MatchEndDialogFragment.TAG)
                }
//                onConfirmFunction()
                viewModel.confirmMatchEscape()
            }
            else -> {
                Timber.i(state.toString())
            }
        }
    }

    private fun handleFightMenuOptionChange(binding: FragmentMainFightRoomBinding, option: FightViewModel.FightMenuOption) {
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
        }
    }

    private fun setUpViewModel() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val token = sharedPreferences.getString(getString(R.string.saved_token_key), null)
        if (token == null) {
            val intent = Intent(activity, EntranceActivity::class.java)
            startActivity(intent)
        } else {
            viewModelFactory = FightViewModelFactory(token)
            viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(FightViewModel::class.java)
        }
    }

    private fun setUpInventory(binding: FragmentMainFightRoomBinding) {
        try {
            val adapter = InventoryRecyclerAdapter(InventoryItemListener {
                viewModel.attack(it.id)
                Timber.i("Inventory item with ${it.id} was clicked")
            })
            binding.inventoryRecyclerView.adapter = adapter

            User.inventory.observe(viewLifecycleOwner, {
                it?.let { list ->
                    adapter.submitList(list.filter { item ->
                        item.type != ItemType.MainWeapon
                    })
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
