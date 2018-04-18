package org.alnet.allnet_android.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.group_item.view.*
import org.alnet.allnet_android.R
import org.alnet.allnet_android.inflate

class GroupAdapter(private val contacts: List<Pair<String, Boolean>>, listener: ItemClickListener): RecyclerView.Adapter<GroupAdapter.ViewHolder>() {

    public interface ItemClickListener{
        fun onclick(contact: String, selected: Boolean)
    }

    var listener: ItemClickListener

    init {
        this.listener = listener
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
            itemView.tvGroup.setText(contact.first)
            itemView.checkBox.isChecked = contact.second
            itemView.setOnClickListener(View.OnClickListener {
                listener.onclick(contact.first, !contact.second)
            })
        }
    }
}