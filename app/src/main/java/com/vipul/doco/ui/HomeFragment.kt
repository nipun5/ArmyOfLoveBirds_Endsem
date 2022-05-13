package com.vipul.doco.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vipul.doco.R
import com.vipul.doco.databinding.FragmentHomeBinding
import com.vipul.doco.ui.adapter.HomeAdapter
import com.vipul.doco.ui.adapter.Post
import com.vipul.doco.ui.adapter.PostViewHolder
import com.vipul.doco.utils.DBConstants.COORDINATES
import com.vipul.doco.utils.DBConstants.DOG_AGE
import com.vipul.doco.utils.DBConstants.DOG_BREED
import com.vipul.doco.utils.DBConstants.DOG_COLOR
import com.vipul.doco.utils.DBConstants.DOG_DESC
import com.vipul.doco.utils.DBConstants.DOG_GENDER
import com.vipul.doco.utils.DBConstants.DOG_IMAGE
import com.vipul.doco.utils.DBConstants.DOG_NAME
import com.vipul.doco.utils.DBConstants.DOG_WEIGHT
import com.vipul.doco.utils.DBConstants.OWNER_UID
import com.vipul.doco.utils.DBConstants.POSTS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(), OnItemClickListener {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: FirestoreRecyclerAdapter<Post, PostViewHolder>
    private lateinit var currentUserID: String
    private lateinit var query: Query

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        // init
        currentUserID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        // query
        query = FirebaseFirestore.getInstance().collection(POSTS)
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<Post>()
            .setQuery(query, Post::class.java)
            .build()

        adapter = HomeAdapter(options = firestoreRecyclerOptions, listener = this)

        binding.homeRv.setHasFixedSize(true)
        binding.homeRv.adapter = adapter

        binding.homeSwipeRefresh.setOnRefreshListener {
            binding.homeSwipeRefresh.isRefreshing = true
            adapter.notifyDataSetChanged()
            binding.homeSwipeRefresh.isRefreshing = false
        }

        binding.homeFab.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_filterBottomSheetFragment)
        }

        return binding.root
    }

    override fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int) {

        GlobalScope.launch(Dispatchers.IO) {
            val dogName = documentSnapshot.getString(DOG_NAME).toString()
            val dogAge = documentSnapshot.getString(DOG_AGE).toString()
            val dogDesc = documentSnapshot.getString(DOG_DESC).toString()
            val dogGender = documentSnapshot.getString(DOG_GENDER).toString()
            val dogBreed = documentSnapshot.getString(DOG_BREED).toString()
            val dogWeight = documentSnapshot.getString(DOG_WEIGHT).toString()
            val dogColor = documentSnapshot.getString(DOG_COLOR).toString()
            val dogImg = documentSnapshot.getString(DOG_IMAGE).toString()
            val ownerID = documentSnapshot.getString(OWNER_UID).toString()
            val geoPoint = documentSnapshot.getGeoPoint(COORDINATES)

            val lat = geoPoint?.latitude.toString()
            val lon = geoPoint?.longitude.toString()

            val action = HomeFragmentDirections.actionHomeFragmentToHomeDetailFragment(
                dogName = dogName,
                dogAge = dogAge,
                dogDesc = dogDesc,
                dogGender = dogGender,
                dogColor = dogColor,
                dogBreed = dogBreed,
                dogWeight = dogWeight,
                dogImg = dogImg,
                ownerID = ownerID,
                latitude = lat,
                longitude = lon
            )
            withContext(Dispatchers.Main) {
                findNavController().navigate(action)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}

interface OnItemClickListener {
    fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int)
}