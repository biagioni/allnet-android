package org.alnet.allnet_android.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.message_item_received.view.*
import org.alnet.allnet_android.R
import org.alnet.allnet_android.inflate
import org.alnet.allnet_android.model.MSG_TYPE_RCVD
import org.alnet.allnet_android.model.MessageModel

/**
 * Created by docouto on 3/26/18.
 */

class MessageAdapter(private val contacts: List<MessageModel>): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == MSG_TYPE_RCVD){
            val inflatedView = parent.inflate(R.layout.message_item_received, false)
            return ViewHolder(inflatedView)
        }else {
            val inflatedView = parent.inflate(R.layout.message_item_send, false)
            return ViewHolder(inflatedView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val contact = contacts[position]
        return contact.type
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(messageModel: MessageModel){
            itemView.tvMessage.text = messageModel.message
        }
    }
}