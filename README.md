# VirtualApp (Kotlin / Android)

Aplikasi Android berbasis Kotlin untuk mengelola dan meluncurkan aplikasi dalam
"ruang virtual" (clone app). Dibangun dengan Gradle + GitHub Actions sehingga
APK otomatis di-compile di CI.

## Fitur
- Daftar aplikasi terinstal (label, package, ikon) dengan pencarian.
- Clone aplikasi ke ruang virtual (disimpan di SharedPreferences).
- Detail aplikasi (package, versi, UID) + tombol Launch.
- Build otomatis via GitHub Actions (debug + release unsigned APK sebagai artifact).

## Struktur