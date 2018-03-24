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


class NetworkAPI(path: String){

    external fun startAllnet(path: String): Int
    external fun getContacts()
    external fun init()

    var socket: Int = 0
    private val path: String

    init {
        this.path = path

        init()

        startAllnet(path)
    }

    fun callback(socket: Int) {
        this.socket = socket
        Log.e("SOCKET: ", socket.toString())
        getContacts()
    }

    fun listContacts(){
        getContacts()
    }
    fun callbackContacts(){

    }

}