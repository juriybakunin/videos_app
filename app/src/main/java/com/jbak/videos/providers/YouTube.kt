package com.jbak.videos.providers

import android.net.Uri
import android.text.TextUtils
import androidx.core.text.isDigitsOnly
import androidx.core.util.isEmpty
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.jbak.formatDuration
import com.jbak.getDescription
import com.jbak.videos.App
import com.jbak.videos.DataLoader
import com.jbak.videos.SerialLoader
import com.jbak.videos.types.IItem
import com.jbak.videos.types.VideoItem
import com.jbak.videos.types.VideosList
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import tenet.lib.base.Err
import tenet.lib.base.Interfaces
import tenet.lib.base.MyLog
import tenet.lib.base.utils.Utils
import kotlin.collections.ArrayList


class MediaInfo : Interfaces.IdNamed{
    override fun getName(): CharSequence {
        return url
    }

    override fun getId(): String {
        return url
    }

    constructor(tag:Int, url:String) {
        this.tag = tag
        this.url = url;
    }
    val tag : Int
    val url : String
}


class YouTubeItem : VideoItem(),
    IItem.INextUrl,
    IItem.IUrlItem,
    IItem.IVideoUrlLoader
{
    var ytFiles : ArrayList<YtFile>? = null
    override fun loadVideoUrlSync(): String? {
        val url = pageUrl
        ytFiles?.clear()
        val files = YouTubeExtractor.SyncExtractor(App.get()).extractSync(url,false,false)
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
        }
        return null
    }

    val pageUrl:String
        get() = "http://www.youtube.com/watch?v=" + getId()
//    override fun getUserAgent(): String? {
//        return USER_AGENT;
//    }

//    override fun loadVideoUrlSync(): String? {
//        val url = INFO_URL.replace("[ID]", URLEncoder.encode(id));
//        MyLog.log("Info url: "+url);
//        val dinfo = YouTubeFormats.TEST_HOST+NetUtils.loadUrl(url);
//        mediaUrls = YouTubeFormats.MediaUrls().fromData(dinfo);
//        if(mediaUrls != null && mediaUrls!!.size > 0){
//            MyLog.log("MEDIA FORMATS:")
//            for (mf in mediaUrls!!){
//               MyLog.log(mf.toString())
//            }
//            return mediaUrls!![0].url;
//        }
//        return null
//    }
//    var mediaUrls : YouTubeFormats.MediaUrls? = null
//override fun getNextUrl(url: String?): String? {
//    if(url == null || mediaUrls == null)
//        return null
//    val next = Utils.getNextPreviousItem(true,url,mediaUrls,false)
//    return if(next == null) null else next.url
//}

    override fun getStartUrl(): String {
        return "http://www.youtube.com/watch?v=" + getId() +"&autoplay=1";
    }


    fun checkMedia(uri: Uri) : Boolean {
//        if(mediaData == null) {
//            mediaData = MediaData()
//        }
        val itagStr = uri.getQueryParameter("itag")
        var itag = 0
        if(itagStr != null)
            itag = Integer.decode(itagStr)
        val fmt = YouTubeFormats.TABLE.get(itag)
        if(fmt != null) {
            return fmt.hasVideo() && fmt.hasAudio()
        }
//        val md = MediaInfo(itag, uri.toString())
//        if(audio) {
//            mediaData!!.audioList.add(md)
//        } else {
//            mediaData!!.videosList.add(md)
//        }
        return false
    }

//    var mediaData : MediaData? = null
//    fun getFirstUrl(): String? {
//        if(mediaData != null && mediaData!!.videosList.size > 0) {
//            return mediaData!!.videosList[0].url;
//        }
//        return null;
//    }

    override fun getNextUrl(url: String?): String? {
        if(ytFiles != null && ytFiles!!.size > 0){
            return ytFiles!![0].url
        }
        return null;
    }

//    override fun interceptResource(resUri: Uri, request: WebResourceRequest?, interceptor: VideoUrlInterceptor): Int {
//        if(resUri.host.endsWith("googlevideo.com") &&
//            resUri.lastPathSegment != null &&
//            resUri.lastPathSegment!!.equals("videoplayback")) {
//
//            val mime = resUri.getQueryParameter("mime");
//            MyLog.log("Mime: $mime, itag: ${resUri.getQueryParameter("itag")} file: ${resUri.lastPathSegment}")
//            if(mime == null) {
//                return IItem.BLOCK_ONCE
//            }
//            if(checkMedia(resUri)) {
//                val audio = mime.startsWith("audio")
//                val builder = resUri.buildUpon().clearQuery();
//                for (key in resUri.queryParameterNames) {
//                    val param = resUri.getQueryParameter(key);
//                    if (!key.equals("range") && !TextUtils.isEmpty(param)) {
//                        builder.appendQueryParameter(key, param)
//                    }
//                }
//                val uri = builder.build();
//                interceptor.videoUrlLoaded(uri.toString());
//                return IItem.STOP_LOAD
//            }
//
//            return IItem.BLOCK_ONCE
//        }
//        return IItem.CONTINUE
//    }

}

class YouTube : Factory.BaseVideoProvider() {

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

        private fun initVideo(a : Element?) : YouTubeItem {
            val vi = YouTubeItem()
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


