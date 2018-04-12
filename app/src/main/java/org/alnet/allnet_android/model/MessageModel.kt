package org.alnet.allnet_android.model

/**
 * Created by docouto on 3/31/18.
 */

val MSG_TYPE_RCVD = 1
val MSG_TYPE_SENT = 2
val MSG_TYPE_SENT_ACKED = 3

class MessageModel(
    var message: String,
    var type: Int,
    var date: String,
    var message_has_been_acked: Int
)