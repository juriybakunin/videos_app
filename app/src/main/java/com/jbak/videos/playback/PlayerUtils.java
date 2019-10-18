package com.jbak.videos.playback;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

public class PlayerUtils {
    public static FrameLayout.LayoutParams getProportionalLayoutParams(
            View playerView, boolean margins, double videoWidth, double videoHeight) {
        FrameLayout parent = (FrameLayout) playerView.getParent();
        parent.setClipChildren(margins);
        double width = parent.getWidth();
        double height = parent.getHeight();
        double multiW,multiH;
        if(width == 0 ||height == 0 ) {
            multiW = multiH = 1f;
        } else {
            double koeffW = videoWidth / width;
            double koeffH = videoHeight / height;
            if(margins) {
                if (koeffW > koeffH) {
                    multiW = 1f;
                    multiH = koeffH / koeffW;
                } else {
                    multiH = 1f;
                    multiW = koeffW / koeffH;
                }
            } else {
                if (koeffW > koeffH) {
                    multiW = koeffW/koeffH;
                    multiH = 1f;
                } else {
                    multiW = 1f;
                    multiH = koeffH/koeffW;
                }

            }
        }
        FrameLayout.LayoutParams flParam = (FrameLayout.LayoutParams) playerView.getLayoutParams();
        flParam.gravity = Gravity.CENTER;
        flParam.width = (int)(width*multiW);
        flParam.height = (int)(height*multiH);
        return flParam;
    }

}
