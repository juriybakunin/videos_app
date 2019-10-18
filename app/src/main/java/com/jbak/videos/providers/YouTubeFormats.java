package com.jbak.videos.providers;

import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import tenet.lib.base.Interfaces;
import tenet.lib.base.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public interface YouTubeFormats {
    String TEST_HOST = "http://y.com/p?";

    enum Container{
        Flv,Tgpp, Mp4, WebM, M4A
    }
    enum VideoEncoding {
        H263, Mp4V, Vp8,Vp9,H264;
    }

    enum AudioEncoding{
        Aac, Vorbis, Opus, Mp3
    }
    enum VideoQuality {
        Low144, Low240, Medium360, Medium480, High720, High1080, High1440, High3072, High4320, High2160
    }

    class MediaFormat {
        public final Container container;
        private final VideoQuality quality;
        private final AudioEncoding audio;
        private final VideoEncoding video;
        public final int itag;

        public MediaFormat( int itag,
                            @NonNull Container container,
                           @Nullable AudioEncoding audioEncoding,
                           @Nullable VideoEncoding videoEncoding,
                           @Nullable VideoQuality videoQuality){
            this.itag = itag;
            this.container = container;
            this.audio = audioEncoding;
            this.video = videoEncoding;
            this.quality = videoQuality;
        }

        public boolean hasAudio(){
            return audio != null;
        }

        public boolean hasVideo(){
            return video != null && quality != null;
        }

    }

    final class Table extends HashMap<Integer, MediaFormat> {
        private Table(){
            // Muxed
            put(5, new MediaFormat(5,Container.Flv, AudioEncoding.Mp3, VideoEncoding.H263, VideoQuality.Low144));
            put(6, new MediaFormat(6,Container.Flv, AudioEncoding.Mp3, VideoEncoding.H263, VideoQuality.Low240));
            put(13, new MediaFormat(13,Container.Tgpp, AudioEncoding.Aac, VideoEncoding.Mp4V, VideoQuality.Low144));
            put(17, new MediaFormat(17,Container.Tgpp, AudioEncoding.Aac, VideoEncoding.Mp4V, VideoQuality.Low144));
            put(18, new MediaFormat(18,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Medium360));
            put(22, new MediaFormat(22,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.High720));
            put(34, new MediaFormat(34,Container.Flv, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Medium360));
            put(35, new MediaFormat(35,Container.Flv, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Medium480));
            put(36, new MediaFormat(36,Container.Tgpp, AudioEncoding.Aac, VideoEncoding.Mp4V, VideoQuality.Low240));
            put(37, new MediaFormat(37,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.High1080));
            put(38, new MediaFormat(38,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.High3072));
            put(43, new MediaFormat(43,Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.Medium360));
            put(44, new MediaFormat(44,Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.Medium480));
            put(45, new MediaFormat(45,Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.High720));
            put(46, new MediaFormat(46,Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.High1080));
            put(59, new MediaFormat(59,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Medium480));
            put(78, new MediaFormat(78,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Medium480));
            put(82, new MediaFormat(82,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Medium360));
            put(83, new MediaFormat(83,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Medium480));
            put(84, new MediaFormat(84,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.High720));
            put(85, new MediaFormat(85,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.High1080));
            put(91, new MediaFormat(91,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Low144));
            put(92, new MediaFormat(92,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Low240));
            put(93, new MediaFormat(93,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Medium360));
            put(94, new MediaFormat(94,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Medium480));
            put(95, new MediaFormat(95,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.High720));
            put(96, new MediaFormat(96,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.High1080));
            put(100, new MediaFormat(100,Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.Medium360));
            put(101, new MediaFormat(101,Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.Medium480));
            put(102, new MediaFormat(102,Container.WebM, AudioEncoding.Vorbis, VideoEncoding.Vp8, VideoQuality.High720));
            put(132, new MediaFormat(132,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Low240));
            put(151, new MediaFormat(151,Container.Mp4, AudioEncoding.Aac, VideoEncoding.H264, VideoQuality.Low144));

            // Video-only (mp4)
            put(133, new MediaFormat(133,Container.Mp4, null, VideoEncoding.H264, VideoQuality.Low240));
            put(134, new MediaFormat(134,Container.Mp4, null, VideoEncoding.H264, VideoQuality.Medium360));
            put(135, new MediaFormat(135,Container.Mp4, null, VideoEncoding.H264, VideoQuality.Medium480));
            put(136, new MediaFormat(136,Container.Mp4, null, VideoEncoding.H264, VideoQuality.High720));
            put(137, new MediaFormat(137,Container.Mp4, null, VideoEncoding.H264, VideoQuality.High1080));
            put(138, new MediaFormat(138,Container.Mp4, null, VideoEncoding.H264, VideoQuality.High4320));
            put(160, new MediaFormat(160,Container.Mp4, null, VideoEncoding.H264, VideoQuality.Low144));
            put(212, new MediaFormat(212,Container.Mp4, null, VideoEncoding.H264, VideoQuality.Medium480));
            put(213, new MediaFormat(213,Container.Mp4, null, VideoEncoding.H264, VideoQuality.Medium480));
            put(214, new MediaFormat(214,Container.Mp4, null, VideoEncoding.H264, VideoQuality.High720));
            put(215, new MediaFormat(215,Container.Mp4, null, VideoEncoding.H264, VideoQuality.High720));
            put(216, new MediaFormat(216,Container.Mp4, null, VideoEncoding.H264, VideoQuality.High1080));
            put(217, new MediaFormat(217,Container.Mp4, null, VideoEncoding.H264, VideoQuality.High1080));
            put(264, new MediaFormat(264,Container.Mp4, null, VideoEncoding.H264, VideoQuality.High1440));
            put(266, new MediaFormat(266,Container.Mp4, null, VideoEncoding.H264, VideoQuality.High2160));
            put(298, new MediaFormat(298,Container.Mp4, null, VideoEncoding.H264, VideoQuality.High720));
            put(299, new MediaFormat(299,Container.Mp4, null, VideoEncoding.H264, VideoQuality.High1080));

            // Video-only (webm)
            put(167, new MediaFormat(167,Container.WebM, null, VideoEncoding.Vp8, VideoQuality.Medium360));
            put(168, new MediaFormat(168,Container.WebM, null, VideoEncoding.Vp8, VideoQuality.Medium480));
            put(169, new MediaFormat(169,Container.WebM, null, VideoEncoding.Vp8, VideoQuality.High720));
            put(170, new MediaFormat(170,Container.WebM, null, VideoEncoding.Vp8, VideoQuality.High1080));
            put(218, new MediaFormat(218,Container.WebM, null, VideoEncoding.Vp8, VideoQuality.Medium480));
            put(219, new MediaFormat(219,Container.WebM, null, VideoEncoding.Vp8, VideoQuality.Medium480));
            put(242, new MediaFormat(242,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.Low240));
            put(243, new MediaFormat(243,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.Medium360));
            put(244, new MediaFormat(244,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.Medium480));
            put(245, new MediaFormat(245,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.Medium480));
            put(246, new MediaFormat(246,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.Medium480));
            put(247, new MediaFormat(247,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High720));
            put(248, new MediaFormat(248,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High1080));
            put(271, new MediaFormat(271,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High1440));
            put(272, new MediaFormat(272,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High2160));
            put(278, new MediaFormat(278,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.Low144));
            put(302, new MediaFormat(302,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High720));
            put(303, new MediaFormat(303,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High1080));
            put(308, new MediaFormat(308,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High1440));
            put(313, new MediaFormat(313,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High2160));
            put(315, new MediaFormat(315,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High2160));
            put(330, new MediaFormat(330,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.Low144));
            put(331, new MediaFormat(331,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.Low240));
            put(332, new MediaFormat(332,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.Medium360));
            put(333, new MediaFormat(333,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.Medium480));
            put(334, new MediaFormat(334,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High720));
            put(335, new MediaFormat(335,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High1080));
            put(336, new MediaFormat(336,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High1440));
            put(337, new MediaFormat(337,Container.WebM, null, VideoEncoding.Vp9, VideoQuality.High2160));

            // Audio-only (mp4)
            put(139, new MediaFormat(139,Container.M4A, AudioEncoding.Aac, null, null));
            put(140, new MediaFormat(140,Container.M4A, AudioEncoding.Aac, null, null));
            put(141, new MediaFormat(141,Container.M4A, AudioEncoding.Aac, null, null));
            put(256, new MediaFormat(256,Container.M4A, AudioEncoding.Aac, null, null));
            put(258, new MediaFormat(258,Container.M4A, AudioEncoding.Aac, null, null));
            put(325, new MediaFormat(325,Container.M4A, AudioEncoding.Aac, null, null));
            put(328, new MediaFormat(328,Container.M4A, AudioEncoding.Aac, null, null));

            // Audio-only (webm)
            put(171, new MediaFormat(171,Container.WebM, AudioEncoding.Vorbis, null, null));
            put(172, new MediaFormat(172,Container.WebM, AudioEncoding.Vorbis, null, null));
            put(249, new MediaFormat(249,Container.WebM, AudioEncoding.Opus, null, null));
            put(250, new MediaFormat(250,Container.WebM, AudioEncoding.Opus, null, null));
            put(251, new MediaFormat(251,Container.WebM, AudioEncoding.Opus, null, null));
        }

        public boolean hasAudio(int itag){
            MediaFormat d = get(itag);
            return d != null && d.hasAudio();
        }
    }
    Table TABLE = new Table();

    class UrlInfo implements Interfaces.IdNamed {

        UrlInfo(MediaFormat f, String url, String s){
            this.fmt = f;
            this.url = url;
            this.sig = s;
        }
        public String url;
        public MediaFormat fmt;
        public String sig;

        @NonNull
        @Override
        public String toString() {
            String s = TextUtils.isEmpty(sig)? "":"sig ";
            return s + fmt.itag +" "+url;
        }

        @Override
        public CharSequence getName() {
            return url;
        }
        @Override
        public String getId() {
            return url;
        }
    }

    class MediaUrls extends ArrayList<UrlInfo> {
        public MediaUrls fromData(String dinfo){
            Uri infUri = Uri.parse(dinfo);
            String fmt_map = infUri.getQueryParameter("url_encoded_fmt_stream_map");
            fromFormat(fmt_map);
//            String adaptive_fmts = infUri.getQueryParameter("adaptive_fmts");
//            fromFormat(adaptive_fmts);
            return this;
        }
        public MediaUrls fromFormat(String format){
            if(TextUtils.isEmpty(format))
                return this;
            String urls[] = format.split(",");
            for (String u : urls){
                UrlInfo ui = getInfo(u);
                if(ui != null)
                    add(ui);
            }
            return this;
        }
        UrlInfo getInfo(String params){
            Uri uri = Uri.parse(TEST_HOST + params);
            String itag = uri.getQueryParameter("itag");
            String url = uri.getQueryParameter("url");
            String sig = uri.getQueryParameter("s");
            int t = Utils.strToInt(itag, 0);
            MediaFormat mf = TABLE.get(t);
            if(mf!=null && !TextUtils.isEmpty(url)){
                return new UrlInfo(mf,url,sig);
            } else {
                return null;
            }
        }
    }

}
