package com.jbak.videos.model.rutube

data class VideoBalancer(val default:String, val m3u8 : String)

data class EmbedInfo(var video_balancer: VideoBalancer? = null) {
    fun getVideoUrl() : String {
        return video_balancer?.m3u8?:""
    }
}