package com.jbak.videos.providers

import android.net.Uri
import android.webkit.WebResourceRequest
import com.jbak.hasInPath
import com.jbak.isExtension
import com.jbak.isFilename
import com.jbak.videos.*
import com.jbak.videos.types.*
import com.jbak.videos.types.IItem.*
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tenet.lib.base.Err
import tenet.lib.base.MyLog
import tenet.lib.base.utils.Utils
import java.io.IOException

open class HDRezkaItem : VideoItem(), IItem.IResourceIntercept {


    fun loadWebResource(resUri: Uri, resource: WebResourceRequest, onLoad: Web.Loader){
        val url = resUri.toString()
        val rb = Request.Builder()
            .url(url)
            .method(resource.method, null);
        val headers = resource.requestHeaders
        for (ent in headers) {
            rb.addHeader(ent.key, ent.value)
        }
        OkHttpClient().newCall(rb.build()).enqueue(onLoad)

    }

    fun checkSeason(resUri: Uri) : Boolean{
        /// https://streamguard.cc/serial/a7c9364e6bc2a4180fc01230aa088b68/iframe?pid_b2cb5260=6c6cba9e&nocontrols=1&autoswitch=1&season=1&episode=1
        if(!id.contains('#'))
            return true
        val ep = resUri.getQueryParameter("episode")
        val seas = resUri.getQueryParameter("season")
        if(ep == null || seas == null)
            return true
        val hash = HdRezka.getEpisodeHash(seas, ep)
        if(id.contains(hash))
            return true
        return false
    }

    open fun processVideoUri(resUri: Uri, resource: WebResourceRequest, webPlayer: VideoUrlInterceptor) : Int{
        if("streamguard.cc".equals(resUri.host)&&
            resUri.isFilename("iframe")) {
            val onLoad = object : Web.Loader() {
                override fun onLoad(call: Call, response: Response?, e: IOException?) {
                    if (response != null && response.isSuccessful) {
                        var playerUrl = response.request().url().toString()
                        playerUrl = playerUrl + "&autoplay=1"
                        webPlayer.loadUrl(playerUrl, true)
                    } else {
                        webPlayer.videoLoadError(response, e)
                    }
                }
            }
            if(checkSeason(resUri)) {
                loadWebResource(resUri, resource, onLoad)
                return STOP_LOAD
            }
            return BLOCK_ONCE;
        } else if(resUri.hasInPath("video")
            && resUri.hasInPath("embed")
            &&"1plus1.video".equals(resUri.host)) {
            val playerUrl = "${resUri}&autoplay=true"
            webPlayer.loadUrl(playerUrl, true)
            return STOP_LOAD;
        }
        return CONTINUE
    }

    override fun onWebViewEvent(event: Int, url: String?, interceptor: VideoUrlInterceptor) {
        when(event) {
            LOAD_EVENT_START -> interceptor.loadUrl(id)

        }
    }

    override fun interceptResource(resUri: Uri, resource: WebResourceRequest, interceptor: VideoUrlInterceptor): Int {
        val proc = processVideoUri(resUri,resource, interceptor);
        if(proc != CONTINUE) {
            return proc;
        } else if(resUri.isExtension(".m3u8")) {
            interceptor.videoUrlLoaded(resUri.toString())
            return STOP_LOAD;
        } else if("grandcentral.1plus1.video".equals(resUri.host)){
            loadWebResource(resUri,resource,object :Web.Loader(){
                override fun onLoad(call: Call, response: Response?, e: IOException?) {
                    if (response != null && response.isSuccessful) {
                        val data = response.body()?.string()
                        val url = response.request().url().toString()
                        MyLog.log(data)
                        interceptor.videoUrlLoaded(url);
                    }

                }
            });
            return STOP_LOAD
        }
        return CONTINUE;
    }

}

class HdRezka : Factory.BaseVideoProvider() {

