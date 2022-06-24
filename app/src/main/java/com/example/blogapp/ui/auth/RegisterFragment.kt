package com.example.blogapp.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.blogapp.R
import com.example.blogapp.core.Result
import com.example.blogapp.data.remote.auth.AuthDataSource
import com.example.blogapp.databinding.FragmentRegisterBinding
import com.example.blogapp.domain.auth.AuthRepoImpl
import com.example.blogapp.presentation.auth.AuthViewModel
import com.example.blogapp.presentation.auth.AuthViewModelFactory

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var binding:FragmentRegisterBinding
    private val viewModel by viewModels<AuthViewModel> {
        AuthViewModelFactory(
            AuthRepoImpl(
                AuthDataSource()
            )
        )
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)
        signUp()
    }

    private fun signUp(){

        binding.btnSignUp.setOnClickListener {
            val userName = binding.registerUserName.text.toString().trim()
            val password = binding.registerPassword.text.toString().trim()
            val passwordConfirm = binding.registerConfirmPassword.text.toString().trim()
            val email = binding.registerMail.text.toString().trim()

            if (validateUserData(
                    password,
                    passwordConfirm,
                    userName,
                    email
                )
            ) return@setOnClickListener

            createUser(email, password, userName)



            Log.d("reg","info: $userName ${password}, $passwordConfirm ${email}}")


        }
    }

    private fun createUser(email: String, password: String, userName: String) {
        viewModel.signUp(email,password, userName).observe(viewLifecycleOwner, Observer { result ->
            when(result){
                is Result.Loading -> {
                    binding.progressBarLogin.visibility = View.VISIBLE
                    binding.btnSignUp.isEnabled = false
                }
                is Result.Success -> {
                    binding.progressBarLogin.visibility = View.GONE
                    findNavController().navigate(R.id.action_registerFragment_to_setupProfileFragment)
                }
                is Result.Failure -> {
                    binding.progressBarLogin.visibility = View.GONE
                    binding.btnSignUp.isEnabled = true
                    Toast.makeText(requireContext(),"Error: ${result.exception}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun validateUserData(
        password: String,
        passwordConfirm: String,
        userName: String,
        email: String
    ): Boolean {
        if (password != passwordConfirm) {
            binding.registerConfirmPassword.error = "Password does not match."
            binding.registerPassword.error = "Password does not match"
            return true
        }

        if (userName.isEmpty()) {
            binding.registerUserName.error = "Username is empty."
            return true
        }

        if (email.isEmpty()) {
            binding.registerMail.error = "Email is empty."
            return true
        }

        if (password.isEmpty()) {
            binding.registerPassword.error = "Password is empty."
            return true
        }

        if (passwordConfirm.isEmpty()) {
            binding.registerConfirmPassword.error = "Password is empty."
            return true
        }
        return false
    }
}