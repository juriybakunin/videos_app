package com.jbak.videos.activity

import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.jbak.setTrueFullscreen
import com.jbak.videos.*
import com.jbak.videos.playback.PlaybackService
import com.jbak.videos.providers.Factory
import com.jbak.videos.types.IItem
import com.jbak.videos.types.VideosList
import com.jbak.videos.view.CustomSearchView
import com.jbak.videos.view.ItemDesign
import com.jbak.videos.view.ItemListView
import com.jbak.videos.view.TechMenu
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
    private val mCompleteLoader = Web.createCompleteLoader(object : DataLoader.OnItemsLoaded{
        override fun onItemsLoaded(err: Err, videosList: VideosList) {
            MyLog.log(err.toString())
            if(err.isOk && mListMenu.visibility == View.VISIBLE){
                mListMenu.itemAdapter.setList(videosList)
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
        itemList.onItemClick = this
        mProvider = Factory.getProvider(App.prefs().getProviderType())
        createProvidersPanel()
        dataLoader = mProvider.createSearchLoader(this)
        controllerDlg = ControllerDialog(this, mProvider)
        controllerDlg.setOnCancelListener {
            mListMenu.postDelayed({
                window.setTrueFullscreen(false)
            },100L)
        }
        App.PLAYER = controllerDlg
        setLoadData(false)
    }

    override fun onPause() {
        controllerDlg.onActivityEvent(ACTIVITY_EVENT_PAUSE)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        controllerDlg.onActivityEvent(ACTIVITY_EVENT_RESUME)
    }

    override fun onDestroy() {
        super.onDestroy()
        controllerDlg.onActivityEvent(ACTIVITY_EVENT_DESTROY)
    }

    private fun createProvidersPanel() {
        val providers = IItem.ItemList();
        var id : String? = null
        for (t in Factory.Type.values()) {
            if(t == Factory.Type.NONE)
                break
            if(mProvider.getType() == t)
                id = t.id;
            providers.add(t)
        }
        val des = ItemDesign()
            .set(ItemDesign.TEXT_COLOR,Color.WHITE)
            .set(ItemDesign.TEXT_COLOR_CURRENT,Color.WHITE)
            .set(ItemDesign.TEXT_BACK_COLOR, App.res().getColor(R.color.colorPrimaryDark))
            .set(ItemDesign.TEXT_BACK_COLOR_CURRENT, App.res().getColor(R.color.colorPrimary))
            .set(ItemDesign.TEXT_SHADOW,0)
        providerList.setBackgroundColor(App.res().getColor(R.color.colorPrimary))
        providerList.setDesign(des)
        providerList.setItems(providers, id, true)
        providerList.onItemClick = object : ItemListView.OnItemClick {
            override fun onItemClick(iItem: IItem, view: View) {
                changeProviderType(Factory.getItemType(iItem))
            }
        }

    }


    fun makeTitle(query : String?){

        var title = App.str(R.string.app_name)
        if(!TextUtils.isEmpty(query))
            title = query!!
        setTitle(title)
        supportActionBar!!.subtitle = mProvider.getType().getName()
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
        controllerDlg.onConfigurationChanged(newConfig)
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
        providerList.setCurrent(factoryType.id)
        mProvider = Factory.getProvider(factoryType)
        startInitialSearch()
    }

    fun onCommand(command:Int) : Boolean{
        when(command){
            R.id.search_history -> showSearchHistory()
            R.id.select_provider -> showProvidersMenu()
            R.id.tech_menu -> TechMenu().showMain(this)
            R.id.refresh -> changeProviderType(mProvider.getType())
            R.id.settings -> showSettings()
            else -> {
                return false
            }
        }
        return true
    }

        private fun showSettings() {
            val set = IItem.ItemList();
            SettingDlg().setGeneral().show(this)
        }

        private fun showProvidersMenu() {
        val list = ArrayList<String>()
        for (t in Factory.Type.values()) {
            if(t != Factory.Type.NONE)
                list.add(t.getName())
        }
        val array = list.toArray(arrayOfNulls<String>(list.size))
        AlertDialog.Builder(this)
            .setItems(array, object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if(which < 0)
                        return
                    changeProviderType(Factory.Type.values()[which])
                }
            })
            .setTitle(R.string.select_provider)
            .setIcon(R.drawable.ic_search_provider)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        closeListMenu()
        if(onCommand(item.itemId))
            return true
        return super.onOptionsItemSelected(item)
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
            itemList.itemAdapter.setList(videosList)
        }
        MyLog.log("Loaded")
    }

    override fun onBackPressed() {
        if(closeListMenu()){
            return
        }
        super.onBackPressed()
    }


    override fun onItemClick(iItem: IItem, view: View) {
        controllerDlg.playItem(iItem, itemList.itemAdapter.items)

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
