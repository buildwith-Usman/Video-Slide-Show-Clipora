package com.acatapps.videomaker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.custom_view.ControlSliderStartEnd
import com.acatapps.videomaker.data.AudioData
import com.acatapps.videomaker.data.MusicReturnData
import com.acatapps.videomaker.databinding.ItemMusicListBinding
import com.acatapps.videomaker.models.AudioDataModel
import com.acatapps.videomaker.utils.Logger

class MusicListAdapter(val callback:MusicCallback) : BaseAdapter<AudioDataModel>() {

    override fun doGetViewType(position: Int): Int = R.layout.item_music_list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemMusicListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = ItemMusicListBinding.bind(holder.itemView)
        val item = mItemList[position]

        binding.musicNameLabel.text = item.audioName
        binding.musicDurationLabel.text = item.durationString

        if(item.isSelect) {
            binding.buttonUseMusic.visibility = View.VISIBLE
            binding.editMusicToolsArea.visibility = View.VISIBLE
            binding.audioControllerEdit.apply {
                setMaxValue(item.duration)
            }
            binding.iconMusic.setImageResource(R.drawable.ic_music_selected)
        } else {
            binding.buttonUseMusic.visibility = View.GONE
            binding.editMusicToolsArea.visibility = View.GONE
            binding.iconMusic.setImageResource(R.drawable.ic_music_list_normal)
        }

        if(item.isPlaying) binding.icPlayAndPause.setImageResource(R.drawable.ic_pause)
        else binding.icPlayAndPause.setImageResource(R.drawable.ic_play)

        holder.itemView.setOnClickListener {
            if(mCurrentItem == item) return@setOnClickListener

            mCurrentItem?.let {
                it.isSelect = false
                it.isPlaying = false
                it.reset()
            }
            mCurrentItem = item
            mCurrentItem?.let {
                it.isSelect = true
                it.isPlaying = true
            }
            callback.onClickItem(item)
            notifyDataSetChanged()
        }

        binding.audioControllerEdit.setStartAndEndProgress(item.startOffset*100f/item.duration, (item.startOffset+item.length)*100f/item.duration)

        binding.audioControllerEdit.setOnChangeListener(object :ControlSliderStartEnd.OnChangeListener{
            override fun onSwipeLeft(progress: Float) {

            }

            override fun onLeftUp(progress: Float) {
                item.startOffset = binding.audioControllerEdit.getStartOffset()
                item.length = binding.audioControllerEdit.getLength().toLong()
                callback.onChangeStart(item.startOffset, item.length.toInt())
            }

            override fun onSwipeRight(progress: Float) {

            }

            override fun onRightUp(progress: Float) {
                item.length = binding.audioControllerEdit.getLength().toLong()
                callback.onChangeEnd(item.length.toInt())
            }

        })

        binding.buttonUseMusic.setClick {
            callback.onClickUse(item)
        }

        binding.icPlayAndPause.setOnClickListener {
            if(item.isPlaying) binding.icPlayAndPause.setImageResource(R.drawable.ic_play)
            else binding.icPlayAndPause.setImageResource(R.drawable.ic_pause)
            item.isPlaying = !item.isPlaying
            callback.onClickPlay(item.isPlaying)
        }
    }

    fun setAudioDataList(audioDataList:ArrayList<AudioData>) {
        mItemList.clear()
        notifyDataSetChanged()
        for(audio in audioDataList) {
            mItemList.add(AudioDataModel(audio))
        }
        notifyDataSetChanged()
    }

    fun restoreBeforeMusic(musicData:MusicReturnData):Int {
        var position = -1
        for(index in 0 until mItemList.size) {
            val item = mItemList[index]
            if(item.audioFilePath == musicData.audioFilePath) {
                item.isPlaying = true
                item.isSelect = true
                item.startOffset = musicData.startOffset
                item.length = musicData.length.toLong()
                position = index
                mCurrentItem = item
                notifyDataSetChanged()
                break
            }
        }
        return position
    }

    fun onPause(){
        mCurrentItem?.let {
            it.isPlaying = false
            notifyDataSetChanged()
        }
    }

    fun setOffAll() {
        mCurrentItem?.let {
            it.isPlaying = false
            it.isSelect = false
            notifyDataSetChanged()
        }
    }

    interface MusicCallback{
        fun onClickItem(audioDataModel: AudioDataModel)
        fun onClickUse(audioDataModel: AudioDataModel)
        fun onClickPlay(isPlay:Boolean)
        fun onChangeStart(startOffsetMilSec:Int, lengthMilSec:Int)
        fun onChangeEnd(lengthMilSec:Int)
    }

}