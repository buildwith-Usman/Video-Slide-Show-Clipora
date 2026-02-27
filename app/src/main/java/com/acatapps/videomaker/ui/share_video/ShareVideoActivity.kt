package com.acatapps.videomaker.ui.share_video

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.view.View
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ExoPlayer
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.base.BaseActivity
import com.acatapps.videomaker.custom_view.VideoControllerView
import com.acatapps.videomaker.databinding.ActivityShareVideoBinding
import com.acatapps.videomaker.modules.rate.RatingManager
import com.acatapps.videomaker.modules.share.Share
import com.acatapps.videomaker.ui.HomeActivity
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils
import com.acatapps.videomaker.utils.Utils
import java.io.File

class ShareVideoActivity : BaseActivity() {
    private var mVideoPath = ""
    private var mTimer: CountDownTimer? = null
    var mTotalDuration = 0
    private lateinit var mShareBinding: ActivityShareVideoBinding

    companion object {
        fun gotoActivity(activity: Activity, videoPath: String, showRating:Boolean=false, fromProcess:Boolean = false) {
            val intent = Intent(activity, ShareVideoActivity::class.java)
            intent.putExtra("VideoPath", videoPath)
            intent.putExtra("ShowRating", showRating)
            intent.putExtra("fromProcess", fromProcess)
            activity.startActivity(intent)
        }
    }

    override fun getContentResId(): Int = R.layout.activity_share_video


    private fun showNativeAds() {
        val ad = VideoMakerApplication.instance.getNativeAds()
        if(ad != null) {
            Utils.bindBigNativeAds(ad, mShareBinding.nativeAdViewInShare.root)
        } else {
            mShareBinding.nativeAdViewInShare.root.visibility = View.GONE
        }
    }

    override fun initViews() {
        mShareBinding = ActivityShareVideoBinding.bind(binding.mainContentLayout.getChildAt(0))
        showNativeAds()

        val fromProcess = intent.getBooleanExtra("fromProcess", false)
        if(fromProcess) {
            VideoMakerApplication.instance.showAdsFull()
        }
        val videoPath = intent.getStringExtra("VideoPath")
        setScreenTitle(getString(R.string.share))

        videoPath?.let {

            mVideoPath = it
            try {
                mTotalDuration = MediaUtils.getVideoDuration(mVideoPath)
                mShareBinding.videoControllerView.setMaxDuration(mTotalDuration)
                initVideoPlayer(mVideoPath)
            } catch (e:Exception) {
                mTotalDuration = 1

            }

        }
        mShareBinding.videoControllerView.onChangeListener = object : VideoControllerView.OnChangeListener {
            override fun onUp(timeMilSec: Int) {

                mPlayer?.seekTo(timeMilSec.toLong())
            }

            override fun onMove(progress: Float) {


                mPlayer?.seekTo((mTotalDuration * progress / 100).toLong())
            }
        }

    }


    private fun listenVideoPosition() {
        mTimer = object : CountDownTimer(120000000, 100) {
            override fun onFinish() {
                this.start()
            }

            override fun onTick(millisUntilFinished: Long) {
                runOnUiThread {
                    mShareBinding.videoControllerView.setCurrentDuration(mPlayer?.contentPosition ?: -1L)
                }

            }
        }
    }
    val mShare = Share()
    override fun initActions() {
        setRightButton(R.drawable.ic_home_white) {
            val intent = Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("play-splash", false)
            }
            startActivity(intent)
        }
        mShareBinding.bgViewInShare.setOnClickListener {
            mPlayer?.playWhenReady = !(mPlayer?.playWhenReady ?: false)
        }

        mShareBinding.logoYouTube.setOnClickListener {
            mShare.shareTo(this, mVideoPath,Share.YOUTUBE_PACKAGE)
        }

        mShareBinding.logoInstagram.setOnClickListener {
            mShare.shareTo(this, mVideoPath,Share.INSTAGRAM_PACKAGE)
        }
        mShareBinding.logoFacebook.setOnClickListener {
            mShare.shareTo(this, mVideoPath,Share.FACEBOOK_PACKAGE)
        }

        mShareBinding.logoMore.setOnClickListener {
            shareVideoFile(mVideoPath)
        }

    }



    override fun onDestroy() {
        super.onDestroy()
        mTimer?.cancel()
        mPlayer?.release()
        mPlayer = null
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        onPauseVideo()
        mTimer?.cancel()
        mPlayer?.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        mTimer?.start()
    }

    private fun onPauseVideo() {
        mTimer?.cancel()
        mShareBinding.icPlay.visibility = View.VISIBLE
    }

    private fun onPlayVideo() {
        mTimer?.start()
        mShareBinding.icPlay.visibility = View.GONE
    }

    private var mPlayer: ExoPlayer? = null
    private fun initVideoPlayer(path:String) {
        mPlayer = ExoPlayer.Builder(VideoMakerApplication.getContext()).build()
        mShareBinding.exoPlayerView.player = mPlayer
        mPlayer?.playWhenReady = false
        mShareBinding.exoPlayerView.useController = false
        mPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        mPlayer?.addListener(object : Player.Listener {

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                onStateChange.invoke(playWhenReady)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if(playbackState == Player.STATE_ENDED) {
                    onEnd.invoke()
                }
            }
        })

        mPlayer?.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(path))))
        mPlayer?.prepare()
        listenVideoPosition()
        Thread{
            Thread.sleep(500)
            runOnUiThread {
                mPlayer?.playWhenReady = true
            }
        }.start()
    }


    private val onEnd = {
        mPlayer?.seekTo(0L)
        mPlayer?.playWhenReady = false
    }

    private val onStateChange = { isPlay:Boolean ->
        if(isPlay) {
            onPlayVideo()
        } else {
            onPauseVideo()
        }
    }

    override fun onBackPressed() {
        finish()
    }

}
