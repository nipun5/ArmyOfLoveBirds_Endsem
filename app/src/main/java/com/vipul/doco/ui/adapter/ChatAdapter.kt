package com.vipul.doco.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.vipul.doco.R
import com.vipul.doco.data.model.Chat

private const val LEFT_BUBBLE = 0
private const val RIGHT_BUBBLE = 1

class ChatAdapter(private val chatList: ArrayList<Chat>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val currentUserID = FirebaseAuth.getInstance().currentUser?.uid.toString()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return if (viewType == RIGHT_BUBBLE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.right_chat_bubble, parent, false)
            ChatViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.left_chat_bubble, parent, false)
            ChatViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.message.text = chatList[position].message
    }

    override fun getItemCount(): Int = chatList.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message: MaterialTextView = itemView.findViewById(R.id.chat_bubble_message)
    }

    override fun getItemViewType(position: Int): Int =
        if (currentUserID == chatList[position].senderId) RIGHT_BUBBLE else LEFT_BUBBLE
}