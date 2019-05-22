package com.jbak.videos.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.webkit.*
import android.widget.FrameLayout
import com.jbak.videos.R
import com.jbak.videos.types.IItem
import tenet.lib.base.MyLog

@SuppressLint("SetJavaScriptEnabled")
class WebPlayer (context: Context, attributeSet: AttributeSet?): FrameLayout(context,attributeSet){
    constructor(context: Context) : this(context, null)
    private val mWebView: WebView

    init {
        LayoutInflater.from(context).inflate(R.layout.web_player, this, true)
        mWebView = findViewById(R.id.webview)
        val ws = mWebView.settings;
        ws.javaScriptEnabled = true
        if(Build.VERSION.SDK_INT >= 21)
            ws.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        mWebView.webViewClient = object : WebViewClient(){

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
                if(Build.VERSION.SDK_INT >= 21)
                    MyLog.log("Load res: "+ request.url)
                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    fun playVideo(item : IItem.IUrlItem) {
        MyLog.log("Play Web Video: "+item.name+" url:"+item.videoUrl)
        mWebView.loadUrl(item.videoUrl)
    }

    fun getWebView() : WebView{
        return mWebView
    }

    fun stop() {
        mWebView.loadUrl("about:blank")
        //mWebView.onPause()
    }
}