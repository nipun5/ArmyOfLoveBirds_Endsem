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
import com.vipul.doco.databinding.FragmentResetPasswordBinding

class ResetPasswordFragment : Fragment() {
    private lateinit var binding: FragmentResetPasswordBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_reset_password, container, false)

        // init
        auth = FirebaseAuth.getInstance()

        binding.resetPassBtn.setOnClickListener {
            val email: String = binding.resetPassEmail.text.toString()
            if (email.isNotEmpty())
                resetPassword(email)
            else
                Toast.makeText(context, "Email can't be empty!", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Please check your Email!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
            } else {
                Toast.makeText(context, "Error : ${task.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}