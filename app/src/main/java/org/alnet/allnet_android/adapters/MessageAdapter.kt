package org.alnet.allnet_android.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.message_item_send.view.*
import org.alnet.allnet_android.R
import org.alnet.allnet_android.inflate
import org.alnet.allnet_android.model.MSG_TYPE_RCVD
import org.alnet.allnet_android.model.MSG_TYPE_SENT
import org.alnet.allnet_android.model.MSG_TYPE_SENT_ACKED
import org.alnet.allnet_android.model.MessageModel

/**
 * Created by docouto on 3/26/18.
 */

class MessageAdapter(private val messages: List<MessageModel>): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == MSG_TYPE_RCVD){
            val inflatedView = parent.inflate(R.layout.message_item_received, false)
            return ViewHolder(inflatedView)
        }else if (viewType == MSG_TYPE_SENT){
            val inflatedView = parent.inflate(R.layout.message_item_send, false)
            return ViewHolder(inflatedView)
        }else{
            val inflatedView = parent.inflate(R.layout.message_item_sent_acked, false)
            return ViewHolder(inflatedView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        if (message.type == MSG_TYPE_SENT){
            if (message.message_has_been_acked != 0) {
                return MSG_TYPE_SENT_ACKED
            }
        }
        return message.type
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(messageModel: MessageModel){
            itemView.tvMessage.text = messageModel.message
            itemView.tvDate.text = messageModel.date
        }
    }
}