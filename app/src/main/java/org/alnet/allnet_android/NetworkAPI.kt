package org.alnet.allnet_android

import android.util.Log
import org.alnet.allnet_android.model.ContactModel
import org.alnet.allnet_android.model.MessageModel
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by docouto on 3/19/18.
 */

interface INetwork {
    fun listContactsUpdated()
    fun listMsgUpdated()
    fun keyGenerated(contact: String)
}

object NetworkAPI{

    var listener: INetwork? = null
    var socket: Int = 0
    var contacts = ArrayList<ContactModel>()
    var messages = ArrayList<MessageModel>()
    var initialized = false
    var contact: String? = null

    fun initialize(path: String){
        if (!initialized) {
            initialized = true
            init()
            startAllnet(path)
        }
    }

    fun listMessages(){
        getMessages(this.contact!!)
    }

    fun clearMessages(){
        messages.clear()
    }

    fun callback(socket: Int) {
        this.socket = socket
        getContacts()
    }

    fun callbackContacts(contact: String, time: Long){
        val formatted = formatDate(time)
        contacts.add(ContactModel(contact, formatted))
        contacts.sortByDescending { it.lastMessage }
        listener?.listContactsUpdated()
    }

    fun callbackMessages(message: String, type: Int, time: Long){
        val formatted = formatDate(time)
        messages.add(MessageModel(message,type, formatted))
        listener?.listMsgUpdated()
    }

    fun callbackKeyGenerated(contact: String){
        listener?.keyGenerated(contact)
    }

    fun formatDate(time: Long): String{
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss")
        val current = Date(time)
        return formatter.format(current)
    }

    external fun startAllnet(path: String): Int
    external fun getContacts()
    external fun init()
    external fun getMessages(contact: String)
    external fun sendMessage(message: String, contact: String)
}