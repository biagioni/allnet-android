package org.alnet.allnet_android.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_more.*
import kotlinx.android.synthetic.main.fragment_more.view.*
import org.alnet.allnet_android.INetwork
import org.alnet.allnet_android.NetworkAPI
import org.alnet.allnet_android.R


class MoreFragment : Fragment(), INetwork {
    override fun listContactsUpdated() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listMsgUpdated() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun generatedRandomKey(key: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun keyGenerated(contact: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun keyExchanged(contact: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun incompletedContactsUpdated() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun msgTrace(msg: String) {
        activity.runOnUiThread {
            if (tvOutput?.text?.count()!! > 0){
                tvOutput?.text = tvOutput?.text.toString() + msg
            }else {
                tvOutput?.text =  msg
            }
        }
    }

    override fun ackedMessage(contact: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun groupCreated(result: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var tvOutput: TextView? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_more, container, false)
        tvOutput = view.tvOutput
        NetworkAPI.listener = this

        view.buttonTrace.setOnClickListener {
            tvOutput?.text = ""
            val hops = view.etHops.text.toString().toInt()
            if (hops != null) {
                NetworkAPI.startTrace(hops)
            }else{
                Toast.makeText(context, "Inform the hops value.", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }

}
