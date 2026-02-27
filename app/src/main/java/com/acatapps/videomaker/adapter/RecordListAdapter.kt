package com.acatapps.videomaker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.databinding.ItemRecordedBinding
import com.acatapps.videomaker.models.RecordedDataModel

class RecordListAdapter : BaseAdapter<RecordedDataModel>() {
    var onSelect: ((RecordedDataModel) -> Unit)? = null
    override fun doGetViewType(position: Int): Int = R.layout.item_recorded

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding =
            ItemRecordedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding as ItemRecordedBinding
        val item = mItemList[position]
        if (item.isSelect) {
            binding.grayBg.visibility = View.VISIBLE
        } else {
            binding.grayBg.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            setOffAll()
            item.isSelect = true
            onSelect?.invoke(item)
            notifyDataSetChanged()
        }
        binding.recordName.text = "Record_${position}"
    }

    fun checkRecordExist(timeMs: Int): RecordedDataModel? {
        for (item in itemList) {
            if (item.checkTime(timeMs)) {

                return item
            }
        }
        return null
    }

    fun deleteRecord(path: String) {
        getItemByPath(path)?.let {
            mItemList.remove(it)
            setOffAll()
            notifyDataSetChanged()
        }
    }

    fun selectRecord(path: String) {
        getItemByPath(path)?.let {
            setOffAll()
            it.isSelect = true
            notifyDataSetChanged()
        }
    }

    private fun getItemByPath(path: String): RecordedDataModel? {
        for (item in mItemList) {
            if (item.path == path) {

                return item
            }
        }
        return null
    }

    fun setOffAll() {
        for (item in mItemList) {
            item.isSelect = false
        }
        notifyDataSetChanged()
    }
}