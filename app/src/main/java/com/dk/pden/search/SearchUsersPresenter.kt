package com.dk.pden.search

import android.annotation.SuppressLint
import android.util.Log
import com.dk.pden.base.BasePresenter
import com.dk.pden.model.User
import com.dk.pden.service.ApiServiceFactory
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

    @SuppressLint("CheckResult")
    fun getUsers() {
        checkViewAttached()
        mvpView?.showLoading()
        isLoading = true


        apiService.groupList(textQuery)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy(

                        onSuccess = { it ->
                            val users = mutableListOf<User>()
                            it.users.forEach {
                                val user = User(it.get("fullyQualifiedName").asString.trim())
                                val profile = it.getAsJsonObject("profile")
                                if (profile.has("image")) {
                                    val url = profile.getAsJsonArray("image").get(0).asJsonObject.get("contentUrl").toString()
                                    user.avatarImage = url.substring(1, url.length - 1)
                                } else
                                    user.avatarImage = "https://api.adorable.io/avatars/285/" + user.blockstackId + ".png"

                                if (profile.has("description"))
                                    user.description = profile.get("description").toString().trim()
                                if (profile.has("nameString"))
                                    user.nameString = profile.get("nameString").toString().trim()

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