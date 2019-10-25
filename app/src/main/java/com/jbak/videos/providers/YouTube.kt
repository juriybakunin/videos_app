package com.jbak.videos.providers

import android.net.Uri
import android.text.TextUtils
import android.util.SparseArray
import android.webkit.WebResourceRequest
import androidx.core.text.isDigitsOnly
import androidx.core.util.isEmpty
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.jbak.formatDuration
import com.jbak.getDescription
import com.jbak.isFilename
import com.jbak.removeParam
import com.jbak.videos.App
import com.jbak.videos.DataLoader
import com.jbak.videos.SerialLoader
import com.jbak.videos.VideoUrlInterceptor
import com.jbak.videos.types.IItem
import com.jbak.videos.types.IItem.*
import com.jbak.videos.types.Media
import com.jbak.videos.types.VideoItem
import com.jbak.videos.types.VideosList
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import tenet.lib.base.Err
import tenet.lib.base.MyLog
import tenet.lib.base.utils.Utils
import kotlin.collections.ArrayList

class YMedia(uri: Uri, val mediaFormat: YouTubeFormats.MediaFormat) : Media(uri){

}

class YouTubeInterceptorItem : VideoItem(), IResourceIntercept, INextMedia {
    override fun getNextMedia(media: Media?): Media? {
        if(media != null && media.videoUri.getQueryParameter("fallback_count")==null)
            return getMedia(true)
        return null

    }

    @Volatile var medias : ArrayList<YMedia>? = null
    var interceptor : VideoUrlInterceptor? = null
    override fun onWebViewEvent(event: Int, url: String?, interceptor: VideoUrlInterceptor) {
        when(event) {
            LOAD_EVENT_START -> {
                this.interceptor = interceptor
                loadWebPage()
//                loadExtractor()
            }
        }
    }

    fun loadExtractor() {
        object : YouTubeExtractor(App.get()){
            override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, videoMeta: VideoMeta?) {

            }

        }.extract(getItemUrl(), true, true)
    }

    fun loadWebPage(){
        interceptor?.loadUrl(getItemUrl()+"&autoplay=true")
    }

    fun getMedia(fallback: Boolean = false) : Media? {
        if (medias == null)
            return null
        var audio : YMedia? = null
        var video : YMedia? = null
        for (m in medias!!) {
            val hasFallback = m.videoUri.getQueryParameter("fallback_count") != null
            if( fallback != hasFallback)
                continue
            if(m.mediaFormat.hasAudioAndVideo()) {
                audio = null
                video = m
                break
            } else if(m.mediaFormat.hasAudio()){
                audio = m
            } else if(m.mediaFormat.hasVideo()) {
                video = m
            }
        }
        if(video != null && video.mediaFormat.hasAudioAndVideo()) {
            return video
        } else if(video != null && audio != null) {
            return Media(video.videoUri, audio.videoUri)
        }
        return null
    }

    fun addMedia(resUri: Uri) : Boolean {
        val etag = resUri.getQueryParameter("itag")
        if(etag == null ||!etag.isDigitsOnly())
            return false
        val fmt = YouTubeFormats.TABLE.get(etag.toInt())
        if(fmt == null)
            return false
        if(medias == null)
            medias = ArrayList()
        medias?.add(YMedia(resUri.removeParam("range"),fmt))
        return true
    }

    override fun interceptResource(resUri: Uri, request: WebResourceRequest, interceptor: VideoUrlInterceptor): Int {
        val gvideo = resUri.host?.endsWith("googlevideo.com") ?: false
        if(gvideo && resUri.isFilename("videoplayback")) {
            if (addMedia(resUri) ) {
                val mediaFallback = getMedia(true)
                if(mediaFallback != null) {
//                    interceptor.videoMediaLoaded(mediaFallback)
//                    return INTERCEPTED
                    val media = getMedia(false)
                    if (media != null) {
                        interceptor.videoMediaLoaded(media)
                        return IItem.INTERCEPTED
                    }
                }
            }
            return BLOCK_ONCE

        }
        return CONTINUE
    }

    override fun getItemUrl(): String? {
        return "https://www.youtube.com/watch?fmt=18&vq=&v=" + getId()
    }
}

class YouTubeItem : VideoItem(),
    //IItem.INextUrl,
    IItem.IVideoUrlLoader
{
    var ytFiles : ArrayList<YtFile>? = null

    override fun loadVideoUrlSync(): String? {
        val url = getItemUrl()
        ytFiles?.clear()
        val files = YouTubeExtractor.SyncExtractor(App.get()).extractSync(url,true,true)
        if (files != null && !files.isEmpty()){
            if(ytFiles == null)
                ytFiles = ArrayList()
            for (i in 0..files.size()-1){
                val key = files.keyAt(i)
                val value = files.get(key)
                val use = value.format != null && value.format.hasAudio() && value.format.hasVideo()
                if(use) {
                    ytFiles?.add(value)
                }
                MyLog.log("format: use=$use ${value.getDescription()}")
            }
        }
        if(ytFiles != null && ytFiles!!.size > 0){
            return ytFiles!![0].url
        } else {
            MyLog.log("Empty format list")

        }
        return null
    }

    override fun getItemUrl(): String {
        return "https://www.youtube.com/watch?v=" + getId()
    }
}

