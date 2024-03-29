package com.example.discussions.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.discussions.Constants
import com.example.discussions.MyApplication
import com.example.discussions.adapters.ProfileRecyclerAdapter
import com.example.discussions.adapters.interfaces.PostClickInterface
import com.example.discussions.databinding.FragmentProfileBinding
import com.example.discussions.databinding.LoadingDialogBinding
import com.example.discussions.ui.EditDetailsActivity
import com.example.discussions.ui.SettingsActivity
import com.example.discussions.ui.UserPostsActivity
import com.example.discussions.ui.ZoomImageActivity
import com.example.discussions.viewModels.PostsViewModel
import com.example.discussions.viewModels.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment(), PostClickInterface {
    private val TAG = "ProfileFragment"

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var postsViewModel: PostsViewModel

    private lateinit var loadingDialog: AlertDialog
    private lateinit var retryDialog: AlertDialog
    private lateinit var profileAdapter: ProfileRecyclerAdapter

    private lateinit var settingsCallback: ActivityResultLauncher<Intent>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        postsViewModel = ViewModelProvider(requireActivity())[PostsViewModel::class.java]

        //username from which profile is to be fetched
        val username =
            requireActivity().intent.getStringExtra(Constants.USERNAME) ?: MyApplication.username

        //for launching profile image zoom activity
        binding.profileIv.setOnClickListener {
            val intent = Intent(requireContext(), ZoomImageActivity::class.java)
            intent.putExtra(
                Constants.ZOOM_IMAGE_URL,
                viewModel.profileDataModel.profileImage
            )
            startActivity(intent)
        }

        //for launching edit details activity
        binding.editProfileBtn.apply {
            setOnClickListener {
                val intent = Intent(requireContext(), EditDetailsActivity::class.java)
                startActivity(intent)
            }
            visibility = if (username == MyApplication.username) View.VISIBLE else View.GONE
        }

        //for launching settings activity
        binding.profileSettingsBtn.apply {
            setOnClickListener {
                settingsCallback.launch(Intent(requireContext(), SettingsActivity::class.java))
            }
            visibility = if (username == MyApplication.username) View.VISIBLE else View.GONE
        }

        //for launching user posts activity
        binding.profilePostsCountCv.setOnClickListener {
            val intent = Intent(requireContext(), UserPostsActivity::class.java)
            intent.putExtra(Constants.USER_ID, viewModel.profileDataModel.userId)
            intent.putExtra(Constants.USERNAME, viewModel.profileDataModel.username)
            startActivity(intent)
        }

        binding.lifecycleOwner = this


        initDialogs(username)
        initPostsRecyclerView()
        initSettingsCallback()

        //swipe to refresh
        binding.profileSwipeLayout.setOnRefreshListener {
            viewModel.refreshProfile()
            getProfile(username)
        }

        getProfileObserver()
        paginatedUserPostsFlow()
        getUserPostsObserver()
        getProfile(username)
        return binding.root
    }

    private fun initDialogs(username: String) {
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialog = MaterialAlertDialogBuilder(requireContext()).setView(dialogBinding.root)
            .setCancelable(false).show()
        loadingDialog.dismiss()

        retryDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Oops!")
            .setMessage("Error getting profile")
            .setCancelable(false)
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
                getProfile(username)
            }
            .setNegativeButton("Cancel") { _, _ ->
                requireActivity().setResult(Constants.RESULT_CLOSE)
                requireActivity().finish()
            }
            .show()
        retryDialog.dismiss()
    }

    private fun initPostsRecyclerView() {
        profileAdapter = ProfileRecyclerAdapter(this)
        binding.profilePostsRv.apply {
            adapter = profileAdapter
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun getProfileObserver() {
        viewModel.isProfileFetched.observe(viewLifecycleOwner) {
            binding.profileSwipeLayout.isRefreshing = false
            if (it != null) {
                loadingDialog.dismiss()
                when (it) {
                    Constants.API_SUCCESS -> {
                        Glide.with(this)
                            .load(viewModel.profileDataModel.profileImage)
                            .into(binding.profileIv)

                        binding.profile = viewModel.profileDataModel
                        getUserPosts()
                    }

                    Constants.AUTH_FAILURE_ERROR -> {
                        requireActivity().setResult(Constants.RESULT_LOGOUT)
                        requireActivity().finish()
                    }

                    else -> {
                        retryDialog.show()
                    }
                }

            }
        }
    }

    private fun getProfile(username: String) {
        loadingDialog.show()
        viewModel.getProfile(requireContext(), username)
    }

    private fun paginatedUserPostsFlow() {
        binding.profilePostsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val layoutManager: RecyclerView.LayoutManager? = recyclerView.layoutManager
                val lastVisibleItemPosition =
                    (layoutManager as StaggeredGridLayoutManager?)!!.findLastVisibleItemPositions(
                        null
                    )[0]


                if (PostsViewModel.hasMorePosts.value!!
                    && PostsViewModel.paginationStatus.value == Constants.PAGE_IDLE
                    && lastVisibleItemPosition != RecyclerView.NO_POSITION
                    // api call when 4 items are left to be seen
                    && lastVisibleItemPosition >= profileAdapter.itemCount - Constants.PROFILE_POSTS_PAGING_SIZE / 2
                ) {
                    postsViewModel.getAllUserPosts(
                        requireContext(),
                        viewModel.profileDataModel.userId
                    )
                }
            }
        })
    }

    private fun getUserPostsObserver() {
        postsViewModel.userPostsList.observe(viewLifecycleOwner) {
            if (it != null) {
                //updating posts list recycler view
                profileAdapter.submitList(it)

                //hiding progress bar and lottie animation, when posts are loaded
                binding.profilePostsProgressBar.visibility = View.GONE
                binding.profilePostsLottieNoData.visibility = View.GONE

                //when no posts are available
                if (it.isEmpty()) {
                    //showing lottie animation
                    binding.profilePostsLottieNoData.visibility = View.VISIBLE
                    val error = postsViewModel.isUserPostsFetched.value

                    //when empty is due to network error, showing toast
                    if (error != Constants.API_SUCCESS && error != null) {
                        Toast.makeText(
                            requireContext(),
                            postsViewModel.isUserPostsFetched.value,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    if (error == Constants.AUTH_FAILURE_ERROR) {
                        requireActivity().setResult(Constants.RESULT_LOGOUT)
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    private fun getUserPosts() {
        //refreshing user posts, doesn't matter if it is first time or not
        postsViewModel.refreshUserPosts()
        //clearing previous posts list
        profileAdapter.submitList(mutableListOf())
        //showing progress bar
        binding.profilePostsProgressBar.visibility = View.VISIBLE
        //getting user posts
        postsViewModel.getAllUserPosts(requireContext(), viewModel.profileDataModel.userId)
    }

    /**
     * Settings Activity launch callback
     */
    private fun initSettingsCallback() {
        settingsCallback =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Constants.RESULT_LOGOUT) {
                    requireActivity().setResult(Constants.RESULT_LOGOUT)
                    requireActivity().finish()
                }
            }
    }

    override fun onPostClick(postId: String) {
        val intent = Intent(requireContext(), UserPostsActivity::class.java)
        intent.putExtra(Constants.POST_ID, postId)
        intent.putExtra(Constants.USER_ID, viewModel.profileDataModel.userId)
        intent.putExtra(Constants.USERNAME, binding.profileUsernameTv.text.toString())
        startActivity(intent)
    }
}