package com.acatapps.videomaker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.databinding.ItemGsTransitionListBinding
import com.acatapps.videomaker.gs_effect.GSEffectUtils
import com.acatapps.videomaker.models.GSEffectDataModel
import com.acatapps.videomaker.utils.BitmapUtils

class GSEffectListAdapter : BaseAdapter<GSEffectDataModel>() {
    var onSelectEffectCallback:((Int, GSEffectUtils.EffectType)->Unit)?=null
    init {
        val effectDataList = GSEffectUtils.getAllGSEffectData()
        for(item in effectDataList) {
            mItemList.add(GSEffectDataModel(item).apply {
                if(item.effectType == GSEffectUtils.EffectType.NONE) {
                    isSelect = true
                }
            })
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemGsTransitionListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun doGetViewType(position: Int): Int = R.layout.item_gs_transition_list

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = ItemGsTransitionListBinding.bind(holder.itemView)
        val item = mItemList[position]
        binding.transitionNameLabel.text = item.name
        if(item.isSelect) {
            binding.strokeBg.visibility = View.VISIBLE
        } else {
            binding.strokeBg.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            setOffAll()
            item.isSelect = true
            notifyDataSetChanged()
            onSelectEffectCallback?.invoke(holder.adapterPosition, item.gsEffectData.effectType)
        }

        binding.imagePreview.setImageBitmap(BitmapUtils.getBitmapFromAsset("effect-preview/${item.gsEffectData.effectType}.jpg"))
    }

    private fun setOffAll() {
        for(item in mItemList) {
            item.isSelect = false
        }
    }

    fun selectEffect(effectType: GSEffectUtils.EffectType) {
        setOffAll()
        for(item in mItemList) {
            if(item.gsEffectData.effectType == effectType) {
                item.isSelect = true
                notifyDataSetChanged()
                return
            }
        }
    }
}
