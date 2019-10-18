package com.jbak.videos

import java.util.concurrent.TimeUnit

interface Const {
    companion object {
        val ID = "[ID]"
        val YOUTUBE_KEY = "AIzaSyCbCAFtFBkid9FFh6t8ED75TFya6lnQdeY"
        val YOUTUBE_TOKEN = "492763130726-albe343h7103kpe7la3c2nfvpjv3gapt.apps.googleusercontent.com"
        var LIST_PAGE_SIZE = 50;
        var RELATED_PAGE_SIZE = 20;
        val MSG_AUTOHIDE = 10;
        val MSG_RESTORE_SCREEN = 12;
        val AUTOHIDE_MILLIS = TimeUnit.SECONDS.toMillis(5)


    }

}
