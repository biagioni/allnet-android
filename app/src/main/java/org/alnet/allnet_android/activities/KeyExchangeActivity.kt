package org.alnet.allnet_android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_key_exchange.*
import org.alnet.allnet_android.INetwork
import org.alnet.allnet_android.NetworkAPI
import org.alnet.allnet_android.R

class KeyExchangeActivity : AppCompatActivity(), INetwork {
    override fun groupCreated(result: Int) {
        if (result == 1) {
            tvInfo.setTextColor(resources.getColor(R.color.colorPrimary))
            tvInfo.text = "Created group with success!"
        }else{
            tvInfo.setTextColor(resources.getColor(R.color.colorAccent))
            tvInfo.text = "It was not possible to create the group" + name!!
        }
    }

    override fun listContactsUpdated() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listMsgUpdated() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun generatedRandomKey(key: String) {
        tvSecret.text = key
        //todo nearby wireless
        NetworkAPI.requestNewContact(name!!,6,key,secret)
    }

    override fun keyGenerated(contact: String) {
        if (name == contact){
            tvInfo.setTextColor(resources.getColor(R.color.colorAccent))
            tvInfo.text = "Key was sent\nWaiting for key from:\n" + contact
        }
    }

    override fun newMessageReceived(contact: String, message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun keyExchanged(contact: String) {
        if (name == contact) {
            tvInfo.setTextColor(resources.getColor(R.color.colorPrimary))
            tvInfo.text = "Key was exchanged with SUCCESS!!!"
        }
        NetworkAPI.completeExchange(contact)
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

        if (connectionType == 2) {
            tvSecret.text = "None"
            tvOptionalSecret.text = "None"
            NetworkAPI.createGroup(name!!)
        }else{
            NetworkAPI.generateRandomKey()
            tvInfo.text = "Key exchange in progress\nWaiting for key from:\n " + name.toString()
            if (secret.isNullOrEmpty()){
                tvOptionalSecret.text = "None"
            }else{
                tvOptionalSecret.text  = secret.toString()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_key_exchange, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.btnCancel -> {
                NetworkAPI.removeNewContact(name!!)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun resendKey(v: View){
        NetworkAPI.resendKeyForNewContact(name!!)
    }
}


