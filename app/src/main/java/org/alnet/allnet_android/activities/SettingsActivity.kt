package org.alnet.allnet_android.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*
import org.alnet.allnet_android.NetworkAPI
import org.alnet.allnet_android.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (NetworkAPI.isGroup(NetworkAPI.contact!!) == 1){
            tvManageParticipants.setText("Manage participants")
            tvDeleteUser.setText("Delete group")
        }else{
            tvManageParticipants.setText("Manage groups")
            tvDeleteUser.setText("Delete user")
        }
    }
}
