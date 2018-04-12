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
import org.alnet.allnet_android.model.MSG_TYPE_SENT
import org.alnet.allnet_android.model.MessageModel
import java.text.SimpleDateFormat
import java.util.*

//todo color aging
class MessageActivity : AppCompatActivity(), INetwork {

    override fun ackedMessage(contact: String) {
        NetworkAPI.listMessages()
    }

    override fun listMsgUpdated() {
        updateUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        NetworkAPI.listener = this

        val layout = LinearLayoutManager(this)
        rvMessage.layoutManager = layout

        supportActionBar!!.setTitle(NetworkAPI.contact)
        NetworkAPI.listMessages()
    }

    override fun onStop() {
        super.onStop()
        NetworkAPI.contact = null
    }

    fun updateUI(){
        runOnUiThread {
            val adapter = MessageAdapter(NetworkAPI.messages)
            rvMessage.adapter = adapter
            rvMessage.scrollToPosition(adapter.itemCount-1)
        }

    }

    fun sendMessage(v: View){
        val msg = etText!!.text.toString()
        NetworkAPI.sendMessage(msg, NetworkAPI.contact!!)
        etText.text.clear()
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss")
        val current = Date()
        NetworkAPI.messages.add(MessageModel(msg, MSG_TYPE_SENT, formatter.format(current), 0))
        updateUI()
    }

}
