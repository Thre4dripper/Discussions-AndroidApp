package com.example.discussions.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.databinding.ActivityCreateEditPollBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.viewModels.CreateEditPollViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreateEditPollActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEditPollBinding
    private lateinit var viewModel: CreateEditPollViewModel

    private lateinit var loadingDialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_edit_poll)
        viewModel = ViewModelProvider(this)[CreateEditPollViewModel::class.java]

        //initializing back button
        binding.createPollBackBtn.setOnClickListener {
            finish()
        }

        //initializing poll creation button
        binding.createPollBtn.setOnClickListener {

        }

        //initializing user profile image click to zoom in button
        binding.createPollProfileIv.setOnClickListener {
            val intent = Intent(this, ZoomImageActivity::class.java)
            intent.putExtra(Constants.ZOOM_IMAGE_URL, viewModel.profileImage)
            startActivity(intent)
        }

        initLoadingDialog()
        initUsernameAndImage()
        initPollOptionsControl()
    }

    /**
     * METHOD FOR INITIALIZING LOADING DIALOG
     */
    private fun initLoadingDialog() {
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root)
            .setCancelable(false).show()
        loadingDialog.dismiss()
    }

    /**
     * METHOD FOR INITIALIZING USERNAME AND PROFILE IMAGE
     */
    private fun initUsernameAndImage() {
        loadingDialog.show()
        viewModel.isApiFetched.observe(this) {
            if (it != null) {
                loadingDialog.dismiss()

                if (it == Constants.API_SUCCESS) {
                    Glide.with(this)
                        .load(viewModel.profileImage)
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.createPollProfileIv)

                    binding.createPollUsernameTv.text = viewModel.username
                } else {
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.getUsernameAndImage(this)
    }

    private fun initPollOptionsControl() {

    }
}