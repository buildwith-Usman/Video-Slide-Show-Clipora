package com.acatapps.videomaker.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.data.FontsData
import com.acatapps.videomaker.databinding.ItemFontsListBinding
import com.acatapps.videomaker.models.FontModel

class FontListAdapter(val callback:(fontId:Int)->Unit) : BaseAdapter<FontModel>() {

    init {
        mItemList.add(FontModel(FontsData(R.font.doubledecker_demo, "Double")))
        mItemList.add(FontModel(FontsData(R.font.doubledecker_dots, "Double Dots")))
        mItemList.add(FontModel(FontsData(R.font.fonseca_grande, "Fonseca")))
        mItemList.add(FontModel(FontsData(R.font.youth_power, "Youth Power")))
        mItemList.add(FontModel(FontsData(R.font.fun_sized, "Fun sized")))

    }
    private var selectedFontId = -1
    override fun doGetViewType(position: Int): Int = R.layout.item_fonts_list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemFontsListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding as ItemFontsListBinding
        val item = mItemList[position]

        binding.fontPreview.typeface = ResourcesCompat.getFont(holder.itemView.context, item.fontId)
        binding.fontPreview.text = item.fontName

        if(item.fontId == selectedFontId) {
            holder.itemView.setBackgroundColor(Color.parseColor("#33000000"))
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            callback.invoke(item.fontId)
            selectedFontId = item.fontId
            notifyDataSetChanged()
        }
    }
}