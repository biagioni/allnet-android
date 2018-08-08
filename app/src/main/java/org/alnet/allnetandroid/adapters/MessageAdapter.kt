package org.alnet.allnetandroid.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.message_item_send.view.*
import org.alnet.allnetandroid.R
import org.alnet.allnetandroid.inflate
import org.alnet.allnetandroid.model.*
import android.graphics.drawable.GradientDrawable
import org.alnet.allnetandroid.toDate
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by docouto on 3/26/18.
 */

class MessageAdapter(private val messages: List<MessageModel>): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            MSG_TYPE_RCVD -> {
                val inflatedView = parent.inflate(R.layout.message_item_received, false)
                ViewHolder(inflatedView)
            }
            MSG_TYPE_SENT -> {
                val inflatedView = parent.inflate(R.layout.message_item_send, false)
                ViewHolder(inflatedView)
            }
            MSG_MISSED -> {
                val inflatedView = parent.inflate(R.layout.message_item_missing, false)
                ViewHolder(inflatedView)
            }
            else -> {
                val inflatedView = parent.inflate(R.layout.message_item_sent_acked, false)
                ViewHolder(inflatedView)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        if (message.type == MSG_TYPE_SENT){
            if (message.message_has_been_acked != 0) {
                return MSG_TYPE_SENT_ACKED
            }
        }
        return message.type
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(messageModel: MessageModel){
            if (messageModel.type == MSG_TYPE_RCVD) {
                var fractionOfDay:Double = 1.0

                val SECONDS_PER_DAY = (24 * 60 * 60).toDouble()

                val date = messageModel.date.toDate().time
                val dateNow = Date().time
                val seconds = dateNow - date
                val elapsed = TimeUnit.MILLISECONDS.toSeconds(seconds)

                if (elapsed < SECONDS_PER_DAY) {
                    fractionOfDay = elapsed / SECONDS_PER_DAY
                }
                val redValue = (255*fractionOfDay).toInt()
                val myGrad = itemView.background as GradientDrawable
                myGrad.setStroke(5, Color.rgb( redValue,255,255))
            }
            itemView.tvMessage.text = messageModel.message
            itemView.tvDate.text = messageModel.date
        }
    }
}