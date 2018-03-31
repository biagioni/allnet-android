package org.alnet.allnet_android

import android.content.ContentValues.TAG
import android.os.AsyncTask
import android.system.Os.*
import android.system.OsConstants.*
import android.util.Log
import java.lang.Thread.sleep


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
    var contacts = ArrayList<String>()
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

    fun callbackContacts(contact: String){
        contacts.add(contact)
        listener?.listContactsUpdated()
    }

}