package org.alnet.allnet_android.activities

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_tab_bar.*
import org.alnet.allnet_android.NetworkAPI
import org.alnet.allnet_android.R
import org.alnet.allnet_android.fragments.ContactListFragment
import org.alnet.allnet_android.fragments.ContactNewFragment
import org.alnet.allnet_android.fragments.MoreFragment

class TabBarActivity : AppCompatActivity() {

    var toolBar: ActionBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_bar)

        val file = filesDir
        val path = file.absolutePath

        NetworkAPI.initialize(path)

        toolBar = supportActionBar
        if (toolBar != null) {
            toolBar!!.setDisplayHomeAsUpEnabled(false)
            toolBar!!.title = "Contacts"
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        loadFragment(ContactListFragment())
    }

    fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_contacts -> {
                if (toolBar != null) {
                    toolBar!!.setTitle(R.string.contacts)
                }
                loadFragment(ContactListFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_new -> {
                if (toolBar != null) {
                    toolBar!!.setTitle(R.string.new_contact)
                }
                loadFragment(ContactNewFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_more -> {
                if (toolBar != null) {
                    toolBar!!.setTitle(R.string.more)
                }
                loadFragment(MoreFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}
