package com.acatapps.videomaker.application

import android.app.Application
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.acatapps.videomaker.broadcast.InternetStateChange
import com.acatapps.videomaker.modules.audio_manager_v3.AudioManagerV3
import com.acatapps.videomaker.modules.audio_manager_v3.AudioManagerV3Impl
import com.acatapps.videomaker.modules.local_storage.LocalStorageData
import com.acatapps.videomaker.modules.local_storage.LocalStorageDataImpl
import com.acatapps.videomaker.modules.music_player.MusicPlayer
import com.acatapps.videomaker.modules.music_player.MusicPlayerImpl
import com.acatapps.videomaker.ui.pick_media.PickMediaViewModelFactory
import com.acatapps.videomaker.ui.select_music.SelectMusicViewModelFactory
import com.acatapps.videomaker.ui.slide_show.SlideShowViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class VideoMakerApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@VideoMakerApplication))

        bind<LocalStorageData>() with singleton { LocalStorageDataImpl() }
        bind() from provider { PickMediaViewModelFactory(instance()) }
        bind() from provider { SelectMusicViewModelFactory(instance()) }
        bind() from provider { SlideShowViewModelFactory() }
        bind<AudioManagerV3>() with  provider { AudioManagerV3Impl() }
        bind<MusicPlayer>() with  provider { MusicPlayerImpl() }
    }

    companion object {
        lateinit var instance: VideoMakerApplication
        fun getContext() = instance.applicationContext!!
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(InternetStateChange(), filter)
    }

    fun loadAd() {}

    fun showInterHome(onClose:(()->Unit)?=null):Boolean {
        onClose?.invoke()
        return false
    }

    fun showAdsFull() {}
    
    fun showAdsFull(onClose: (() -> Unit)?):Boolean {
        onClose?.invoke()
        return false
    }

    fun getNativeAds(): Any? = null

    var onRewardLoaded:(()->Unit)?=null
    fun loadRewardAd() {}
    fun loadRewardAd(onLoaded:()->Unit, onError:(()->Unit)?=null) {
        onLoaded.invoke()
    }

    fun rewardAdReady():Boolean {
        return true
    }
    
    fun showRewardAd(onComplete:()->Unit):Boolean {
        onComplete.invoke()
        return true
    }
    
    fun releaseRewardAd() {}

    fun loadAdFullForTheme(onLoaded:()->Unit) {
        onLoaded.invoke()
    }

}