package com.vipul.doco.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vipul.doco.R
import com.vipul.doco.databinding.FragmentContactBottomSheetBinding
import com.vipul.doco.utils.DBConstants.REQUESTS
import com.vipul.doco.utils.DBConstants.USERNAME
import com.vipul.doco.utils.DBConstants.USERS
import com.vipul.doco.utils.DBConstants.USER_BIO
import com.vipul.doco.utils.DBConstants.USER_PROFILE_PIC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContactOwnerBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentContactBottomSheetBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var ownerID: String
    private lateinit var currentUserID: String
    private lateinit var currentUserName: String
    private lateinit var currentUserBio: String
    private lateinit var currentUserImg: String
    private lateinit var firebaseFirestore: FirebaseFirestore

    private val args: ContactOwnerBottomSheetFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_contact_bottom_sheet,
            container,
            false
        )

        // init
        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser?.uid.toString()
        firebaseFirestore = FirebaseFirestore.getInstance()

        GlobalScope.launch(Dispatchers.IO) {
            firebaseFirestore.collection(USERS).document(currentUserID)
                .get().addOnSuccessListener { documentSnapshot ->
                    currentUserName = documentSnapshot.getString(USERNAME).toString()
                    currentUserBio = documentSnapshot.getString(USER_BIO).toString()
                    currentUserImg = documentSnapshot.getString(USER_PROFILE_PIC).toString()
                }
        }

        // args
        ownerID = args.ownerID
        binding.contactName.text = args.ownerName
        binding.contactBio.text = args.ownerBio
        Glide.with(this).load(args.ownerImg).into(binding.contactProfilePic)

        // btn
        binding.askForAdoptionBtn.setOnClickListener {
            val reason = binding.contactWhy.text.toString()
            if (reason.isNotEmpty())
                storeToFirestore(reason, args.dogName)
        }
        return binding.root
    }

    private fun storeToFirestore(reason: String, dogName: String) {
        val fields = hashMapOf(
            "userID" to currentUserID,
            "reason" to reason,
            "username" to currentUserName,
            "user_bio" to currentUserBio,
            "user_profile_pic" to currentUserImg,
            "dog_name" to dogName
        )

        firebaseFirestore.collection(USERS).document(ownerID).collection(REQUESTS)
            .document().set(fields)

        Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show()
    }

    override fun getTheme(): Int = R.style.BottomSheetTheme
}