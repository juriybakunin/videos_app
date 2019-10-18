package com.jbak.videos.providers

import com.jbak.videos.DataLoader
import com.jbak.videos.SerialLoader
import com.jbak.videos.types.VideosList
import com.jbak.videos.model.rutube.RutubeSearchResult
import com.jbak.videos.types.IItem
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import tenet.lib.base.Err

class RuTube : Factory.BaseVideoProvider(){

    override fun getType(): Factory.Type {
        return Factory.Type.RUTUBE
    }

    override fun createRelatedLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader {
        return object : DataLoader(onItemsLoaded){
            override fun loadDataSync(videosList: VideosList): Err {
                return Err.ERR_NOT_AVALIABLE;
            }

        }
    }

    override fun createSearchLoader(onItemsLoaded: DataLoader.OnItemsLoaded): DataLoader {
        return object : DataLoader(onItemsLoaded){
            override fun loadDataSync(videosList: VideosList): Err {
                if(!videosList.hasNextPage())
                    return Err.ERR_DATA_LOAD
                val page = when(videosList.isFirstPage()){
                    true->"1"
                    else->videosList.nextPageToken
                }
                val videos = API.searchVideos(videosList.query, page!!).execute()
                if(!videos.isSuccessful)
                    return Err.ERR_DATA_LOAD;

                videos.body()?.let {
                    videosList.addAll(it.results)
                    val pagInt = page.toInt() + 1
                    if(pagInt < it.num_pages)
                        videosList.nextPageToken =  pagInt.toString()
                    else
                        videosList.nextPageToken = null
                }
                return Err.OK
            }

        }
    }

    companion object {
        private val BASE_URL = "https://rutube.ru/api/";
        private val API = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(RutubeRetrofit::class.java)

        private interface RutubeRetrofit {
            @GET("search/video?format=json")
            fun searchVideos(@Query("query") query: String, @Query("page") page: String) : Call<RutubeSearchResult>
        }

    }

    override fun createSerialLoader(
        iItem: IItem,
        onItemsLoaded: DataLoader.OnItemsLoaded
    ): SerialLoader? {
        return null
    }

}