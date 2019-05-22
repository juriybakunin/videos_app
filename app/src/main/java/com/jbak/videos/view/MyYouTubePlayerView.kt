package com.jbak.videos.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.google.android.youtube.player.YouTubePlayer
import com.jbak.videos.*
import com.jbak.videos.providers.Factory
import com.jbak.videos.types.IItem

class MyYouTubePlayerView : FrameLayout {

    var youTubePlayer : YouTubePlayer? = null
    lateinit var stateChangeListener: YouTubePlayer.PlayerStateChangeListener
    var controllerDialog : ControllerDialog? = null
    var dataLoader : DataLoader? = null
    lateinit var mProvider : Factory.VideoProvider


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context,attributeSet: AttributeSet) : super(context, attributeSet){
        init()
    }

    private fun init(){
        mProvider = Factory.createProvider(Factory.Type.YOTUBE)
        stateChangeListener = object : YouTubePlayer.PlayerStateChangeListener{
            override fun onAdStarted() {

            }

            override fun onLoading() {

            }

            override fun onVideoStarted() {
                controllerDialog?.updateController()
            }

            override fun onLoaded(videoId: String?) {
                videoId?.run {
                    controllerDialog?.let {
                        it.onVideoLoaded(videoId)
                        dataLoader?.run {
                            this.cancelJob()
                        }
                        dataLoader = mProvider.createRelatedLoader(it)
                        dataLoader!!.loadSearch(videoId)

                    }
                }
            }

            override fun onVideoEnded() {

            }

            override fun onError(p0: YouTubePlayer.ErrorReason?) {

            }

        }
        LayoutInflater.from(context).inflate(R.layout.youtube_player_layout,this,true)
    }

    private var topView: View? = null

    fun setYouTubePlayer(youTubePlayer: YouTubePlayer, myYouTubeFragment: MyYouTubeFragment){
        this.youTubePlayer = youTubePlayer
        youTubePlayer.setShowFullscreenButton(false)
        youTubePlayer.setPlayerStateChangeListener(stateChangeListener)
        this.topView = myYouTubeFragment.view
        this.topView?.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                controllerDialog?.show()
            }

        })
        controllerDialog?.run {
            youTubePlayer.setPlaybackEventListener(this)
            iPlayback = IPlayback.createYoutubeCallback(youTubePlayer)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        controllerDialog?.run {
            if(isHidden){
                show()
                return true;
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun playItem(iItem: IItem, items: IItem.IItemList?) {
        controllerDialog?.onOpenVideo(iItem,items)
        youTubePlayer?.run {
            loadVideo(iItem.id)
            if(Const.USE_CONTROLLER_DLG)
                setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS)
        }
    }

}