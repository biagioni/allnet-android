package org.alnet.allnetandroid.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_settings.*
import org.alnet.allnetandroid.NetworkAPI
import org.alnet.allnetandroid.R

class SettingsActivity : AppCompatActivity() {

    private var isGroup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (NetworkAPI.isGroup(NetworkAPI.contact!!) == 1){
            tvManageParticipants.text = getString(R.string.manage_participants)
            tvDeleteUser.text = getString(R.string.delete_group)
            isGroup = true
        }else{
            tvManageParticipants.text = getString(R.string.manage_groups)
            tvDeleteUser.text = getString(R.string.delete_user)
            isGroup = false
        }

        supportActionBar!!.title = NetworkAPI.contact
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
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete the conversation?")

        builder.setPositiveButton("Delete", {
            dialogInterface, i ->
            NetworkAPI.deleteConversation(NetworkAPI.contact!!)
            updateUI()
        })

        builder.setNegativeButton("Cancel", {
            dialogInterface, i ->

        })
        val alert  = builder.create()
        alert.show()
    }

    fun deleteUser(view: View){
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete the conversation?")

        builder.setPositiveButton("Delete", {
            dialogInterface, i ->
            NetworkAPI.deleteUser(NetworkAPI.contact!!)
            finish()
        })

        builder.setNegativeButton("Cancel", {
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

    @SuppressLint("SetTextI18n")
    private fun updateUI(){
        etName.setText(NetworkAPI.contact)
        val size = NetworkAPI.conversationSize(NetworkAPI.contact!!)
        tvConversationSize.text = "$size${getString(R.string.mb)}"
        swuitchVisible.isChecked = NetworkAPI.isInvisible(NetworkAPI.contact!!) == 0
    }
}
