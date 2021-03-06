package org.alnet.allnetandroid

import org.alnet.allnetandroid.model.ContactModel
import org.alnet.allnetandroid.model.MSG_MISSED
import org.alnet.allnetandroid.model.MessageModel
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by docouto on 3/19/18.
 */

interface INetwork {
    fun listContactsUpdated(){}
    fun listHiddenContactsUpdated(){}
    fun listMsgUpdated(){}
    fun listGroupUpdated(){}
    fun listMemberUpdated(){}

    fun newMsgReceived(contact: String, message: String){}
    fun generatedRandomKey(key: String){}
    fun keyGenerated(contact: String){}
    fun keyExchanged(contact: String){}
    fun incompletedContactsUpdated(){}
    fun msgTrace(msg: String){}
    fun ackedMessage(contact: String){}
    fun groupCreated(result: Int){}
}

object NetworkAPI{

    var listener: INetwork? = null
    var socket: Int = 0
    var contacts = ArrayList<ContactModel>()
    var hiddencontacts = ArrayList<ContactModel>()
    var incompleteContacts = ArrayList<String>()
    var messages = ArrayList<MessageModel>()
    var unreadMessages = ArrayList<String>()

    var groups = ArrayList<Pair<String, Boolean>>()
    var members = ArrayList<Pair<String, Boolean>>()
    private var groupMembers = ArrayList<String>()

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
        messages.clear()
        getMessages(this.contact!!)
    }

    fun callback(socket: Int) {
        this.socket = socket
        getContacts()
    }

    fun checkMissingMessages(){
        val missingMessages = messages.filter{it.prevMissing > 0}
        for (msg in missingMessages) {
            val msgModel = MessageModel(
                    "${msg.prevMissing} message${
            if (msg.prevMissing == 1) "" else "s"} missing", MSG_MISSED,
                    msg.date, msg.message_has_been_acked,
                    0)
            messages.add(missingMessages.indexOf(msg) + 1, msgModel)

        }
        messages.sortBy { it.date.toDate() }
        listener?.listMsgUpdated()
    }

    fun callbackContacts(contact: String, time: Long){
        val formatted = Date(time*1000L).toDateString()
        contacts.add(ContactModel(contact, formatted, 1))
        contacts.sortByDescending { it.lastMessage.toDate() }
        listener?.listContactsUpdated()
    }

    fun callbackHiddenContacts(contact: String){
        hiddencontacts.add(ContactModel(contact, "", 0))
        listener?.listHiddenContactsUpdated()
    }

    fun callbackMessages(message: String, type: Int, time: Long, acked: Int, prevMissing: Int){
        val formatted = Date(time*1000L).toDateString()
        messages.add(MessageModel(message,type, formatted, acked, prevMissing))
        checkMissingMessages()
        messages.sortBy { it.date.toDate() }
        listener?.listMsgUpdated()
    }

    fun callbackGroups(contact: String){
        val allGroups = contacts.filter { isGroup(it.name) == 1 }.map { it.name }
        groups.add(Pair(contact, allGroups.contains(contact)))
        listener?.listGroupUpdated()
    }

    fun callbackMembers(contact: String){
        groupMembers.add(contact)

        members = contacts.filter { it.name != this.contact }
                .map { Pair(it.name, groupMembers
                        .contains(it.name))} as ArrayList<Pair<String, Boolean>>
        listener?.listMemberUpdated()
    }

    fun callbackRandomKey(key: String){
        listener?.generatedRandomKey(key)
    }

    fun callbackKeyGenerated(contact: String?){
        if (contact != null) {
            listener?.keyGenerated(contact)
        }else{
            listener?.keyGenerated("")
        }
    }

    fun callbackKeyExchanged(contact: String){
        listener?.keyExchanged(contact)
    }

    fun callbackGroupCreated(result: Int){
        listener?.groupCreated(result)
    }

    fun callbackIncompleteContacts(contact: String){
        incompleteContacts.add(contact)
        listener?.incompletedContactsUpdated()
    }

    fun callbackKeyForContact(key: String){
        listener?.generatedRandomKey(key)
    }

    fun callbackTrace(msg: String){
        listener?.msgTrace(msg)
    }

    fun callbackAckMessages(contact: String) {
        if (!this.contact.isNullOrBlank()) {
            if (this.contact == contact) {
                listener?.ackedMessage(contact)
            }
        }
    }

    fun callbackNewMessage(contact: String, message: String){
        if (!this.contact.isNullOrBlank()){
            if (this.contact == contact) {
                listMessages()
            }else {
                unreadMessages.add(contact)
                listener?.newMsgReceived(contact, message)
            }
        }else{
            unreadMessages.add(contact)
            listener?.newMsgReceived(contact, message)
        }
    }

    external fun addToGroup(group: String, contact: String)
    external fun removeFromGroup(group: String, contact: String)
    external fun loadGroups(contact: String)
    external fun loadMembers(Contact: String)
    external fun startAllnet(path: String): Int
    external fun getContacts()
    external fun getHiddenContacts()
    external fun init()
    external fun getMessages(contact: String)
    external fun sendMessage(message: String, contact: String)
    external fun generateRandomKey()
    external fun requestNewContact(name: String, hops: Int, secret: String, optionalSecret: String?)
    external fun resendKeyForNewContact(contact: String)
    external fun removeNewContact(contact: String)
    external fun createGroup(name: String)
    external fun completeExchange(contact: String)
    external fun fecthIncompletedKeys()
    external fun getKeyForContact(contact: String)
    external fun startTrace(hops: Int)
    external fun isGroup(contact: String): Int
    external fun conversationSize(contact: String): String
    external fun isInvisible(contact: String): Int
    external fun deleteConversation(contact: String): Int
    external fun deleteUser(contact: String): Int
    external fun makeVisible(contact: String)
    external fun makeInvisible(contact: String)
    external fun renameContact(contact: String, newName: String)
}