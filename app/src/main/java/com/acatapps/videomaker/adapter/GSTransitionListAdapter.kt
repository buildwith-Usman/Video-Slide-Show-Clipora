package com.acatapps.videomaker.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.databinding.ItemGsTransitionListBinding
import com.acatapps.videomaker.models.GSTransitionDataModel
import com.acatapps.videomaker.slide_show_transition.GSTransitionUtils
import com.acatapps.videomaker.slide_show_transition.transition.GSTransition

class GSTransitionListAdapter(private val onSelectTransition:(GSTransitionDataModel)->Unit) : BaseAdapter<GSTransitionDataModel>() {

    init {
        addGSTransitionData(GSTransitionUtils.getGSTransitionList())
    }

    override fun doGetViewType(position: Int): Int = R.layout.item_gs_transition_list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemGsTransitionListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding as ItemGsTransitionListBinding
        val item = mItemList[position]

        binding.transitionNameLabel.text = item.gsTransition.transitionName
        if(item.selected) {
            binding.strokeBg.visibility = View.VISIBLE
        } else {
            binding.strokeBg.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            highlightItem(item.gsTransition)
            onSelectTransition.invoke(item)
        }
        Glide.with(holder.itemView.context)
            .load(Uri.parse("file:///android_asset/transition-preview/${item.gsTransition.transitionName}.jpg"))
            .into(binding.imagePreview)
    }

    fun addGSTransitionData(gsTransitionList:ArrayList<GSTransition>) {
        mItemList.clear()
        notifyDataSetChanged()
        for(gsTransition in gsTransitionList) {
            mItemList.add(GSTransitionDataModel(gsTransition))
        }
        notifyDataSetChanged()
    }

    fun highlightItem(gsTransition: GSTransition) {
        for(item in mItemList) {
            item.selected = item.gsTransition.transitionCodeId == gsTransition.transitionCodeId
        }
        notifyDataSetChanged()
    }

}