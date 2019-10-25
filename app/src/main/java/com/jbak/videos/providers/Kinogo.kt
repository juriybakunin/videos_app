package com.jbak.videos.providers

import com.jbak.videos.DataLoader
import com.jbak.videos.SerialLoader
import com.jbak.videos.VideoUrlInterceptor
import com.jbak.videos.model.kinokrad.Playlist
import com.jbak.videos.types.IItem
import com.jbak.videos.types.IItem.LOAD_EVENT_PAGE_FINISHED
import com.jbak.videos.types.SerialList
import com.jbak.videos.types.VideoItem
import com.jbak.videos.types.VideosList
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tenet.lib.base.Err
import tenet.lib.base.MyLog
import java.lang.Runnable
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher.quoteReplacement
import java.util.regex.Pattern


private val SEARCH_URL = "https://kinogo.by/index.php?do=search"

private val REGEX_DURATION = Pattern.compile("<b>Продолжительность:</b>\\s+(.*?)\\n")
private val REGEX_YEAR = Pattern.compile("<b>Год выпуска:</b>\\s*<a href=\".*?\">(\\d+)</a>",Pattern.MULTILINE)

fun removeUTFCharacters(data: String): String{
    val p = Pattern.compile("\\\\u(\\p{XDigit}{4})")
    val m = p.matcher(data)
    val buf = StringBuffer(data.length)
    while (m.find()) {
        val ch = Integer.parseInt(m.group(1), 16).toChar().toString()
        m.appendReplacement(buf, quoteReplacement(ch))
    }
    m.appendTail(buf)
    return buf.toString().replace("\\\"", "\"");
}

private fun parseElement(el: Element): KinogoItem {
    val item = KinogoItem()
    val shimg: Element? = el.getElementsByClass("shortimg").firstOrNull()
    val img : Element? = shimg?.getElementsByTag("img")?.firstOrNull()
    img?.let {
        item.image = img.absUrl("src")
        val a = img.parent()
        item.setIdAndName(a.absUrl("href"),img.attr("alt"))
    }
    val str = el.toString()
    var year = ""
    var dur = ""
    val myear = REGEX_YEAR.matcher(str)
    if (myear.find()){
        year = myear.group(1)?: ""
    }
    val mdur = REGEX_DURATION.matcher(str)
    if(mdur.find()) {
        dur = mdur.group(1)?: ""
        val index = dur.indexOf(" / ")
        if(index > 0)
            dur = dur.substring(0,index).trim()
    }
    item.dur = "$year $dur".trim()
    return item
}

private fun parseSerial(playlist: Element): SerialList? {
    return null
}

private fun parseHtmlValue(rawValue : String?, interceptor: VideoUrlInterceptor) {
    if(rawValue == null) {
        interceptor.videoLoadError("Page is empty")
        return
    }
    val html = removeUTFCharacters(rawValue);
    MyLog.log("Value is:"+html)
    val doc = Jsoup.parse(html)
    val vid = doc.getElementsByTag("video").first()
    val playlist = doc.getElementById("1212_playlist")
    if(playlist != null) {
        val serial = parseSerial(playlist)
        if(serial != null) {
            interceptor.setSerial(serial)
        }
    }
    vid?.let {
        val url = it.absUrl("src")
        interceptor.videoUrlLoaded(url)
    }



}

class Kinogo : Factory.BaseVideoProvider() {


    override fun createSearchLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader {
        return object : DataLoader(onItemsLoaded){
            override fun loadDataSync(videosList: VideosList): Err {
                val conn = Jsoup.connect(SEARCH_URL)
                    .header("Accept-Encoding", "gzip, deflate")
                    .data("story",URLEncoder.encode(videosList.query,"CP1251"))
                    //.data("do", "search")
                    .data("full_search", "0")
                    .data("subaction","search")
                if(!videosList.isFirstPage()) {
                    //conn.data("result_from", "11")
                    conn.data("search_start", videosList.nextPageToken)
                    //conn.data("result_from", videosList.nextPageToken)
                }
                val doc = conn.post()
                val elements = doc.getElementsByClass("shortstory")
                for (el in elements) {
                    val item = parseElement(el)
                    videosList.add(item)
                }

                if(doc.getElementById("nextlink") != null) {
                    var nextStart = 1
                    if(!videosList.isFirstPage() && videosList.nextPageToken != null) {
                        nextStart = videosList.nextPageToken!!.toInt()
                    }
                    nextStart += 1
                    videosList.nextPageToken = "$nextStart"
                } else {
                    videosList.nextPageToken = null
                }


                return Err.OK
            }

        }
    }

    override fun getUrlExpireTime(iItem: IItem, url: String): Long {
        return System.currentTimeMillis() + TimeUnit.HOURS.toMillis(8)
    }

    override fun createRelatedLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader? {
        return object : DataLoader(onItemsLoaded) {
            override fun loadDataSync(videosList: VideosList): Err {
                val doc = Jsoup.connect(videosList.query).get()
                val related = doc.getElementsByClass("ul_related").first()
                videosList.nextPageToken = null
                if(related == null)
                    return Err.ERR_DATA_LOAD
                for (e in related.children()) {
                    val a = e.getElementsByTag("a").firstOrNull()
                    val img = a?.getElementsByTag("img")?.firstOrNull()
                    if(a == null || img == null) {
                        continue
                    }
                    val item = Factory.getProvider(Factory.Type.KINOGO).createItem()
                    item.setIdAndName(a.absUrl("href"), img.attr("alt"))
                    item.image = img.absUrl("data-original")
                    videosList.addCheckId(item)
                }
                return Err.OK
            }

        }
    }

    override fun createSerialLoader(parentItem: IItem, onItemsLoaded: DataLoader.OnItemsLoaded): SerialLoader? {
        return null;
    }

    override fun getType(): Factory.Type {
        return Factory.Type.KINOGO
    }

    override fun getItemClass(): Class<out VideoItem> {
        return KinogoItem::class.java
    }

}

class KinogoItem : HDRezkaItem(){

    override fun onWebViewEvent(event: Int, url: String?, interceptor: VideoUrlInterceptor) {
        super.onWebViewEvent(event, url, interceptor)
        if(event == LOAD_EVENT_PAGE_FINISHED && id.equals(url)){
            MyLog.log("Finished page")
            interceptor.mWebView.postDelayed({
                interceptor.mWebView.evaluateJavascript(
                    "(function() { return (document.getElementsByTagName('body')[0].outerHTML); })();"){
                        value: String? -> parseHtmlValue(value, interceptor)
                }
            }, 1000)
        }
    }
}