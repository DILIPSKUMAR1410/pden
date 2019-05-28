package com.dk.pden.feed

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import com.dk.pden.App.Constants.mixpanel
import com.dk.pden.ObjectBox
import com.dk.pden.R
import com.dk.pden.common.PreferencesHelper
import com.dk.pden.common.Utils
import com.dk.pden.common.visible
import com.dk.pden.compose.ComposeThoughtActivity
import com.dk.pden.custom.decorators.SpaceTopItemDecoration
import com.dk.pden.discuss.DiscussActivity
import com.dk.pden.events.NewThoughtsEvent
import com.dk.pden.events.RemoveThoughtsEvent
import com.dk.pden.feed.holder.FeedInteractionListener
import com.dk.pden.model.*
import com.dk.pden.mybook.MyBookActivity
import com.dk.pden.search.SearchActivity
import com.dk.pden.search.ShelfActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.objectbox.Box
import io.objectbox.query.QueryBuilder
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FeedActivity : AppCompatActivity(), FeedMvpView, FeedInteractionListener {

    private val presenter: FeedPresenter by lazy { getFeedPresenter() }
    private lateinit var adapter: FeedAdapter
    private lateinit var thoughtBox: Box<Thought>
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var preferencesHelper: PreferencesHelper
    private var blockstack_id: String = ""
    private lateinit var db: FirebaseFirestore
    private lateinit var transactionBox: Box<Transaction>
    private fun getFeedPresenter() = FeedPresenter()
    private val TAG = "FeedActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        // Get the support action bar
        val actionBar = supportActionBar
        db = FirebaseFirestore.getInstance()
        preferencesHelper = PreferencesHelper(this)
        blockstack_id = preferencesHelper.blockstackId
        // Set the action bar title, subtitle and elevation
        actionBar!!.title = "Feed"
        actionBar.elevation = 4.0F
        mixpanel.timeEvent("Feed")
        mixpanel.people.increment("Feed opened", 1.0)

        adapter = FeedAdapter(this)
        recyclerView = findViewById(R.id.tweetsRecyclerView)
//        swipeRefreshLayout = findViewById(R.pk.swipeRefreshLayout)
        floatingActionButton = findViewById(R.id.fab_compose)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        thoughtBox = ObjectBox.boxStore.boxFor(Thought::class.java)
        transactionBox = ObjectBox.boxStore.boxFor(Transaction::class.java)
        floatingActionButton.setOnClickListener {
            ComposeThoughtActivity.launch(this)
        }
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(SpaceTopItemDecoration(Utils.dpToPx(this, 10)))
        recyclerView.adapter = adapter

//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                if (linearLayoutManager.childCount + linearLayoutManager
//                                .findFirstVisibleItemPosition() + 1 > linearLayoutManager.itemCount - 10)
//                    presenter.getMoreThoughts(this)
//                Log.d("Need more thoughts-->>", "Fixthis")
//
//            }
//        })


//        swipeRefreshLayout.setOnRefreshListener {
//            //            presenter.onRefresh(this, MyBookActivity.user!!, self)
//        }

        if (adapter.thoughts.isEmpty()) {
            showThoughts(thoughtBox.query().equal(Thought_.isComment, false).filter { !it.user.target.isSelf }
                    .order(Thought_.timestamp, QueryBuilder.DESCENDING).build().find())
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.menu_feed, menu)

        val search = menu?.findItem(R.id.action_search)
        val searchView = search?.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.queryHint = "Search any person"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                search(query)
                return true
            }

            override fun onQueryTextChange(s: String) = false
        })

        Handler().postDelayed(
                {
                    val fancyShowCaseView0 = FancyShowCaseView.Builder(this)
                            .title(" \n\n\n Compose your thoughts")
                            .focusOn(floatingActionButton)
                            .titleStyle(R.style.MyTitleStyle, Gravity.CENTER)
                            .showOnce("Compose")
                            .build()
                    val fancyShowCaseView1 = FancyShowCaseView.Builder(this)
                            .focusOn(findViewById(R.id.myBook)) // ActionBar menu item id
                            .title("\n\n\n    Book is your collection of thoughts.")
                            .titleStyle(R.style.MyTitleStyle, Gravity.FILL)
                            .showOnce("Book")
                            .build()
                    val fancyShowCaseView2 = FancyShowCaseView.Builder(this)
                            .focusOn(findViewById(R.id.myShelf)) // ActionBar menu item id
                            .title("\n\n\n    Shelf is list of books you have borrowed.")
                            .titleStyle(R.style.MyTitleStyle, Gravity.FILL)
                            .showOnce("Shelf")
                            .build()
                    val fancyShowCaseView3 = FancyShowCaseView.Builder(this)
                            .focusOn(findViewById(R.id.action_search)) // ActionBar menu item id
                            .title("\n\n\n    Search users")
                            .titleStyle(R.style.MyTitleStyle, Gravity.FILL)
                            .showOnce("Search")
                            .build()
                    val fancyShowCaseView4 = FancyShowCaseView.Builder(this)
                            .title("Discuss your views on the post")
                            .titleStyle(R.style.MyTitleStyle, Gravity.END)
                            .showOnce("Discuss")
                            .focusOn(recyclerView.getChildAt(0).findViewById(R.id.threadImageButton))
                            .build()
                    val fancyShowCaseView5 = FancyShowCaseView.Builder(this)
                            .title("Spread the thought in other communities")
                            .enableAutoTextPosition()
                            .titleStyle(R.style.MyTitleStyle, Gravity.END)
                            .showOnce("Spread")
                            .focusOn(recyclerView.getChildAt(0).findViewById(R.id.spreadImageButton))
                            .build()
                    val fancyShowCaseView6 = FancyShowCaseView.Builder(this)
                            .title("Tell other communities about the thought")
                            .enableAutoTextPosition()
                            .titleStyle(R.style.MyTitleStyle, Gravity.END)
                            .showOnce("Social group")
                            .focusOn(recyclerView.getChildAt(0).findViewById(R.id.spreadOutsideImageButton))
                            .build()


                    val mQueue = FancyShowCaseQueue()
                            .add(fancyShowCaseView0)
                            .add(fancyShowCaseView1)
                            .add(fancyShowCaseView2)
                            .add(fancyShowCaseView3)
                            .add(fancyShowCaseView4)
                            .add(fancyShowCaseView5)
                            .add(fancyShowCaseView6)

                    mQueue.show()
                }, 1000
        )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.myBook -> openProfile()
            R.id.myShelf -> openShelf()
            R.id.myInk -> openInk()
            R.id.share_pden -> sharePden()
        }

        return super.onOptionsItemSelected(item)
    }


    fun search(string: String) {
        SearchActivity.launch(this, string)
    }

    private fun openProfile() {
        val preferencesHelper = PreferencesHelper(this)
        val blockstack_id = preferencesHelper.blockstackId
        val user = ObjectBox.boxStore.boxFor(User::class.java).query().equal(User_.blockstackId, blockstack_id).build().findFirst()
        user?.let { MyBookActivity.launch(this, it) }
    }

    private fun openShelf() {
        ShelfActivity.launch(this)
    }

    private fun openInk() {
        val items = arrayOf("Ink bal : ${preferencesHelper.inkBal}",
                "Free promo post : ${preferencesHelper.freePromoPost}",
                "Free promo love : ${preferencesHelper.freePromoLove}")
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle("Balance")
            setItems(items) { _, _ -> }
            show()
        }
    }

    fun sharePden() {
        if (isPackageExist("com.whatsapp")) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Checkout Pden app, I found it best for thoughtful expressions \n https://play.google.com/store/apps/details?id=com.dk.pden")
            sendIntent.type = "text/plain"
            sendIntent.setPackage("com.whatsapp")
            startActivity(sendIntent)
        } else {
            Toast.makeText(this, "You dont have whatsapp ?", Toast.LENGTH_SHORT).show()
        }
    }

    fun isPackageExist(targetPackage: String): Boolean {
        val pm: PackageManager = getPackageManager()
        val packages = pm.getInstalledApplications(0);
        for (packageInfo in packages) {
            if (packageInfo.packageName.equals(targetPackage)) {
                return true
            }
        }
        return false
    }

    override fun spreadOutside(thought: Thought) {
        if (isPackageExist("com.whatsapp")) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, thought.user.target.blockstackId + " \nsays: \n" + thought.text +
                    "\nCheckout this pden app I found it best for thoughtful expressions \n https://play.google.com/store/apps/details?id=com.dk.pden")
            sendIntent.type = "text/plain"
            sendIntent.setPackage("com.whatsapp")
            startActivity(sendIntent)
        } else {
            Toast.makeText(this, "You dont have whatsapp ?", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showThoughts(thoughts: MutableList<Thought>) {

        runOnUiThread {
            adapter.thoughts = thoughts
            adapter.notifyDataSetChanged()
        }
    }

    override fun updateAdapter() {
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }

    override fun removeThoughts(thoughts: MutableList<Thought>) {
        runOnUiThread {
            adapter.thoughts.removeAll(thoughts)
            adapter.notifyDataSetChanged()
        }
    }

    override fun showThought(thought: Thought) {
        var removedPosition = adapter.thoughts.size - 1
        if (removedPosition < 0) {
            removedPosition = 0
            adapter.thoughts.removeAt(removedPosition)
            adapter.notifyItemRemoved(removedPosition)
        }

        adapter.thoughts.add(0, thought)
        adapter.notifyItemInserted(0)
        recyclerView.scrollToPosition(0)
    }

    override fun showMoreMyThoughts(thoughts: MutableList<Thought>) {
        thoughts.addAll(adapter.thoughts)
        thoughts.sortByDescending { it.timestamp }
        adapter.thoughts = thoughts
        adapter.notifyDataSetChanged()
    }

    override fun getLastMyThoughtId(): Long = if (adapter.thoughts.size > 0) adapter.thoughts[0].id else -1

    override fun stopRefresh() {
        runOnUiThread {
            if (swipeRefreshLayout.isRefreshing)
                swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun startRefresh() {
        runOnUiThread {
            swipeRefreshLayout.isRefreshing = true
        }
    }

    override fun showLoading() {
        if (!swipeRefreshLayout.isRefreshing)
            loadingProgressBar.visible()
    }

    override fun hideLoading() {
        if (loadingProgressBar.isEnabled)
            loadingProgressBar.visible(false)
    }


    override fun updateRecyclerViewView() {
        adapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddEvent(event: NewThoughtsEvent) {
        /* Do something */
        if (event.thoughts.size > 1)
            showMoreMyThoughts(event.thoughts)
        else
            showThought(event.thoughts[0])
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveEvent(event: RemoveThoughtsEvent) {
        /* Do something */
        removeThoughts(event.thoughts)

    }

    public override fun onStart() {
        super.onStart()
        Log.d("Eventbus -->>", "regiser")
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)

    }

    public override fun onStop() {
        super.onStop()
        mixpanel.track("Feed");
    }


    override fun spread(thought: Thought) {
        presenter.spreadThought(thought, this)
    }

    override fun love(thought: Thought) {
        val status = Utils.checkPostBalance(this)
        if (0 < status) {
            if (status == 1) {
                deductFromFreeLove(thought)
            } else if (status == 2) {
                deductFromInk(thought)
            }
            presenter.loveThought(thought, this)
        }
    }


    override fun showUser(user: User) {
        MyBookActivity.launch(this, user)
    }

    override fun showThread(thought: Thought) {
        DiscussActivity.launch(this, thought.uuid)
    }

    private fun deductFromFreeLove(thought: Thought) {
        val docRef = db.collection("users").document(blockstack_id)
        docRef.get()
                .addOnSuccessListener { user ->
                    val newValue = HashMap<String, Any>()
                    val leftPromoLve = user.getLong("free_promo_love")!! - 1
                    newValue["free_promo_love"] = leftPromoLve
                    docRef.set(newValue, SetOptions.merge())
                    preferencesHelper.freePromoLove = leftPromoLve
                    val transaction = Transaction(blockstack_id, thought.user.target.blockstackId, 4, "LOVE")
                    transaction.thought.setAndPutTarget(thought)

                    // Create a new transaction
                    val transactionFS = HashMap<String, Any>()
                    transactionFS["timestamp"] = transaction.timestamp
                    transactionFS["from"] = blockstack_id
                    transactionFS["to"] = thought.user.target.blockstackId
                    transactionFS["amount"] = 4
                    transactionFS["activity"] = "LOVE"
                    db.collection("thoughts").document(thought.uuid).collection("transactions")
                            .add(transactionFS)
                            .addOnSuccessListener {
                                transactionBox.put(transaction)
                                Log.d("ComposeThoughtPresenter", "Transaction successfully written!")
                            }
                            .addOnFailureListener { e -> Log.w("ComposeThoughtPresenter", "Error writing document", e) }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
    }

    private fun deductFromInk(thought: Thought) {

        val docRef = db.collection("users").document(blockstack_id)
        docRef.get()
                .addOnSuccessListener { user ->
                    val newValue = HashMap<String, Any>()
                    val remainingInkBal = user.getLong("ink_bal")!! - 4
                    newValue["ink_bal"] = remainingInkBal
                    docRef.set(newValue, SetOptions.merge())
                    preferencesHelper.inkBal = remainingInkBal
                    val transaction = Transaction(blockstack_id, thought.user.target.blockstackId, 4, "LOVE")
                    transaction.thought.setAndPutTarget(thought)

                    // Create a new transaction
                    val transactionFS = HashMap<String, Any>()
                    transactionFS["timestamp"] = transaction.timestamp
                    transactionFS["from"] = blockstack_id
                    transactionFS["to"] = thought.user.target.blockstackId
                    transactionFS["amount"] = 4
                    transactionFS["activity"] = "LOVE"
                    db.collection("thoughts").document(thought.uuid).collection("transactions")
                            .add(transactionFS)
                            .addOnSuccessListener {
                                transactionBox.put(transaction)
                                Log.d(TAG, "Transaction successfully written!")
                            }
                            .addOnFailureListener { e -> Log.w("ComposeThoughtPresenter", "Error writing document", e) }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
    }

}
