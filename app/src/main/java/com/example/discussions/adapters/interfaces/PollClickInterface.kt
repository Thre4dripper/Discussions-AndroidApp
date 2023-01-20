package com.example.discussions.adapters.interfaces

interface PollClickInterface {
    fun onPollDelete(pollId: String)
    fun onPollVote(pollId: String, optionId: String)
    fun onPollResult(pollId: String)
}