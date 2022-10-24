package com.example.discussions.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.databinding.FragmentSignupBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.viewModels.LoginViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import org.json.JSONObject

class SignupFragment(private var tabLayout: TabLayout) : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var viewModel: LoginViewModel

    private lateinit var loadingDialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]

        initLoadingDialog()
        initSignup()
        binding.lifecycleOwner = requireActivity()
        binding.viewModel = viewModel

        binding.signupBtn.setOnClickListener { signup() }
        return binding.root
    }

    private fun initLoadingDialog() {
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialog = MaterialAlertDialogBuilder(requireContext()).setView(dialogBinding.root)
            .setCancelable(false).show()
        loadingDialog.dismiss()
    }

    private fun initSignup() {
        viewModel.isRegistered.observe(viewLifecycleOwner) {
            //initial case
            if (it == null) return@observe

            //login success
            if (it == LoginViewModel.API_SUCCESS) {
                loadingDialog.dismiss()
                //switching to login tab
                tabLayout.selectTab(tabLayout.getTabAt(0))
                Toast.makeText(requireContext(), "Registered Successfully", Toast.LENGTH_SHORT)
                    .show()
            }
            //login failed
            else {
                loadingDialog.dismiss()
                handleSignupErrors(it)
            }
        }
    }

    private fun handleSignupErrors(message: String) {
        //showing toast for other errors and returning
        if (!message.contains("{")) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            return
        }
        val rootObject = JSONObject(message)
        val usernameError =
            if (rootObject.has("username")) rootObject.getJSONArray("username").get(0)
                .toString() else null
        val emailError = if (rootObject.has("email")) rootObject.getJSONArray("email").get(0)
            .toString() else null

        //username already exists error
        binding.usernameEt.error =
            if (usernameError != null) {
                binding.usernameEt.requestFocus()
                usernameError
            } else null

        //email already exists error
        binding.emailEt.error =
            if (emailError != null) {
                binding.emailEt.requestFocus()
                emailError
            } else null

    }

    private fun signup() {
        loadingDialog.show()
        val username = binding.usernameEt.text.toString()
        val email = binding.emailEt.text.toString()
        val password = binding.passwordEt.text.toString()

        //checking for username field
        if (username.isEmpty()) {
            binding.usernameEt.error = "Username is required"
            binding.usernameEt.requestFocus()
            loadingDialog.dismiss()
            return
        } else {
            binding.usernameEt.error = null
        }

        //checking for email field
        if (email.isEmpty()) {
            binding.emailEt.error = "Email is required"
            binding.emailEt.requestFocus()
            loadingDialog.dismiss()
            return
        } else {
            binding.emailEt.error = null
        }

        //checking for password field (error should be displayed on text input layout in password field)
        if (password.isEmpty()) {
            binding.passwordTil.error = "Password is required"
            binding.passwordEt.requestFocus()
            loadingDialog.dismiss()
            return
        } else {
            binding.passwordTil.error = null
        }

        viewModel.signup(requireActivity(), username, email, password)
    }
}