package com.acatapps.videomaker.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.image_slide_show.drawer.ImageSlideData
import com.acatapps.videomaker.utils.DimenUtils
import com.acatapps.videomaker.utils.LookupUtils
import com.acatapps.videomaker.databinding.ItemImageListInSlideShowBinding

class ImageWithLookupAdapter(private val onSelectImage:(Long)->Unit): BaseAdapter<ImageSlideData>() {
    private var mCurrentPositon = -1
    override fun doGetViewType(position: Int): Int = R.layout.item_image_list_in_slide_show

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemImageListInSlideShowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val binding = ItemImageListInSlideShowBinding.bind(holder.itemView)
        val item = mItemList[position]
        binding.root.setOnClickListener {
            mCurrentItem = item
            mCurrentPositon = position
            onSelectImage.invoke(item.slideId)
            notifyDataSetChanged()
        }
        if(position == mCurrentPositon) {
            binding.strokeBg.visibility = View.VISIBLE
        } else {
            binding.strokeBg.visibility = View.GONE
        }
        Glide.with(binding.root.context).load(item.fromImagePath).apply(RequestOptions().override((DimenUtils.density(binding.root.context)*64).toInt())).into(binding.imagePreview)
    }

    fun changeLookupOfCurretItem(lookupType: LookupUtils.LookupType) {
        mCurrentItem?.lookupType = lookupType
    }
    fun changeHighlightItem(position:Int) :LookupUtils.LookupType{
        if(position >= 0 && position < mItemList.size) {
            mCurrentPositon = position
            mCurrentItem = mItemList[mCurrentPositon]
            notifyDataSetChanged()
            return mItemList[mCurrentPositon].lookupType
        }
        return LookupUtils.LookupType.NONE

    }

}
