package com.vipul.doco.ui.base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vipul.doco.R

class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }
}