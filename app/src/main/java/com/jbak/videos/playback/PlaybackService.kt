package com.jbak.videos.playback

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.jbak.videos.App
import com.jbak.videos.R
import com.jbak.videos.activity.MainActivity
import com.jbak.videos.types.IItem
import tenet.lib.tv.MediaEventListener


private val NOTIF_ID = 1
private val CHANNEL_ID = "MediaBackground"
private val CHANNEL_NAME = "Media Background"

class PlaybackNotification {
    val notificationManager : NotificationManager
    val mBuilder: NotificationCompat.Builder
    val mRemoteViews:RemoteViews
    init{
        val openIntent = Intent(App.get(),MainActivity::class.java)
            .setAction(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        val pi = PendingIntent.getActivity(App.get(), 10, openIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        mRemoteViews = RemoteViews(App.get().packageName,R.layout.notif_player_view);
        mRemoteViews.setOnClickPendingIntent(R.id.mPlayPause,PlayerReceiver.pendingIntent(PlayerReceiver.ACT_PLAY))
        mRemoteViews.setOnClickPendingIntent(R.id.mNext,PlayerReceiver.pendingIntent(PlayerReceiver.ACT_NEXT))
        mRemoteViews.setOnClickPendingIntent(R.id.mPrevious,PlayerReceiver.pendingIntent(PlayerReceiver.ACT_PREV))
        mRemoteViews.setOnClickPendingIntent(R.id.image,pi)
        notificationManager = App.get().getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        createChannel()

//        val style = androidx.media.app.NotificationCompat.MediaStyle()
//            .setShowActionsInCompactView()


        mBuilder = NotificationCompat.Builder(App.get(), CHANNEL_ID)
//            .setStyle(NotificationCompat.Style
//                .setMediaSession(App.get().audioSession)
//                .setShowActionsInCompactView())
            .setContent(mRemoteViews)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(App.str(R.string.app_name))



            //.setContentIntent(SendGpsActions.createPendingAction(App.get(), SendGpsActions.TYPE_RUN_MAIN))
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || notificationManager.getNotificationChannel(CHANNEL_ID) != null)
            return
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    fun update(): PlaybackNotification {
        notificationManager.notify(NOTIF_ID, mBuilder.build())
        return this
    }

}


class PlaybackService(
    val notif:PlaybackNotification = PlaybackNotification()
) : Service(), MediaEventListener, IChangeItem {

    override fun onCreate() {
        super.onCreate()
        startedService = this
        startForeground(NOTIF_ID,notif.mBuilder.build())
        App.PLAYER?.let {
            it.getPlayback().addMediaListener(this, true)
            it.getNextPrevious().addChangeItemListener(this,true)
            val item = it.getNextPrevious().getCurrentItem()
            if(item != null)
                onCurItemChange(item)

        }
    }

    override fun onCurItemChange(iItem: IItem) {
        notif.mRemoteViews
            .setTextViewText(R.id.text, iItem.name)
        notif.update()

    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    companion object {
        private var startedService:PlaybackService? = null
        fun stop() {
            if(startedService != null) {
                App.PLAYER?.let {
                    it.getPlayback().addMediaListener(startedService!!, false)
                    it.getNextPrevious().addChangeItemListener(startedService!!, false)
                }
            }
            startedService?.stopSelf()
            startedService = null
        }
        fun start(context: Context) {
            val intent = Intent(context, PlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= 26)
                context.startForegroundService(intent)
            else
                context.startService(intent)
        }

    }

    override fun onVideoEvent(event: Int, player: Any?, param1: Any?, param2: Any?) {
        val play = App.PLAYER?.getPlayback()?.isPlaying()?:false
        val img = if(play) R.drawable.ic_pause else R.drawable.ic_play
        notif.mRemoteViews.setImageViewResource(R.id.mPlayPause, img)
        notif.update()
    }

}
