package com.dk.pen.mybook

import com.dk.pen.base.MvpView
import com.dk.pen.model.Interest
import com.dk.pen.model.OthersThought


/**
 * Created by andrea on 17/05/16.
 */
interface TimelineMvpView : MvpView {

    fun showTweets(tweets: MutableList<OthersThought>)

    fun showTweet(tweet: OthersThought)

    fun showMoreTweets(tweets: MutableList<OthersThought>)

    fun getLastTweetId(): Long

    fun stopRefresh()

    fun showEmpty()

    fun showError()

    fun showSnackBar(stringResource: Int)

    fun showLoading()

    fun hideLoading()

    fun showNewTweet(tweet: OthersThought, user: Interest)

    fun updateRecyclerViewView()

}