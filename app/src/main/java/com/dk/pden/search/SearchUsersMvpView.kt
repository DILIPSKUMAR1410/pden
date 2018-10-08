package com.dk.pden.search

import com.dk.pden.base.MvpView
import com.dk.pden.model.User

interface SearchUsersMvpView : MvpView {

    fun showUsers(users: MutableList<User>)

    fun showMoreUsers(users: MutableList<User>)

    fun showLoading()

    fun hideLoading()

    fun updateRecyclerViewView()

    fun close()

}