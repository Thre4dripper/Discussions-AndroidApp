package com.example.discussions.api.apiCalls.poll

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PollModel
import com.example.discussions.models.PollOptionModel
import com.example.discussions.models.PollVotedByModel
import org.json.JSONObject

class GetPollByIdApi {
    companion object {
        private const val TAG = "GetPostByIdApi"

        fun getPollByIdJson(
            context: Context,
            token: String,
            pollId: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.POLL_GET_POLL_BY_ID}$pollId/"

            val request = object : JsonObjectRequest(Method.GET, url, null, { response ->
                callback.onSuccess(response.toString())
            }, { error ->
                callback.onError(error.toString())
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $token"
                    return headers
                }
            }

            request.retryPolicy = DefaultRetryPolicy(
                15000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )

            queue.add(request)
        }

        fun parsePollByIdJson(json: String): PollModel {
            val rootObject = JSONObject(json)

            val createdByObject = rootObject.getJSONObject("created_by")
            val username = createdByObject.getString("username")
            val userImage = createdByObject.getString("image")

            val pollOptionsList = mutableListOf<PollOptionModel>()
            val pollOptionsArray = rootObject.getJSONArray("poll_option")

            //this loop is used to parse poll options
            for (i in 0 until pollOptionsArray.length()) {
                val pollOptionObject = pollOptionsArray.getJSONObject(i)

                val votedByList = mutableListOf<PollVotedByModel>()
                val votedByArray = pollOptionObject.getJSONArray("voted_by")

                //this loop is used to parse voted by list
                for (j in 0 until votedByArray.length()) {
                    val votedByObject = votedByArray.getJSONObject(j)

                    //filling voted by list
                    votedByList.add(
                        PollVotedByModel(
                            votedByObject.getString("id"),
                            votedByObject.getString("username"),
                            votedByObject.getString("image")
                        )
                    )
                }

                //filling poll options list
                pollOptionsList.add(
                    PollOptionModel(
                        pollOptionObject.getString("id"),
                        pollOptionObject.getString("content"),
                        "",
                        pollOptionObject.getInt("votes"),
                        votedByList
                    )
                )
            }

            return PollModel(
                rootObject.getString("id"),
                rootObject.getString("title"),
                rootObject.getString("content"),
                rootObject.getInt("total_votes"),
                rootObject.getBoolean("private"),
                rootObject.getBoolean("is_voted"),
                username,
                userImage,
                rootObject.getString("created_at"),
                rootObject.getBoolean("is_liked"),
                rootObject.getInt("like_count"),
                rootObject.getBoolean("allow_comments"),
                rootObject.getInt("comment_count"),
                pollOptionsList
            )
        }
    }
}