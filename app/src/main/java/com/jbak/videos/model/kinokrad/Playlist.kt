package com.jbak.videos.model.kinokrad

data class PlaylistItem(val file:String, val comment: String)

class Playlist : ArrayList<PlaylistItem>()

data class PlayEntry (val playlist : Playlist)