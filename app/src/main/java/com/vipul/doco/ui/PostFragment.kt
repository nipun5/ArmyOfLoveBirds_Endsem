package com.vipul.doco.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vipul.doco.R
import com.vipul.doco.databinding.FragmentPostBinding
import com.vipul.doco.utils.DBConstants.POSTS
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import java.util.*


class PostFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentPostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var currentUserID: String
    private lateinit var gender: String
    private lateinit var breed: String
    private var dogImgURI = Uri.EMPTY

    private var currentAddress: String? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_CODE = 123
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_post, container, false)

        // init
        firebaseFirestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser?.uid.toString()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // image selection
        binding.newDogImg.setOnClickListener {
            openGallery()
        }

        // Breed Selection
        val breeds = resources.getStringArray(R.array.breeds)
        val arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, breeds)
        binding.newDogBreed.setAdapter(arrayAdapter)

        // Breed Drop Down Menu
        binding.newDogBreed.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, position, _ ->
                breed = adapterView.getItemAtPosition(position).toString()
            }

        // Gender Selection
        binding.maleContainer.setOnClickListener {
            binding.maleContainer.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray
                )
            )
            binding.femaleContainer.setBackgroundColor(Color.TRANSPARENT)
            gender = resources.getString(R.string.male)
        }

        binding.femaleContainer.setOnClickListener {
            binding.femaleContainer.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray
                )
            )
            binding.maleContainer.setBackgroundColor(Color.TRANSPARENT)
            gender = resources.getString(R.string.female)
        }

        // location
        binding.locationEditTxt.setOnTouchListener { _, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.locationEditTxt.right - binding.locationEditTxt.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    if (hasLocationPermission()) {
                        getCurrentLocation()
                    } else
                        requestLocationPermission()
                }
            }
            false
        }

        // Post button
        binding.newPostBtn.setOnClickListener {
            val name = binding.newDogName.text.toString().trim()
            val desc = binding.newDogDesc.text.toString().trim()
            val age = binding.newDogAge.text.toString().trim()
            val weight = binding.newDogWeight.text.toString().trim()
            val color = binding.newColor.text.toString().capitalize(Locale.ROOT).trim()

            if (currentAddress == null)
                currentAddress = binding.locationEditTxt.text.toString()

            // chips
            val tagsList = binding.chipGroup.children.toList().filter { (it as Chip).isChecked }
                .joinToString(", ") { (it as Chip).text }.split(", ")

            if (name.isNotEmpty() && desc.isNotEmpty() && age.isNotEmpty()
                && gender.isNotEmpty() && breed.isNotEmpty() && weight.isNotEmpty()
                && color.isNotEmpty() && tagsList.isNotEmpty() && dogImgURI != Uri.EMPTY
            ) {
                createPost(name, desc, age, weight, color, gender, breed, tagsList)
            }
        }
        return binding.root
    }

    private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "We can't get your location without this permission",
            LOCATION_PERMISSION_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun getCoordinatesFromLocation(): GeoPoint {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address> = geoCoder.getFromLocationName(currentAddress, 1)
        val address: Address = addresses[0]
        return GeoPoint(address.latitude, address.longitude)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                val geoCoder = Geocoder(requireContext())
                val currentLocation = geoCoder.getFromLocation(latitude, longitude, 1)
                currentAddress = currentLocation.first().locality
                binding.locationEditTxt.setText(currentAddress)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enable GPS and try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openGallery() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(requireContext(), this);
    }

    private fun createPost(
        name: String,
        desc: String,
        age: String,
        weight: String,
        color: String,
        gender: String,
        breed: String,
        tagsList: List<String>
    ) {

        val fields = hashMapOf(
            "owner_uid" to currentUserID,
            "dog_name" to name,
            "dog_desc" to desc,
            "dog_age" to age,
            "dog_weight" to weight,
            "dog_color" to color,
            "dog_gender" to gender,
            "dog_breed" to breed,
            "post_tags" to tagsList,
            "coordinates" to
                    if (latitude == 0.0 && longitude == 0.0)
                        getCoordinatesFromLocation()
                    else GeoPoint(latitude, longitude),
            "current_location" to currentAddress
        )

        val imagePath = storageReference.child(POSTS).child(UUID.randomUUID().toString())
        imagePath.putFile(dogImgURI).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imagePath.downloadUrl.addOnSuccessListener { uri ->
                    fields["dog_image"] = uri.toString()
                    firebaseFirestore.collection(POSTS).document().set(fields, SetOptions.merge())
                }
            } else {
                Toast.makeText(context, "${task.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        getCurrentLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                dogImgURI = result.uri
                binding.newDogImg.setImageURI(dogImgURI)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(context, "Error : $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}