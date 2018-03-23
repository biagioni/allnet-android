package org.alnet.allnet_android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem

class KeyExchangeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_exchange)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.key_exchange)
    }

}
