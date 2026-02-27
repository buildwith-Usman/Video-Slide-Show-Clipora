package com.acatapps.videomaker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.databinding.ItemSlideThemeBinding
import com.acatapps.videomaker.models.ThemeDataModel
import com.acatapps.videomaker.slide_show_theme.ThemeUtils
import com.acatapps.videomaker.slide_show_theme.data.ThemeData
import com.acatapps.videomaker.utils.Logger
import javax.security.auth.callback.Callback

class SlideThemeListAdapter(val callback: (ThemeData)->Unit) : BaseAdapter<ThemeDataModel>() {

    init {
       getAllThemeOnDevice()
    }


    override fun doGetViewType(position: Int): Int = R.layout.item_slide_theme

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemSlideThemeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding as ItemSlideThemeBinding
        val item = mItemList[position]
        binding.root.setOnClickListener {
            callback.invoke(item.themeData)
            highlightItem(item.themeData.themeVideoFilePath)
        }
        binding.themeNameLabel.text = item.name
        if(item.name == "none") {
            binding.themeIcon.setImageResource(R.drawable.ic_none)
        } else {
            Glide.with(binding.root.context).load(item.videoPath).apply(RequestOptions().override(200)).into(binding.themeIcon)
        }
        if(item.selected) {
            binding.strokeBg.visibility = View.VISIBLE
            binding.blackBgOfTitleView.setBackgroundColor(VideoMakerApplication.getContext().resources.getColor(R.color.orangeA02))
        } else {
            binding.strokeBg.visibility = View.GONE
            binding.blackBgOfTitleView.setBackgroundColor(VideoMakerApplication.getContext().resources.getColor(R.color.blackAlpha45))
        }


    }

    fun highlightItem(path:String) {
        for(item in mItemList) {
            item.selected = item.themeData.themeVideoFilePath == path
        }
        notifyDataSetChanged()
    }

    private fun getAllThemeOnDevice() {
        for(themeData in ThemeUtils.getThemeDataList()) {
            mItemList.add(ThemeDataModel(themeData))
        }
        notifyDataSetChanged()
    }

}