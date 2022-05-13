package com.vipul.doco.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vipul.doco.R
import com.vipul.doco.databinding.FragmentHomeDetailBinding
import com.vipul.doco.utils.DBConstants.USERNAME
import com.vipul.doco.utils.DBConstants.USERS
import com.vipul.doco.utils.DBConstants.USER_BIO
import com.vipul.doco.utils.DBConstants.USER_PROFILE_PIC


const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

class HomeDetailFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentHomeDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var currentUserID: String
    private lateinit var name: String
    private lateinit var bio: String
    private lateinit var img: String

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private val args: HomeDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_detail, container, false)

        // hide toolbar
        (activity as AppCompatActivity).supportActionBar?.hide()

        // init
        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser?.uid.toString()
        firebaseFirestore = FirebaseFirestore.getInstance()

        // args data
        binding.detailDogName.text = args.dogName
        binding.detailDogBreed.text = args.dogBreed
        binding.detailDogDesc.text = args.dogDesc
        binding.detailDogColor.text = args.dogColor
        binding.detailDogWeight.text = resources.getString(R.string.get_weight, args.dogWeight)
        binding.detailDogAge.text = resources.getString(R.string.get_age, args.dogAge)
        Glide.with(this).load(args.dogImg).into(binding.detailDogImg)
        if (args.dogGender == resources.getString(R.string.male))
            setGenderSymbol(R.drawable.male)
        else
            setGenderSymbol(R.drawable.female)

        // disable adoption button for owner
        if (currentUserID == args.ownerID)
            binding.detailContactBtn.isEnabled = false

        firebaseFirestore.collection(USERS).document(args.ownerID).get()
            .addOnSuccessListener { documentSnapshot ->
                name = documentSnapshot.getString(USERNAME).toString()
                bio = documentSnapshot.getString(USER_BIO).toString()
                img = documentSnapshot.getString(USER_PROFILE_PIC).toString()
            }

        // map
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null)
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)

        mapView = binding.detailMap
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        // btn click
        binding.detailContactBtn.setOnClickListener {
            val action =
                HomeDetailFragmentDirections.actionHomeDetailFragmentToContactOwnerBottomSheetFragment(
                    ownerName = name,
                    ownerBio = bio,
                    ownerImg = img,
                    ownerID = args.ownerID,
                    dogName = args.dogName
                )
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun setGenderSymbol(sym: Int) =
        binding.detailGenderSym.setImageDrawable(ContextCompat.getDrawable(requireContext(), sym))

    override fun onMapReady(gMap: GoogleMap) {
        map = gMap
        map.uiSettings.isZoomControlsEnabled = true
        val ny = LatLng(args.latitude.toDouble(), args.longitude.toDouble())
        map.addMarker(MarkerOptions().position(ny))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ny, 15F))
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}