    companion object {
        val SEARCH_URL = "http://hdrezka.ag/index.php?do=search&subaction=search"
        val SEARCH_ITEM_CLASS = "b-content__inline_item"
//        fun prepareSerialPlayerUrl(item: HDRezkaItem, season:String, episode:String) : String?{
//            if(item.playerUrl == null)
//                return null
//            val uri = Uri.parse(item.playerUrl)
//            val builder = uri.buildUpon()
//            builder.clearQuery()
//            for(key in uri.queryParameterNames){
//                if("season".equals(key) || "episode".equals(key)) {
//                    continue
//                }
//                builder.appendQueryParameter(key,uri.getQueryParameter(key))
//            }
//            builder.appendQueryParameter("season", season)
//            builder.appendQueryParameter("episode", episode)
//            return builder.build().toString()
//        }

        fun getEpisodeHash(season:String, episode: String): String {
            return "#t:0-s:$season-e:$episode"
        }

        fun parseElement(el : Element) : IItem {
            val item = HDRezkaItem()
            val cover = el.getElementsByClass("b-content__inline_item-cover").first()
            cover?.let {
                val img = it.getElementsByTag("img").first()
                item.image = img?.absUrl("src")?:""
            }
            val link = el.getElementsByClass("b-content__inline_item-link").first()
            link?.let {
                val a = it.getElementsByTag("a").first()
                a?.let {
                    item.setIdAndName(it.attr("href"), it.text())
                }
            }
            val entity = el.getElementsByClass("entity").first()
            entity?.let {
                item.dur = it.text()
            }

            return item
        }
    }


    override fun createSearchLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader {
        return object : DataLoader(onItemsLoaded) {
            override fun loadDataSync(videosList: VideosList): Err {
                val doc = Jsoup.connect(SEARCH_URL)
                    .header("Accept-Encoding", "gzip, deflate")
                    .data("q",videosList.query)
                    .get()
                val elements = doc.getElementsByClass(SEARCH_ITEM_CLASS)
                for (e in elements) {
                    val item = parseElement(e)
                    if(item.id != null)
                        videosList.add(item)
                }
                videosList.nextPageToken = null
                return Err.OK
            }

        }
    }

    override fun createRelatedLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader? {
        return object : DataLoader(onItemsLoaded){
            override fun loadDataSync(videosList: VideosList): Err {
                val doc = Jsoup.connect(videosList.query)
                    .header("Accept-Encoding", "gzip, deflate")
                    .get()
                val elements = doc.getElementsByClass(SEARCH_ITEM_CLASS)
                for (e in elements) {
                    val item = parseElement(e)
                    if(item.id != null)
                        videosList.add(item)
                }
                videosList.nextPageToken = null
                return Err.OK
            }

        }
    }

    override fun getType(): Factory.Type {
        return Factory.Type.HDREZKA
    }



    override fun createSerialLoader(parentItem: IItem,
        onItemsLoaded: DataLoader.OnItemsLoaded
    ): SerialLoader? {
        return object : SerialLoader(parentItem, onItemsLoaded){
            override fun loadSerial(serial: SerialList): Err {
                val doc = Jsoup.connect(serial.query)
                    .header("Accept-Encoding", "gzip, deflate")
                    .get()
                val seasons = doc.getElementsByClass("b-simple_season__item")
                val series = doc.getElementsByClass("b-simple_episode__item")
                for (s in seasons) {
                    val sid = s.attr("data-tab_id")
                    val season = Season(s.text(),sid)
                    serial.add(season)
                }
                val hindex = serial.query.indexOf('#')
                val baseUrl = if(hindex >0) serial.query.substring(0,hindex) else serial.query

                var season : Season? = null
                for (s in series) {
                    val ser = HDRezkaItem()
                    val epId = s.attr("data-episode_id");
                    val epName = s.text()
                    val sid = s.attr("data-season_id")
                    val id = baseUrl + getEpisodeHash(sid, epId)
                    ser.setIdAndName(id, epName)
                    if(relatedItem is HDRezkaItem && relatedItem!!.imageUrl !=null) {
//                        ser.playerUrl = prepareSerialPlayerUrl(relatedItem as HDRezkaItem,sid,epId)
                        ser.image = relatedItem!!.imageUrl!!
                    }

                    if(season == null || !season.id.equals(sid)){
                        val item  = Utils.itemById(sid,serial)
                        if(item is Season) {
                            season = item
                        }
                    }
                    if(season != null){
                        season.add(ser)
                    }
                }
                serial.nextPageToken = null
                return Err.OK
            }
        }
    }

}