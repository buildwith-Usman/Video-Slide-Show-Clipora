package com.acatapps.videomaker.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.databinding.ItemMyStudioInHomeBinding
import com.acatapps.videomaker.models.MyStudioDataModel
import com.acatapps.videomaker.utils.DimenUtils
import com.acatapps.videomaker.utils.MediaUtils
import com.acatapps.videomaker.utils.Utils
import java.io.File


class MyStudioInHomeAdapter : BaseAdapter<MyStudioDataModel>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_my_studio_in_home

    var onClickItem: ((MyStudioDataModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemMyStudioInHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = ItemMyStudioInHomeBinding.bind(holder.itemView)
        val item = mItemList[position]
        val context = holder.itemView.context
        val size = DimenUtils.density(context) * 98
        Glide.with(context).load(item.filePath).placeholder(R.drawable.ic_load_thumb).apply(
            RequestOptions().override(size.toInt())
        ).into(binding.imageThumb)

        if (item.filePath.lowercase().contains(".mp4")) {
            binding.grayBg.visibility = View.VISIBLE
            binding.durationLabel.visibility = View.VISIBLE
            try {
                val duration = MediaUtils.getVideoDuration(item.filePath)
                binding.durationLabel.text = Utils.convertSecToTimeString(duration / 1000)
            } catch (e: Exception) {
                File(item.filePath).delete()
                VideoMakerApplication.getContext().sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(File(item.filePath))
                    )
                )
            }


        } else {
            binding.grayBg.visibility = View.GONE
            binding.durationLabel.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            onClickItem?.invoke(item)
        }

    }

    override fun setItemList(arrayList: ArrayList<MyStudioDataModel>) {
        arrayList.sort()
        mItemList.clear()
        mItemList.addAll(arrayList)
        notifyDataSetChanged()
    }
}