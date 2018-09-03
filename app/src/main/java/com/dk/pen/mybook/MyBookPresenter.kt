package com.dk.pen.mybook

import com.dk.pen.base.BasePresenter


open class MyBookPresenter : BasePresenter<MyBookMvpView>() {

    var page: Int = 1
    protected var isLoading: Boolean = false

    open fun getThoughts() {
        checkViewAttached()
        mvpView?.showLoading()
        isLoading = true


//        disposables
//                .add(TwitterAPI.getHomeTimeline(Paging(page, 50))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({
//                    mvpView?.hideLoading()
//
//                    when {
//                        it == null -> mvpView?.showError()
//                        it.isEmpty() -> mvpView?.showEmpty()
//                        else -> {
//                            mvpView?.showThoughts(it.map(::Tweet).toMutableList())
//                            page++
//                        }
//                    }
//
//                    isLoading = false
//                }, {
//                    Timber.e(it?.message)
//                    mvpView?.hideLoading()
//                    mvpView?.showError()
//                    isLoading = false
//                }))
//    }

//    open fun getMoreThoughts() {
//        if (isLoading)
//            return
//
//        checkViewAttached()
//        isLoading = true
//
//        disposables.add(TwitterAPI.getHomeTimeline(Paging(page, 50))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({
//                    if (it != null) {
//                        if (it.isNotEmpty())
//                            mvpView?.showMoreTweets(it.map(::Tweet).toMutableList())
//                        page++
//                    }
//                    isLoading = false
//                }, {
//                    Timber.e(it?.message)
//                    isLoading = false
//                }))
//    }

//    open fun onRefresh() {
//        checkViewAttached()
//
//        val sinceId = mvpView?.getLastTweetId()
//        if (sinceId != null && sinceId > 0) {
//            val page = Paging(1, 200)
//            page.sinceId = sinceId
//            disposables.add(TwitterAPI.refreshTimeLine(page)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeOn(Schedulers.io())
//                    .subscribe({
//                        mvpView?.stopRefresh()
//                        if (it != null) {
//                            it.reversed().forEach { status -> mvpView?.showTweet(Tweet(status)) }
//                        } else {
//                            mvpView?.showSnackBar(R.string.error_refreshing_timeline)
//                        }
//                    }, {
//                        Timber.e(it?.message)
//                        mvpView?.stopRefresh()
//                        mvpView?.showSnackBar(R.string.error_refreshing_timeline)
//                    }))
//        } else mvpView?.stopRefresh()
    }
}
