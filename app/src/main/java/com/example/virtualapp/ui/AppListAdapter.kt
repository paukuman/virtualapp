package com.example.virtualapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.virtualapp.data.AppInfo
import com.example.virtualapp.databinding.ItemAppBinding

class AppListAdapter(
    private val onItemClick: (AppInfo) -> Unit,
    private val onCloneClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppListAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.binding.appLabel.text = item.label
        holder.binding.appPackage.text = item.packageName
        holder.binding.appIcon.setImageDrawable(item.icon)
        holder.binding.root.setOnClickListener { onItemClick(item) }
        holder.binding.cloneButton.setOnClickListener { onCloneClick(item) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(a: AppInfo, b: AppInfo) =
                a.packageName == b.packageName

            override fun areContentsTheSame(a: AppInfo, b: AppInfo) = a == b
        }
    }
}