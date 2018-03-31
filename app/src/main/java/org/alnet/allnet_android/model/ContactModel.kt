package org.alnet.allnet_android.model

/**
 * Created by docouto on 3/31/18.
 */
class ContactModel(name: String, lastMessage: String) {
    var name: String
    var lastMessage: String

    init {
        this.name = name
        this.lastMessage = lastMessage
    }
}