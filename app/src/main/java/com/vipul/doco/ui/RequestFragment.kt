package com.vipul.doco.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.vipul.doco.R
import com.vipul.doco.databinding.FragmentRequestBinding
import com.vipul.doco.ui.adapter.Request
import com.vipul.doco.ui.adapter.RequestAdapter
import com.vipul.doco.ui.adapter.RequestViewHolder
import com.vipul.doco.utils.DBConstants.FRIENDS
import com.vipul.doco.utils.DBConstants.REQUESTS
import com.vipul.doco.utils.DBConstants.USERNAME
import com.vipul.doco.utils.DBConstants.USERS
import com.vipul.doco.utils.DBConstants.USER_BIO
import com.vipul.doco.utils.DBConstants.USER_PROFILE_PIC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RequestFragment : Fragment(), OnButtonClickListener {

    private lateinit var binding: FragmentRequestBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var adapter: FirestoreRecyclerAdapter<Request, RequestViewHolder>

    // current user
    private lateinit var currentUserID: String
    private lateinit var ownerName: String
    private lateinit var ownerBio: String
    private lateinit var ownerImg: String

    // user who sent request
    private lateinit var userID: String
    private lateinit var userName: String
    private lateinit var userBio: String
    private lateinit var userImg: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_request, container, false)

        // init
        firebaseFirestore = FirebaseFirestore.getInstance()
        currentUserID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        // firestore query
        val query = FirebaseFirestore.getInstance().collection(USERS).document(currentUserID)
            .collection(REQUESTS)
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<Request>()
            .setQuery(query, Request::class.java)
            .build()

        adapter = RequestAdapter(options = firestoreRecyclerOptions, listener = this)

        binding.requestRv.setHasFixedSize(true)
        binding.requestRv.adapter = adapter

        // get current user data
        GlobalScope.launch(Dispatchers.IO) {
            firebaseFirestore.collection(USERS).document(currentUserID).get()
                .addOnSuccessListener {
                    ownerName = it.getString(USERNAME).toString()
                    ownerBio = it.getString(USER_BIO).toString()
                    ownerImg = it.getString(USER_PROFILE_PIC).toString()
                }
        }

        return binding.root
    }

    override fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int) {
        GlobalScope.launch {
            userID = documentSnapshot.getString("userID").toString()

            firebaseFirestore.collection(USERS).document(userID).get()
                .addOnSuccessListener {
                    userName = it.getString(USERNAME).toString()
                    userBio = it.getString(USER_BIO).toString()
                    userImg = it.getString(USER_PROFILE_PIC).toString()
                }

            withContext(Dispatchers.Main) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Request")
                    .setMessage("Do you want to add ?")
                    .setNegativeButton("Decline") { _, _ ->
                        declineRequest(documentSnapshot)
                    }
                    .setPositiveButton("Accept") { _, _ ->
                        acceptRequest(documentSnapshot)
                    }.show()
            }
        }
    }

    private fun acceptRequest(documentSnapshot: DocumentSnapshot) {
        // for current user (add the person who sent request to friend list)
        val currentUserFriendFields = getFields(userName, userBio, userImg, userID)
        // for user who sent request (add owner of dog to friend list)
        val userFriendFields = getFields(ownerName, ownerBio, ownerImg, currentUserID)

        firebaseFirestore.collection(USERS).document(currentUserID).collection(FRIENDS)
            .document(userID).set(currentUserFriendFields)

        firebaseFirestore.collection(USERS).document(userID).collection(FRIENDS)
            .document(currentUserID).set(userFriendFields)

        firebaseFirestore.collection(USERS).document(currentUserID).collection(REQUESTS)
            .document(documentSnapshot.id).delete().addOnSuccessListener {
                Toast.makeText(context, "Request Accepted!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun declineRequest(documentSnapshot: DocumentSnapshot) {
        firebaseFirestore.collection(USERS).document(currentUserID).collection(REQUESTS)
            .document(documentSnapshot.id).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Request Declined!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getFields(
        name: String, bio: String, img: String, ID: String
    ): HashMap<String, String> {
        return hashMapOf(
            "friend_name" to name,
            "friend_bio" to bio,
            "friend_profile_pic" to img,
            "friend_id" to ID
        )
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

interface OnButtonClickListener {
    fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int)
}