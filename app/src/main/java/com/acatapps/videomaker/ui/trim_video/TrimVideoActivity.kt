package com.acatapps.videomaker.ui.trim_video

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.view.View
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.base.BaseActivity
import com.acatapps.videomaker.custom_view.CropVideoTimeView
import com.acatapps.videomaker.custom_view.VideoControllerView
import com.acatapps.videomaker.databinding.ActivityTrimVideoBinding
import com.acatapps.videomaker.ui.process_video.ProcessVideoActivity
import com.acatapps.videomaker.utils.DimenUtils
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils
import java.io.File
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class TrimVideoActivity : BaseActivity(), MediaPlayer.OnCompletionListener {

    override fun getContentResId(): Int = R.layout.activity_trim_video

    private lateinit var mTrimBinding: ActivityTrimVideoBinding
    private var mTimer:CountDownTimer? = null

    var mTotalDuration = 0

    private var mStartOffset = 0
    private var mEndOffset = 0
    private var mVideoPath = ""
    companion object {
        fun gotoActivity(activity:Activity, videoPath:String) {
            val intent = Intent(activity, TrimVideoActivity::class.java)
            intent.putExtra("VideoPath", videoPath)
            activity.startActivity(intent)
        }
    }

    var trimAvailable = true
    override fun isShowAds(): Boolean = true
    override fun initViews() {
        mTrimBinding = ActivityTrimVideoBinding.bind(binding.mainContentLayout.getChildAt(0))
        val scale = DimenUtils.videoScaleInTrim()
        Logger.e("scale in trim = $scale")
        mTrimBinding.bgView.layoutParams.width = (DimenUtils.screenWidth(this)*scale).toInt()
        mTrimBinding.bgView.layoutParams.height = (DimenUtils.screenWidth(this)*scale).toInt()
        needShowDialog = true
        setScreenTitle(getString(R.string.trim_video))
        mTrimBinding.playerViewInTrim.hideController()
        val videoPath = intent.getStringExtra("VideoPath")
        videoPath?.let {videoPath ->
            val duration = MediaUtils.getAudioDuration(videoPath)
            mStartOffset = 0
            mEndOffset = duration.toInt()
            mTrimBinding.videoControllerView.setMaxDuration(duration.toInt())
            mTrimBinding.cropTimeView.loadImage(videoPath, DimenUtils.screenWidth(this)-(76*DimenUtils.density(this)).roundToInt())
        }
        mTrimBinding.videoControllerView.onChangeListener = object :VideoControllerView.OnChangeListener {
            override fun onUp(timeMilSec: Int) {

                mPlayer?.seekTo(timeMilSec.toLong())
            }
            override fun onMove(progress: Float) {
                mPlayer?.seekTo((mTotalDuration*progress/100).toLong())
            }
        }

        mTrimBinding.cropTimeView.onChangeListener = object :CropVideoTimeView.OnChangeListener {
            override fun onSwipeLeft(startTimeMilSec: Float) {
                mStartOffset = startTimeMilSec.roundToInt()
                mPlayer?.seekTo(startTimeMilSec.toLong())

            }

            override fun onUpLeft(startTimeMilSec: Float) {
                mStartOffset = startTimeMilSec.roundToInt()
                mPlayer?.seekTo(startTimeMilSec.toLong())
                mTrimBinding.videoControllerView.changeStartPositionOffset(startTimeMilSec.roundToLong())
            }

            override fun onSwipeRight(endTimeMilSec: Float) {
                mEndOffset = endTimeMilSec.roundToInt()
                mPlayer?.seekTo(endTimeMilSec.toLong()-2000)
            }

            override fun onUpRight(endTimeMilSec: Float) {
                mEndOffset = endTimeMilSec.roundToInt()
                mPlayer?.seekTo(endTimeMilSec.toLong()-2000)
            }

        }

        mTrimBinding.buttonPlayAndPause.setOnClickListener {
            mPlayer?.playWhenReady = !(mPlayer?.playWhenReady ?: true)
        }
        mTrimBinding.bgView.setOnClickListener {  mPlayer?.playWhenReady = !(mPlayer?.playWhenReady ?: true)}
        mTrimBinding.buttonTrimVideo.setClick {
            if(checkCutTime()) {

                if(trimAvailable) {
                    trimAvailable = false
                    mPlayer?.playWhenReady = false
                    mPlayer?.release()
                    mPlayer = null
                    val intent = Intent(this,  ProcessVideoActivity::class.java).apply {
                        putExtra(ProcessVideoActivity.action, ProcessVideoActivity.trimVideoActon)
                        putExtra("path", mVideoPath)
                        putExtra("startTime", mStartOffset)
                        putExtra("endTime", mEndOffset)
                    }
                    startActivity(intent)

                    object :CountDownTimer(1000,1000) {
                        override fun onFinish() {
                            trimAvailable = true
                        }

                        override fun onTick(millisUntilFinished: Long) {

                        }

                    }.start()
                }


            }

        }

    }

    private val limitTime = 5000
    private fun checkCutTime():Boolean {

        if((mEndOffset - mStartOffset ) < limitTime) {
            showToast(getString(R.string.minimum_time_is_5_s))
            return false
        }

        return true
    }


    private fun listenVideoPosition() {
        mTimer = object :CountDownTimer(120000000, 100) {
            override fun onFinish() {
                this.start()
            }

            override fun onTick(millisUntilFinished: Long) {
                val currentPosition = mPlayer?.currentPosition ?: 0
                if(currentPosition > mEndOffset ) {
                    mPlayer?.seekTo(mStartOffset.toLong())
                } else if(currentPosition <= mStartOffset) {
                    mPlayer?.seekTo(mStartOffset.toLong())
                }
                runOnUiThread {
                    mTrimBinding.videoControllerView.setCurrentDuration(mPlayer?.currentPosition ?: 0)
                }
            }
        }.start()
    }

    override fun initActions() {

    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer?.cancel()
        mPlayer?.release()
        mPlayer = null
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
        val videoPath = intent.getStringExtra("VideoPath")
        videoPath?.let {
            mVideoPath = it
            initVideoPlayer(mVideoPath)

        }
    }

    private fun onPauseVideo() {

        mTrimBinding.buttonPlayAndPause.setImageResource(R.drawable.ic_play)
    }

    private fun onPlayVideo() {
        mTrimBinding.buttonPlayAndPause.setImageResource(R.drawable.ic_pause)
    }

    override fun onCompletion(mp: MediaPlayer?) {

    }

    private var mPlayer: ExoPlayer? = null
    private fun initVideoPlayer(path:String) {
        mPlayer = SimpleExoPlayer.Builder(VideoMakerApplication.getContext()).build()

        mTrimBinding.playerViewInTrim.player = mPlayer
        mPlayer?.playWhenReady = true

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
        mTrimBinding.playerViewInTrim.useController = false
        mPlayer?.playWhenReady = false
        mPlayer?.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(path))))
        mPlayer?.prepare()
        mPlayer?.seekTo(mStartOffset.toLong())
        listenVideoPosition()
    }


    private val onEnd = {
        mPlayer?.seekTo(mStartOffset.toLong())
        mPlayer?.playWhenReady = false
    }

    private val onStateChange = { isPlay:Boolean ->
        if(isPlay) {
            onPlayVideo()
        } else {
            onPauseVideo()
        }
    }

    override fun onStop() {
        super.onStop()
    }


}
