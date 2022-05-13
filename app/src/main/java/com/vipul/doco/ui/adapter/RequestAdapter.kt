package com.vipul.doco.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.textview.MaterialTextView
import com.vipul.doco.R
import com.vipul.doco.ui.OnButtonClickListener

class RequestAdapter(
    private val options: FirestoreRecyclerOptions<Request>,
    private val listener: OnButtonClickListener
) : FirestoreRecyclerAdapter<Request, RequestViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view, options, listener)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int, model: Request) {
        holder.userName.text = model.username
        holder.dogName.text = model.dog_name
        holder.reason.text = model.reason
        Glide.with(holder.itemView.context).load(model.user_profile_pic).into(holder.userImg)
    }
}

class RequestViewHolder(
    itemView: View,
    private val options: FirestoreRecyclerOptions<Request>,
    private val listener: OnButtonClickListener
) :
    RecyclerView.ViewHolder(itemView), View.OnClickListener {

    init {
        itemView.setOnClickListener(this)
    }

    val userName: MaterialTextView = itemView.findViewById(R.id.request_name)
    val userImg: ImageView = itemView.findViewById(R.id.request_img)
    val dogName: MaterialTextView = itemView.findViewById(R.id.request_dog_name)
    val reason : MaterialTextView = itemView.findViewById(R.id.request_reason)

    override fun onClick(v: View?) {
        listener.onItemClick(
            documentSnapshot = options.snapshots.getSnapshot(adapterPosition),
            position = adapterPosition
        )
    }
}

data class Request(
    val userID: String = "userID",
    val username: String = "username",
    val user_bio: String = "user_bio",
    val user_profile_pic: String = "user_profile_pic",
    val reason: String = "reason",
    val dog_name: String = "dog_name"
)