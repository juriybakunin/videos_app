package com.jbak.videos
import android.view.View
import com.jbak.videos.types.IItem
import com.jbak.videos.types.SerialList
import com.jbak.videos.types.VideosList
import kotlinx.coroutines.*
import tenet.lib.base.Err
import tenet.lib.base.MyLog

abstract class DataLoader(private var onItemsLoaded: OnItemsLoaded, relatedItem: IItem? = null) : RecyclerUtils.IPageLoader {
    var myJob : Job? = null;
    private val videosList : VideosList;
    var relatedItem: IItem? = null

    init {
        this.relatedItem = relatedItem
        videosList = createVideosList();
    }

    @Throws(Throwable::class)
    abstract fun loadDataSync(videosList: VideosList) : Err

    fun cancelJob() {
        val isActive = myJob?.isActive?:false;
        if(isActive)
            myJob?.cancel()
        myJob = null;
    }

    override fun hasNextPage(): Boolean {
        return videosList.hasNextPage()
    }

    open fun createVideosList(): VideosList {
        return VideosList()
    }

    override fun loadNextPage() {
        MyLog.log("Loader page: q=${videosList.query}; first=${videosList.isFirstPage()} exist=${videosList.size}")
        val uiScope = CoroutineScope(Dispatchers.Main)
        myJob = uiScope.launch(Dispatchers.IO) {
            var err: Err = Err.OK
            for(i in 0..2) {
                try {
                    err = loadDataSync(videosList)
                    break
                } catch (t: Throwable) {
                    MyLog.err(t)
                    err = Err.ERR_DATA_LOAD;
                }
            }
            MyLog.log("Loaded ok=${err.isOk} items=${videosList.size}")

            withContext(Dispatchers.Main){
                onItemsLoaded.onItemsLoaded(err, videosList)
                myJob = null;
            }
        }

    }

    fun loadSearch(query: String) {
        videosList.reset().setQuery(query)
        loadNextPage()
    }

    interface OnItemsLoaded {
        fun onItemsLoaded(err : Err, videosList: VideosList)
    }

    override fun isLoading(): Boolean {
        return myJob != null
    }
}

abstract class SerialLoader(parent: IItem, onItemsLoaded: OnItemsLoaded) : DataLoader(onItemsLoaded, parent) {
    override fun loadDataSync(videosList: VideosList): Err {
        return loadSerial(videosList as SerialList)
    }

    override fun createVideosList(): VideosList {
        val ser = SerialList()
        ser.parentItem = relatedItem!!
        return ser
    }

    abstract fun loadSerial(serial : SerialList) : Err

}