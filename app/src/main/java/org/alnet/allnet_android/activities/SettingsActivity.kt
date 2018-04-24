package org.alnet.allnet_android.activities

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_settings.*
import org.alnet.allnet_android.NetworkAPI
import org.alnet.allnet_android.R

class SettingsActivity : AppCompatActivity() {

    var isGroup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (NetworkAPI.isGroup(NetworkAPI.contact!!) == 1){
            tvManageParticipants.setText("Manage participants")
            tvDeleteUser.setText("Delete group")
            isGroup = false
        }else{
            tvManageParticipants.setText("Manage groups")
            tvDeleteUser.setText("Delete user")
            isGroup = true
        }

        supportActionBar!!.setTitle(NetworkAPI.contact)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_settings,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                finish()
            }
            R.id.menuSave -> {
                if (swuitchVisible.isChecked){
                    NetworkAPI.makeVisible(NetworkAPI.contact!!)
                }else{
                    NetworkAPI.makeInvisible(NetworkAPI.contact!!)
                }
                if (!etName.text.isNullOrEmpty() && !etName.text.equals(NetworkAPI.contact!!)){
                    NetworkAPI.renameContact(NetworkAPI.contact!!, etName.text.toString())
                }
                finish()
            }
        }
        return true
    }

    fun deleteConversation(view: View){
        var builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete the conversation?")

        builder.setPositiveButton("Delete", DialogInterface.OnClickListener {
            dialogInterface, i ->
            NetworkAPI.deleteConversation(NetworkAPI.contact!!)
            updateUI()
        })

        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener {
            dialogInterface, i ->

        })
        val alert  = builder.create()
        alert.show()
    }

    fun deleteUser(view: View){
        var builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete the conversation?")

        builder.setPositiveButton("Delete", DialogInterface.OnClickListener {
            dialogInterface, i ->
            NetworkAPI.deleteUser(NetworkAPI.contact!!)
            finish()
        })

        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener {
            dialogInterface, i ->

        })
        val alert  = builder.create()
        alert.show()
    }

    fun manageGroups(view: View){
        val intent = Intent(this, GroupsActivity::class.java)
        intent.putExtra("isGroup", isGroup)
        startActivity(intent)
    }

    fun updateUI(){
        etName.setText(NetworkAPI.contact)
        var size = NetworkAPI.conversationSize(NetworkAPI.contact!!)
        tvConversationSize.setText(size + " MB")
        swuitchVisible.isChecked = NetworkAPI.isInvisible(NetworkAPI.contact!!) == 0
    }
}
