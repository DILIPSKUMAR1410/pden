package com.dk.pen.shelf

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import com.dk.pen.ObjectBox
import com.dk.pen.R
import com.dk.pen.common.PreferencesHelper
import com.dk.pen.mybook.MyBookActivity

class ShelfActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shelf)
        ObjectBox.build(this)

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.menu_shelf, menu)

        val search = menu?.findItem(R.id.action_search)
        val searchView = search?.actionView as SearchView
        searchView .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        search(query)
                        return true
                    }

                    override fun onQueryTextChange(s: String) = false
                })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.myBook -> openProfile()
        }

        return super.onOptionsItemSelected(item)
    }


    fun search(string: String) {
        Log.d("Query",string)
//        SearchActivity.launch(this, string)
    }

    fun openProfile() {
        val preferencesHelper = PreferencesHelper(this)
        val blockstack_id = preferencesHelper.deviceToken
        MyBookActivity.launch(this, blockstack_id)
    }
}
