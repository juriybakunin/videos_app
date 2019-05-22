package com.jbak.videos.providers

import com.jbak.videos.DataLoader
import com.jbak.videos.types.IItem
import com.jbak.videos.types.SimpleItem
import com.jbak.videos.types.VideosList
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tenet.lib.base.Err

class HdRezka : Factory.VideoProvider {
    companion object {
        val SEARCH_URL = "http://hdrezka.ag/index.php?do=search&subaction=search"
        val SEARCH_ITEM_CLASS = "b-content__inline_item"

        class HDRezkaItem : SimpleItem(), IItem.IUrlItem {
            override fun getVideoUrl(): String {
                return id
            }

        }

        fun parseElement(el : Element) : IItem {
            val item = HDRezkaItem()
            val cover = el.getElementsByClass("b-content__inline_item-cover").first()
            cover?.let {
                val img = it.getElementsByTag("img").first()
                item.image = img?.attr("src")?:""
            }
            val link = el.getElementsByClass("b-content__inline_item-link").first()
            link?.let {
                val a = it.getElementsByTag("a").first()
                a?.let {
                    item.setIdAndName(it.attr("href"), it.text())
                }
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

    override fun createRelatedLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getType(): Factory.Type {
        return Factory.Type.HDREZKA
    }
}