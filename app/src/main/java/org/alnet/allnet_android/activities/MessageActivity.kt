package org.alnet.allnet_android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_message.*
import org.alnet.allnet_android.R
import org.alnet.allnet_android.adapters.MessageAdapter
import org.alnet.allnet_android.model.MessageModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class MessageActivity : AppCompatActivity() {

    var messages = ArrayList<MessageModel>()
    lateinit var contact: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        contact = intent.extras["contact"].toString()

        supportActionBar!!.setTitle(contact)
        getMessages(contact)
    }

    fun updateUI(){
        val layout = LinearLayoutManager(this)
        rvMessage.layoutManager = layout
        val adapter = MessageAdapter(messages)
        rvMessage.adapter = adapter
    }

    fun callbackMessages(message: String, type: Int, time: Long){
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss")
        val current = Date(time)
        val formatted = formatter.format(current)
        messages.add(MessageModel(message,type, formatted))
        updateUI()
    }

    fun sendMessage(v: View){
        val msg = etText!!.text.toString()
        sendMessage(msg,contact)
        etText.text.clear()
    }

    external fun getMessages(contact: String)
    external fun sendMessage(message: String, contact: String)
}
