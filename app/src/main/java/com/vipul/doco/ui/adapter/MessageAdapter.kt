package com.vipul.doco.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.textview.MaterialTextView
import com.mikhaellopez.circularimageview.CircularImageView
import com.vipul.doco.R
import com.vipul.doco.ui.OnItemClickListener

class MessageAdapter(
    private val options: FirestoreRecyclerOptions<Friend>,
    private val listener: OnItemClickListener
) : FirestoreRecyclerAdapter<Friend, FriendViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_messages, parent, false)
        return FriendViewHolder(view, options, listener)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int, model: Friend) {
        Glide.with(holder.itemView).load(model.friend_profile_pic).into(holder.messageProfilePic)
        holder.messageName.text = model.friend_name
        holder.lastMessage.text = model.friend_last_message
    }
}

class FriendViewHolder(
    itemView: View,
    private val options: FirestoreRecyclerOptions<Friend>,
    val listener: OnItemClickListener
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    val messageProfilePic: CircularImageView = itemView.findViewById(R.id.message_profile_pic)
    val messageName: MaterialTextView = itemView.findViewById(R.id.message_name)
    val lastMessage: MaterialTextView = itemView.findViewById(R.id.last_message)

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        listener.onItemClick(
            documentSnapshot = options.snapshots.getSnapshot(adapterPosition),
            position = adapterPosition
        )
    }
}

data class Friend(
    val friend_name: String = "friend_name",
    val friend_id: String = "friend_id",
    val friend_profile_pic: String = "friend_profile_pic",
    val friend_last_message: String = "Say Hi!"
)