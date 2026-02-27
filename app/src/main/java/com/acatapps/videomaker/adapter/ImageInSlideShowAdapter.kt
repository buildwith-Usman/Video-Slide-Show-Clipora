package com.acatapps.videomaker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.models.ImageInSlideShowDataModel
import com.acatapps.videomaker.utils.DimenUtils
import com.acatapps.videomaker.databinding.ItemImageListInSlideShowBinding

class ImageInSlideShowAdapter : BaseAdapter<ImageInSlideShowDataModel>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_image_list_in_slide_show

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemImageListInSlideShowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding as ItemImageListInSlideShowBinding
        val item = mItemList[position]
        val imageSize = DimenUtils.density(holder.itemView.context)*64
        Glide.with(holder.itemView.context).load(item.imagePath).apply(RequestOptions().override(imageSize.toInt())).into(binding.imagePreview)
    }

    fun addImagePathList(arrayList: ArrayList<String>) {
        for(item in arrayList) {
            mItemList.add(ImageInSlideShowDataModel(item))
        }
        notifyDataSetChanged()
    }

}