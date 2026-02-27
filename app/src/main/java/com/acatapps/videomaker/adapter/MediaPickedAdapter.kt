package com.acatapps.videomaker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.databinding.ItemImagePickedBinding
import com.acatapps.videomaker.models.MediaPickedDataModel
import com.acatapps.videomaker.utils.DimenUtils
import java.io.File
import java.util.Collections.swap

class MediaPickedAdapter(private val onClickDelete:(Int)->Unit) : BaseAdapter<MediaPickedDataModel>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_image_picked

    var itemTouchHelper:ItemTouchHelper? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemImagePickedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = ItemImagePickedBinding.bind(holder.itemView)
        val item = mItemList[position]
        Glide.with(holder.itemView.context).load(item.path).apply(RequestOptions().override(DimenUtils.screenWidth(holder.itemView.context)/4)).into(binding.mediaThumb)
        binding.iconDelete.setInstanceClick {
            onClickDelete.invoke(holder.adapterPosition)
        }
        holder.itemView.setOnLongClickListener {
            itemTouchHelper?.startDrag(holder)
            return@setOnLongClickListener true
        }
    }

    fun onMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                swap(mItemList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                swap(mItemList, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    fun checkFile() {
        val newList = ArrayList<MediaPickedDataModel>()
        for(item in mItemList) {
            if(File(item.path).exists()) {
               newList.add(item)
            }

        }
        mItemList.clear()
        mItemList.addAll(newList)
        notifyDataSetChanged()
    }

    fun registerItemTouch(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }

    interface ItemTouchListenner {
        fun onItemMove(fromPosition: Int, toPosition: Int)
    }
}