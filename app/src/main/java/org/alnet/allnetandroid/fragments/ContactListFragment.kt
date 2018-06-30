package org.alnet.allnetandroid.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import kotlinx.android.synthetic.main.fragment_contact_list.view.*
import org.alnet.allnetandroid.*
import org.alnet.allnetandroid.activities.MessageActivity
import org.alnet.allnetandroid.activities.SettingsActivity
import org.alnet.allnetandroid.adapters.ContactAdapter
import org.alnet.allnetandroid.model.ContactModel


class ContactListFragment : Fragment(), INetwork, ContactAdapter.ItemClickListener {

    var settings = false
    private var mRecyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view  = inflater.inflate(R.layout.fragment_contact_list, container, false)
        mRecyclerView = view.recyclerView
        NetworkAPI.listener = this
        updateUI(NetworkAPI.contacts)
        return view
    }

    override fun onPause() {
        super.onPause()
        NetworkAPI.listener = null
    }

    override fun onResume() {
        super.onResume()
        settings = false
        NetworkAPI.listener = this
        NetworkAPI.contacts.clear()
        NetworkAPI.getContacts()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_contacts, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuEdit -> {
                if (!settings){
                    item.title = getString(R.string.done)
                    settings = true
                    updateUI(NetworkAPI.contacts)
                    NetworkAPI.hiddencontacts.clear()
                    NetworkAPI.getHiddenContacts()
                }else {
                    settings = false
                    item.title = getString(R.string.edit)
                    updateUI(NetworkAPI.contacts)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun updateUI(contacts: ArrayList<ContactModel>){
        activity?.runOnUiThread {
            val layoutManager = LinearLayoutManager(activity)
            mRecyclerView!!.layoutManager = layoutManager
            val adapter = ContactAdapter(contacts, settings, this)
            mRecyclerView!!.adapter = adapter
        }
    }

    //-----------Adapter delegation----------------------

    override fun onclick(contact: ContactModel) {
        NetworkAPI.contact = contact.name
        if (settings){
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }else{
            NetworkAPI.unreadMessages.removeAll { it == contact.name }
            val intent = Intent(activity, MessageActivity::class.java)
            startActivity(intent)
        }

    }

    //-----------NetworkAPI delegation----------------------

    override fun listContactsUpdated() {
        updateUI(NetworkAPI.contacts)
    }

    override fun newMsgReceived(contact: String) {
        updateUI(NetworkAPI.contacts)
    }

    override fun listHiddenContactsUpdated() {
        val contacts = ArrayList<ContactModel>()
        NetworkAPI.contacts.forEach {
            contacts.add(it)
        }
        contacts.add(ContactModel("","",2))
        NetworkAPI.hiddencontacts.forEach {
            contacts.add(it)
        }
        updateUI(contacts)
    }

}
