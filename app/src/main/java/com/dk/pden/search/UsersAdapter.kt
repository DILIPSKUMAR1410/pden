package com.dk.pden.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dk.pden.App.Constants.mixpanel
import com.dk.pden.R
import com.dk.pden.common.loadAvatar
import com.dk.pden.model.User
import com.dk.pden.mybook.MyBookActivity
import kotlinx.android.synthetic.main.user.view.*

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.profilePicImageView.loadAvatar(user.avatarImage)
        holder.userNameTextView.text = user.name
        holder.screenNameTextView.text = "@${user.blockstackId}"
        holder.descriptionTextView.text = user.description
        holder.container.setOnClickListener {
            MyBookActivity.launch(holder.container.context!!, user)
            mixpanel.track("Search")

        }
//        if (user.isVerified)
//            holder?.userNameTextView
//                    ?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified_user, 0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        return UserViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.user, parent, false))
    }

    var users: MutableList<User> = ArrayList()

    override fun getItemCount() = users.size

    class UserViewHolder(val container: View) : RecyclerView.ViewHolder(container) {

        val profilePicImageView: ImageView = container.userProfilePicImageView
        val userNameTextView: TextView = container.userNameTextView
        val screenNameTextView: TextView = container.screenNameTextView
        val descriptionTextView: TextView = container.descriptionTextView

    }

}