package com.acatapps.videomaker.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.media.MediaRecorder
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.daasuu.gpuv.player.GPUPlayerView
import com.google.android.exoplayer2.ui.PlayerView
import com.acatapps.videomaker.R
import com.acatapps.videomaker.adapter.RecordListAdapter
import com.acatapps.videomaker.adapter.StickerAddedAdapter
import com.acatapps.videomaker.adapter.TextStickerAddedAdapter
import com.acatapps.videomaker.custom_view.*
import com.acatapps.videomaker.data.MusicReturnData
import com.acatapps.videomaker.databinding.ActivityBaseToolsEditBinding
import com.acatapps.videomaker.databinding.LayoutChangeMusicToolsBinding
import com.acatapps.videomaker.databinding.LayoutChangeRecordToolsBinding
import com.acatapps.videomaker.databinding.LayoutChangeStickerToolsBinding
import com.acatapps.videomaker.databinding.LayoutChangeTextToolsBinding
import com.acatapps.videomaker.models.RecordedDataModel
import com.acatapps.videomaker.models.StickerAddedDataModel
import com.acatapps.videomaker.models.TextStickerAddedDataModel
import com.acatapps.videomaker.modules.audio_manager_v3.AudioManagerV3
import com.acatapps.videomaker.ui.select_music.SelectMusicActivity
import com.acatapps.videomaker.utils.BitmapUtils
import com.acatapps.videomaker.utils.DimenUtils
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.Logger
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.io.IOException
import kotlin.math.roundToInt

abstract class BaseSlideShow : BaseActivity(), KodeinAware {
    override val kodein by closestKodein()
    override fun getContentResId(): Int = R.layout.activity_base_tools_edit
    protected var onEditSticker = false
    private val mAudioManager: AudioManagerV3 by instance<AudioManagerV3>()
    private var mCurrentMusicData: MusicReturnData? = null
    protected var toolType = ToolType.NONE
    private var mCurrentVideoVolume = 1f

    protected lateinit var toolsBinding: ActivityBaseToolsEditBinding

    @Volatile
    protected var mTouchEnable = true
    private val mStickerAddedAdapter = StickerAddedAdapter(object : StickerAddedAdapter.OnChange {
        override fun onClickSticker(stickerAddedDataModel: StickerAddedDataModel) {
            updateChangeStickerLayout(stickerAddedDataModel, true)
        }
    })

    private val mTextStickerAddedAdapter = TextStickerAddedAdapter(object : TextStickerAddedAdapter.OnChange {
            override fun onClickTextSticker(textStickerAddedDataModel: TextStickerAddedDataModel) {
                updateChangeTextStickerLayout(textStickerAddedDataModel, true)
            }

        })

