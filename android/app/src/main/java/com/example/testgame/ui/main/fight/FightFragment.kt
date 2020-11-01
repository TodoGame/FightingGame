package com.example.testgame.ui.main.fight

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.testgame.R
import com.example.testgame.databinding.FragmentMainFightLocationsBinding

class FightFragment : Fragment() {
    private lateinit var viewModel: FightViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMainFightLocationsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main_fight_room,
            container,
            false)

        viewModel = ViewModelProvider(this).get(FightViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        return binding.root
    }
}