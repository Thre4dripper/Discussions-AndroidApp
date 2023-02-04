package com.example.discussions.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.discussions.Constants
import com.example.discussions.api.ResponseCallback
import com.example.discussions.models.PollModel
import com.example.discussions.repositories.PollRepository

class PollDetailsViewModel : ViewModel() {
    private val TAG = "PollDetailsViewModel"

    private var _poll = PollRepository.singlePoll
    val poll: LiveData<PollModel?>
        get() = _poll

    private var _isPollFetched = MutableLiveData<String?>(null)
    val isPollFetched: MutableLiveData<String?>
        get() = _isPollFetched

    private var _isPollVoted = MutableLiveData<String?>(null)
    val isPollVoted: MutableLiveData<String?>
        get() = _isPollVoted

    fun isPollInAlreadyFetched(pollId: String): Boolean {
        //check if poll is in poll list
        return PollRepository.allPollsList.value?.any { it.pollId == pollId }
        //if not, check if poll is in user poll list
            ?: PollRepository.userPollsList.value?.any { it.pollId == pollId }
            //if not, then poll is not in any list so far
            ?: false
    }

    fun getPollFromPollRepository(pollId: String) {
        _poll.value = PollRepository.allPollsList.value?.find { it.pollId == pollId }
            ?: PollRepository.userPollsList.value?.find { it.pollId == pollId }!!
    }

    fun pollVote(context: Context, pollId: String, optionId: String) {
        //change the poll voting state
        val votedPoll = _poll.value!!.copy(isVoting = true)
        _poll.value = votedPoll

        //trigger the observer to update the UI
        _isPollVoted.value = null

        //send the vote to the server
        PollRepository.pollVote(context, pollId, optionId, object : ResponseCallback {
            override fun onSuccess(response: String) {
                _isPollVoted.value = Constants.API_SUCCESS
            }
            override fun onError(response: String) {
                _isPollVoted.value = response
            }
        })
    }

    fun likePoll(context: Context, postId: String) {
        PollRepository.likePoll(context, postId, object : ResponseCallback {
            override fun onSuccess(response: String) {}
            override fun onError(response: String) {}
        })
    }
}