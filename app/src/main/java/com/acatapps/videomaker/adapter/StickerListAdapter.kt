package com.acatapps.videomaker.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.databinding.ItemStickerListBinding

class StickerListAdapter(val callback:(String)->Unit) : BaseAdapter<String>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_sticker_list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemStickerListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding as ItemStickerListBinding
        val item = mItemList[position]
        binding.previewSticker.setImageResource(item.toInt())
        binding.root.setOnClickListener {
            callback.invoke(item)
        }
    }
}