package org.alnet.allnet_android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_contact.*

class ContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        val file = filesDir
        val path = file.absolutePath

        val networkAPI = NetworkAPI(path)
    }


    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    external fun init()
}


