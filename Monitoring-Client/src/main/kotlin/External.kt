
external class Media {

    companion object {

        var instance: Media

    }

    fun update(data: Any)

    fun setPlaying(playing: Boolean)

    fun setTitle(title: String)

    fun setAlbum(album: String)

    fun setThumbnail(url: String)

    fun setTime(currentTime: String, duration: String)

    fun setProgress(progress: String)

}
