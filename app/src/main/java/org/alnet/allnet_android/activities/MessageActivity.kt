package org.alnet.allnet_android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_message.*
import org.alnet.allnet_android.INetwork
import org.alnet.allnet_android.NetworkAPI
import org.alnet.allnet_android.R
import org.alnet.allnet_android.adapters.MessageAdapter


class MessageActivity : AppCompatActivity(), INetwork {

    //todo list updated
    override fun listContactsUpdated() {
    }

    override fun listMsgUpdated() {
        updateUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        NetworkAPI.listener = this

        supportActionBar!!.setTitle(NetworkAPI.contact)
        NetworkAPI.listMessages()
    }

    fun updateUI(){
        val layout = LinearLayoutManager(this)
        rvMessage.layoutManager = layout
        val adapter = MessageAdapter(NetworkAPI.messages)
        rvMessage.adapter = adapter
        rvMessage.scrollToPosition(adapter.itemCount-1)
    }

    fun sendMessage(v: View){
        val msg = etText!!.text.toString()
        NetworkAPI.sendMessage(msg, NetworkAPI.contact!!)
        etText.text.clear()
        NetworkAPI.listMessages()
    }

}
