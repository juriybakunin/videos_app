package com.jbak.videos.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.jbak.setVisGone
import com.jbak.videos.R
import com.jbak.videos.types.IItem
import kotlinx.android.synthetic.main.activity_webview.*
import tenet.lib.base.MyLog

class WebViewActivity : AppCompatActivity() {


    private var mStartUrl: String? = null

    companion object {
        fun start(context: Context, url:String){
            val intent = Intent(context, WebViewActivity::class.java)
                .putExtra(IItem.URL, url)
            context.startActivity(intent)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        setSupportActionBar(customToolbar)
        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_back)
        }
        mStartUrl = intent.getStringExtra(IItem.URL)
        mWebView.settings.javaScriptEnabled = true
        mWebView.webViewClient = createWebViewClient()
        mWebView.webChromeClient = createWebChromeClient()
        mStartUrl?.let {
            title = it
            mWebView.loadUrl(it)
        }
    }

    fun createWebChromeClient() : WebChromeClient {
        return object : WebChromeClient(){
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                mWebView.post {
                    this@WebViewActivity.title = title
                }
            }
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                mProgress.progress = if(newProgress < 5) 5 else newProgress
                mProgress.setVisGone(newProgress < 100)
            }
        }
    }
    fun createWebViewClient() : WebViewClient {
        return object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                MyLog.log("RESOURCE: ${request.url}")
                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            if(mWebView.canGoBack()) {
                mWebView.goBack()
            } else {
                onBackPressed()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}