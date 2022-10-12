package com.simple.player.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.simple.player.Store
import com.simple.player.activity.BaseActivity
import com.simple.player.view.layout.LayoutParser

class CustomFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parser = LayoutParser(requireContext())
        val layout = """
            Text(text = "敬请期待", textColor = "#ff0000", textSize = "24")
        """.trimIndent()
        val linearLayout = LinearLayout(requireContext())
        linearLayout.background = BaseActivity.windowBackground
        linearLayout.gravity = Gravity.CENTER
        linearLayout.orientation = LinearLayout.VERTICAL
        return parser.parse(layout, parent = linearLayout)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

}