class YouTube : Factory.BaseVideoProvider() {
    override fun getItemClass(): Class<out VideoItem> {
        if(USE_INTERCEPTOR) {
            return YouTubeInterceptorItem::class.java
        }
        else {
            return YouTubeItem::class.java
        }
    }

    override fun createSearchLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader {
        return object : DataLoader(onItemsLoaded){
            override fun loadDataSync(videosList: VideosList): Err {
                val conn = jsoupConnect(URL_SEARCH)
                    .data(
                        QUERY,videosList.query,
                        "filters", "video",
                        "max_results","50");
                if(!videosList.isFirstPage())
                    conn.data(SP, videosList.nextPageToken)
                val doc = conn.get()
                val divs = doc.getElementsByClass("yt-uix-tile")
                for (div in divs)
                    videosList.addCheckId(parseSearchElement(div))
                videosList.nextPageToken = parseNextPage(doc)
                return Err.OK
            }
        }
    }

    override fun getUrlExpireTime(iItem: IItem, url: String): Long {
        val uri = Uri.parse(url)
        val sexp = uri.getQueryParameter("expire")
        if(!TextUtils.isEmpty(sexp) && sexp!!.isDigitsOnly()) {
            val exp = sexp.toLong()
            return exp * 1000L;
        }
        return super.getUrlExpireTime(iItem, url)
    }

    override fun createRelatedLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader {
        return object : DataLoader(onItemsLoaded) {
            override fun loadDataSync(videosList: VideosList): Err {
                val conn = jsoupConnect(URL_VIDEO)
                    .data("v",videosList.query)
                val doc = conn.get()
                val lis = doc.getElementsByClass("related-list-item")
                for (li in lis)
                    videosList.addCheckId(parseRelatedVideo(li))
                videosList.nextPageToken = null
                return Err.OK
            }
        }
    }

    override fun getType(): Factory.Type {
        return Factory.Type.YOTUBE
    }


    companion object {
        public var USE_INTERCEPTOR = false
        private val URL_SEARCH = "http://www.youtube.com/results"
        private val URL_VIDEO = "https://www.youtube.com/watch"
        private val USER_AGENT = "Mozilla/5.0"
        private val QUERY = "search_query"
        private val SP = "sp"

        private fun jsoupConnect(baseUrl : String) : Connection{
            return Jsoup.connect(baseUrl)
                .userAgent(USER_AGENT)
                .header("Accept-Encoding", "gzip, deflate")

        }

        private fun fillVideoImage(span: Element?, vi : VideoItem){
            if(span == null)
                return
            val img = span.getElementsByTag("img").first()
            if(img != null) {
                vi.image = img.attr("data-thumb")
                if(TextUtils.isEmpty(vi.image))
                    vi.image = img.attr("src")
            }

        }

        private fun initVideo(a : Element?) : VideoItem {
            val vi = Factory.getProvider(Factory.Type.YOTUBE).createItem()
            if(a == null)
                return vi;
            val href = a.attr("href")
            val uri = Uri.parse(href)
            val id = uri.getQueryParameter("v")
            vi.setIdAndName(id,a.attr("title"))
            return vi;
        }

        private fun parseRelatedVideo(li : Element) : IItem {
            val a = li.getElementsByClass("content-link").first()
            val vi = initVideo(a)
            val thumbSpan = li.getElementsByClass("yt-uix-simple-thumb-related").first()
            fillVideoImage(thumbSpan, vi)
            vi.dur = parseDuration(li)
            return vi
        }



        private fun parseDuration(parent : Element) : String{
            val span = parent.getElementsByClass("video-time").first();
            if(span == null)
                return "";
            var str = span.text()
            if(TextUtils.isEmpty(str))
                return "";
            str = str.trim()
            val items = str.split(":")
            if(items.size < 1)
                return "";
            var dur = 0
            if (items.size > 0)
                dur += Utils.strToInt(items[items.size - 1],0)
            if (items.size > 1)
                dur += Utils.strToInt(items[items.size - 2],0) * 60
            if (items.size > 2)
                dur += Utils.strToInt(items[items.size - 3],0) * 3600
            return dur.formatDuration()
        }

        private fun parseSearchElement(div : Element) : IItem {
            val a = div.select(".yt-lockup-title > a[title]").first()
            val vi = initVideo(a)
            val thumbSpan = div.getElementsByClass("yt-thumb-simple").first()
            fillVideoImage(thumbSpan, vi)
            vi.dur = parseDuration(div)
            return vi
        }

        private fun parseNextPage(doc : Document) : String?{
            val divPager = doc.getElementsByClass("search-pager").first()
            if(divPager == null)
                return null
            val aLast = divPager.getElementsByTag("a").last()
            if(aLast == null)
                return null
            return Uri.parse(aLast.attr("href")).getQueryParameter("sp")

        }

    }
    override fun createSerialLoader(
        iItem: IItem,
        onItemsLoaded: DataLoader.OnItemsLoaded
    ): SerialLoader? {
        return null
    }

}


