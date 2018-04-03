package org.alnet.allnet_android.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_contact_list.view.*
import org.alnet.allnet_android.*
import org.alnet.allnet_android.activities.MessageActivity
import org.alnet.allnet_android.adapters.ContactAdapter
import org.alnet.allnet_android.model.ContactModel


class ContactListFragment : Fragment(), INetwork, ContactAdapter.ItemClickListener {
    override fun onclick(contact: ContactModel) {
        NetworkAPI.contact = contact.name
        NetworkAPI.clearMessages()
        val intent = Intent(activity, MessageActivity::class.java)
        startActivity(intent)
    }

    override fun listContactsUpdated() {
        updateUI()
    }

    override fun keyGenerated(contact: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun newMessageReceived(contact: String, message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //TODO notifications for messages
    override fun listMsgUpdated() {}

    var mRecyclerView: RecyclerView? = null
    lateinit var networkAPI: NetworkAPI

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view  = inflater!!.inflate(R.layout.fragment_contact_list, container, false)
        mRecyclerView = view.recyclerView
        NetworkAPI.listener = this

        updateUI()
        return view
    }


    fun updateUI(){
        val layoutManager = LinearLayoutManager(activity)
        mRecyclerView!!.layoutManager = layoutManager
        val adapter = ContactAdapter(NetworkAPI.contacts, this);
        mRecyclerView!!.adapter = adapter
    }

}
