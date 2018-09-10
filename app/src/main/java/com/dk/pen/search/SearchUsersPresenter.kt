package com.dk.pen.search

import android.util.Log
import com.dk.pen.base.BasePresenter
import com.dk.pen.model.User
import com.dk.pen.service.ApiServiceFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers


class SearchUsersPresenter(private val textQuery: String) : BasePresenter<SearchUsersMvpView>() {

    private var isLoading: Boolean = false
    private val disposables = CompositeDisposable()
    private val apiService by lazy {
        ApiServiceFactory.createService()
    }


//    override fun detachView() {
//        super.detachView()
//        disposables.clear()
//    }

    fun getUsers() {
        checkViewAttached()
        mvpView?.showLoading()
        isLoading = true


        apiService.groupList(textQuery)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy(

                        onSuccess = {
                            var users = mutableListOf<User>()
                            it.users.forEach {
                                var user = User(it.get("fullyQualifiedName").asString.trim())
                                var profile = it.getAsJsonObject("profile")
                                if (profile.has("image")) {
                                    var url = profile.getAsJsonArray("image").get(0).asJsonObject.get("contentUrl").toString()
                                    user.avatarImage = url.substring(1, url.length - 1)
                                }
                                if (profile.has("description"))
                                    user.description = profile.get("description").toString().trim()
                                if (profile.has("name"))
                                    user.name = profile.get("name").toString().trim()

                                users.add(user)
                            }

                            mvpView?.hideLoading()
                            mvpView?.showUsers(users)
                            isLoading = false
                        },
                        onError =
                        {
                            Log.d("error-->>", it.message)
                        }
                )
    }

//    fun getUsers() {
//        checkViewAttached()
//        mvpView?.showLoading()
//        isLoading = true
//
//        disposables.add(TwitterAPI.searchUsers(textQuery, Paging(page, 50))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({
//                    mvpView?.hideLoading()
//
//                    when {
//                        it == null -> mvpView?.showError()
//                        it.isEmpty() -> mvpView?.showEmpty()
//                        else -> {
//                            mvpView?.showUsers(it)
//                            page++
//                        }
//                    }
//
//                    isLoading = false
//                }, {
//                    Timber.e(it)
//                    mvpView?.hideLoading()
//                    mvpView?.showError()
//                    isLoading = false
//                }))
//    }
//    fun getMoreUsers() {
//        if (isLoading)
//            return
//
//        checkViewAttached()
//        isLoading = true
//
//        disposables.add(TwitterAPI.searchUsers(textQuery, Paging(page, 50))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({
//                    if (it != null) {
//                        if (it.isNotEmpty())
//                            mvpView?.showMoreUsers(it)
//                        page++
//                    }
//                    isLoading = false
//                }, {
//                    Timber.e(it)
//                    isLoading = false
//                }))
//    }

}