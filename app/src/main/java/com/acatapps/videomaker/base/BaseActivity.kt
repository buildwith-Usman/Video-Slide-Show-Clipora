package com.acatapps.videomaker.base

import android.animation.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAdView
import com.acatapps.videomaker.BuildConfig
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.data.ThemeLinkData
import com.acatapps.videomaker.databinding.*
import com.acatapps.videomaker.extentions.fadeInAnimation
import com.acatapps.videomaker.extentions.openAppInStore
import com.acatapps.videomaker.extentions.scaleAnimation
import com.acatapps.videomaker.modules.rate.RatingManager
import com.acatapps.videomaker.utils.FileDownloader
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.Utils
import java.io.File

abstract class BaseActivity : AppCompatActivity() {

    private var mProgressIsShowing = false
    protected var mExportDialogShowing = false
    protected var mYesNoDialogShowing = false
    protected var mRateDialogShowing = false
    var needShowDialog = false
    var comebackStatus = ""
    var mRateAvailable = true
    protected var isHome = false

    protected lateinit var binding: ActivityBaseLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityBaseLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        comebackStatus = getString(R.string.do_you_want_to_come_back)
        binding.mainContentLayout.apply {
            removeAllViews()
            addView(View.inflate(context, getContentResId(), null))
        }
        showHeader()

        binding.headerView.screenTitle.text = screenTitle()

        binding.headerView.icBack.setOnClickListener {
            hideKeyboard()
            onBackPressed()
        }


