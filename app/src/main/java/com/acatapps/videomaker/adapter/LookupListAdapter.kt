package com.acatapps.videomaker.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.models.LookupDataModel
import com.acatapps.videomaker.utils.BitmapUtils
import com.acatapps.videomaker.utils.LookupUtils
import com.acatapps.videomaker.databinding.ItemLookupBinding

class LookupListAdapter(val onSelectLookup:(LookupUtils.LookupType)->Unit): BaseAdapter<LookupDataModel>() {
    private var mCurrentPosition = -1
    init {
        val lookupDataList = LookupUtils.getLookupDataList()
        for(item in lookupDataList) {
            mItemList.add(LookupDataModel(item))
        }
        notifyDataSetChanged()
    }

    override fun doGetViewType(position: Int): Int = R.layout.item_lookup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemLookupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val binding = ItemLookupBinding.bind(holder.itemView)
        val item = mItemList[position]
        binding.root.setOnClickListener {
            onSelectLookup.invoke(item.lookupType)
            mCurrentPosition = position
            notifyDataSetChanged()
        }
        if(mCurrentPosition == position) {
            binding.strokeBg.visibility = View.VISIBLE
        } else {
            binding.strokeBg.visibility = View.GONE
        }
        binding.imageThumb.setImageBitmap(BitmapUtils.getBitmapFromAsset("lut-preview/${item.lookupType}.jpg"))
        binding.lookupNameLabel.text = item.name
    }

    fun highlightItem(lookupType: LookupUtils.LookupType) {
        for(index in 0 until  mItemList.size) {
            val item = mItemList[index]
            if(lookupType == item.lookupType) {
                mCurrentPosition = index
                notifyDataSetChanged()
                break
            }
        }
    }
}
