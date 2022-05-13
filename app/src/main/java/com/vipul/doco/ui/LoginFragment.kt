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
import com.vipul.doco.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        // firebase
        auth = FirebaseAuth.getInstance()

        binding.loginGotoReg.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.loginForgotPass.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_resetPasswordFragment)
        }

        binding.loginBtn.setOnClickListener {
            val email: String = binding.loginEmail.text.toString()
            val pass: String = binding.loginPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                loginUser(email, pass)
            } else if (email.isEmpty() && pass.isEmpty()) {
                binding.loginEmailLayout.error = "Email can't be empty"
                binding.loginPasswordLayout.error = "Password can't be empty"
            } else if (email.isEmpty()) {
                binding.loginEmailLayout.error = "Email can't be empty"
            } else if (pass.isEmpty()) {
                binding.loginPasswordLayout.error = "Password can't be empty"
            }
        }
        return binding.root
    }

    private fun loginUser(email: String, pass: String) {
        toggleProgressBar()
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                findNavController().navigate(R.id.action_loginFragment_to_mainActivity)
                binding.loginBtn.isEnabled = true
            } else {
                toggleProgressBar()
                Toast.makeText(context, "Error : ${task.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun toggleProgressBar() {
        if (binding.loginProgressBar.visibility == View.VISIBLE) {
            binding.loginProgressBar.visibility = View.INVISIBLE
            binding.loginBtn.isEnabled = true
        } else {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.loginBtn.isEnabled = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_mainActivity)
        }
    }
}