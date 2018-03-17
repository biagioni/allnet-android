package com.coutocode.allnet_android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_contact.*

class ContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        // Example of a call to a native method
        sample_text.text = Math_add(12,2).toString()
    }


    external fun Math_add(a: Int, b: Int): Int

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}
