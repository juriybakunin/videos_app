package com.jbak.videos.providers

import android.net.Uri
import android.webkit.WebResourceRequest
import com.google.gson.Gson
import com.jbak.hasInPath
import com.jbak.isExtension
import com.jbak.videos.*
import com.jbak.videos.model.kinokrad.PlayEntry
import com.jbak.videos.model.kinokrad.Playlist
import com.jbak.videos.types.*
import com.jbak.videos.types.IItem.LOAD_EVENT_START
import com.jbak.videos.types.IItem.INTERCEPTED
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import tenet.lib.base.Err
import java.io.IOException

private val BASE_URL = "https://kinokrad.co/"

class KinokradItem : HDRezkaItem(){
    private var mPlaylist: Playlist? = null
    private var mUrlLoaderJob: Job? = null
    var pageDocument: Document? = null

    override fun processVideoUri(resUri: Uri, resource: WebResourceRequest, webPlayer: VideoUrlInterceptor): Int {
        if ("kinokrad.co".equals(resUri.host)&&resUri.isExtension(".txt")&& resUri.hasInPath("playlist")) {
            val onLoader =  object : Web.Loader(){
                override fun onLoad(call: Call, response: Response?, e: IOException?) {
                    if(response != null && response.isSuccessful) {
                        val str = response.body()?.string()
                        str?.let {
                            val pe = Gson().fromJson<PlayEntry>(it, PlayEntry::class.java)
                            val serial = Kinokrad.createSerialFromPlaylist(this@KinokradItem, pe)
                            mPlaylist = pe.playlist
                            webPlayer.setSerial(serial)
                            webPlayer.videoUrlLoaded(mPlaylist!![0].file)
                        }
                    }
                }
            }
            loadWebResource(resUri, resource, onLoader);
            return INTERCEPTED
        }

        return super.processVideoUri(resUri, resource, webPlayer)
    }

    override fun onWebViewEvent(event: Int, url: String?, interceptor: VideoUrlInterceptor) {
        when(event) {
            LOAD_EVENT_START -> {
                val uri = Uri.parse(id)
                if(uri.isExtension(".m3u8")) {
                    interceptor.videoUrlLoaded(id)
                } else {
                    interceptor.loadUrl(id)
                }
            }
        }
    }

}

class Kinokrad : Factory.BaseVideoProvider(){

    companion object {

        fun createSerialFromPlaylist(item: KinokradItem, playEntry: PlayEntry) : SerialList{
            val serialList = SerialList()
            serialList.parentItem = item
            val seas = Season("1", App.str(R.string.season)+" 1");
            serialList.add(seas)
            for (p in playEntry.playlist){
                val kk = KinokradItem()
                kk.setIdAndName(p.file, p.comment)
                kk.image = item.image
                seas.add(kk)
            }
            return serialList
        }
        fun parseElement(el:Element) : KinokradItem {
            val item = KinokradItem()
            val poster = el.getElementsByClass("postershort").first()
            poster?.let {
                val a = it.getElementsByTag("a").first()
                val img = it.getElementsByTag("img").first()
                val id = a.attr("href")
                val name = img.attr("alt")
                item.setIdAndName(id,name)
                item.image = img?.absUrl("src")?:""
            }
            return item
        }
    }

    override fun getItemClass(): Class<out VideoItem> {
        return KinokradItem::class.java
    }

    override fun createSearchLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader {
        return object : DataLoader(onItemsLoaded){
            override fun loadDataSync(videosList: VideosList): Err {
                val doc = Jsoup.connect(BASE_URL)
                    .header("Accept-Encoding", "gzip, deflate")
                    .data("do","search")
                    .data("subaction","search")
                    .data("story",videosList.query)
                    .post()
                val elements = doc.getElementsByClass("searchitem")
                for (el in elements){
                    val item = parseElement(el)
                    videosList.add(item)
                }
                videosList.nextPageToken = null
                return Err.OK
            }

        }
    }

    override fun createRelatedLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader? {
        return null
    }

    override fun createSerialLoader(parentItem: IItem, onItemsLoaded: DataLoader.OnItemsLoaded): SerialLoader? {
        return null
    }

    override fun getType(): Factory.Type {
        return Factory.Type.KINOKRAD;
    }

}