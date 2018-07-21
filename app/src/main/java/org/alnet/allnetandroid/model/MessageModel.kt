package org.alnet.allnetandroid.model

/**
 * Created by docouto on 3/31/18.
 */

const val MSG_TYPE_RCVD = 1
const val MSG_TYPE_SENT = 2
const val MSG_TYPE_SENT_ACKED = 3
const val MSG_MISSED = 1212

class MessageModel(
    var message: String,
    var type: Int,
    var date: String,
    var message_has_been_acked: Int,
    var prevMissing: Int
)