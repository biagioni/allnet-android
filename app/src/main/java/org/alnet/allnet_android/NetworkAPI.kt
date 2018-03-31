package org.alnet.allnet_android

import android.content.ContentValues.TAG
import android.os.AsyncTask
import android.system.Os.*
import android.system.OsConstants.*
import android.util.Log
import org.alnet.allnet_android.model.ContactModel
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by docouto on 3/19/18.
 */

interface INetwork {
    fun listContactsUpdated()
}

object NetworkAPI{

    var listener: INetwork? = null
    external fun startAllnet(path: String): Int
    external fun getContacts()
    external fun init()

    var socket: Int = 0
    var contacts = ArrayList<ContactModel>()
    var initialized = false

    fun initialize(path: String){
        if (!initialized) {
            initialized = true
            init()
            startAllnet(path)
        }
    }

    fun callback(socket: Int) {
        this.socket = socket
        Log.e("SOCKET: ", socket.toString())
        getContacts()
    }

    fun callbackContacts(contact: String, time: Long){
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss")
        val current = Date(time)
        val formatted = formatter.format(current)
        contacts.add(ContactModel(contact, formatted))
        contacts.sortByDescending { it.lastMessage }
        listener?.listContactsUpdated()
    }

}