    private val mRecoredAdapter = RecordListAdapter()
    override fun initViews() {
        toolsBinding = ActivityBaseToolsEditBinding.bind(binding.mainContentLayout.getChildAt(0))
        if(!isImageSlideShow())
        for(index in 0 until toolsBinding.menuItemContainer.childCount) {
            toolsBinding.menuItemContainer[index].apply {
                layoutParams.width = DimenUtils.screenWidth(this@BaseSlideShow)/4
            }

        }
        doInitViews()
        val screenW = DimenUtils.screenWidth(this)
        val videoPreviewScale = DimenUtils.videoPreviewScale()
        Logger.e("scale = $videoPreviewScale")
        toolsBinding.slideBgPreview.layoutParams.width = (screenW*videoPreviewScale).toInt()
        toolsBinding.slideBgPreview.layoutParams.height = (screenW*videoPreviewScale).toInt()


        binding.baseRootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.baseRootView.getWindowVisibleDisplayFrame(rect)
            if(binding.baseRootView.rootView.height - (rect.bottom-rect.top) > 500) {
                addTextLayout?.translationY = -56*DimenUtils.density()
            } else {
                addTextLayout?.translationY = 0f
            }
        }

    }


    override fun initActions() {



        toolsBinding.changeThemeTools.setOnLongClickListener {
            showToast(getString(R.string.change_theme))
            return@setOnLongClickListener true
        }

        toolsBinding.changeTransitionTools.setOnLongClickListener {
            showToast("Change transition effect")
            return@setOnLongClickListener true
        }

        toolsBinding.changeDurationTools.setOnLongClickListener {
            showToast(getString(R.string.change_duration))
            return@setOnLongClickListener true
        }

        toolsBinding.changeEffectTools.setOnLongClickListener {
            showToast(getString(R.string.change_video_effect))
            return@setOnLongClickListener true
        }

        toolsBinding.changeMusicTools.setOnLongClickListener {
            showToast(getString(R.string.change_music))
            return@setOnLongClickListener true
        }

        toolsBinding.changeStickerTools.setOnLongClickListener {
            showToast(getString(R.string.add_sticker))
            return@setOnLongClickListener true
        }

        toolsBinding.changeTextTools.setOnLongClickListener {
            showToast(getString(R.string.add_text))
            return@setOnLongClickListener true
        }

        toolsBinding.changeFilterTools.setOnLongClickListener {
            showToast(getString(R.string.change_image_filter))
            return@setOnLongClickListener true
        }

        toolsBinding.changeMusicTools.setOnClickListener {
            if (toolType == ToolType.MUSIC || !mTouchEnable) return@setOnClickListener
            toolType = ToolType.MUSIC
            showLayoutChangeMusic()

        }

        toolsBinding.changeStickerTools.setOnClickListener {
            if (toolType == ToolType.STICKER || !mTouchEnable) return@setOnClickListener
            toolType = ToolType.STICKER
            showLayoutChangeSticker()
        }

        toolsBinding.changeTextTools.setOnClickListener {
            if (toolType == ToolType.TEXT || !mTouchEnable) return@setOnClickListener
            toolType = ToolType.TEXT
            showLayoutChangeText()

        }

        toolsBinding.changeRecordTools.setOnClickListener {
            if (toolType == ToolType.RECORDER) return@setOnClickListener
            toolType = ToolType.RECORDER
            showLayoutChangeRecord()

        }
        setRightButton(R.drawable.ic_save_vector) {
            performExportVideo()
            hideKeyboard()
        }
        doInitActions()
    }

    fun useDefaultMusic() {
        mAudioManager.useDefault()
    }

    var clickSelectMusicAvailable = true
    private fun showLayoutChangeMusic() {
        val changeMusicBinding = LayoutChangeMusicToolsBinding.inflate(layoutInflater, toolsBinding.toolsAction, false)
        showToolsActionLayout(changeMusicBinding.root)

        changeMusicBinding.soundNameLabel.setOnClickListener {
            if(clickSelectMusicAvailable) {
                clickSelectMusicAvailable = false
                val intent = Intent(this, SelectMusicActivity::class.java)
                mCurrentMusicData?.let {
                    Bundle().apply {
                        putSerializable("CurrentMusic", it)
                        intent.putExtra("bundle", this)
                    }
                }

                startActivityForResult(intent, SelectMusicActivity.SELECT_MUSIC_REQUEST_CODE)

                object :CountDownTimer(1000, 1000) {
                    override fun onFinish() {
                        clickSelectMusicAvailable = true
                    }

                    override fun onTick(millisUntilFinished: Long) {

                    }

                }.start()
            }

        }
        changeMusicBinding.icDelete.setOnClickListener {
            changeMusicBinding.icDelete.visibility = View.INVISIBLE
            mAudioManager.returnToDefault(getCurrentVideoTimeMs())
            mCurrentMusicData = null
            updateChangeMusicLayout(changeMusicBinding)
        }
        updateChangeMusicLayout(changeMusicBinding)
        changeMusicBinding.musicVolumeSeekBar.setProgressChangeListener {
            mAudioManager.setVolume(it / 100f)
        }
        changeMusicBinding.videoVolumeSeekBar.setProgressChangeListener {
            performChangeVideoVolume(it/100f)
            mCurrentVideoVolume = it/100f
        }
        if(isImageSlideShow()) {
            changeMusicBinding.videoVolumeSeekBar.visibility = View.GONE
            changeMusicBinding.icVideoVolume.visibility = View.INVISIBLE
        }
    }

    private fun updateChangeMusicLayout(binding: LayoutChangeMusicToolsBinding) {
        if (mAudioManager.getAudioName() == "none") {
            binding.icDelete.visibility = View.INVISIBLE
            binding.soundNameLabel.text = getString(R.string.default_)
        } else {
            binding.icDelete.visibility = View.VISIBLE
            binding.soundNameLabel.text = mAudioManager.getAudioName()

        }
        binding.musicVolumeSeekBar.setProgress(mAudioManager.getVolume() * 100)
        binding.videoVolumeSeekBar.setProgress(mCurrentVideoVolume*100)
    }

    protected fun getMusicData(): String = mAudioManager.getOutMusicPath()
    protected fun getMusicVolume():Float = mAudioManager.getVolume()



    private fun showLayoutChangeSticker() {
        val changeStickerBinding = LayoutChangeStickerToolsBinding.inflate(layoutInflater, toolsBinding.toolsAction, false)
        showToolsActionLayout(changeStickerBinding.root)

        changeStickerBinding.stickerAddedListView.apply {
            adapter = mStickerAddedAdapter
            layoutManager =
                LinearLayoutManager(this@BaseSlideShow, LinearLayoutManager.HORIZONTAL, false)
        }

        changeStickerBinding.confirmAddSticker.setOnClickListener {
            setOffAllSticker()
            mStickerAddedAdapter.setOffAll()
            changeStickerBinding.cropTimeView.visibility = View.INVISIBLE
            changeStickerBinding.buttonPlayAndPause.visibility = View.INVISIBLE
            showVideoController()
        }

        if(mStickerAddedAdapter.itemCount < 1) {
            changeStickerBinding.cancelAddSticker.visibility = View.GONE
        }

        changeStickerBinding.cancelAddSticker.setOnClickListener {
            showYesNoDialog(getString(R.string.do_you_want_delete_all_sticker)) {
                deleteAllSticker()
                changeStickerBinding.cropTimeView.visibility = View.INVISIBLE
                changeStickerBinding.buttonPlayAndPause.visibility = View.INVISIBLE
                showVideoController()
                changeStickerBinding.cancelAddSticker.visibility = View.GONE
            }
        }

        changeStickerBinding.buttonAddSticker.setOnClickListener {
            mTouchEnable = false
            val chooseStickerLayout = ChooseStickerLayout(this)
            toolsBinding.otherLayoutContainer.removeAllViews()
            toolsBinding.otherLayoutContainer.addView(chooseStickerLayout)
            playSlideDownToUpAnimation(chooseStickerLayout, toolsBinding.otherLayoutContainer.height)
            chooseStickerLayout.callback = object : ChooseStickerLayout.StickerCallback {
                override fun onSelectSticker(stickerPath: String) {
                    setOffAllSticker()
                    Thread {
                        BitmapUtils.loadBitmapFromXML(stickerPath) {
                            runOnUiThread {
                                it?.let { bitmap ->
                                    val viewId = View.generateViewId()
                                    val stickerAddedDataModel = StickerAddedDataModel(
                                        bitmap,
                                        true,
                                        0,
                                        getMaxDuration(),
                                        viewId
                                    )
                                    mStickerAddedAdapter.setOffAll()
                                    mStickerAddedAdapter.addNewSticker(stickerAddedDataModel)
                                    toolsBinding.stickerContainer.addView(
                                        StickerView(this@BaseSlideShow, null).apply {
                                            setBitmap(
                                                bitmap,
                                                true,
                                                toolsBinding.stickerContainer.width,
                                                toolsBinding.stickerContainer.height
                                            )
                                            id = viewId
                                            deleteCallback = {
                                                toolsBinding.stickerContainer.removeView(this)
                                                mStickerAddedAdapter.deleteItem(
                                                    stickerAddedDataModel
                                                )
                                                setOffAllSticker()
                                                mStickerAddedAdapter.setOffAll()

                                                changeStickerBinding.cropTimeView.visibility = View.INVISIBLE
                                                changeStickerBinding.buttonPlayAndPause.visibility = View.INVISIBLE
                                                Logger.e(" --> on delete sticker")
                                                showVideoController()
                                                if(mStickerAddedAdapter.itemCount< 1) {
                                                    changeStickerBinding.cancelAddSticker.visibility = View.GONE
                                                }
                                            }
                                        })
                                    updateChangeStickerLayout(stickerAddedDataModel, false)
                                }
                            }
                        }
                    }.start()
                    onBackPressed()
                }
            }
            activeTouch()
        }
    }

    private fun deleteAllSticker() {
        val listView = ArrayList<View>()
        for(i in 0 until toolsBinding.stickerContainer.childCount) {
            val view = toolsBinding.stickerContainer.getChildAt(i)
            if(view is StickerView) {
                listView.add(view)
            }
        }
        listView.forEach {
            toolsBinding.stickerContainer.removeView(it)
        }
        mStickerAddedAdapter.deleteAllItem()
    }

    protected fun getStickerAddedList():ArrayList<StickerAddedDataModel> = mStickerAddedAdapter.itemList

    private fun updateChangeStickerLayout(
        stickerAddedDataModel: StickerAddedDataModel,
        autoSeek: Boolean
    ) {

        if (autoSeek) {
            performSeekTo(stickerAddedDataModel.startTimeMilSec)
        }
        val view = toolsBinding.toolsAction.getChildAt(toolsBinding.toolsAction.childCount - 1)
        val binding = LayoutChangeStickerToolsBinding.bind(view)
        binding.cropTimeView.visibility = View.VISIBLE
        binding.buttonPlayAndPause.apply {
            visibility = View.VISIBLE
            setOnClickListener { changeVideoStateInAddSticker(binding) }
        }
        if(mStickerAddedAdapter.itemCount >0) {
            binding.cancelAddSticker.visibility = View.VISIBLE
        }
        binding.cropTimeView.apply {
            if(!isImageSlideShow()){
                loadVideoImagePreview(getSourcePathList(), DimenUtils.screenWidth(this@BaseSlideShow)-(76* DimenUtils.density(this@BaseSlideShow)).roundToInt())
            }else {
                loadImage(getSourcePathList())
            }

            setMax(getMaxDuration())
            setStartAndEnd(
                stickerAddedDataModel.startTimeMilSec,
                stickerAddedDataModel.endTimeMilSec
            )
        }

        binding.cropTimeView.onChangeListener = object : CropVideoTimeView.OnChangeListener {
            override fun onSwipeLeft(startTimeMilSec: Float) {
                changeVideoStateToPauseInAddSticker(binding)
                stickerAddedDataModel.startTimeMilSec = startTimeMilSec.toInt()
            }

            override fun onUpLeft(startTimeMilSec: Float) {
                changeVideoStateToPauseInAddSticker(binding)
                stickerAddedDataModel.startTimeMilSec = startTimeMilSec.toInt()
                performSeekTo(stickerAddedDataModel.startTimeMilSec)
            }

            override fun onSwipeRight(endTimeMilSec: Float) {
                changeVideoStateToPauseInAddSticker(binding)
                stickerAddedDataModel.endTimeMilSec = endTimeMilSec.toInt()
            }

            override fun onUpRight(endTimeMilSec: Float) {
                changeVideoStateToPauseInAddSticker(binding)
                stickerAddedDataModel.endTimeMilSec = endTimeMilSec.toInt()
            }

        }
        hideVideoController()
        setOffAllSticker()
        detectInEdit(stickerAddedDataModel)
    }

    private fun changeVideoStateInAddSticker(binding: LayoutChangeStickerToolsBinding) {
        binding.buttonPlayAndPause.apply {
            if (isPlaying()) {
                setImageResource(R.drawable.ic_play)
                performPauseVideo()
            } else {
                setImageResource(R.drawable.ic_pause)
                performPlayVideo()
            }
        }
    }

    private fun changeVideoStateToPauseInAddSticker(binding: LayoutChangeStickerToolsBinding) {
        binding.buttonPlayAndPause.apply {
            setImageResource(R.drawable.ic_play)
            performPauseVideo()
        }
    }

    private fun detectInEdit(stickerAddedDataModel: StickerAddedDataModel) {
        for (index in 0 until toolsBinding.stickerContainer.childCount) {
            val view = toolsBinding.stickerContainer.getChildAt(index)
            if (view is StickerView) {
                if (view.getBitmap() == stickerAddedDataModel.bitmap) {
                    view.setInEdit(true)
                    toolsBinding.stickerContainer.removeView(view)
                    toolsBinding.stickerContainer.addView(view)
                    return
                }
            }
        }
    }

    fun setOffAllSticker() {
        for (index in 0 until toolsBinding.stickerContainer.childCount) {
            val view = toolsBinding.stickerContainer.getChildAt(index)
            if (view is StickerView) {
                view.setInEdit(false)
            }
        }
    }

    private var addTextLayout: AddTextLayout? = null
    private fun showLayoutChangeText() {
        setOffAllTextSticker()
        mTextStickerAddedAdapter.setOffAll()
        val changeTextBinding = LayoutChangeTextToolsBinding.inflate(layoutInflater, toolsBinding.toolsAction, false)
        showToolsActionLayout(changeTextBinding.root)

        changeTextBinding.buttonAddText.setOnClickListener {
            setOffAllTextSticker()
            changeTextBinding.cropTimeViewInText.visibility = View.INVISIBLE
            changeTextBinding.buttonPlayAndPauseInText.visibility = View.INVISIBLE
            showAddTextLayout(null,true)
        }

        changeTextBinding.confirmAddText.setOnClickListener {
            setOffAllTextSticker()
            mTextStickerAddedAdapter.setOffAll()
            changeTextBinding.cropTimeViewInText.visibility = View.INVISIBLE
            changeTextBinding.buttonPlayAndPauseInText.visibility = View.INVISIBLE
            showVideoController()
        }
        if(mTextStickerAddedAdapter.itemCount < 1) {
            changeTextBinding.cancelAddTextSticker.visibility = View.GONE
        }
        changeTextBinding.cancelAddTextSticker.setOnClickListener {
            showYesNoDialog(getString(R.string.do_you_want_delete_all_text)) {
                changeTextBinding.cropTimeViewInText.visibility = View.INVISIBLE
                changeTextBinding.buttonPlayAndPauseInText.visibility = View.INVISIBLE

                deleteAllTextSticker()
                showVideoController()
                hideKeyboard()
                changeTextBinding.cancelAddTextSticker.visibility = View.GONE
            }

        }
        changeTextBinding.textStickerAddedListView.apply {
            adapter = mTextStickerAddedAdapter
            layoutManager = LinearLayoutManager(
                this@BaseSlideShow,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun deleteAllTextSticker() {
        val listView = ArrayList<View>()
        for(i in 0 until toolsBinding.stickerContainer.childCount) {
            val view = toolsBinding.stickerContainer.getChildAt(i)
            if(view is EditTextSticker) {
                listView.add(view)
            }
        }
        listView.forEach {
            toolsBinding.stickerContainer.removeView(it)
        }
        mTextStickerAddedAdapter.deleteAllItem()
        setOffAllTextSticker()
        mTextStickerAddedAdapter.setOffAll()
    }

    protected fun getTextAddedList():ArrayList<TextStickerAddedDataModel> = mTextStickerAddedAdapter.itemList

    private fun showAddTextLayout(editTextSticker: EditTextSticker? = null, isEdit:Boolean=false) {
        mTouchEnable = false

        setOffAllTextSticker()
        mTextStickerAddedAdapter.setOffAll()
        editTextSticker?.let {
            it.changeIsAdded(false)
            toolsBinding.stickerContainer.removeView(it)
            it.setInEdit(true)

        }

        addTextLayout = AddTextLayout(this, editTextSticker)
        performPauseVideo()
        toolsBinding.fullScreenOtherLayoutContainer.apply {
            removeAllViews()
            addView(addTextLayout)
            playTranslationYAnimation(this)

        }

        setRightButton(R.drawable.ic_check) {
            addTextLayout?.hideKeyboard()
            addTextLayout?.getEditTextView()?.let {

                performAddText(it)
            }

        }
        setScreenTitle(getString(R.string.text_editor))
        onPauseVideo()
        if(isEdit) addTextLayout?.showKeyboard()
        activeTouch()
    }
    private fun activeTouch() {
        Thread{
            Thread.sleep(500)
            mTouchEnable = true
        }.start()
    }
    private fun performAddText(editTextSticker: EditTextSticker) {

        toolsBinding.stickerContainer.addView(editTextSticker)
        editTextSticker.changeIsAdded(true)
        val changeTextBinding = LayoutChangeTextToolsBinding.bind(toolsBinding.toolsAction.getChildAt(toolsBinding.toolsAction.childCount - 1))
        changeTextBinding.cancelAddTextSticker.visibility = View.VISIBLE
        val textStickerAddedDataModel:TextStickerAddedDataModel
        if(mTextStickerAddedAdapter.getItemBytViewId(editTextSticker.id) == null) {
            textStickerAddedDataModel = TextStickerAddedDataModel(editTextSticker.getMainText(), true, 0, getMaxDuration(), editTextSticker.id)
            mTextStickerAddedAdapter.addNewText(textStickerAddedDataModel)
        } else {
            textStickerAddedDataModel = mTextStickerAddedAdapter.getItemBytViewId(editTextSticker.id)!!
            textStickerAddedDataModel.inEdit = true
            mTextStickerAddedAdapter.notifyDataSetChanged()
        }

        updateChangeTextStickerLayout(textStickerAddedDataModel, false)
        editTextSticker.deleteCallback = {
            changeTextBinding.cropTimeViewInText.visibility = View.INVISIBLE
            changeTextBinding.buttonPlayAndPauseInText.visibility = View.INVISIBLE



            toolsBinding.stickerContainer.removeView(editTextSticker)
            mTextStickerAddedAdapter.deleteItem(textStickerAddedDataModel)
            setOffAllTextSticker()
            mTextStickerAddedAdapter.setOffAll()
            showVideoController()
            hideKeyboard()
            if(mTextStickerAddedAdapter.itemCount < 1) {
                changeTextBinding.cancelAddTextSticker.visibility = View.GONE
            }
        }
        editTextSticker.editCallback = { textSticker ->
            showAddTextLayout(textSticker, true)
            Logger.e("onEdit")
        }
        hideAllViewInFullScreenLayout()
    }

    private fun updateChangeTextStickerLayout(
        textStickerAddedDataModel: TextStickerAddedDataModel,
        autoSeek: Boolean
    ) {
        val view = toolsBinding.toolsAction.getChildAt(toolsBinding.toolsAction.childCount - 1)
        val binding = LayoutChangeTextToolsBinding.bind(view)
        if (autoSeek) {
            performSeekTo(textStickerAddedDataModel.startTimeMilSec)
        }
        binding.cropTimeViewInText.visibility = View.VISIBLE
        binding.buttonPlayAndPauseInText.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                changeVideoStateInAddStickerInText(binding)
            }
        }

        binding.cropTimeViewInText.apply {
            if(!isImageSlideShow()){
                loadVideoImagePreview(getSourcePathList(), DimenUtils.screenWidth(this@BaseSlideShow)-(76* DimenUtils.density(this@BaseSlideShow)).roundToInt())
            }else {
                loadImage(getSourcePathList())
            }
            setMax(getMaxDuration())
            setStartAndEnd(
                textStickerAddedDataModel.startTimeMilSec,
                textStickerAddedDataModel.endTimeMilSec
            )
        }
        binding.cropTimeViewInText.onChangeListener = object : CropVideoTimeView.OnChangeListener {
            override fun onSwipeLeft(startTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(binding)
                textStickerAddedDataModel.startTimeMilSec = startTimeMilSec.toInt()
            }

            override fun onUpLeft(startTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(binding)
                textStickerAddedDataModel.startTimeMilSec = startTimeMilSec.toInt()
                performSeekTo(textStickerAddedDataModel.startTimeMilSec)
            }

            override fun onSwipeRight(endTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(binding)
                textStickerAddedDataModel.endTimeMilSec = endTimeMilSec.toInt()
            }

            override fun onUpRight(endTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(binding)
                textStickerAddedDataModel.endTimeMilSec = endTimeMilSec.toInt()
            }

        }
        hideVideoController()
        setOffAllTextSticker()
        detectInEdit(textStickerAddedDataModel)
        changeVideoStateToPauseInAddStickerInText(binding)
        onPauseVideo()
    }

    private fun changeVideoStateInAddStickerInText(binding: LayoutChangeTextToolsBinding) {
        binding.buttonPlayAndPauseInText.apply {
            if (isPlaying()) {
                setImageResource(R.drawable.ic_play)
                performPauseVideo()
            } else {
                setImageResource(R.drawable.ic_pause)
                performPlayVideo()
            }
        }
    }

    private fun changeVideoStateToPauseInAddStickerInText(binding: LayoutChangeTextToolsBinding) {
        binding.buttonPlayAndPauseInText.apply {
            setImageResource(R.drawable.ic_play)
            performPauseVideo()
        }
    }

    private fun setOffAllTextSticker() {
        for (index in 0 until toolsBinding.stickerContainer.childCount) {
            val view = toolsBinding.stickerContainer.getChildAt(index)
            if (view is EditTextSticker) {
                view.setInEdit(false)
            }
        }
    }

    private fun detectInEdit(textStickerAddedDataModel: TextStickerAddedDataModel) {
        for (index in 0 until toolsBinding.stickerContainer.childCount) {
            val view = toolsBinding.stickerContainer.getChildAt(index)
            if (view is EditTextSticker) {
                if (view.id == textStickerAddedDataModel.viewId) {
                    view.setInEdit(true)
                    toolsBinding.stickerContainer.removeView(view)
                    toolsBinding.stickerContainer.addView(view)
                    return
                }
            }
        }
    }
    private var mRecorder: MediaRecorder? = null
    private var mRecordingFilePath = ""
    private var mRecordingTimer:CountDownTimer? = null
    private var mCurrentRecordObject:RecordedDataModel? = null
    private fun showLayoutChangeRecord() {
        val changeRecordBinding = LayoutChangeRecordToolsBinding.inflate(layoutInflater, toolsBinding.toolsAction, false)
        showToolsActionLayout(changeRecordBinding.root)
        changeRecordBinding.recordedListView.adapter = mRecoredAdapter
        changeRecordBinding.recordedListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        changeRecordBinding.videoTimelineView.setMaxValue(getMaxDuration())
        if(isImageSlideShow()) {
            changeRecordBinding.videoTimelineView.loadImage(getSourcePathList())
        } else {
            changeRecordBinding.videoTimelineView.loadImageVideo(getSourcePathList())
        }

        changeRecordBinding.videoTimelineView.setDataList(mRecoredAdapter.itemList)
        changeRecordBinding.videoTimelineView.onUpCallback = {
            performSeekTo(it,false)

        }

        changeRecordBinding.videoTimelineView.onStartFail = {
            Toast.makeText(this, "StartRe", Toast.LENGTH_LONG).show()
        }
        changeRecordBinding.videoTimelineView.onStropSuccess = {
            mRecoredAdapter.addItem(RecordedDataModel(it))
        }
        changeRecordBinding.videoTimelineView.onStopRecording = {


        }
        mRecoredAdapter.onSelect = {
            mCurrentRecordObject = it
            changeRecordBinding.buttonRecord.setImageResource(R.drawable.ic_delete_white)
            performSeekTo(it.startOffset)
            changeRecordBinding.videoTimelineView.moveTo(it.startOffset)
        }
        changeRecordBinding.buttonRecord.setOnTouchListener { v, event ->


            return@setOnTouchListener true
        }


    }

    override fun onResume() {
        super.onResume()
        addTextLayout?.onResume()
    }

    private fun startRecordAudio() {
        performPauseVideo()
        hideVideoController()
        mRecordingFilePath = FileUtils.getAudioRecordTempFilePath()
        mRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(mRecordingFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {

            }

            start()
            mRecordingTimer?.start()
        }
    }


    protected fun setGLView(glSurfaceView: GLSurfaceView) {

        toolsBinding.slideGlViewContainer.addView(
            glSurfaceView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }
    protected fun setExoPlayerView(playerView: GPUPlayerView) {
        toolsBinding.videoGlViewContainer.removeAllViews()
        toolsBinding.videoGlViewContainer.addView(
            playerView,FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }
    protected fun releaseExoPlayerView() {
        toolsBinding.slideGlViewContainer.removeAllViews()

    }
    protected fun removeGLiew() {
        toolsBinding.slideGlViewContainer.removeAllViews()
    }

    fun updateTimeline() {
        toolsBinding.videoControllerView.setCurrentDuration(getCurrentVideoTimeMs())
        checkInTime(getCurrentVideoTimeMs())
    }

    protected fun checkInTime(timeMs: Int) {
        checkStickerInTime(timeMs)
        checkTextInTime(timeMs)
    }

    private fun checkStickerInTime(timeMilSec: Int) {
        for (item in getStickerAddedList()) {
            val view = findViewById<View>(item.stickerViewId) ?: continue
            if (timeMilSec >= item.startTimeMilSec && timeMilSec <= item.endTimeMilSec) {
                if (view.visibility != View.VISIBLE) view.visibility = View.VISIBLE
            } else {
                if (view.visibility == View.VISIBLE) view.visibility = View.INVISIBLE
            }
        }
    }


    private fun checkTextInTime(timeMilSec: Int) {
        for (item in getTextAddedList()) {
            val view = findViewById<View>(item.viewId) ?: continue
            if (timeMilSec >= item.startTimeMilSec && timeMilSec <= item.endTimeMilSec) {
                if (view.visibility != View.VISIBLE) view.visibility = View.VISIBLE
            } else {
                if (view.visibility == View.VISIBLE) view.visibility = View.INVISIBLE
            }
        }
    }

    fun setMaxTime(timeMs: Int) {
        toolsBinding.videoControllerView.setMaxDuration(timeMs)
    }

    protected fun showToolsActionLayout(view: View) {
        showVideoController()
        setOffAllSticker()
        setOffAllTextSticker()
        mStickerAddedAdapter.setOffAll()
        mTextStickerAddedAdapter.setOffAll()
        toolsBinding.toolsAction.removeAllViews()
        toolsBinding.toolsAction.addView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        playTranslationYAnimation(view)
    }

    protected fun onPauseVideo() {
        if (toolType == ToolType.STICKER) {
            val binding = LayoutChangeStickerToolsBinding.bind(toolsBinding.toolsAction.getChildAt(toolsBinding.toolsAction.childCount - 1))
            binding.buttonPlayAndPause.setImageResource(R.drawable.ic_play)
        } else if (toolType == ToolType.TEXT) {
            val binding = LayoutChangeTextToolsBinding.bind(toolsBinding.toolsAction.getChildAt(toolsBinding.toolsAction.childCount - 1))
            binding.buttonPlayAndPauseInText.setImageResource(R.drawable.ic_play)
        }
        mAudioManager.pauseAudio()
        toolsBinding.icPlay.visibility = View.VISIBLE
    }

    protected fun onPlayVideo() {
        if (toolType == ToolType.STICKER) {
            val binding = LayoutChangeStickerToolsBinding.bind(toolsBinding.toolsAction.getChildAt(toolsBinding.toolsAction.childCount - 1))
            binding.buttonPlayAndPause.setImageResource(R.drawable.ic_pause)
        } else if (toolType == ToolType.TEXT) {
            val binding = LayoutChangeTextToolsBinding.bind(toolsBinding.toolsAction.getChildAt(toolsBinding.toolsAction.childCount - 1))
            binding.buttonPlayAndPauseInText.setImageResource(R.drawable.ic_pause)
        }
        mAudioManager.playAudio()
        toolsBinding.icPlay.visibility = View.GONE
    }

    protected fun onSeekTo(timeMs: Int) {
        Logger.e("seek to $timeMs")
        mAudioManager.seekTo(timeMs)
        updateTimeline()
    }

    protected fun onRepeat() {
        mAudioManager.repeat()
    }

    private fun hideVideoController() {
        onEditSticker = true
        performPauseVideo()
        toolsBinding.videoControllerView.visibility = View.GONE
        toolsBinding.icPlay.alpha = 0f
    }

    private fun showVideoController() {
        onEditSticker = false
        toolsBinding.videoControllerView.visibility = View.VISIBLE
        toolsBinding.icPlay.alpha = 1f
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {


        if (resultCode == Activity.RESULT_OK && requestCode == SelectMusicActivity.SELECT_MUSIC_REQUEST_CODE) {
            if (data != null) {
                val bundle = data.getBundleExtra("bundle")
                val musicReturnData = (bundle?.getSerializable(SelectMusicActivity.MUSIC_RETURN_DATA_KEY)) as MusicReturnData
                changeMusicData(musicReturnData)
            }

        }


        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun changeMusicData(musicReturnData: MusicReturnData) {

        if (mCurrentMusicData == null || mCurrentMusicData?.audioFilePath != musicReturnData.audioFilePath || mCurrentMusicData?.startOffset != musicReturnData.startOffset || mCurrentMusicData?.length != musicReturnData.length) {
            mCurrentMusicData = musicReturnData
            mAudioManager.changeAudio(musicReturnData, getCurrentVideoTimeMs())
        }

        val view = toolsBinding.toolsAction.getChildAt(toolsBinding.toolsAction.childCount - 1)
        updateChangeMusicLayout(LayoutChangeMusicToolsBinding.bind(view))
    }

    private fun getTopViewInToolAction(): View = toolsBinding.toolsAction.getChildAt(toolsBinding.toolsAction.childCount - 1)

    abstract fun isImageSlideShow():Boolean

    abstract fun doInitViews()
    abstract fun doInitActions()
    abstract fun getCurrentVideoTimeMs(): Int

    abstract fun performPlayVideo()
    abstract fun performPauseVideo()
    abstract fun getMaxDuration(): Int
    abstract fun performSeekTo(timeMs: Int)
    abstract fun performSeekTo(timeMs: Int, showProgress:Boolean)
    abstract fun isPlaying(): Boolean
    abstract fun getSourcePathList(): ArrayList<String>
    abstract fun getScreenTitle():String
    abstract fun performExportVideo()
    enum class ToolType {
        NONE,TRIM ,EFFECT,THEME, TRANSITION, DURATION, MUSIC, STICKER, TEXT, FILTER, RECORDER
    }
    abstract fun performChangeVideoVolume(volume:Float)
    private fun hideKeyboard() {

        addTextLayout?.hideKeyboard()
    }

    override fun onBackPressed() {

        addTextLayout?.hideKeyboard()
        when {
            toolsBinding.otherLayoutContainer.childCount > 0 -> {
                toolsBinding.otherLayoutContainer.removeAllViews()
                return
            }
            toolsBinding.fullScreenOtherLayoutContainer.childCount > 0 -> {
                showYesNoDialog(getString(R.string.do_you_want_to_save), {

                    if (toolType == ToolType.TEXT) {
                        addTextLayout?.getEditTextView()?.let {
                            performAddText(it)
                        }
                    }
                },{ hideAllViewInFullScreenLayout()
                    if (toolType == ToolType.TEXT) {
                        addTextLayout?.onCancelEdit()?.let {
                            performAddText(it)
                            Logger.e("on cancel edit text")

                        }

                        addTextLayout = null
                    }})

                return
            }
            else -> {
                    super.onBackPressed()
            }
        }
    }

    private fun hideAllViewInFullScreenLayout() {

        toolsBinding.fullScreenOtherLayoutContainer.removeAllViews()
        setScreenTitle(screenTitle())
        setRightButton(R.drawable.ic_save_vector) {
            performExportVideo()
            hideKeyboard()
        }
        setScreenTitle(getScreenTitle())

    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }

    protected fun setOffAllStickerAndText() {
        setOffAllSticker()
        setOffAllTextSticker()
        mStickerAddedAdapter.setOffAll()
        mTextStickerAddedAdapter.setOffAll()

    }



}