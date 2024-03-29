package com.example.discussions.api.apiCalls.comments

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.discussions.adapters.CommentsRecyclerAdapter
import com.example.discussions.api.ApiRoutes
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.CommentModel
import org.json.JSONObject

class CreateCommentApi {
    companion object {
        fun createComment(
            context: Context,
            token: String,
            postId: String?,
            pollId: String?,
            commentId: String?,
            content: String,
            callback: ResponseCallback
        ) {
            val queue = Volley.newRequestQueue(context)
            val url = "${ApiRoutes.BASE_URL}${ApiRoutes.COMMENTS_CREATE_COMMENT}"

            val body = JSONObject()
            if (postId != null) {
                body.put("post_id", postId)
            } else if (pollId != null) {
                body.put("poll_id", pollId)
            } else if (commentId != null) {
                body.put("comment_id", commentId)
            }

            body.put("content", content)

            val request = object : JsonObjectRequest(Method.POST, url, body, { response ->
                callback.onSuccess(response.toString())
            }, { err ->
                callback.onError(err.toString())
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $token"
                    return headers
                }
            }

            queue.add(request)
        }

        fun parseCreateCommentJson(response: String): CommentModel {
            val rootObject = JSONObject(response)
            val createdByObject = rootObject.getJSONObject("created_by")
            val username = createdByObject.getString("username")
            val userImage = createdByObject.getString("image")

            val commentId = rootObject.getString("id")
            val parentCommentId = rootObject.getString("parent_id")
            val comment = rootObject.getString("content")
            val createdAt = rootObject.getString("created_at")
            val isLiked = rootObject.getBoolean("is_liked")
            val likes = rootObject.getInt("like_count")

            return CommentModel(
                commentId,
                0,
                null,
                null,
                CommentsRecyclerAdapter.COMMENTS_TYPE_COMMENT,
                if (parentCommentId == "null") null else parentCommentId,
                comment,
                username,
                userImage,
                createdAt,
                isLiked,
                likes,
                mutableListOf()
            )
        }
    }
}