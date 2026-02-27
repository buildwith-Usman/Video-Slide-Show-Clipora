package com.acatapps.videomaker.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.databinding.ItemColorListBinding
import java.lang.Exception

class ColorListAdapter(val callback:(Int)->Unit) : BaseAdapter<String>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_color_list
    private var mColorSelected = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemColorListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding as ItemColorListBinding
        val item = mItemList[position]
        try {
            binding.colorPreview.setBackgroundColor(Color.parseColor(item))
            holder.itemView.setOnClickListener {
                mColorSelected = item
                callback.invoke(Color.parseColor(item))
                notifyDataSetChanged()
            }
            if(mColorSelected == item) {
                holder.itemView.translationY = -20f
                binding.strokeInColor.visibility = View.VISIBLE
            } else {
                holder.itemView.translationY = 0f
                binding.strokeInColor.visibility = View.GONE
            }
        } catch (e:Exception){

        }
    }
}