        showAds()
        initViews()
        initActions()
    }

    protected fun shareVideoFile(filePath: String) {
        if (!File(filePath).exists()) return
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "video/*"
        if (Build.VERSION.SDK_INT < 24) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(filePath)))
        } else {
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    File(filePath)
                )
            )
        }

        startActivity(shareIntent)
    }

    protected fun checkSettingAutoUpdateTime(): Boolean {
        val i1 = Settings.Global.getInt(contentResolver, Settings.Global.AUTO_TIME)
        val i2 = Settings.Global.getInt(contentResolver, Settings.Global.AUTO_TIME_ZONE)
        Logger.e("i1 = $i1 --- i2 = $i2")
        if (i1 == 1 && i2 == 1) return true
        return false
    }

    private fun showAds() {


        binding.bannerAdsView.loadAd(AdRequest.Builder().build())
        binding.bannerAdsView.adListener = object : AdListener() {

            override fun onAdLoaded() {
                super.onAdLoaded()
                if (isShowAds()) {
                    visibilityAds()
                }
            }
        }
    }

    fun visibilityAds() {
        binding.bannerAdsView.visibility = View.VISIBLE
    }

    fun hideAds() {
        binding.bannerAdsView.visibility = View.GONE
    }

    open fun isShowAds() = false

    fun setSearchInputListener(onSearchQuery: (String) -> Unit) {
        binding.headerView.inputSearchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onSearchQuery.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    var searchMode = false
    fun showSearchInput() {
        if (searchMode) return
        searchMode = true
        openKeyboard()
        binding.headerView.screenTitle.visibility = View.GONE
        binding.headerView.inputSearchEditText.visibility = View.VISIBLE
        binding.headerView.inputSearchEditText.requestFocus()
        binding.headerView.icClearSearch.visibility = View.VISIBLE
        binding.headerView.icClearSearch.setOnClickListener {
            binding.headerView.inputSearchEditText.setText("")
        }

    }

    fun hideSearchInput() {
        searchMode = false
        binding.headerView.screenTitle.visibility = View.VISIBLE
        binding.headerView.inputSearchEditText.visibility = View.GONE
        binding.headerView.icClearSearch.visibility = View.GONE
        binding.headerView.inputSearchEditText.setText("")

    }


    protected fun setScreenTitle(title: String) {
        binding.headerView.screenTitle.text = title
    }


    protected fun showHeader() {
        binding.headerView.root.visibility = View.VISIBLE
    }

    protected fun hideHeader() {
        binding.headerView.root.visibility = View.GONE
    }

    open fun screenTitle(): String = ""

    fun setRightButton(drawableId: Int? = null, onClick: () -> Unit) {
        drawableId?.let {
            binding.headerView.rightButton.setImageResource(it)
            binding.headerView.rightButton.visibility = View.VISIBLE
            binding.headerView.rightButton.setOnClickListener {
                onClick.invoke()
            }
        }
    }

    fun setSubRightButton(drawableId: Int? = null, onClick: () -> Unit) {
        drawableId?.let {
            binding.headerView.subRightButton.setImageResource(it)
            binding.headerView.subRightButton.visibility = View.VISIBLE
            binding.headerView.subRightButton.setOnClickListener {
                onClick.invoke()
            }
        }
    }

    protected fun playTranslationYAnimation(view: View) {
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(view, "alpha", 0.5f, 1f),
            ObjectAnimator.ofFloat(view, "translationY", 64f, 0f)
        )
        animatorSet.duration = 250
        animatorSet.interpolator = FastOutLinearInInterpolator()
        animatorSet.start()
    }

    protected fun playSlideDownToUpAnimation(view: View, viewH: Int) {
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(view, "translationY", viewH.toFloat(), 0f)
        )
        animatorSet.duration = 200
        animatorSet.interpolator = FastOutLinearInInterpolator()
        animatorSet.start()
    }

    protected fun showProgressDialog() {
        if (!mProgressIsShowing) {
            LayoutInflater.from(this).inflate(R.layout.layout_progress_dialog, binding.baseRootView, true)
            mProgressIsShowing = true
        }
    }

    protected fun dismissProgressDialog() {
        if (mProgressIsShowing) {
            binding.baseRootView.removeViewAt(binding.baseRootView.childCount - 1)
            mProgressIsShowing = false
        }
    }

    protected fun showExportDialog(isEditVideo: Boolean = false, callback: (Int, Int) -> Unit) {
        if (!mExportDialogShowing) {
            mExportDialogShowing = true
            val dialogBinding = LayoutExportVideoDialogBinding.inflate(layoutInflater, binding.baseRootView, true)
            scaleAnimation(dialogBinding.dialogContent)
            alphaInAnimation(dialogBinding.bgBlack)
            if (isEditVideo) {
                dialogBinding.lineInExportDialog.visibility = View.VISIBLE
                dialogBinding.ratioLabel.visibility = View.VISIBLE
                dialogBinding.ratioRadioGroup.visibility = View.VISIBLE

            } else {
                dialogBinding.lineInExportDialog.visibility = View.GONE
                dialogBinding.ratioLabel.visibility = View.GONE
                dialogBinding.ratioRadioGroup.visibility = View.GONE
            }
            dialogBinding.cancelButton.setOnClickListener {
                dismissExportDialog()
            }
            dialogBinding.saveButton.setOnClickListener {
                val outQuality = when {
                    dialogBinding.normalQuality.isChecked -> {
                        480
                    }
                    dialogBinding.hdQuality.isChecked -> {
                        720
                    }
                    dialogBinding.fullHDQuality.isChecked -> {
                        1080
                    }
                    else -> {
                        0
                    }
                }

                val outRatioString = when {
                    dialogBinding.wideRatio.isChecked -> {
                        1
                    }
                    dialogBinding.verticalRatio.isChecked -> {
                        2
                    }
                    dialogBinding.squareRatio.isChecked -> {
                        3
                    }
                    else -> {
                        3
                    }
                }

                callback.invoke(outQuality, outRatioString)

            }
            dialogBinding.bgBlack.setOnClickListener {
                dismissExportDialog()
            }
        }
    }

    protected fun showYesNoDialog(title: String, onClickYes: () -> Unit) {
        if (mYesNoDialogShowing) return

        val dialogBinding = LayoutYesNoDialogBinding.inflate(layoutInflater, binding.baseRootView, true)
        dialogBinding.dialogTitle.text = title
        dialogBinding.noButton.setOnClickListener {
            dismissYesNoDialog()

        }
        dialogBinding.yesButton.setOnClickListener {
            dismissYesNoDialog()
            onClickYes.invoke()
        }
        dialogBinding.bgBlackOnYesNo.setOnClickListener {
            dismissYesNoDialog()
        }

        val ad = VideoMakerApplication.instance.getNativeAds()
        Logger.e("native ad in yes no dialog = ${ad}")
        if (ad != null) {
            Utils.bindBigNativeAds(ad, dialogBinding.nativeAdViewInYesNoDialog.root)
            dialogBinding.nativeAdViewInYesNoDialog.root.visibility = View.VISIBLE

        } else {
            dialogBinding.nativeAdViewInYesNoDialog.root.visibility = View.GONE
            VideoMakerApplication.instance.loadAd()
        }

        scaleAnimation(dialogBinding.dialogContentOnYesNo)
        alphaInAnimation(dialogBinding.bgBlackOnYesNo)
        mYesNoDialogShowing = true
    }

    protected fun showYesNoDialog(
        title: String,
        onClickYes: () -> Unit,
        onClickNo: (() -> Unit)? = null
    ): View? {
        if (mYesNoDialogShowing) return null

        val dialogBinding = LayoutYesNoDialogBinding.inflate(layoutInflater, binding.baseRootView, true)
        dialogBinding.dialogTitle.text = title
        dialogBinding.noButton.setOnClickListener {
            dismissYesNoDialog()
            onClickNo?.invoke()
        }
        dialogBinding.yesButton.setOnClickListener {
            dismissYesNoDialog()
            onClickYes.invoke()
        }
        dialogBinding.bgBlackOnYesNo.setOnClickListener {
            dismissYesNoDialog()
        }

        val ad = VideoMakerApplication.instance.getNativeAds()
        if (ad != null) {
            Utils.bindBigNativeAds(ad, dialogBinding.nativeAdViewInYesNoDialog.root)
            dialogBinding.nativeAdViewInYesNoDialog.root.visibility = View.VISIBLE

        } else {
            dialogBinding.nativeAdViewInYesNoDialog.root.visibility = View.GONE

        }

        scaleAnimation(dialogBinding.dialogContentOnYesNo)
        alphaInAnimation(dialogBinding.bgBlackOnYesNo)
        mYesNoDialogShowing = true
        return dialogBinding.root
    }

    protected fun showYesNoDialog(
        title: String,
        onClickYes: () -> Unit,
        onClickNo: (() -> Unit)? = null,
        onClickBg: (() -> Unit)? = null
    ): View? {
        if (mYesNoDialogShowing) return null

        val dialogBinding = LayoutYesNoDialogBinding.inflate(layoutInflater, binding.baseRootView, true)
        dialogBinding.dialogTitle.text = title
        dialogBinding.noButton.setOnClickListener {
            dismissYesNoDialog()
            onClickNo?.invoke()
        }
        dialogBinding.yesButton.setOnClickListener {
            dismissYesNoDialog()
            onClickYes.invoke()
        }
        dialogBinding.bgBlackOnYesNo.setOnClickListener {
            dismissYesNoDialog()
            onClickBg?.invoke()
        }

        val ad = VideoMakerApplication.instance.getNativeAds()
        if (ad != null) {
            Utils.bindBigNativeAds(ad, dialogBinding.nativeAdViewInYesNoDialog.root)
            dialogBinding.nativeAdViewInYesNoDialog.root.visibility = View.VISIBLE
        } else {
            dialogBinding.nativeAdViewInYesNoDialog.root.visibility = View.GONE

        }

        scaleAnimation(dialogBinding.dialogContentOnYesNo)
        alphaInAnimation(dialogBinding.bgBlackOnYesNo)
        mYesNoDialogShowing = true
        return dialogBinding.root
    }

    protected fun showYesNoDialogForOpenSetting(
        title: String,
        onClickYes: () -> Unit,
        onClickNo: (() -> Unit)? = null,
        onClickBg: (() -> Unit)? = null
    ) {
        if (mYesNoDialogShowing) return

        val dialogBinding = LayoutYesNoDialogBinding.inflate(layoutInflater, binding.baseRootView, true)
        dialogBinding.dialogTitle.text = title
        dialogBinding.yesButton.text = getString(R.string.setting)
        dialogBinding.dialogTitle.setTextColor(Color.parseColor("#73000000"))
        dialogBinding.noButton.setOnClickListener {
            dismissYesNoDialog()
            onClickNo?.invoke()
        }
        dialogBinding.yesButton.setOnClickListener {
            dismissYesNoDialog()
            onClickYes.invoke()
        }
        dialogBinding.bgBlackOnYesNo.setOnClickListener {
        }

        val ad = VideoMakerApplication.instance.getNativeAds()
        if (ad != null) {
            Utils.bindBigNativeAds(ad, dialogBinding.nativeAdViewInYesNoDialog.root)
            dialogBinding.nativeAdViewInYesNoDialog.root.visibility = View.VISIBLE
        } else {
            dialogBinding.nativeAdViewInYesNoDialog.root.visibility = View.GONE
        }

        scaleAnimation(dialogBinding.dialogContentOnYesNo)
        alphaInAnimation(dialogBinding.bgBlackOnYesNo)
        mYesNoDialogShowing = true
        return
    }

    protected fun dismissYesNoDialog() {

        if (mYesNoDialogShowing) {
            binding.baseRootView.removeViewAt(binding.baseRootView.childCount - 1)
            mYesNoDialogShowing = false
        }


    }

    protected fun dismissExportDialog() {
        if (mExportDialogShowing) {
            binding.baseRootView.removeViewAt(binding.baseRootView.childCount - 1)
            mExportDialogShowing = false
        }


    }


    private var mAutoShowRating = false
    protected fun showRatingDialog(autoShow:Boolean=true) {
        if (mRateDialogShowing) return
        mRateDialogShowing = true
        val rateBinding = LayoutRateDialogBinding.inflate(layoutInflater, binding.baseRootView, true)
        rateBinding.bgBlackViewInRate.setOnClickListener {
        }

        rateBinding.mainRatingContentLayout.setOnClickListener {

        }

        rateBinding.bgBlackViewInRate.fadeInAnimation()
        rateBinding.layoutRateDialogMainContentGroup.scaleAnimation()

        rateBinding.layoutRateDialogRateUsButton.setOnClickListener {
            openAppInStore()
            RatingManager.getInstance().setRated()
            dismissRatingDialog()
            if(autoShow)
            finishAfterTransition()

        }

        rateBinding.layoutRateDialogNoThankButton.setOnClickListener {
            RatingManager.getInstance().setRated()
            dismissRatingDialog()
            if(autoShow)
                finishAfterTransition()

        }

        rateBinding.layoutRateDialogLaterButton.setOnClickListener {
            RatingManager.getInstance().setTimeShowRating(30*60*1000)
            dismissRatingDialog()
            if(autoShow)
            finishAfterTransition()
        }

    }



    private fun highlightStar(targetIndex: Int, groupStar: ArrayList<LottieAnimationView>) {
        for (index in 0..targetIndex) {
            groupStar[index].progress = 1f
        }
        object : CountDownTimer(500, 500) {
            override fun onFinish() {
                runOnUiThread { dismissRatingDialog() }

            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()

    }

    private fun playAnimator(
        startValue: Float,
        endValue: Float,
        delay: Long,
        duration: Long,
        onUpdate: (Float) -> Unit,
        onEnd: () -> Unit
    ) {
        val animator = ValueAnimator.ofFloat(startValue, endValue)
        animator.addUpdateListener {
            val value = it.animatedFraction

            onUpdate.invoke(value)
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                onEnd.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationStart(animation: Animator) {

            }

        })
        animator.duration = duration
        animator.startDelay = delay
        animator.start()
    }

    protected fun dismissRatingDialog() {
        if (mRateDialogShowing) {
            binding.baseRootView.removeViewAt(binding.baseRootView.childCount - 1)
            mRateDialogShowing = false
        }

    }

    abstract fun getContentResId(): Int
    abstract fun initViews()
    abstract fun initActions()
    override fun onBackPressed() {
        hideKeyboard()
        if(mRateDialogShowing) return
        if (mDownloadDialogIsShow) {
            return
        }

        if (!mRateAvailable) return
        if (mProgressIsShowing) return

        if (mExportDialogShowing) {
            dismissExportDialog()
            return
        }
        if (mRateDialogShowing) {
            dismissRatingDialog()
            return
        }
        if (mYesNoDialogShowing) {
            dismissYesNoDialog()
            return
        }
        if (needShowDialog) {

            showYesNoDialog(comebackStatus) {
                super.onBackPressed()
            }


        } else {
            super.onBackPressed()
        }

    }

    private fun checkRate(): Boolean {
        val rated = RatingManager.getInstance().isRated()
        if (rated) return false
        val timeShow = RatingManager.getInstance().getTimeShowRating()
        Logger.e("time show = $timeShow")
        if (timeShow <= System.currentTimeMillis() || timeShow < 0) {

            return true
        }
        return false
    }

    protected enum class VideoQuality {
        NORMAL, HD, FULL_HD, NONE
    }

    fun scaleAnimation(view: View) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f, 1f)
        ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY).apply {
            interpolator = LinearOutSlowInInterpolator()
            duration = 250
        }.start()
    }

    fun alphaInAnimation(view: View) {
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
        ObjectAnimator.ofPropertyValuesHolder(view, alpha).apply {
            interpolator = LinearOutSlowInInterpolator()
            duration = 250
        }.start()
    }

    fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

    }


    fun hideRightButton() {
        binding.headerView.rightButton.visibility = View.GONE
    }

    fun hideSubRightButton() {
        binding.headerView.subRightButton.visibility = View.GONE
    }

    fun showRightButton() {
        binding.headerView.rightButton.visibility = View.VISIBLE
    }

    fun showSubRightButton() {
        binding.headerView.subRightButton.visibility = View.VISIBLE
    }

    fun doSendBroadcast(filePath: String) {
        sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(File(filePath))
            )
        )
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    private fun openKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInputFromWindow(
            binding.baseRootView.applicationWindowToken,
            InputMethodManager.SHOW_FORCED,
            1
        )
    }

    private fun hideKeyboard() {

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(binding.baseRootView.applicationWindowToken, 0)
    }

    fun showFullAds() {

    }

    private val downloadViewHashMap = HashMap<String, View?>()
    var mDownloadDialogIsShow = false
    fun showDownloadThemeDialog(
        linkData: ThemeLinkData,
        onClickDone: () -> Unit,
        onDownloadComplete: () -> Unit
    ) {
        if (mDownloadDialogIsShow) return
        mDownloadDialogIsShow = true
        
        var downloadBinding: LayoutDownloadThemeDialogBinding? = null
        val view = if (downloadViewHashMap[linkData.link] != null) {
            downloadViewHashMap[linkData.link]!!
        } else {
            downloadBinding = LayoutDownloadThemeDialogBinding.inflate(layoutInflater, null, false)
            downloadBinding!!.root
        }
        
        binding.baseRootView.addView(view)
        
        if (downloadViewHashMap[linkData.link] == null && downloadBinding != null) {
            downloadViewHashMap[linkData.link] = view

            downloadBinding!!.themeNameLabel.text = linkData.name
            val uriString = "file:///android_asset/theme-icon/${linkData.fileName}.jpg"
            Glide.with(this)
                .load(Uri.parse(uriString))
                .into(downloadBinding!!.themeIconInDownloadDialog)

            downloadBinding!!.blackViewInDownloadThemeDialog.setOnClickListener {

            }
            downloadBinding!!.icClose.setOnClickListener {
                dismissDownloadDialog()
            }
            downloadBinding!!.doneButton.setOnClickListener {
                dismissDownloadDialog()
                onClickDone.invoke()

            }

            val ad = VideoMakerApplication.instance.getNativeAds()
            if (ad != null) {
                Utils.bindBigNativeAds(
                    ad,
                    (downloadBinding!!.nativeAdViewInDownloadDialog.root as NativeAdView)
                )
                downloadBinding!!.nativeAdViewInDownloadDialog.root.visibility = View.VISIBLE
            } else {
                downloadBinding!!.nativeAdViewInDownloadDialog.root.visibility = View.GONE
            }

            downloadBinding!!.tryAgainButton.setOnClickListener {
                downloadBinding!!.tryAgainButton.visibility = View.INVISIBLE
                downloadBinding!!.downloadingViewContainer.visibility = View.VISIBLE
                onDownloadTheme(linkData.link, linkData.fileName, onDownloadComplete, downloadBinding!!)
            }

            downloadBinding!!.watchVideoButton.setOnClickListener {

                Logger.e("load ad")
                showProgressDialog()
                VideoMakerApplication.instance.loadAdFullForTheme {
                    runOnUiThread {
                        downloadBinding!!.watchVideoButton.visibility = View.GONE
                        downloadBinding!!.downloadingViewContainer.visibility = View.VISIBLE
                        dismissProgressDialog()
                    }
                    onDownloadTheme(linkData.link, linkData.fileName, onDownloadComplete, downloadBinding!!)
                }


            }
        }

        val content = view.findViewById<View>(R.id.downloadThemeDialogContent)
        val black = view.findViewById<View>(R.id.blackViewInDownloadThemeDialog)
        if (content != null) scaleAnimation(content)
        if (black != null) alphaInAnimation(black)

    }

    private val fileDownloader = FileDownloader()

    private fun onDownloadTheme(
        link: String,
        fileName: String,
        onComplete: () -> Unit,
        downloadBinding: LayoutDownloadThemeDialogBinding
    ) {
        fileDownloader.download(link, FileUtils.themeFolderPath, "${fileName}.mp4", object : FileDownloader.OnDownloadListener {
            override fun onDownloadComplete() {
                runOnUiThread {
                    downloadBinding.doneButton.visibility = View.VISIBLE
                    downloadBinding.tryAgainButton.visibility = View.INVISIBLE
                    downloadBinding.downloadingViewContainer.visibility = View.GONE
                    downloadViewHashMap.remove(link)
                    onComplete.invoke()
                }
            }

            override fun onProgress(progress: Int) {
                if (mDownloadDialogIsShow)
                    runOnUiThread {
                        downloadBinding.downloadingProgressBar.setProgress(progress)
                    }
            }

            override fun onError(e: Exception) {
                Logger.e("download error --> ${e.message}")
                runOnUiThread {
                    downloadBinding.tryAgainButton.visibility = View.VISIBLE
                    downloadBinding.downloadingViewContainer.visibility = View.GONE
                }
                Thread {
                    if (!Utils.isInternetAvailable()) {
                        runOnUiThread {
                            showToast(getString(R.string.no_internet_connection_please_connect_to_the_internet_and_try_again))
                        }

                    }
                }.start()
            }
        })
    }

    fun dismissDownloadDialog() {
        if (mDownloadDialogIsShow) {
            binding.baseRootView.removeViewAt(binding.baseRootView.childCount - 1)
            mDownloadDialogIsShow = false
        }
    }


}