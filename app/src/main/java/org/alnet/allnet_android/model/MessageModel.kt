package org.alnet.allnet_android.model

/**
 * Created by docouto on 3/31/18.
 */

val MSG_TYPE_RCVD = 1
val MSG_TYPE_SENT = 2

class MessageModel(message: String, type: Int, date: String) {
    var message: String
    var type: Int
    var date: String

    init {
        this.message = message
        this.type = type
        this.date = date
    }
}