package com.simple.player.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.simple.player.fragment.CustomFragment
import com.simple.player.fragment.HomeFragment

class HomePagerFragmentAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    private val fragments = arrayListOf(
        CustomFragment(),
        HomeFragment()
    )

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}