package tenet.lib.tv;

import android.media.MediaPlayer;

import tenet.lib.base.MyLog;
import tenet.lib.base.utils.Listeners;
import tenet.lib.base.utils.Utils;


/** Обработчик событий Media */
public interface MediaEventListener {

    /** Событие окончания подготовки  media*/
    int EVENT_MEDIA_PREPARE = 0x1;

    /** Событие ошибки загрузки или воспроизведения */
    int EVENT_MEDIA_ERROR =   0x2;

    /** Событие изменения размера видео */
    int EVENT_MEDIA_SIZE_CHANGED = 0x4;

    /** Событие получения различной информации */
    int EVENT_MEDIA_INFO = 0x8;

    /** Событие загрузки каких-либо данных, перед установкой media.
     *  Это событие должно отправляться вручную
     */
    int EVENT_MEDIA_START_LOAD = 0x10;

    /** Событие установки данных media в MediaPlayer */
    int EVENT_MEDIA_SET_DATASOURCE = 0x20;

    /** Событие старта буфферизации */
    int EVENT_MEDIA_BUFFERING_START = 0x40;

    /** Событие старта буфферизации */
    int EVENT_MEDIA_BUFFERING_END = 0x80;

    /** Событие старта буфферизации */
    int EVENT_MEDIA_COMPLETED = 0x100;

    /** Событие старта буфферизации */
    int EVENT_MEDIA_STARTED = 0x200;

    int EVENT_MEDIA_PAUSED = 0x400;
    int EVENT_MEDIA_PLAYED = 0x200;

    /** Событие изменения настроек видео */
    int EVENT_MEDIA_SETTINGS_CHANGED = 0x400;

    int ERROR_LOADING = -1024;


    void onVideoEvent(int event, Object player, Object param1, Object param2);

    /** Массив слушателей {@link MediaEventListener},
     *  который можно приаттачить к MediaPlayer и
     *  передавать его события всем слушателям
     */
    class MediaPlayerListeners extends Listeners<MediaEventListener>
        implements
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener,
            MediaPlayer.OnCompletionListener,
            MediaPlayer.OnInfoListener,
            MediaPlayer.OnBufferingUpdateListener
    {
        private boolean mBufferingStarted = false;
        /** Устанавливает на MediaPlayer все доступные обработчики */
        public void setToMediaPlayer(MediaPlayer player) {
            setToMediaPlayer(player,0xffffffff);
        }

        /** Устанавливает на MediaPlayer обработчики по маске
         * @param player исходный {@link MediaPlayer}
         * @param eventMask Маска - набор констант EVENT_
         */
        public void setToMediaPlayer(MediaPlayer player,int eventMask) {
            if(Utils.intHas(eventMask, EVENT_MEDIA_ERROR))
                player.setOnErrorListener(this);
            if(Utils.intHas(eventMask, EVENT_MEDIA_PREPARE))
                player.setOnPreparedListener(this);
            if(Utils.intHas(eventMask, EVENT_MEDIA_SIZE_CHANGED))
                player.setOnVideoSizeChangedListener(this);
            if(Utils.intHas(eventMask, EVENT_MEDIA_INFO))
                player.setOnInfoListener(this);
            if(Utils.intHas(eventMask, EVENT_MEDIA_COMPLETED))
                player.setOnCompletionListener(this);
            if(Utils.intHas(eventMask, EVENT_MEDIA_BUFFERING_START))
                player.setOnBufferingUpdateListener(this);
        }

        public void notifyListeners(int event,Object mediaPlayer,Object param1,Object param2){
            for (MediaEventListener listener:mList)
                listener.onVideoEvent(event,mediaPlayer,param1,param2);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            notifyListeners(EVENT_MEDIA_ERROR, mp, what, extra);
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            notifyListeners(EVENT_MEDIA_PREPARE, mp, null, null);
        }

        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            notifyListeners(EVENT_MEDIA_SIZE_CHANGED, mp, width, height);
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            notifyListeners(EVENT_MEDIA_INFO, mp, what, extra);
            int infEvent = Func.getInfoEvent(what,extra);
            if(infEvent != 0) {
                if(infEvent == EVENT_MEDIA_BUFFERING_END) {
                    mBufferingStarted = false;
                }
                if(infEvent == EVENT_MEDIA_BUFFERING_START) {
                    mBufferingStarted = true;
                    notifyListeners(infEvent, mp, 0, null);
                } else {
                    notifyListeners(infEvent, mp, null, null);
                }
            }
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            notifyListeners(EVENT_MEDIA_COMPLETED,mp,null,null);
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if(!mBufferingStarted)
                return;
            MyLog.log("Buffering upd: "+percent);
            if(percent == 100) {
                notifyListeners(EVENT_MEDIA_BUFFERING_END, mp, percent, null);
            } else  {
                notifyListeners(EVENT_MEDIA_BUFFERING_START, mp, percent, null);
            }
        }
    }

    class Func{

        public static int getInfoEvent(int what,int extra) {
            if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START)
                return EVENT_MEDIA_BUFFERING_START;
            else if(what == MediaPlayer.MEDIA_INFO_BUFFERING_END)
                return EVENT_MEDIA_BUFFERING_END;
            else if(what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)
                return EVENT_MEDIA_STARTED;


            return 0;
        }

        public static boolean isErrorNetwork(int event, Object param1, Object param2){
            int code = (int)param1, extra = (int) param2;
            return event == EVENT_MEDIA_ERROR && code == 1 && extra == MediaPlayer.MEDIA_ERROR_IO;
        }

        public static String getEventDescript(int event, Object param1, Object param2){
            if(event == EVENT_MEDIA_ERROR) {
                if(isErrorNetwork(event,param1,param2)) {
                    return "Video error: network error, check link";
                } else {
                    return "Video error:" + param1 + ", " + param2;
                }
            }
            if(event == EVENT_MEDIA_SIZE_CHANGED)
                return "Video size:" + param1+"x"+param2;
            if(event == EVENT_MEDIA_PREPARE)
                return "Video prepared";
            if(event == EVENT_MEDIA_INFO) {
                int code = (int)param1;
                switch (code) {
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        return "Video started";
                    case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        return "Video not seekable";
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        return "Buffering start";
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        return "Buffering end";
                    case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                        return "Video bad interleaving";
                    case MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING:
                        return "Video audio not playing";
                    default:
                        return "Video info:" + param1 + ", " + param2;
                }
            }
            return null;
        }



        public static void logMediaEvent(int event, Object param1, Object param2){
            String log = getEventDescript(event,param1,param2);
            if(log != null)
                MyLog.log(log);
        }
    }

}
