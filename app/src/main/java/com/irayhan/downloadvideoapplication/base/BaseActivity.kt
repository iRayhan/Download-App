package com.irayhan.downloadvideoapplication.base

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.irayhan.downloadvideoapplication.R

abstract class BaseActivity<B : ViewDataBinding> : AppCompatActivity() {

    @get:LayoutRes
    protected abstract val contentView: Int
    protected lateinit var binding: B

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, contentView)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        init(savedInstanceState)
    }

    protected abstract fun init(savedInstanceState: Bundle?)

    fun invokeActivity(cls: Class<*>?) {
        val intent = Intent(this, cls)
        startActivity(intent)
    }

    fun invokeActivity(cls: Class<*>?, key: String, value: String) {
        val intent = Intent(this, cls).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(key, value)
        }
        startActivity(intent)
    }

    fun invokeActivity(activity: Activity, cls: Class<*>?) {
        val intent = Intent(activity, cls)
        activity.startActivity(intent)
    }

    fun invokeActivityAndFinish(cls: Class<*>?) {
        val intent = Intent(this, cls).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    protected fun replaceFragment(fragment: Fragment, tag: String, allowBackTrack: Boolean) {
        if (allowBackTrack) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                .disallowAddToBackStack()
                .replace(R.id.fragment_container, fragment, tag)
                .commitAllowingStateLoss()
        }
    }

}