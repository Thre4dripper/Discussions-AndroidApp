package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.api.ResponseCallback
import com.example.discussions.repositories.UserRepository

class EditDetailsViewModel : ViewModel() {
    private val TAG = "EditProfileViewModel"

    companion object {
        const val API_SUCCESS = "success"
    }

    var profileImage: String = ""
    var username: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var gender: String = ""
    var email: String = ""
    var mobileNo: String = ""
    var dob: String = ""
    var address: String = ""

    private val _isDetailsLoaded = MutableLiveData<String>(null)
    private val _isDetailsUpdated = MutableLiveData<String>(null)

    val isDetailsLoaded: LiveData<String>
        get() = _isDetailsLoaded

    val isDetailsUpdated: LiveData<String>
        get() = _isDetailsUpdated

    fun getDetails(context: Context) {
        UserRepository.getDetails(context, object : ResponseCallback {
            override fun onSuccess(response: String) {
                UserRepository.map["profileImage"]?.let { profileImage = it }
                UserRepository.map["username"]?.let { username = it }
                UserRepository.map["firstName"]?.let { firstName = it }
                UserRepository.map["lastName"]?.let { lastName = it }
                UserRepository.map["gender"]?.let { gender = it }
                UserRepository.map["email"]?.let { email = it }
                UserRepository.map["mobileNo"]?.let { mobileNo = it }
                UserRepository.map["dob"]?.let { dob = it }
                UserRepository.map["address"]?.let { address = it }
                _isDetailsLoaded.postValue(API_SUCCESS)
            }

            override fun onError(response: String) {
                _isDetailsLoaded.postValue(response)
            }
        })

    }

    fun updateDetails(
        context: Context,
        imageUrl: String,
        firstName: String,
        lastName: String,
        gender: String,
        email: String,
        mobileNo: String,
        dob: String,
        address: String,
    ) {

        UserRepository.updateDetails(context,
            imageUrl,
            username,
            firstName,
            lastName,
            gender,
            email,
            mobileNo,
            dob,
            address,
            object : ResponseCallback {
                override fun onSuccess(response: String) {
                    _isDetailsUpdated.postValue(API_SUCCESS)
                }

                override fun onError(response: String) {
                    _isDetailsUpdated.postValue(response)
                }
            })
    }
}