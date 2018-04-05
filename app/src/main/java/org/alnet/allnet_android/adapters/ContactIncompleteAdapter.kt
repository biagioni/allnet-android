package org.alnet.allnet_android.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.contact_incomplete_item.view.*
import kotlinx.android.synthetic.main.contact_item.view.*
import org.alnet.allnet_android.R
import org.alnet.allnet_android.inflate
import org.alnet.allnet_android.model.ContactModel

/**
 * Created by docouto on 4/5/18.
 */

class ContactIncompleteAdapter(private val contacts: List<String>, listener: ItemClickListener): RecyclerView.Adapter<ContactIncompleteAdapter.ViewHolder>() {

    public interface ItemClickListener{
        fun onclick(contact: String)
    }

    var listener: ItemClickListener

    init {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.contact_incomplete_item, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact, listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contact: String, listener: ItemClickListener){
            itemView.tvContactIncomplet.text = contact

            itemView.setOnClickListener(View.OnClickListener {
                listener.onclick(contact)
            })
        }
    }
}