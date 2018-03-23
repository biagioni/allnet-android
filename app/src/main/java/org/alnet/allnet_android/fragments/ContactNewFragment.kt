package org.alnet.allnet_android.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_contact_new.*
import org.alnet.allnet_android.R
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import kotlinx.android.synthetic.main.fragment_contact_new.view.*


class ContactNewFragment : Fragment() {

    var spinner: Spinner? = null
    var editTextName: EditText? = null
    var editTextSecret: EditText? = null

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
            val name = editTextName!!.text
            val secret = editTextSecret!!.text
            val index = spinner!!.selectedItemPosition
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


}
