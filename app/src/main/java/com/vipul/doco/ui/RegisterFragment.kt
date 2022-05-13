package com.vipul.doco.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.vipul.doco.R
import com.vipul.doco.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        auth = FirebaseAuth.getInstance()

        binding.regGotoLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.regBtn.setOnClickListener {
            val email = binding.regEmail.text.toString()
            val pass = binding.regPassword.text.toString()
            val confirmPass = binding.regConfirmPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    registerUser(email, pass)
                } else {
                    binding.regConfirmPasswordLayout.error = "Password Doesn't Match"
                }
            } else {
                Toast.makeText(context, "Fields can't be empty!", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun registerUser(email: String, pass: String) {
        toggleProgressBar()
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                toggleProgressBar()
                findNavController().navigate(R.id.action_registerFragment_to_setupFragment)
            } else {
                toggleProgressBar()
                Toast.makeText(context, "Error : ${task.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun toggleProgressBar() {
        if (binding.regProgressBar.visibility == View.VISIBLE) {
            binding.regProgressBar.visibility = View.INVISIBLE
            binding.regBtn.isEnabled = true
        } else {
            binding.regProgressBar.visibility = View.VISIBLE
            binding.regBtn.isEnabled = false
        }
    }
}