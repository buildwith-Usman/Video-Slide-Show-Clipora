package com.acatapps.videomaker.base

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class BaseViewHolder(open val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)