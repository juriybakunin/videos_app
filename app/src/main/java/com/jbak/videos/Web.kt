package com.jbak.videos

import com.jbak.videos.model.GoogleCompletions
import com.jbak.videos.types.VideosList
import com.jbak.videos.types.SearchItem
import com.jbak.videos.types.TypeSearchItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import tenet.lib.base.Err
import tenet.lib.base.MyLog

class Web{

    companion object {
        private val COMPLETE_BASE_URL = "https://suggestqueries.google.com/complete/"
        val COMPLETIONS = Retrofit.Builder().baseUrl(COMPLETE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GoogleAutocomple::class.java);
        interface GoogleAutocomple{
            @GET("search?client=firefox&ds=yt")
            fun loadGoogleCompletions(@Query("q") query: String) : Call<GoogleCompletions>
        }

        fun createCompleteLoader(onItemsLoaded: DataLoader.OnItemsLoaded) : DataLoader{
            return object : DataLoader(onItemsLoaded){
                override fun loadDataSync(videosList: VideosList): Err {
                    if(videosList.query.length > 2) {
                        val resp = COMPLETIONS.loadGoogleCompletions(videosList.query).execute()
                        if (resp.isSuccessful && resp.body() != null) {
                            val results = resp.body()!!.getStrings()
                            for (r in results)
                                videosList.add(SearchItem(r))
                        }
                    }
                    val c = Db.get().getSearches(videosList.query)
                    c?.let {
                        if (it.moveToFirst()) {
                            do {
                                val query = c.getString(c.getColumnIndex(QUERY))
                                videosList.add(SearchItem(query, TypeSearchItem.DB))
                            } while (it.moveToNext())
                        }
                        c.close()
                    }
                    return Err.OK
                }

            }
        }
    }

    abstract class OnLoad<T> : Callback<T> {
        abstract fun onLoad(call: Call<T>, response: Response<T>?, throwable: Throwable?)
        protected fun onLoadEnd(call: Call<T>, response: Response<T>?, throwable: Throwable?){
            MyLog.log("Url loaded: "+call.request().toString())
            throwable?.let { MyLog.err(it) }
            response?.let { MyLog.log(" Responce: "+it.toString()) }
            onLoad(call, response, throwable)
        }
        override fun onResponse(call: Call<T>, response: Response<T>) {
            onLoadEnd(call,response, null)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            onLoadEnd(call,null, t)
        }
    }
}