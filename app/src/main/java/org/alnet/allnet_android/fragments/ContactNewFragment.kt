package org.alnet.allnet_android.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.alnet.allnet_android.R
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import kotlinx.android.synthetic.main.fragment_contact_new.view.*
import org.alnet.allnet_android.INetwork
import org.alnet.allnet_android.NetworkAPI
import org.alnet.allnet_android.activities.KeyExchangeActivity
import org.alnet.allnet_android.adapters.ContactIncompleteAdapter


class ContactNewFragment : Fragment(), INetwork, ContactIncompleteAdapter.ItemClickListener {

    override fun onclick(contact: String) {
        editTextName?.setText(contact)
        NetworkAPI.getKeyForContact(contact)
    }

    override fun incompletedContactsUpdated() {
        updateUI()
    }

    override fun generatedRandomKey(key: String) {
        editTextSecret?.setText(key)
        sendInfo()
    }

    var spinner: Spinner? = null
    var editTextName: EditText? = null
    var editTextSecret: EditText? = null
    var mRecyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_contact_new, container, false)

        val adapter = ArrayAdapter.createFromResource(activity,
                R.array.contact_type, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner = view.spinnerType
        spinner!!.adapter = adapter
        editTextName = view.etName
        editTextSecret = view.etSecret

        view.buttonRequest.setOnClickListener {
            sendInfo()
        }

        mRecyclerView = view.rvIncompletes
        NetworkAPI.listener = this

        NetworkAPI.incompleteContacts.clear()

        NetworkAPI.fecthIncompletedKeys()

        updateUI()

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun updateUI(){
        val layoutManager = LinearLayoutManager(activity)
        mRecyclerView!!.layoutManager = layoutManager
        val adapter = ContactIncompleteAdapter(NetworkAPI.incompleteContacts, this);
        mRecyclerView!!.adapter = adapter
    }

    fun sendInfo(){
        val name = editTextName!!.text
        val secret = editTextSecret!!.text
        val index = spinner!!.selectedItemPosition

        val intent = Intent(activity, KeyExchangeActivity::class.java)
        intent.putExtra("name", name)
        intent.putExtra("secret", secret)
        intent.putExtra("type", index)
        startActivity(intent)
    }

}
