package com.jbak.videos

import android.net.Uri
import android.os.Looper
import android.webkit.*
import com.jbak.videos.types.IItem
import com.jbak.videos.types.Media
import com.jbak.videos.types.SerialList
import com.jbak.videos.view.VideoPlayer
import kotlinx.android.synthetic.main.video_player_layout.view.*
import okhttp3.Response
import tenet.lib.base.MyLog
import tenet.lib.base.utils.Utils
import java.io.ByteArrayInputStream
import java.io.IOException
import java.lang.Exception

private val ABOUT_BLANK = "about:blank";
class VideoUrlInterceptor (videoPlayer : VideoPlayer){
    companion object {
        public val MOZILLA_CLIENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
        public val OLD_WEBVIEW_AGENT = "Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/KLP) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30"
    }
    val videoPlayer: VideoPlayer
    val mWebView: WebView
    private var mPlayItem: IItem.IResourceIntercept? = null
    @Volatile private var mIterceptResource = true;
    private var mLoadUrl : String? = null;
    val defaultUserAgent : String;

    init {
        this.videoPlayer = videoPlayer
        WebView.setWebContentsDebuggingEnabled(true)
        mWebView = videoPlayer.mWebView
        val ws = mWebView.settings;
        ws.loadsImagesAutomatically = false
        ws.blockNetworkImage = true
        ws.javaScriptEnabled = true
        ws.mediaPlaybackRequiresUserGesture = false
        defaultUserAgent = ws.userAgentString

        ws.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        mWebView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if(ABOUT_BLANK.equals(url))
                    return
                mPlayItem?.onWebViewEvent(IItem.LOAD_EVENT_PAGE_FINISHED, url, this@VideoUrlInterceptor)
                MyLog.log("Page finished: $url")
                //setLoad(false)
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
                return checkRequestUrl(request.url, request);
            }

        }
    }

    fun loadUrl(url:String, interceptResources:Boolean = true){
        if(Looper.myLooper() == Looper.getMainLooper()) {
            MyLog.log("LOAD WEBVIEW URL: $url")
            mIterceptResource = interceptResources
            mLoadUrl = url
            mWebView.loadUrl(url)
        } else {
            mWebView.post(object : Runnable{
                override fun run() {
                    loadUrl(url, interceptResources)
                }

            })
        }
    }


    private var mAccDenied : WebResourceResponse? = null;
    fun accessDenied():WebResourceResponse {
        if(mAccDenied != null)
            return mAccDenied!!
        try {
            val str = "Access Denied"
            val data = ByteArrayInputStream(str.toByteArray(charset("UTF-8")))
            mAccDenied = WebResourceResponse("text/css", "UTF-8", data)
        } catch (e: IOException) {
        }
        return mAccDenied!!
    }


    fun checkRequestUrl(uri: Uri, request: WebResourceRequest) : WebResourceResponse?{
        var resourceResponse : WebResourceResponse? = null
        val urlStr = uri.toString();
        var resultStr = ""
        if(urlStr.equals(mLoadUrl)){
            resultStr = "Same url";
        } else if(mIterceptResource == true) {
            val result = mPlayItem?.interceptResource(uri, request, this)
            if (result == IItem.INTERCEPTED) {
                resultStr = "Intercepted int="+mIterceptResource
                mIterceptResource = false
//                stopLoad()
                resourceResponse = accessDenied()
            } else if(result == IItem.BLOCK_ONCE) {
                resourceResponse = accessDenied()
                resultStr = "Block"
            }
        }
        if(resultStr.isEmpty() && isAd(uri)) {
            resultStr = "Ad blocked"
            resourceResponse =  accessDenied()
        }
        MyLog.log("RES: $resultStr $urlStr")
        return resourceResponse
    }

    fun isAd(uri: Uri) : Boolean {
        if("cdn.irsdn.net".equals(uri.host))
            return true;
        if("cecidio.com".equals(uri.host))
            return true;
        return false;
    }

    fun loadUrlItem(item : IItem.IResourceIntercept) {
        mPlayItem = item;
        mWebView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        item.onWebViewEvent(IItem.LOAD_EVENT_START,"",this)
    }

    fun cancel(){
        if(Utils.isUIThread()){
            loadEmpty()
        } else {
            mWebView.post { cancel() }
        }
    }

    fun loadEmpty(){
        loadUrl(ABOUT_BLANK,false)
    }
    fun videoMediaLoaded(media: Media) {
        if(Utils.isUIThread()) {
            loadEmpty()
            videoPlayer.playMedia(media)
        } else {
            mWebView.post {videoMediaLoaded(media)}
        }

    }
    fun videoUrlLoaded(url: String) {
        videoMediaLoaded(Media(url))
    }

    fun videoLoadError(response: Response?, e: Exception?) {
        var err = App.str(R.string.err_load_url)
        if(e != null){
            err = "$err: ${e.toString()}"
        } else if(response != null) {
            err = "$err: ${response.code()} ${response.message()}"
        }
        videoLoadError(err)

    }

    fun videoLoadError(error: String) {
        if(Utils.isUIThread()) {
            loadEmpty()
            videoPlayer.setError(error)
        } else {
            mWebView.post({videoLoadError(error)})
        }
    }

    fun setSerial(serial: SerialList) {
        videoPlayer.setSerial(serial)
    }

}