package com.example.virtualapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.virtualapp.data.AppRepository
import com.example.virtualapp.databinding.ActivityVirtualSpaceBinding
import com.example.virtualapp.virtual.VirtualAppManager
import com.google.android.material.snackbar.Snackbar

class VirtualSpaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVirtualSpaceBinding
    private lateinit var virtualManager: VirtualAppManager
    private lateinit var repository: AppRepository
    private lateinit var adapter: ClonedAppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVirtualSpaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        virtualManager = VirtualAppManager(this)
        repository = AppRepository(this)

        adapter = ClonedAppAdapter(
            iconLoader = { pkg -> repository.getAppInfo(pkg)?.icon },
            onRunClick = { runCloned(it) },
            onDeleteClick = { deleteCloned(it) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadCloned()
    }

    override fun onResume() {
        super.onResume()
        loadCloned()
    }

    private fun loadCloned() {
        val list = virtualManager.listCloned()
        adapter.submitList(list)
        binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun runCloned(item: VirtualAppManager.ClonedApp) {
        val launchIntent = packageManager.getLaunchIntentForPackage(item.packageName)
        if (launchIntent == null) {
            Snackbar.make(
                binding.root,
                "Aplikasi \"${item.label}\" sudah tidak terpasang / tidak punya launcher",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        try {
            startActivity(launchIntent)
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Gagal menjalankan: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun deleteCloned(item: VirtualAppManager.ClonedApp) {
        virtualManager.removeClone(item.packageName)
        loadCloned()
        Snackbar.make(binding.root, "\"${item.label}\" dihapus dari ruang virtual", Snackbar.LENGTH_SHORT).show()
    }
}