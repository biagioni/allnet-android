package org.alnet.allnet_android.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.contact_item.view.*
import kotlinx.android.synthetic.main.message_item_received.view.*
import org.alnet.allnet_android.R
import org.alnet.allnet_android.inflate

/**
 * Created by docouto on 3/26/18.
 */

class MessageAdapter(private val contacts: List<String>): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.message_item_received, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: String){
            itemView.tvMessageRcv.text = message
        }
    }
}