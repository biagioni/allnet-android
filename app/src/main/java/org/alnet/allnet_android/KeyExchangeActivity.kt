package org.alnet.allnet_android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_key_exchange.*

class KeyExchangeActivity : AppCompatActivity() {

    var name: String? = null
    var secret: String? = null
    var connectionType: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_exchange)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        name = intent.extras["name"].toString()
        secret = intent.extras["secret"].toString()
        var connectionType = intent.extras["type"]

        supportActionBar!!.setTitle(name)

        generateRandomKey()


        tvInfo.text = "Key exchange in progress\nWaiting for key from:\n " + name.toString()
        tvOptionalSecret.text  = secret.toString()
    }

    fun callbackRandomKey(key: String){
        tvSecret.text = key
        requestNewContact(name!!,6,key,secret)
    }

    external fun generateRandomKey()
    external fun requestNewContact(name: String, hops: Int, secret: String, optionalSecret: String?)
}
