package com.acatapps.videomaker.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.data.ThemeLinkData
import com.acatapps.videomaker.databinding.ItemThemeInHomeBinding
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.ThemeLinkUtils
import java.io.File

class ThemeInHomeAdapter : BaseAdapter<ThemeLinkData>() {

    var onItemClick:((ThemeLinkData)->Unit)?=null
    private var mCurrentThemeFileName = "None"
    var rewardIsLoaded = false
    override fun doGetViewType(position: Int): Int = R.layout.item_theme_in_home

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemThemeInHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding as ItemThemeInHomeBinding
        val item = mItemList[position]
        binding.themeName.text = item.name
        val uriString = "file:///android_asset/theme-icon/${item.fileName}.jpg"
        if(item.link == "none") {
            binding.themeIcon.setImageResource(R.drawable.ic_none)
            binding.iconDownload.visibility = View.GONE
        } else {
            Glide.with(binding.root.context)
                .load(Uri.parse(uriString))
                .into(binding.themeIcon)

            if(File(FileUtils.themeFolderPath+"/${item.fileName}.mp4").exists()) {
                binding.iconDownload.visibility = View.GONE
                binding.bgAlpha.visibility = View.GONE
            } else {
                binding.iconDownload.visibility = View.VISIBLE
                if(rewardIsLoaded) {
                    binding.bgAlpha.visibility = View.GONE
                } else {
                    binding.bgAlpha.visibility = View.VISIBLE
                }
            }
        }

        if(mCurrentThemeFileName == item.fileName) {
            binding.strokeBg.visibility = View.VISIBLE
        } else {
            binding.strokeBg.visibility = View.GONE
        }

        binding.root.setOnClickListener {
            onItemClick?.invoke(item)
        }


    }

    fun changeCurrentThemeName(themeFileName:String) {
        mCurrentThemeFileName = themeFileName
        notifyDataSetChanged()
    }

}