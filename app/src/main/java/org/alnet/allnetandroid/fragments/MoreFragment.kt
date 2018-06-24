package org.alnet.allnetandroid.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_more.view.*
import org.alnet.allnetandroid.INetwork
import org.alnet.allnetandroid.NetworkAPI
import org.alnet.allnetandroid.R


class MoreFragment : Fragment(), INetwork {

    var tvOutput: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)
        tvOutput = view.tvOutput
        NetworkAPI.listener = this

        view.buttonTrace.setOnClickListener {
            tvOutput?.text = ""
            val hops = view.etHops.text.toString().toInt()
            NetworkAPI.startTrace(hops)
        }
        return view
    }

    //-----------NetworkAPI delegation----------------------

    @SuppressLint("SetTextI18n")
    override fun msgTrace(msg: String) {
        activity?.runOnUiThread {
            if (tvOutput?.text?.count()!! > 0){
                tvOutput?.text = tvOutput?.text.toString() + msg
            }else {
                tvOutput?.text =  msg
            }
        }
    }

}
