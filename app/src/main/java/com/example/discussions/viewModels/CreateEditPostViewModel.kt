package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.PostRepository
import com.example.discussions.repositories.UserRepository

class CreateEditPostViewModel : ViewModel() {
    private val TAG = "CreateEditPostViewModel"

    var profileImage: String? = null
    var username: String = ""
    var postTitle: String = ""
    var postContent: String = ""
    var postImage: String = ""
    var allowComments: Boolean = true


    private var _isApiFetched = MutableLiveData<String>(null)
    val isApiFetched: LiveData<String>
        get() = _isApiFetched

    private var _isPostCreatedOrUpdated = MutableLiveData<String?>(null)
    val isPostCreatedOrUpdated: LiveData<String?>
        get() = _isPostCreatedOrUpdated

    private var _allPosts = PostRepository.allPostsList
    private var _userPosts = PostRepository.userPostsList


    fun getUsernameAndImage(context: Context) {
        UserRepository.getUsernameAndImage(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                UserRepository.map[Constants.PROFILE_IMAGE]?.let { profileImage = it }
                UserRepository.map[Constants.USERNAME]?.let { username = it }
                _isApiFetched.postValue(Constants.API_SUCCESS)
            }

            override fun onError(response: String) {
                _isApiFetched.postValue(response)
            }
        })
    }

    fun createPost(context: Context) {
        _isPostCreatedOrUpdated.postValue(null)
        PostRepository.createPost(
            context,
            postTitle,
            postContent,
            postImage,
            allowComments,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    _isPostCreatedOrUpdated.postValue(Constants.API_SUCCESS)
                }

                override fun onError(response: String) {
                    _isPostCreatedOrUpdated.postValue(response)
                }
            })
    }

    fun editPost(context: Context, postId: String) {
        _isPostCreatedOrUpdated.postValue(null)
        PostRepository.updatePost(
            context,
            postId,
            postTitle,
            postContent,
            postImage,
            allowComments,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    _isPostCreatedOrUpdated.postValue(Constants.API_SUCCESS)

                    //getting old post from all posts list and user posts list
                    val oldPost = _allPosts.value?.find { it.postId == postId }!!
                    val oldUserPost = _userPosts.value?.find { it.postId == postId }!!

                    //creating copy of the post from all posts list
                    val newPost = oldPost.copy(
                        title = postTitle,
                        content = postContent,
                        postImage = postImage,
                    )

                    //creating copy of the post from user posts list
                    val newUserPost = oldUserPost.copy(
                        title = postTitle,
                        content = postContent,
                        postImage = postImage,
                    )

                    //updating the post in all posts list and user posts list for real time update in the UI
                    val newAllPosts = _allPosts.value?.toMutableList()
                    val newUserPosts = _userPosts.value?.toMutableList()

                    newAllPosts?.set(newAllPosts.indexOf(oldPost), newPost)
                    newUserPosts?.set(newUserPosts.indexOf(oldUserPost), newUserPost)

                    _allPosts.postValue(newAllPosts)
                    _userPosts.postValue(newUserPosts)
                }

                override fun onError(response: String) {
                    _isPostCreatedOrUpdated.postValue(response)
                }
            })
    }
}