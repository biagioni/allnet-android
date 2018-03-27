package org.alnet.allnet_android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_message.*
import org.alnet.allnet_android.R
import org.alnet.allnet_android.adapters.MessageAdapter

class MessageActivity : AppCompatActivity() {

    var messages = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        var contact = intent.extras["contact"].toString()

        supportActionBar!!.setTitle(contact)
        getMessages(contact)
    }

    fun updateUI(){
        val layout = LinearLayoutManager(this)
        rvMessage.layoutManager = layout
        val adapter = MessageAdapter(messages)
        rvMessage.adapter = adapter
    }

    fun callbackMessages(message: String){
        messages.add(toString())
        updateUI()
    }

    external fun getMessages(contact: String)
}
