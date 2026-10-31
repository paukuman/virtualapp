package com.example.virtualapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.virtualapp.data.AppRepository
import com.example.virtualapp.databinding.ActivityAppDetailBinding
import com.google.android.material.snackbar.Snackbar

class AppDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pkg = intent.getStringExtra(EXTRA_PACKAGE)
        if (pkg == null) {
            finish()
            return
        }

        val repo = AppRepository(this)
        val info = repo.getAppInfo(pkg)
        if (info == null) {
            Snackbar.make(binding.root, "App tidak ditemukan", Snackbar.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.detailLabel.text = info.label
        binding.detailPackage.text = "Package: ${info.packageName}"
        binding.detailVersion.text = "Version: ${info.versionName ?: "-"}"
        binding.detailUid.text = "UID: ${info.uid}"

        binding.launchButton.setOnClickListener {
            val launchIntent = packageManager.getLaunchIntentForPackage(info.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Snackbar.make(binding.root, "Tidak ada launcher activity", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE = "extra_package"
    }
}