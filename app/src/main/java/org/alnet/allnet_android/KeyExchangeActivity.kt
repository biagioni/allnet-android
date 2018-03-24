package org.alnet.allnet_android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_key_exchange.*

class KeyExchangeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_exchange)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val name: String = intent.extras["name"].toString()
        val secret = intent.extras["secret"]
        var connectionType = intent.extras["type"]

        supportActionBar!!.setTitle(name)

        generateRandomKey()
    }

    fun callbackRandomKey(key: String){
        tvSecret.text = key
    }

    external fun generateRandomKey()
}
