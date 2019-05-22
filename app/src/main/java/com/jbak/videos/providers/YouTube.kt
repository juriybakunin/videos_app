package com.jbak.videos.providers

import android.net.Uri
import android.text.TextUtils
import com.jbak.videos.DataLoader
import com.jbak.videos.types.IItem
import com.jbak.videos.types.SimpleItem
import com.jbak.videos.types.VideosList
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import tenet.lib.base.Err
import tenet.lib.base.utils.Utils

class YouTube : Factory.VideoProvider {
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

        private fun fillVideoImage(span: Element?, vi : SimpleItem){
            if(span == null)
                return
            val img = span.getElementsByTag("img").first()
            if(img != null) {
                vi.image = img.attr("data-thumb")
                if(TextUtils.isEmpty(vi.image))
                    vi.image = img.attr("src")
            }

        }

        private fun initVideo(a : Element?) : SimpleItem {
            val vi = SimpleItem()
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



        private fun parseDuration(parent : Element) : Int{
            val span = parent.getElementsByClass("video-time").first();
            if(span == null)
                return 0;
            var str = span.text()
            if(TextUtils.isEmpty(str))
                return 0;
            str = str.trim()
            val items = str.split(":")
            if(items.size < 1)
                return 0;
            var dur = 0
            if (items.size > 0)
                dur += Utils.strToInt(items[items.size - 1],0)
            if (items.size > 1)
                dur += Utils.strToInt(items[items.size - 2],0) * 60
            if (items.size > 2)
                dur += Utils.strToInt(items[items.size - 3],0) * 3600
            return dur
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
}