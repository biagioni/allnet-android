package org.alnet.allnet_android.activities

import android.annotation.SuppressLint
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

    var name: String? = null
    var secret: String? = null
    var connectionType: Int? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_exchange)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        name = intent.extras["name"].toString()
        secret = intent.extras["secret"].toString()
        var connectionType = intent.extras["type"]

        supportActionBar?.title = name

        NetworkAPI.listener = this

        if (connectionType == 2) {
            tvSecret.text = getString(R.string.none)
            tvOptionalSecret.text = getString(R.string.none)
            NetworkAPI.createGroup(name!!)
        }else{
            NetworkAPI.generateRandomKey()
            tvInfo.text = "${getString(R.string.key_in_progress)}\n${getString(R.string.waiting_key)}\n${name.toString()}"
            if (secret.isNullOrEmpty()){
                tvOptionalSecret.text = getString(R.string.none)
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


    //-----------NetworkAPI delegation----------------------

    @SuppressLint("SetTextI18n")
    override fun groupCreated(result: Int) {
        runOnUiThread {
            if (result == 1) {
                tvInfo.setTextColor(resources.getColor(R.color.colorPrimary))
                tvInfo.text = getString(R.string.created_group)
            } else {
                tvInfo.setTextColor(resources.getColor(R.color.colorAccent))
                tvInfo.text = "${getString(R.string.not_possible)}${name!!}"
            }
        }
    }

    override fun generatedRandomKey(key: String) {
        runOnUiThread {
            tvSecret.text = key
            //todo nearby wireless
            NetworkAPI.requestNewContact(name!!, 6, key, secret)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun keyGenerated(contact: String) {
        if (name == contact){
            runOnUiThread {
                tvInfo.setTextColor(resources.getColor(R.color.colorAccent))
                tvInfo.text = "${getString(R.string.key_sent)}\n${getString(R.string.waiting_key)}\n$contact"
            }
        }
    }

    override fun keyExchanged(contact: String) {
        if (name == contact) {
            runOnUiThread {
                tvInfo.setTextColor(resources.getColor(R.color.colorPrimary))
                tvInfo.text = getString(R.string.key_exchanged)
            }
        }
        NetworkAPI.completeExchange(contact)
    }
}


