package com.acatapps.videomaker.modules.audio_manager_v3

import android.media.MediaPlayer
import android.net.Uri
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.data.MusicReturnData
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils
import java.io.File
import java.lang.Exception

class AudioManagerV3Impl : AudioManagerV3 {

    private var mAudioName = "none"
    private var mMediaPlayer= SimpleExoPlayer.Builder(VideoMakerApplication.getContext()).build().apply {
        repeatMode = Player.REPEAT_MODE_ALL
    }
    private var mLength = 0
    private var mStartOffset = 0
    private var mCurrentVolume = 1f
    private var mCurrentAudioData:MusicReturnData? = null
    private var mMusicPath = ""
    init {

        mMediaPlayer.addListener(object : Player.Listener {})


    }

    override fun getAudioName(): String = mAudioName

    override fun playAudio() {
        mMediaPlayer.playWhenReady = true
    }

    override fun pauseAudio() {
        mMediaPlayer.playWhenReady = false
    }

    override fun returnToDefault(currentTimeMs: Int) {

        mAudioName = "none"
        mMusicPath = ""
        mMediaPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(""))))
        mMediaPlayer.prepare()
    }

    override fun seekTo(currentTimeMs: Int) {
        try {
            mMediaPlayer.seekTo((currentTimeMs%mLength).toLong())
        } catch ( e:Exception) {
            mMediaPlayer.seekTo(0L)
        }

    }

    override fun repeat() {
        mMediaPlayer.seekTo(0)
    }

    override fun setVolume(volume: Float) {
        mCurrentVolume = volume
        mMediaPlayer.setVolume(volume)
    }

    override fun getVolume(): Float = mCurrentVolume

    override fun changeAudio(musicReturnData: MusicReturnData, currentTimeMs: Int) {
        val autoPlay = mMediaPlayer.isPlaying
        mCurrentAudioData = musicReturnData
        mAudioName = File(musicReturnData.audioFilePath).name
        mLength = musicReturnData.length
        mMusicPath = musicReturnData.outFilePath

        mMediaPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(musicReturnData.outFilePath))))
        mMediaPlayer.prepare()
        setVolume(mCurrentVolume)
        if(autoPlay) playAudio()
        else pauseAudio()
        seekTo(currentTimeMs)
    }
    override fun changeMusic(path:String) {
        val autoPlay = mMediaPlayer.isPlaying
        mMusicPath = path
        mLength = MediaUtils.getVideoDuration(path)

        mMusicPath = path
        mMediaPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(path))))
        mMediaPlayer.prepare()
        setVolume(mCurrentVolume)
        if(autoPlay) playAudio()
        else pauseAudio()
        seekTo(0)
    }
    override fun getOutMusicPath(): String = mMusicPath

    override fun getOutMusic(): MusicReturnData {
        return MusicReturnData(mMusicPath, "",mStartOffset, mLength)
    }

    override fun useDefault() {
        mMusicPath = FileUtils.defaultAudio
        mMediaPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(FileUtils.defaultAudio))))
        mMediaPlayer.prepare()
    }

}