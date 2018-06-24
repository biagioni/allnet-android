package org.alnet.allnetandroid.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.group_item.view.*
import org.alnet.allnetandroid.R
import org.alnet.allnetandroid.inflate

class GroupAdapter(private val contacts: List<Pair<String, Boolean>>, val listener: ItemClickListener): RecyclerView.Adapter<GroupAdapter.ViewHolder>() {

    interface ItemClickListener{
        fun onclick(contact: String, selected: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.group_item, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact, listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contact: Pair<String, Boolean>, listener: ItemClickListener){
            itemView.tvGroup.text = contact.first
            itemView.checkBox.isChecked = contact.second
            itemView.setOnClickListener({
                itemView.checkBox.isChecked = !itemView.checkBox.isChecked
                listener.onclick(contact.first, itemView.checkBox.isChecked)
            })
        }
    }
}