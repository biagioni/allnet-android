package org.alnet.allnetandroid.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_message.*
import org.alnet.allnetandroid.INetwork
import org.alnet.allnetandroid.NetworkAPI
import org.alnet.allnetandroid.R
import org.alnet.allnetandroid.adapters.MessageAdapter
import org.alnet.allnetandroid.model.MSG_TYPE_SENT
import org.alnet.allnetandroid.model.MessageModel
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : AppCompatActivity(), INetwork {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        NetworkAPI.listener = this

        val layout = LinearLayoutManager(this)
        rvMessage.layoutManager = layout

        supportActionBar!!.title = NetworkAPI.contact
        NetworkAPI.listMessages()
    }

    override fun onStop() {
        super.onStop()
        NetworkAPI.contact = null
    }

    fun sendMessage(v: View){
        val msg = etText!!.text.toString()
        NetworkAPI.sendMessage(msg, NetworkAPI.contact!!)
        etText.text.clear()
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss", Locale.getDefault())
        val current = Date()
        NetworkAPI.messages.add(MessageModel(msg, MSG_TYPE_SENT, formatter.format(current), 0))
        updateUI()
    }

    private fun updateUI(){
        runOnUiThread {
            val adapter = MessageAdapter(NetworkAPI.messages)
            rvMessage.adapter = adapter
            rvMessage.scrollToPosition(adapter.itemCount-1)
        }

    }

    //-----------NetworkAPI delegation----------------------

    override fun ackedMessage(contact: String) {
        NetworkAPI.listMessages()
    }

    override fun listMsgUpdated() {
        updateUI()
    }

}
