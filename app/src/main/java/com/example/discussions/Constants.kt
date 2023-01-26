package com.example.discussions

class Constants {
    companion object {
        const val API_SUCCESS = "success"
        const val API_FAILED = "error"
        const val AUTH_FAILURE_ERROR = "Auth Error"

        const val LIKE_DEBOUNCE_TIME = 3000L

        //for activity results
        const val RESULT_CLOSE = 101
        const val RESULT_LOGOUT = 100

        //for fragments
        const val TAG_DISCUSS_FRAGMENT = "discuss_fragment"
        const val TAG_POLLS_FRAGMENT = "polls_fragment"
        const val TAG_NOTIFICATION_FRAGMENT = "notification_fragment"
        const val TAG_PROFILE_FRAGMENT = "profile_fragment"

        //user constants
        const val PROFILE_IMAGE = "profile_image"
        const val USERNAME = "username"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val GENDER = "gender"
        const val MALE = "Male"
        const val FEMALE = "Female"
        const val EMAIL = "email"
        const val MOBILE = "mobile_no"
        const val DOB = "dob"
        const val ADDRESS = "address"

        //for intents
        const val ZOOM_IMAGE_URL = "imageUrl"
        const val USER_POST_INDEX = "userPostIndex"

        //for posts modes
        const val POST_MODE = "post_mode"
        const val MODE_CREATE_POST = "create_post"
        const val MODE_EDIT_POST = "edit_post"

        //for passing post data
        const val POST_ID = "post_id"
        const val POST_TITLE = "post_title"
        const val POST_CONTENT = "post_content"
        const val POST_IMAGE = "post_image"

        //for passing poll data
        const val POLL_ID = "poll_id"

        //for comments modes
        const val COMMENT_TYPE_POST = "post"
        const val COMMENT_TYPE_POLL = "poll"
        const val COMMENT_TYPE_REPLY = "reply"
        const val COMMENT_TYPE_EDIT = "edit"

        //notification channels
        const val POST_NOTIFICATION_CHANNEL = "post_notification_channel"
        const val POLL_NOTIFICATION_CHANNEL = "poll_notification_channel"
        const val COMMENT_NOTIFICATION_CHANNEL = "comment_notification_channel"

        //notification ids
        const val POST_LIKE_NOTIFICATION_ID = 1
        const val POST_COMMENT_NOTIFICATION_ID = 2
        const val POLL_LIKE_NOTIFICATION_ID = 3
        const val POLL_COMMENT_NOTIFICATION_ID = 4
        const val POLL_VOTE_NOTIFICATION_ID = 5
        const val COMMENT_LIKE_NOTIFICATION_ID = 6
        const val COMMENT_REPLY_NOTIFICATION_ID = 7
    }
}