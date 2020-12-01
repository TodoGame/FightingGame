package testgame.ui.main.fight

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainFightRoomBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import testgame.activities.EntranceActivity
import testgame.activities.FightActivity
import testgame.activities.MainActivity
import testgame.data.GameApp
import testgame.data.GameApp.Companion.ATTACK_ANIMATION_PLAY_DELAY
import testgame.data.Match
import timber.log.Timber

class FightFragment : Fragment() {

    private lateinit var viewModel: FightViewModel
    private lateinit var viewModelFactory: FightViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        setUpInventory(binding)

        val actionTextView = binding.actionTextView
        val timeTextVIew = binding.timeTextView
        val myHealthBar = binding.myHealthBar
        val enemyHealthBar = binding.enemyHealthBar

        val playerWarriorImage = binding.myImageView
        val enemyWarriorImage = binding.enemyImageView
        var idlePlayerWarrior = playerWarriorImage.drawable as AnimationDrawable
        var idleEnemyWarrior = enemyWarriorImage.drawable as AnimationDrawable
        idlePlayerWarrior.start()
        idleEnemyWarrior.start()

        viewModel.infoDisplayIsCalled.observe(viewLifecycleOwner, { isCalled ->
            if (isCalled) {
                val errorString = viewModel.toastInfo
                Timber.i(errorString.value)
                viewModel.onErrorDisplayed()
            }
        })

        viewModel.playerWantToEscape.observe(viewLifecycleOwner, { wantToEscape ->
            if (wantToEscape) {
                val activity = activity as FightActivity
                val dialog = activity.buildOnEscapeDialog().setPositiveButton(R.string.yes) { _, _ ->
                    viewModel.confirmMatchEscape()
                }.create()
                dialog.show()
            }
        })

        println("Fragment. Test1")
        GlobalScope.launch {
            viewModel.attackingPlayer.observe(viewLifecycleOwner, { attackingPlayer ->
                println("Fragment. AtackingPlayer: $attackingPlayer")
                if (attackingPlayer == FightViewModel.AttackingPlayer.PLAYER) {
                    idlePlayerWarrior.stop()
                    playerWarriorImage.setImageResource(R.drawable.animation_warrior_idle)
                    val attackPlayerWarrior = playerWarriorImage.drawable as AnimationDrawable
                    attackPlayerWarrior.start()
                    Thread.sleep(ATTACK_ANIMATION_PLAY_DELAY)
                    idlePlayerWarrior = playerWarriorImage.drawable as AnimationDrawable
                    idlePlayerWarrior.start()
                } else if (attackingPlayer == FightViewModel.AttackingPlayer.ENEMY) {
                    idleEnemyWarrior.stop()
                    enemyWarriorImage.setImageResource(R.drawable.animation_warrior_idle)
                    val attackEnemyWarrior = enemyWarriorImage.drawable as AnimationDrawable
                    GlobalScope.launch {
                        attackEnemyWarrior.start()
                        delay(ATTACK_ANIMATION_PLAY_DELAY)
                        idleEnemyWarrior = enemyWarriorImage.drawable as AnimationDrawable
                        idleEnemyWarrior.start()
                    }
                }
            })
        }
        println("Fragment. Test2")
        viewModel.action.observe(viewLifecycleOwner, { action ->
            println("Fragment. Action value: $action")
            binding.actionTextView.text = action
        })
        viewModel.matchState.observe(viewLifecycleOwner, { state ->
            println("Fragment. State value: $state")
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
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.putExtra(getString(R.string.match_finish_winner_extra_key), viewModel.matchWinner)
                    viewModel.confirmMatchRoomExit()
                    startActivity(intent)
                }
                else -> {
                    Timber.i(state.toString())
                }
            }
        })

        viewModel.currentFightMenuOption.observe(viewLifecycleOwner, { option ->
            println("Fragment. Option: $option")
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
        println("Fragment. Test5")
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Fragment. OnDestroy")
        viewModel.confirmMatchEscape()
    }

    private fun setUpViewModel(app: GameApp) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val token = sharedPreferences.getString(getString(R.string.saved_token_key), null)
        if (token == null) {
            val intent = Intent(activity, EntranceActivity::class.java)
            startActivity(intent)
        } else {
            viewModelFactory = FightViewModelFactory(app, token)
            viewModel = ViewModelProvider(this, viewModelFactory).get(FightViewModel::class.java)
        }
    }

    private fun setUpInventory(binding: FragmentMainFightRoomBinding) {
        try {

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