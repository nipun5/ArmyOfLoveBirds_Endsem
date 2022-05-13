package com.vipul.doco.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vipul.doco.R
import com.vipul.doco.databinding.FragmentProfileBinding
import com.vipul.doco.utils.DBConstants.USERNAME
import com.vipul.doco.utils.DBConstants.USERS
import com.vipul.doco.utils.DBConstants.USER_BIO
import com.vipul.doco.utils.DBConstants.USER_PROFILE_PIC
import com.vipul.doco.utils.DBConstants.USER_PROFILE_PICTURES
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ProfileFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var currentUserID: String
    private var profileImageURI = Uri.EMPTY

    companion object {
        private const val STORAGE_PERMISSION_CODE = 678
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        // init
        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser?.uid.toString()
        firebaseFirestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        GlobalScope.launch(Dispatchers.IO) {
            val source = Source.CACHE
            firebaseFirestore.collection(USERS).document(currentUserID).get(source)
                .addOnSuccessListener { documentSnapshot ->
                    val name = documentSnapshot.getString(USERNAME).toString()
                    val bio = documentSnapshot.getString(USER_BIO)
                    val img = documentSnapshot.getString(USER_PROFILE_PIC)

                    binding.profileName.setText(name)
                    binding.profileBio.setText(bio)
                    Glide.with(requireContext()).load(img).into(binding.profileImg)
                }
        }

        binding.profileImg.setOnClickListener {
            if (hasStoragePermission())
                openGallery()
            else
                requestStoragePermission()
        }

        binding.profileSaveBtn.setOnClickListener {
            val name = binding.profileName.text.toString()
            val bio = binding.profileBio.text.toString()
            if (name.isNotEmpty() && bio.isNotEmpty())
                saveToFirebase(name, bio)
        }

        binding.profileLogoutBtn.setOnClickListener {
            auth.signOut()
            activity?.finish()
        }

        return binding.root
    }

    private fun hasStoragePermission() =
        EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)

    private fun requestStoragePermission() {
        EasyPermissions.requestPermissions(
            this,
            "We can't choose a profile picture without this permission",
            STORAGE_PERMISSION_CODE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun openGallery() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(requireContext(), this)
    }

    private fun saveToFirebase(name: String, bio: String) {
        if (profileImageURI != Uri.EMPTY) {
            saveProfileImage()
            saveInfo(name, bio)
        } else {
            saveInfo(name, bio)
        }
    }

    private fun saveInfo(name: String, bio: String) {
        val fields = hashMapOf("username" to name, "user_bio" to bio)
        firebaseFirestore.collection(USERS).document(currentUserID)
            .set(fields, SetOptions.merge())
    }

    private fun saveProfileImage() {
        val imagePath = storageReference.child(USER_PROFILE_PICTURES).child("$currentUserID.jpg")
        imagePath.putFile(profileImageURI).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imagePath.downloadUrl.addOnSuccessListener { uri ->
                    val field = hashMapOf(USER_PROFILE_PIC to uri.toString())
                    firebaseFirestore.collection(USERS).document(currentUserID)
                        .set(field, SetOptions.merge())
                }
            } else {
                Toast.makeText(context, "Error : ${task.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestStoragePermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        openGallery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                profileImageURI = result.uri
                binding.profileImg.setImageURI(profileImageURI)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(requireContext(), "Error : ${result.error}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}