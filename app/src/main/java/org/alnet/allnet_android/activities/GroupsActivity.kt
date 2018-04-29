package org.alnet.allnet_android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_groups.*
import org.alnet.allnet_android.INetwork
import org.alnet.allnet_android.NetworkAPI
import org.alnet.allnet_android.R
import org.alnet.allnet_android.adapters.GroupAdapter

class GroupsActivity : AppCompatActivity(), INetwork, GroupAdapter.ItemClickListener {

    var isGroup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        isGroup = intent.extras["isGroup"] as Boolean

        val layoutManager = LinearLayoutManager(this)
        rvGroups.layoutManager = layoutManager

        NetworkAPI.listener = this

        if (isGroup){
            supportActionBar!!.setTitle("Groups")
            NetworkAPI.members.clear()
            NetworkAPI.loadMembers(NetworkAPI.contact!!)
        }else{
            supportActionBar!!.setTitle("Participants")
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
