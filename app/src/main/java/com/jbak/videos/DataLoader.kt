package com.jbak.videos
import com.jbak.videos.types.VideosList
import kotlinx.coroutines.*
import tenet.lib.base.Err
import tenet.lib.base.MyLog

abstract class DataLoader(private var onItemsLoaded: OnItemsLoaded) : RecyclerUtils.IPageLoader {
    var myJob : Job? = null;
    val videosList  = VideosList();

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

    override fun loadNextPage() {
        MyLog.log("Load search: q=${videosList.query}; first=${videosList.isFirstPage()} exist=${videosList.size}")
        val uiScope = CoroutineScope(Dispatchers.Main)
        myJob = uiScope.launch(Dispatchers.IO) {
            var err: Err
            try {
                err = loadDataSync(videosList)
            } catch (t:Throwable){
                MyLog.err(t)
                err = Err.ERR_DATA_LOAD;
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
