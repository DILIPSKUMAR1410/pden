package com.dk.pen.search

import com.dk.pen.base.MvpView
import com.dk.pen.model.User

interface SearchUsersMvpView : MvpView {

    fun showUsers(users: MutableList<User>)

    fun showMoreUsers(users: MutableList<User>)

    fun showLoading()

    fun hideLoading()

    fun updateRecyclerViewView()

}