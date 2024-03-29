package com.example.discussions.repositories

import android.content.Context
import com.example.discussions.api.ResponseCallback
import com.example.discussions.api.apiCalls.auth.LoginApi
import com.example.discussions.api.apiCalls.auth.SignupApi
import com.example.discussions.store.UserStore
import org.json.JSONObject

class AuthRepository {
    companion object {
        private const val TAG = "LoginRepository"

        fun loginUser(
            context: Context, username: String, password: String, callback: ResponseCallback
        ) {

            val deviceToken = UserStore.getDeviceToken(context)!!

            LoginApi.loginUser(context, username, password, deviceToken, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    //getting jwt token from response
                    val rootObject = JSONObject(response)
                    val token = rootObject.getString("token")
                    callback.onSuccess(token)
                }

                override fun onError(response: String) {
                    if (response.contains("com.android.volley.TimeoutError")) {
                        callback.onError("Time Out")
                    } else if (response.contains("com.android.volley.NoConnectionError")) {
                        callback.onError("Please check your internet connection")
                    } else if (response.contains("com.android.volley.AuthFailureError")) {
                        callback.onError("Invalid Credentials")
                    } else if (response.contains("com.android.volley.NetworkError")) {
                        callback.onError("Network Error")
                    } else if (response.contains("com.android.volley.ServerError")) {
                        callback.onError("Server Error")
                    } else if (response.contains("com.android.volley.ParseError")) {
                        callback.onError("Parse Error")
                    } else {
                        callback.onError("Something went wrong")
                    }
                }
            })
        }


        fun signupUser(
            context: Context,
            username: String,
            email: String,
            password: String,
            callback: ResponseCallback
        ) {
            SignupApi.signupUser(context, username, email, password, object : ResponseCallback {
                override fun onSuccess(response: String) {
                    callback.onSuccess(response)
                }

                override fun onError(response: String) {
                    //if response is in json format
                    if (response.contains("{")) {
                        callback.onError(response)
                    } else if (response.contains("com.android.volley.ClientError")) {
                        callback.onError("User Already Exists")
                    } else if (response.contains("com.android.volley.TimeoutError")) {
                        callback.onError("Time Out")
                    } else if (response.contains("com.android.volley.NoConnectionError")) {
                        callback.onError("Please check your internet connection")
                    } else if (response.contains("com.android.volley.AuthFailureError")) {
                        callback.onError("Invalid Credentials")
                    } else if (response.contains("com.android.volley.NetworkError")) {
                        callback.onError("Network Error")
                    } else if (response.contains("com.android.volley.ServerError")) {
                        callback.onError("Server Error")
                    } else if (response.contains("com.android.volley.ParseError")) {
                        callback.onError("Parse Error")
                    } else {
                        callback.onError("Something went wrong")
                    }
                }
            })
        }
    }
}