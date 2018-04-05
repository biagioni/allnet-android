package org.alnet.allnet_android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_key_exchange.*
import org.alnet.allnet_android.INetwork
import org.alnet.allnet_android.NetworkAPI
import org.alnet.allnet_android.R

class KeyExchangeActivity : AppCompatActivity(), INetwork {
    override fun listContactsUpdated() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listMsgUpdated() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun generatedRandomKey(key: String) {
        tvSecret.text = key
        NetworkAPI.requestNewContact(name!!,6,key,secret)
    }

    override fun keyGenerated(contact: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun newMessageReceived(contact: String, message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun keyExchanged(contact: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun msgTrace(msg: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ackedMessage(contact: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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

        supportActionBar?.setTitle(name)

        NetworkAPI.listener = this

        NetworkAPI.generateRandomKey()


        tvInfo.text = "Key exchange in progress\nWaiting for key from:\n " + name.toString()
        tvOptionalSecret.text  = secret.toString()
    }
}
