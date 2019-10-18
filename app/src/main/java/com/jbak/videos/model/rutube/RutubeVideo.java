
package com.jbak.videos.model.rutube;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebResourceRequest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.jbak.videos.types.IItem;
import org.jetbrains.annotations.NotNull;
import tenet.lib.base.MyLog;
import tenet.lib.base.utils.NetUtils;
import tenet.lib.base.utils.TimeUtils;

import java.util.List;

public class RutubeVideo implements IItem.IUrlItem, IItem.IVideoUrlLoader {
    private static final String URL_OPT = "https://rutube.ru/api/play/options/"+ID+"/?format=json&no_404=true";
    public Boolean has_high_quality;
    public String commentEditors;
    public String embed_url;
    public String description;
    public String video_url;
    public Integer views;
    public List<Object> hashtags = null;
    public Boolean isAdult;
    public Integer duration;
    public String thumbnail_url;
    public String feedName;
    public List<Object> allTags = null;
    public Boolean is_livestream;
    public Boolean isOfficial;
    public String publication_ts;
    public String id;
    public Category category;
    public Integer hits;
    public String feedUrl;
    public String title;
    public String picture_url;
    public PgRating pg_rating;
    public Integer trackId;
    public String html;
    public Integer commentsCount;
    public Author author;
    public String short_description;
    public String createdTs;
    public String lastUpdateTs;
    public Boolean isSpatial;
    public Integer originType;

    @NotNull
    @Override
    public String getImageUrl() {
        return thumbnail_url;
    }

    @Override
    public String getShortDescText() {
        return duration>0? TimeUtils.getTimeRangeText(duration, true, null).toString() : "";
    }

    @Override
    public CharSequence getName() {
        return title;
    }

    @Override
    public String getId() {
        return id;
    }


    @Override
    public String loadVideoUrlSync() throws Throwable {
        Uri uri = Uri.parse(embed_url);
        List<String> pathSeg = uri.getPathSegments();
        String eid = pathSeg.get(pathSeg.size()-1);
        String url = URL_OPT.replace(ID,eid);
        MyLog.log("Embed url: "+url);
        String data = NetUtils.loadUrl(url);
        EmbedInfo ei = new Gson().fromJson(data, EmbedInfo.class);
        return ei.getVideoUrl();
    }
}
