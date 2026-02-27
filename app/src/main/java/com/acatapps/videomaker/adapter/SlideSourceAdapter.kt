package com.acatapps.videomaker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.databinding.ItemImageListInSlideShowBinding
import com.acatapps.videomaker.models.SlideSourceDataModel
import com.acatapps.videomaker.utils.DimenUtils

class SlideSourceAdapter : BaseAdapter<SlideSourceDataModel>() {

    var onClickItem:((Int)->Unit)?=null

    override fun doGetViewType(position: Int): Int = R.layout.item_image_list_in_slide_show

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemImageListInSlideShowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding as ItemImageListInSlideShowBinding
        val item = mItemList[position]
        val imageSize = DimenUtils.density(binding.root.context)*64
        if(item.isSelect) {
            binding.strokeBg.visibility = View.VISIBLE
        } else {
            binding.strokeBg.visibility = View.GONE
        }
        Glide.with(binding.root.context).load(item.path).apply(RequestOptions().override(imageSize.toInt())).into(binding.imagePreview)
        binding.root.setOnClickListener {
           setOffAll()

            item.isSelect = true
            notifyDataSetChanged()
            onClickItem?.invoke(position)
        }
    }

    private fun setOffAll(){
        for(item in mItemList) item.isSelect = false

    }

    fun addImagePathList(arrayList: ArrayList<String>) {
        mItemList.clear()
        notifyDataSetChanged()
        for(item in arrayList) {
            mItemList.add(SlideSourceDataModel(item))
        }
        notifyDataSetChanged()
    }

    fun changeVideo(position:Int) {
        if(position >= 0 && position < mItemList.size) {

            setOffAll()
            mItemList[position].isSelect =true
            notifyDataSetChanged()
        }
    }

    fun changeHighlightItem(position:Int) {
        if(position >= 0 && position < mItemList.size) {
            setOffAll()
            mItemList[position].isSelect =true
            notifyDataSetChanged()
        }
    }
}