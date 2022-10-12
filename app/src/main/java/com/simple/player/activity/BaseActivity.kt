package com.simple.player.activity

import android.content.ComponentName
import android.content.ServiceConnection
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.simple.player.R
import com.simple.player.Store
import com.simple.player.view.IconButton

open class BaseActivity() : AppCompatActivity(), ServiceConnection {

    private var mEvents: Events? = null
    private var mCustomActionBar: View? = null
    private var mLayoutId = R.layout.back_action_bar
    @DrawableRes
    private var mOptionIcon = 0
    lateinit var toolbar: Toolbar

    companion object {
        const val ICON_BACK = "\ue314"
        val windowBackground: ColorDrawable = ColorDrawable(0xFFEDEDED.toInt())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawable(windowBackground)
        mEvents = Events()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
    }

    //须在setContentView(int layoutResID)前使用
    var showActionBar: Boolean = true
        protected set(value) {
            field = value
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }

    override fun setContentView(layoutResID: Int) {
        //导入框架布局
        super.setContentView(R.layout.activity)
        val layout = findViewById<View>(R.id.activity_layout) as LinearLayout
        //导入用户布局
        val inflater = LayoutInflater.from(this)
        val v = inflater.inflate(layoutResID, layout, false)
//        v.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.MATCH_PARENT)

        layout.addView(v)
        //标题栏布局
        toolbar = findViewById(R.id.toolbar)
        if (showActionBar) {
            val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)
            appBarLayout.visibility = View.VISIBLE
            setSupportActionBar(toolbar)
            toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            toolbar.setNavigationOnClickListener {
                onActionBarBackPressed()
            }
        }
    }

    @DrawableRes
    var backIcon: Int? = null
        protected set(value) {
            field = value
            if (value != null) {
                toolbar.setNavigationIcon(value)
            } else {
                toolbar.navigationIcon = null
            }
        }

    var actionTitle: String = ""
        protected set(value) {
            field = value
            toolbar.title = value
        }

    @DrawableRes
    var optionIcon: Int? = 0
        protected set(value) {
            field = value
            mOptionIcon = value ?: 0
            invalidateOptionsMenu()
        }

    constructor(parcel: Parcel) : this() {
        mLayoutId = parcel.readInt()
    }

    open fun onActionBarBackPressed() {
        onBackPressed()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (mOptionIcon != 0 && menu != null) {
            val item = menu.findItem(R.id.activity_option)
            item.setIcon(mOptionIcon)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (mOptionIcon != 0) {
            menuInflater.inflate(R.menu.toolbar_menu, menu)
            return super.onCreateOptionsMenu(menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.activity_option) {
            onOptionPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    open fun onOptionPressed() {}

    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {}

    override fun onServiceDisconnected(componentName: ComponentName) {}

    private inner class Events : View.OnClickListener {
        override fun onClick(p1: View) {
            val id = p1.id
            if (id == R.id.action_back) {
                onActionBarBackPressed()
            } else if (id == R.id.action_option) {
                onOptionPressed()
            }
        }
    }

}