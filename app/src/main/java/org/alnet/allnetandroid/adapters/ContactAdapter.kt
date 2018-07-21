package org.alnet.allnetandroid.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.contact_item.view.*
import org.alnet.allnetandroid.NetworkAPI
import org.alnet.allnetandroid.R
import org.alnet.allnetandroid.inflate
import org.alnet.allnetandroid.model.ContactModel

/**
 * Created by docouto on 3/26/18.
 */
class ContactAdapter(private val contacts: List<ContactModel>, val sets: Boolean, var listener:
ItemClickListener): RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    interface ItemClickListener{
        fun onclick(contact: ContactModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 2){
            val inflatedView = parent.inflate(R.layout.item_header_hidden, false)
            ViewHolder(inflatedView)
        }else {
            val inflatedView = parent.inflate(R.layout.contact_item, false)
            ViewHolder(inflatedView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return contacts[position].type
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        if (contact.type != 2) {
            holder.bind(contact, listener, sets)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contact: ContactModel, listener: ItemClickListener, sets: Boolean){
            if (NetworkAPI.unreadMessages.contains(contact.name)){
                itemView.tvNotification.visibility = View.VISIBLE
                val count = NetworkAPI.unreadMessages.filter { it == contact.name }.count()
                itemView.tvNotification.text = count.toString()
            }else{
                itemView.tvNotification.visibility = View.INVISIBLE
            }
            if (sets){
                itemView.ivSettings.visibility = View.VISIBLE
            }else{
                itemView.ivSettings.visibility = View.INVISIBLE
            }

            itemView.tvName.text = contact.name
            itemView.tvDate.text = contact.lastMessage

            itemView.setOnClickListener({
                listener.onclick(contact)
            })
        }
    }
}