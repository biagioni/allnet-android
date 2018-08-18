package org.alnet.allnetandroid.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import kotlinx.android.synthetic.main.fragment_contact_list.view.*
import org.alnet.allnetandroid.*
import org.alnet.allnetandroid.activities.MessageActivity
import org.alnet.allnetandroid.activities.SettingsActivity
import org.alnet.allnetandroid.adapters.ContactAdapter
import org.alnet.allnetandroid.model.ContactModel
import android.app.PendingIntent
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import android.content.Context
import android.app.NotificationChannel
import android.os.Build




class ContactListFragment : Fragment(), INetwork, ContactAdapter.ItemClickListener {

    var settings = false
    val CHANNEL_ID = "mynotifications"
    private var mRecyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        createNotificationChannel()
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

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "notification"
            val description = "messages"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = activity!!.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
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

    override fun newMsgReceived(contact: String, message: String) {
        updateUI(NetworkAPI.contacts)
        val emptyIntent = Intent()
        val pendingIntent = PendingIntent.getActivity(context, 0,
                emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val mBuilder = NotificationCompat.Builder(context!!, CHANNEL_ID)
                .setContentTitle(contact)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        val notificationManager = activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, mBuilder.build())
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
