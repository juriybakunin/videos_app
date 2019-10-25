package com.jbak.videos.playback;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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

    public static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
                //return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
                {
                    @Override public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }
          );
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
