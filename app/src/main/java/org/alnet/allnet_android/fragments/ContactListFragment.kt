package org.alnet.allnet_android.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_contact_list.view.*
import org.alnet.allnet_android.*


/**
 * A simple [Fragment] subclass.
 */
class ContactListFragment : Fragment(), INetwork {
    override fun listContacts(contact: String) {
        contacts.add(contact)
    }

    var mRecyclerView: RecyclerView? = null
    lateinit var networkAPI: NetworkAPI
    var contacts = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view  = inflater!!.inflate(R.layout.fragment_contact_list, container, false)
        mRecyclerView = view.recyclerView
        networkAPI = (activity as TabBarActivity).networkAPI!!
        contacts = (activity as TabBarActivity).contacts
        networkAPI.listener = this

        updateUI()
        return view
    }


    fun updateUI(){
        val layoutManager = LinearLayoutManager(activity)
        mRecyclerView!!.layoutManager = layoutManager
        val adapter = ContactAdapter(contacts);
        mRecyclerView!!.adapter = adapter
    }

}
