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


class MessageActivity : AppCompatActivity(), INetwork {
    override fun incompletedContactsUpdated() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun generatedRandomKey(key: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun groupCreated(result: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun keyExchanged(contact: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun msgTrace(msg: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ackedMessage(contact: String) {
        if (NetworkAPI.contact == contact) {
            updateUI()
        }
    }

    //todo list updated
    override fun listContactsUpdated() {
    }

    override fun keyGenerated(contact: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
