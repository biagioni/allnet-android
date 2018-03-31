package org.alnet.allnet_android.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.contact_item.view.*
import org.alnet.allnet_android.R
import org.alnet.allnet_android.inflate
import org.alnet.allnet_android.model.ContactModel

/**
 * Created by docouto on 3/26/18.
 */
class ContactAdapter(private val contacts: List<ContactModel>, listener: ItemClickListener): RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    public interface ItemClickListener{
        fun onclick(contact: ContactModel)
    }

    var listener: ItemClickListener

    init {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.contact_item, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact, listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contact: ContactModel, listener: ItemClickListener){
            itemView.tvName.text = contact.name
            itemView.tvDate.text = contact.lastMessage

            itemView.setOnClickListener(View.OnClickListener {
                listener.onclick(contact)
            })
        }
    }
}