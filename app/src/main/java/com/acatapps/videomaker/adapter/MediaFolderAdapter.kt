package com.acatapps.videomaker.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.data.MediaData
import com.acatapps.videomaker.databinding.ItemMediaAlbumBinding
import com.acatapps.videomaker.models.MediaAlbumDataModel

class MediaFolderAdapter : BaseAdapter<MediaAlbumDataModel>() {

    override fun doGetViewType(position: Int): Int = R.layout.item_media_album

    var onClickItem: ((MediaAlbumDataModel) -> Unit)? = null

    private inner class MediaFolderViewHolder(override val binding: ItemMediaAlbumBinding) : BaseViewHolder(binding)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding =
            ItemMediaAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaFolderViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = (holder as MediaFolderViewHolder).binding
        val item = mItemList[position]
        binding.albumDescription.text = "${item.albumName}(${item.mediaItemPaths.size})"
        if (item.mediaItemPaths.size > 0) {
            Glide.with(binding.root.context).load(item.mediaItemPaths[0].filePath)
                .apply(RequestOptions().override(200)).into(binding.albumCover)
        }
        binding.root.setOnClickListener {
            onClickItem?.invoke(item)
        }
    }

    fun setItemListFromData(itemsInput: ArrayList<MediaData>) {
        if (itemsInput.size < 1) return
        val finalItems = arrayListOf<MediaAlbumDataModel>()
        finalItems.add(MediaAlbumDataModel(itemsInput[0].folderName, itemsInput[0].folderId))
        finalItems[0].mediaItemPaths.add(itemsInput[0])

        for (index in 1 until itemsInput.size) {
            val itemInput = itemsInput[index]
            var flag = true
            for (j in 0 until finalItems.size) {
                if (finalItems[j].folderId == itemInput.folderId) {
                    finalItems[j].mediaItemPaths.add(itemInput)
                    flag = false
                    break
                }
            }
            if (flag) {
                finalItems.add(MediaAlbumDataModel(itemInput.folderName, itemInput.folderId))
                finalItems[finalItems.size - 1].mediaItemPaths.add(itemInput)
            }
        }
        mItemList.clear()
        mItemList.addAll(finalItems)
        notifyDataSetChanged()
    }

    fun addItemToAlbum(mediaData: MediaData) {
        for (folder in mItemList) {
            if (folder.folderId == mediaData.folderId) {
                folder.mediaItemPaths.add(mediaData)
                break
            }
        }
        notifyDataSetChanged()
    }
}
