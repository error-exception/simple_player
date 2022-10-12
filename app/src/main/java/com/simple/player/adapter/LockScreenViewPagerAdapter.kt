package com.simple.player.adapter

import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import android.view.View
import java.util.ArrayList

class LockScreenViewPagerAdapter(private val viewsList: ArrayList<View>) : PagerAdapter() {
    override fun getCount(): Int {
        return viewsList.size
    }

    override fun isViewFromObject(p1: View, p2: Any): Boolean {
        return p1 === p2
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(viewsList[position])
        return viewsList[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(viewsList[position])
    }
}