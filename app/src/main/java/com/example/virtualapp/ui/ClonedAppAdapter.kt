package com.example.virtualapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.virtualapp.databinding.ItemClonedAppBinding
import com.example.virtualapp.virtual.VirtualAppManager

class ClonedAppAdapter(
    private val iconLoader: (String) -> android.graphics.drawable.Drawable?,
    private val onRunClick: (VirtualAppManager.ClonedApp) -> Unit,
    private val onDeleteClick: (VirtualAppManager.ClonedApp) -> Unit
) : ListAdapter<VirtualAppManager.ClonedApp, ClonedAppAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemClonedAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemClonedAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.binding.appLabel.text = item.label
        holder.binding.appPackage.text = item.packageName
        holder.binding.appIcon.setImageDrawable(iconLoader(item.packageName))
        holder.binding.runButton.setOnClickListener { onRunClick(item) }
        holder.binding.deleteButton.setOnClickListener { onDeleteClick(item) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<VirtualAppManager.ClonedApp>() {
            override fun areItemsTheSame(
                a: VirtualAppManager.ClonedApp,
                b: VirtualAppManager.ClonedApp
            ) = a.packageName == b.packageName

            override fun areContentsTheSame(
                a: VirtualAppManager.ClonedApp,
                b: VirtualAppManager.ClonedApp
            ) = a == b
        }
    }
}