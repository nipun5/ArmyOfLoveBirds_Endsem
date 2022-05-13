package com.vipul.doco.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.textview.MaterialTextView
import com.vipul.doco.ui.OnItemClickListener
import com.vipul.doco.R

class HomeAdapter(
    private val options: FirestoreRecyclerOptions<Post>,
    private val listener: OnItemClickListener
) :
    FirestoreRecyclerAdapter<Post, PostViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
        return PostViewHolder(view, options, listener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
        holder.dogName.text = model.dog_name
        holder.dogGender.text = model.dog_gender
        holder.dogBreed.text = model.dog_breed
        Glide.with(holder.itemView).load(model.dog_image).into(holder.dogImage)

        holder.chip1.text = model.post_tags[0]
        holder.chip2.text = model.post_tags[1]
        holder.chip3.text = model.post_tags[2]


        if (model.dog_gender == "Male")
            holder.dogGenderContainer.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.male_bg
                )
            )
        else
            holder.dogGenderContainer.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.female_bg
                )
            )

    }
}

class PostViewHolder(
    itemView: View,
    private val options: FirestoreRecyclerOptions<Post>,
    private val listener: OnItemClickListener
) :
    RecyclerView.ViewHolder(itemView), View.OnClickListener {

    val dogName: MaterialTextView = itemView.findViewById(R.id.home_puppy_name)

    val dogGender: MaterialTextView = itemView.findViewById(R.id.home_puppy_gender)
    val dogBreed: MaterialTextView = itemView.findViewById(R.id.home_puppy_breed)
    val dogImage: ImageView = itemView.findViewById(R.id.home_puppy_img)
    val dogGenderContainer: MaterialCardView =
        itemView.findViewById(R.id.home_puppy_gender_container)

    val chip1: Chip = itemView.findViewById(R.id.home_chip_1)
    val chip2: Chip = itemView.findViewById(R.id.home_chip_2)
    val chip3: Chip = itemView.findViewById(R.id.home_chip_3)

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

data class Post(
    val dog_name: String = "dog_name",
    val dog_desc: String = "dog_desc",
    val dog_age: String = "dog_age",
    val dog_gender: String = "dog_gender",
    val dog_breed: String = "dog_breed",
    val dog_image: String = "dog_image",
    val owner_uid: String = "uid",
    val post_tags: List<String> = arrayListOf("chip1")
)