package org.alnet.allnetandroid.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_groups.*
import org.alnet.allnetandroid.INetwork
import org.alnet.allnetandroid.NetworkAPI
import org.alnet.allnetandroid.R
import org.alnet.allnetandroid.adapters.GroupAdapter

class GroupsActivity : AppCompatActivity(), INetwork, GroupAdapter.ItemClickListener {

    private var isGroup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        isGroup = intent.extras["isGroup"] as Boolean

        val layoutManager = LinearLayoutManager(this)
        rvGroups.layoutManager = layoutManager

        NetworkAPI.listener = this

        if (isGroup){
            supportActionBar!!.title = "Groups"
            NetworkAPI.members.clear()
            NetworkAPI.loadMembers(NetworkAPI.contact!!)
        }else{
            supportActionBar!!.title = "Participants"
            NetworkAPI.groups.clear()
            NetworkAPI.loadGroups(NetworkAPI.contact!!)
        }
    }


    override fun listGroupUpdated() {

        runOnUiThread{
            val adapter = GroupAdapter(NetworkAPI.groups, this)
            rvGroups.adapter = adapter
        }

    }

    override fun listMemberUpdated() {
        val adapter = GroupAdapter(NetworkAPI.members, this)
        rvGroups.adapter = adapter
    }

    override fun onclick(contact: String, selected: Boolean) {
        if(isGroup){
            if(selected){
                NetworkAPI.addToGroup(NetworkAPI.contact!!, contact)
            }else{
                NetworkAPI.removeFromGroup(NetworkAPI.contact!!, contact)
            }
        }else{
            if(selected){
                NetworkAPI.addToGroup(contact, NetworkAPI.contact!!)
            }else{
                NetworkAPI.removeFromGroup(contact, NetworkAPI.contact!!)
            }
        }
        if (isGroup){
            NetworkAPI.members.clear()
            NetworkAPI.loadMembers(NetworkAPI.contact!!)
        }else{
            NetworkAPI.groups.clear()
            NetworkAPI.loadGroups(NetworkAPI.contact!!)
        }
    }

}
