package com.example.discussions.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.discussions.Constants
import com.example.discussions.R
import com.example.discussions.adapters.UserPostsRecyclerAdapter
import com.example.discussions.adapters.interfaces.LikeCommentInterface
import com.example.discussions.adapters.interfaces.PostMenuInterface
import com.example.discussions.databinding.ActivityUserPostsBinding
import com.example.discussions.viewModels.UserPostsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UserPostsActivity : AppCompatActivity(), PostMenuInterface, LikeCommentInterface {
    private val TAG = "UserPostsActivity"

    private lateinit var binding: ActivityUserPostsBinding
    private lateinit var viewModel: UserPostsViewModel

    private lateinit var userPostsAdapter: UserPostsRecyclerAdapter
    private var handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_posts)
        viewModel = ViewModelProvider(this)[UserPostsViewModel::class.java]

        binding.userPostsRv.apply {
            userPostsAdapter =
                UserPostsRecyclerAdapter(this@UserPostsActivity, this@UserPostsActivity)
            adapter = userPostsAdapter
        }

        //getting user posts from view model
        val username = intent.getStringExtra(Constants.USERNAME)
        binding.userPostsHeaderTv.text = getString(R.string.user_post_label, username)

        //getting post index from intent
        val postIndex = intent.getIntExtra(Constants.USER_POST_INDEX, 0)
        UserPostsViewModel.userPostsScrollToIndex = true

        getUserPosts(postIndex)
    }

    /**
     * METHOD FOR GETTING USER POSTS FROM POST REPOSITORY AND POPULATING THE RECYCLER VIEW
     */
    private fun getUserPosts(postIndex: Int) {
        viewModel.userPosts.observe(this) {
            userPostsAdapter.submitList(it) {
                if (UserPostsViewModel.userPostsScrollToIndex) binding.userPostsRv.scrollToPosition(
                    postIndex
                )
            }
        }
    }

    override fun onPostEdit(postId: String) {
        val intent = Intent(this, CreateEditPostActivity::class.java)
        intent.putExtra(Constants.POST_MODE, Constants.MODE_EDIT_POST)
        intent.putExtra(Constants.POST_ID, postId)
        val post = viewModel.userPosts.value?.find { it.postId == postId }!!
        intent.putExtra(Constants.POST_TITLE, post.title)
        intent.putExtra(Constants.POST_CONTENT, post.content)
        intent.putExtra(Constants.POST_IMAGE, post.postImage)
        startActivity(intent)
    }

    override fun onPostDelete(postId: String) {

        MaterialAlertDialogBuilder(this).setTitle("Delete")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                deletePost(postId)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    /**
     * METHOD FOR SENDING DELETE POST REQ TO THE VIEW MODEL
     */
    private fun deletePost(postId: String) {
        //post delete api observer
        viewModel.isPostDeleted.observe(this) {
            if (it != null) {
                if (it == Constants.API_SUCCESS) Toast.makeText(
                    this,
                    "Post Deleted",
                    Toast.LENGTH_SHORT
                ).show()
                else if (it == Constants.API_FAILED) Toast.makeText(
                    this,
                    "Problem Deleting Post",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.deletePost(this, postId)
    }

    override fun onLike(postOrPollId: String, isLiked: Boolean, btnLikeStatus: Boolean) {
        viewModel.isPostLikedChanged.observe(this) {
            if (it != null) {
                if (it == Constants.API_FAILED) {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                } else if (it == Constants.AUTH_FAILURE_ERROR) {
                    setResult(Constants.RESULT_LOGOUT)
                    finish()
                }
            }
        }

        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (isLiked == btnLikeStatus) viewModel.likePost(this, postOrPollId)

        }, Constants.LIKE_DEBOUNCE_TIME)
    }

    override fun onComment(id: String, type: String) {

    }
}