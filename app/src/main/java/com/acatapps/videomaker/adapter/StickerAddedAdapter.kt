package com.acatapps.videomaker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.databinding.ItemStickerAddedBinding
import com.acatapps.videomaker.models.StickerAddedDataModel
import com.acatapps.videomaker.utils.Logger

class StickerAddedAdapter(private val onChange: OnChange) : BaseAdapter<StickerAddedDataModel>() {

    override fun doGetViewType(position: Int): Int = R.layout.item_sticker_added

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemStickerAddedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding as ItemStickerAddedBinding
        val item = mItemList[position]
        binding.root.setOnClickListener {
            setOffAll()
            mCurrentItem?.inEdit = false
            item.inEdit = true
            mCurrentItem = item
            notifyDataSetChanged()
            onChange.onClickSticker(item)
        }
        Logger.e("start end --> ${item.startTimeMilSec} ${item.endTimeMilSec}")
        if(item.inEdit) {
            binding.grayBg.visibility = View.VISIBLE
        } else {
            binding.grayBg.visibility = View.GONE
        }
        binding.stickerAddedPreview.setImageBitmap(item.bitmap)
    }

    fun addNewSticker(stickerAddedDataModel: StickerAddedDataModel) {
        mCurrentItem?.inEdit = false
        mItemList.add(stickerAddedDataModel)
        mCurrentItem = stickerAddedDataModel
        notifyDataSetChanged()
    }

    fun changeStartTime(startTimeMilSec:Int) {
        mCurrentItem?.startTimeMilSec = startTimeMilSec
    }

    fun changeEndTime(endTimeMilSec:Int) {
        mCurrentItem?.endTimeMilSec = endTimeMilSec
    }

    fun deleteItem(stickerAddedDataModel: StickerAddedDataModel) {
        mItemList.remove(stickerAddedDataModel)
        notifyDataSetChanged()
    }

    fun deleteAllItem() {
        mItemList.clear()
        notifyDataSetChanged()
    }

    fun setOffAll() {
        for(item in mItemList) {
            item.inEdit = false
        }
        notifyDataSetChanged()
    }

    interface OnChange {
        fun onClickSticker(stickerAddedDataModel: StickerAddedDataModel)
    }
}