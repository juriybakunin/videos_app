package com.jbak.videos.activity

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.jbak.videos.*
import com.jbak.videos.providers.Factory
import com.jbak.videos.types.IItem
import com.jbak.videos.types.VideosList
import com.jbak.videos.view.CustomSearchView
import com.jbak.videos.view.ItemListView
import kotlinx.android.synthetic.main.activity_main.*
import tenet.lib.base.Err
import tenet.lib.base.MyLog


//import com.google.api.client.util.ExponentialBackOff



class MainActivity : AppCompatActivity(),
    DataLoader.OnItemsLoaded,
    SearchView.OnQueryTextListener,
    ItemListView.OnItemClick,
        CustomSearchView.OnSearchShowChangeListener
    {

    var dataLoader:DataLoader? = null;
    var mSearchView:CustomSearchView? = null;
    var queryText : String = "";
    lateinit var controllerDlg : ControllerDialog
    var isPlayerShown = false
    private val mCompleteLoader = Web.createCompleteLoader(object : DataLoader.OnItemsLoaded{
        override fun onItemsLoaded(err: Err, videosList: VideosList) {
            MyLog.log(err.toString())
            if(err.isOk && mListMenu.visibility == View.VISIBLE){
                mListMenu.getItemAdapter().setList(videosList)
            }
        }

    })


    private val onSearchClick =  object : ItemListView.OnItemClick{
        override fun onItemClick(iItem: IItem, view: View) {
            onSearchItemClick(SearchAction.ITEM_CLICK, iItem,view)
        }
    }

    private val onSearchImageClick = object : ItemListView.OnItemClick{
        override fun onItemClick(iItem: IItem, view: View) {
            val searchAction = when(view.id){
                R.id.mImage -> SearchAction.ITEM_DELETE
                else -> SearchAction.ITEM_ADD_TEXT
            }
            onSearchItemClick(searchAction, iItem,view)
        }
    }

    lateinit var mProvider : Factory.VideoProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(customToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
        mVideoPlayer.visibility = View.INVISIBLE
        mYouTubePlayer.visibility = View.INVISIBLE
        mWebPlayer.visibility = View.INVISIBLE
        itemList.onItemClick = this
        mNavView.setNavigationItemSelectedListener(object : NavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when(item.itemId){
                    R.id.youtube_provider -> changeProviderType(Factory.Type.YOTUBE)
                    R.id.rutube_provider->changeProviderType(Factory.Type.RUTUBE)
                    R.id.hdrezka_provider->changeProviderType(Factory.Type.HDREZKA)
                    else -> Toast.makeText(this@MainActivity,"Not implemented",Toast.LENGTH_SHORT).show()
                }
                mDrawerLayout.closeDrawer(GravityCompat.START)
                return true
            }

        })
        mProvider = Factory.createProvider(App.prefs().getProviderType())
        dataLoader = mProvider.createSearchLoader(this)
        if(Const.USE_CONTROLLER_DLG) {
            controllerDlg = ControllerDialog(this)
            mYouTubePlayer.controllerDialog = controllerDlg
        }
        setLoadData(false)
    }

    fun makeTitle(query : String?){

        var title = App.str(R.string.app_name)
        if(!TextUtils.isEmpty(query))
            title = mProvider.getType().getName() +": "+query
        setTitle(title)
    }

    fun setError(error : String = App.str(R.string.err_general)){
        MyLog.log("Error: "+error)
        errorText.text = error
        errorText.visibility = View.VISIBLE
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(mListMenu.visibility == View.VISIBLE) {
            newText?.let {
                mCompleteLoader.loadSearch(it)
            }
        }

        return true
    }

    fun runSearch(text:String) : Boolean{
        if(TextUtils.isEmpty(text) || text.equals(queryText))
            return false;
        Db.get().saveSearch(text)
        makeTitle(text)
        queryText = text;
        App.prefs().saveSearch(queryText)
        itemList.clear()
        dataLoader?.run {
            this.cancelJob()
        }
        dataLoader = mProvider.createSearchLoader(this)
        setLoadData(true)
        dataLoader!!.loadSearch(queryText)
        itemList.setPageLoader(dataLoader!!).setStartAfter(10)
        return true;
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        customToolbar.collapseActionView()
        return runSearch(query?:"")
    }

    fun setLoadData(show: Boolean) {
        loadProgress.visibility = if(show) View.VISIBLE else View.INVISIBLE
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        event?.run {
            if(action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK&& !(mSearchView?.isIconified?:false)){
                customToolbar.collapseActionView()
            }
        }
        return super.dispatchKeyEvent(event)
    }



    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar,menu)
        val searchItem = menu?.findItem(R.id.action_search);
        mSearchView = searchItem?.actionView as CustomSearchView

        mSearchView?.run {
            setOnQueryTextListener(this@MainActivity)
            setIconifiedByDefault(true)
            startInitialSearch()
            onSearchShowChange = this@MainActivity

        }
        return super.onCreateOptionsMenu(menu)
    }


    fun changeProviderType(factoryType : Factory.Type){
        queryText = ""
        App.prefs().setProviderType(factoryType)
        mProvider = Factory.createProvider(factoryType)
        startInitialSearch()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.youtube_provider ->{changeProviderType(Factory.Type.YOTUBE); return true}
            R.id.rutube_provider ->{changeProviderType(Factory.Type.RUTUBE); return true}
            R.id.hdrezka_provider ->{changeProviderType(Factory.Type.HDREZKA); return true}
            R.id.search_history -> {showSearchHistory(); return true}
            android.R.id.home -> {mDrawerLayout.openDrawer(GravityCompat.START); return true}
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun closeListMenu () : Boolean{
        if(mListMenu.visibility != View.VISIBLE)
            return false;
        mListMenu.clear()
        mListMenu.visibility = View.INVISIBLE
        return true
    }

    private fun showSearchHistory() {
        val cursor = Db.get().getAllSearches()
        mListMenu.onItemClick = onSearchClick
        mListMenu.onImageItemClick = onSearchImageClick

        mListMenu.setItems(Db.SearchList().setCursor(cursor))
        mListMenu.visibility = View.VISIBLE
    }

    fun startInitialSearch(){
        val q = App.prefs().getSearch();
        makeTitle(q)
        mSearchView?.run {
            setQuery(q,!q.isEmpty())
        }
    }

    override fun onItemsLoaded(err: Err, videosList: VideosList) {
        setLoadData(false)
        if(err.isOk) {
            itemList.getItemAdapter().setList(videosList)
        }
        MyLog.log("Loaded")
    }

    override fun onBackPressed() {
        if(closeListMenu()){
            return
        }

        if(isPlayerShown) {
            closePlayer()
            return
        }

        if(Const.USE_CONTROLLER_DLG)
            controllerDlg.iPlayback = null
        super.onBackPressed()
    }

    override fun onItemClick(iItem: IItem, view: View) {
        isPlayerShown = true
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if(mProvider.getType() == Factory.Type.YOTUBE) {
            mYouTubePlayer.visibility = View.VISIBLE
            mYouTubePlayer.playItem(iItem, itemList.getItemAdapter().items)
            if (Const.USE_CONTROLLER_DLG) {
                controllerDlg.setOnCancelListener(object : DialogInterface.OnCancelListener {
                    override fun onCancel(dialog: DialogInterface?) {
                        closePlayer()
                    }
                })
            }
        } else if(iItem is IItem.IVideoUrlLoader){
            mVideoPlayer.visibility = View.VISIBLE
            mVideoPlayer.playVideo(iItem)
        } else if(iItem is IItem.IUrlItem) {
            mWebPlayer.visibility = View.VISIBLE
            mWebPlayer.playVideo(iItem)
        }
    }

    fun closePlayer() {
        if(mYouTubePlayer.visibility == View.VISIBLE){
            mYouTubePlayer.youTubePlayer?.run {
                setFullscreen(false)
                if(isPlaying)
                    pause()
            }
            mYouTubePlayer.visibility = View.INVISIBLE
        } else if(mWebPlayer.visibility == View.VISIBLE){
            mWebPlayer.stop()
            mWebPlayer.visibility = View.INVISIBLE
        }else if(mVideoPlayer.visibility == View.VISIBLE){
            mVideoPlayer.stop()
            mVideoPlayer.visibility = View.INVISIBLE
        }
        controllerDlg.hide()
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        isPlayerShown = false
    }

    fun deleteSearchWithDialog(iItem: IItem) {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_delete)
            .setTitle(R.string.delete_confirm)
            .setMessage(App.str(R.string.delete_confirm_msg).replace("[NAME]",iItem.name.toString()))
            .setPositiveButton(R.string.yes) { dlg, w->deleteSearch(iItem)}
            .setNegativeButton(R.string.no) { dlg, w->}
            .show()
    }

    fun deleteSearch(iItem: IItem) {
        Db.get().deleteSearch(iItem.name.toString())
        showSearchHistory()

    }


    fun onSearchItemClick(action: SearchAction, iItem: IItem, view: View) {

        when(action){
            SearchAction.ITEM_ADD_TEXT-> mSearchView?.replaceSelectedText(iItem.name.toString())
            SearchAction.ITEM_CLICK -> {
                mSearchView?.setQuery(iItem.name,true)
                closeListMenu()}
            SearchAction.ITEM_DELETE -> deleteSearchWithDialog(iItem)
        }

    }

    override fun onSearchShowChange(visible: Boolean) {
        if(visible)
            showSearchHistory()
        else
            closeListMenu()
    }

    enum class SearchAction{
        ITEM_CLICK, ITEM_DELETE, ITEM_ADD_TEXT
    }
}
