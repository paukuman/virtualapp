package com.example.virtualapp.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.virtualapp.data.AppInfo
import com.example.virtualapp.data.AppRepository
import com.example.virtualapp.databinding.ActivityMainBinding
import com.example.virtualapp.virtual.VirtualAppManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: AppRepository
    private lateinit var virtualManager: VirtualAppManager
    private lateinit var adapter: AppListAdapter

    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = AppRepository(this)
        virtualManager = VirtualAppManager(this)

        adapter = AppListAdapter(
            onItemClick = { openDetail(it) },
            onCloneClick = { cloneApp(it) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.virtualSpaceButton.setOnClickListener {
            startActivity(Intent(this, VirtualSpaceActivity::class.java))
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) = Unit
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        loadApps()
    }

    private fun loadApps() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            allApps = repository.loadInstalledApps()
            binding.progressBar.visibility = View.GONE
            filter(binding.searchInput.text?.toString().orEmpty())
        }
    }

    private fun filter(query: String) {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) allApps else allApps.filter {
            it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
        }
        adapter.submitList(filtered)
        binding.emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openDetail(app: AppInfo) {
        startActivity(Intent(this, AppDetailActivity::class.java).apply {
            putExtra(AppDetailActivity.EXTRA_PACKAGE, app.packageName)
        })
    }

    private fun cloneApp(app: AppInfo) {
        val added = virtualManager.clone(app.packageName, app.label)
        val msg = if (added) "\"${app.label}\" ditambahkan ke Ruang Virtual"
        else "\"${app.label}\" sudah ada di Ruang Virtual"
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).setAction("Buka") {
            startActivity(Intent(this, VirtualSpaceActivity::class.java))
        }.show()
    }
}