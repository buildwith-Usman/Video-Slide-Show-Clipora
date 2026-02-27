package com.acatapps.videomaker.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.acatapps.videomaker.R
import com.acatapps.videomaker.base.BaseAdapter
import com.acatapps.videomaker.base.BaseViewHolder
import com.acatapps.videomaker.databinding.ItemAllMyStudioBinding
import com.acatapps.videomaker.databinding.ItemHeaderViewDateBinding
import com.acatapps.videomaker.models.MyStudioDataModel
import com.acatapps.videomaker.utils.DimenUtils
import com.acatapps.videomaker.utils.Utils
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class AllMyStudioAdapter : BaseAdapter<MyStudioDataModel>() {

    var onSelectChange:((Boolean)->Unit)? = null
    var onLongPress:(()->Unit)? = null
    var onClickItem:((MyStudioDataModel)->Unit)? = null
    var selectMode = false

    var onClickOpenMenu:((View, MyStudioDataModel)->Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            R.layout.item_header_view_date -> {
                val binding = ItemHeaderViewDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BaseViewHolder(binding)
            }
            else -> {
                val binding = ItemAllMyStudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BaseViewHolder(binding)
            }
        }
    }

    override fun doGetViewType(position: Int): Int {
        return if(mItemList[position].filePath.isEmpty()) {
            R.layout.item_header_view_date
        } else {
            R.layout.item_all_my_studio
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = mItemList[position]
        val context = holder.itemView.context
        
        if(getItemViewType(position) == R.layout.item_header_view_date) {
            val binding = ItemHeaderViewDateBinding.bind(holder.itemView)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = item.dateAdded

            val today = Calendar.getInstance()
            if(calendar.timeInMillis > today.timeInMillis) {
                binding.dateAddedLabel.text = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH)+1}/${calendar.get(Calendar.YEAR)}"
            } else {
                if(calendar.get(Calendar.YEAR) != today.get(Calendar.YEAR)) {
                    binding.dateAddedLabel.text = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH)+1}/${calendar.get(Calendar.YEAR)}"
                } else {
                    if(calendar.get(Calendar.MONTH) != today.get(Calendar.MONTH)) {
                        binding.dateAddedLabel.text = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH)+1}/${calendar.get(Calendar.YEAR)}"
                    } else {
                        if(calendar.get(Calendar.DAY_OF_MONTH) != today.get(Calendar.DAY_OF_MONTH)) {
                            if(today.timeInMillis-calendar.timeInMillis < (24*60*60*1000)) {
                                binding.dateAddedLabel.text = context.getString(R.string.yesterday)
                            } else {
                                binding.dateAddedLabel.text = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH)+1}/${calendar.get(Calendar.YEAR)}"
                            }
                        } else {
                            binding.dateAddedLabel.text = context.getString(R.string.today)
                        }
                    }
                }
            }

        } else if(getItemViewType(position) == R.layout.item_all_my_studio) {
            val binding = ItemAllMyStudioBinding.bind(holder.itemView)
            val size = DimenUtils.density(context)*98
            Glide.with(context)
                .load(item.filePath)
                .placeholder(R.drawable.ic_load_thumb)
                .apply(RequestOptions().override(size.toInt()))
                .into(binding.imageThumb)

            binding.durationLabel.text = Utils.convertSecToTimeString((item.duration.toFloat()/1000).roundToInt())
            binding.checkbox.isSelected = item.checked

            if(selectMode) {
                binding.checkbox.visibility = View.VISIBLE
            } else {
                binding.checkbox.visibility = View.GONE
            }

            binding.checkbox.setOnClickListener {
                item.checked = !item.checked
                binding.checkbox.isSelected = item.checked
                onSelectChange?.invoke(item.checked)
            }

            binding.icOpenMenu.setOnClickListener {
                if(!selectMode) {
                    onClickOpenMenu?.invoke(binding.icOpenMenu, item)
                }
            }
            holder.itemView.setOnLongClickListener {
                onLongPress?.invoke()
                return@setOnLongClickListener true
            }
            holder.itemView.setOnClickListener {
                onClickItem?.invoke(item)
            }
            if(selectMode) {
                binding.icOpenMenu.alpha = 0.2f
            } else {
                binding.icOpenMenu.alpha = 1f
            }
        }
    }

    override fun setItemList(arrayList:ArrayList<MyStudioDataModel>) {
        mItemList.clear()
        if(arrayList.size < 1) return
        val finalItems = arrayListOf<MyStudioDataModel>()

        finalItems.add(MyStudioDataModel("",arrayList[0].dateAdded, arrayList[0].duration))
        finalItems.add(arrayList[0])

        val preItemCalendar = Calendar.getInstance()
        val currentItemCalendar = Calendar.getInstance()
        for(index in 1 until arrayList.size) {
            val preItem = arrayList[index-1]
            val item = arrayList[index]

            preItemCalendar.timeInMillis = preItem.dateAdded
            currentItemCalendar.timeInMillis = item.dateAdded

            if(preItemCalendar.get(Calendar.YEAR) != currentItemCalendar.get(Calendar.YEAR)) {
                finalItems.add(MyStudioDataModel("", item.dateAdded, -1))
                finalItems.add(item)
            } else {
                if(preItemCalendar.get(Calendar.MONTH) != currentItemCalendar.get(Calendar.MONTH)) {
                    finalItems.add(MyStudioDataModel("", item.dateAdded, item.duration))
                    finalItems.add(item)
                } else {
                    if(preItemCalendar.get(Calendar.DAY_OF_MONTH) != currentItemCalendar.get(
                            Calendar.DAY_OF_MONTH)) {
                        finalItems.add(MyStudioDataModel("", item.dateAdded,item.duration))
                        finalItems.add(item)
                    } else {
                        finalItems.add(item)
                    }
                }
            }
        }
        mItemList.clear()
        mItemList.addAll(finalItems)
    }

    fun selectAll() {
        for(item in mItemList) {
            if(item.filePath.length > 5)
            item.checked = true
        }
        notifyDataSetChanged()
    }

    fun setOffAll() {
        for(item in mItemList) {
            item.checked = false
        }
        notifyDataSetChanged()
    }

    fun getNumberItemSelected() :Int{
        var count = 0
        for(item in mItemList) {
            if(item.checked && item.filePath.isNotEmpty()) ++count
        }
        return count
    }

    fun getTotalItem():Int {
        var count = 0
        for(item in mItemList) {
            if(item.filePath.length > 5) ++count
        }
        return count
    }

    fun onDeleteItem(path:String) {

        for(index in 0 until mItemList.size) {
            val item = mItemList[index]
            if(item.filePath == path) {
                mItemList.removeAt(index)
                notifyItemRemoved(index)
                break
            }
        }

        deleteEmptyDay()

    }

    fun deleteEmptyDay() {
        for(index in 0 until mItemList.size) {
            val item = mItemList[index]
            if(item.filePath.isEmpty()) {
                if(index == mItemList.size-1) {
                    mItemList.removeAt(index)
                    notifyItemRemoved(index)
                    return
                } else {
                    val nextItem = mItemList[index+1]
                    if(nextItem.filePath.isEmpty()) {
                        mItemList.removeAt(index)
                        notifyItemRemoved(index)
                        return
                    }
                }
            }
        }
    }

    fun checkDeleteItem() {
        for(index in 0 until mItemList.size) {
            val item = mItemList[index]
           if(!File(item.filePath).exists()) {
               mItemList.removeAt(index)
           }
        }
    }
}
