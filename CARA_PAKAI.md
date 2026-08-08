# 📱 Panduan Build APK — Lewat GitHub Actions (Bisa dari HP!)

Karena kamu belum punya PC dan Android Studio gak bisa jalan di HP, kita pakai cara ini:
**Termux cuma dipakai buat kirim/upload kode ke GitHub** (ringan, bukan buat compile). Nanti **GitHub yang compile-in APK-nya di server mereka** — gratis. Kamu tinggal download hasilnya.

---

## Langkah 1: Buat Akun GitHub

1. Buka https://github.com lewat browser HP
2. Daftar akun gratis (isi email, username, password)

## Langkah 2: Buat Repository Baru

1. Setelah login, klik ikon **+** di kanan atas → **New repository**
2. Kasih nama misalnya `alpha-chatbot`
3. Pilih **Public** atau **Private** (bebas)
4. **JANGAN** centang "Add a README file"
5. Klik **Create repository**
6. Nanti muncul halaman dengan alamat repo, formatnya:
   `https://github.com/USERNAME-KAMU/alpha-chatbot.git`
   (catat ini, dipakai di langkah berikutnya)

## Langkah 3: Extract ZIP di Termux

1. Buka Termux, extract file `AIChatbot.zip` yang saya kasih (pindahkan dulu ke folder Termux, misal via `termux-setup-storage` lalu `cp`)
2. Install tools yang dibutuhkan:
   ```
   pkg install git -y
   ```

## Langkah 4: Upload Kode ke GitHub Lewat Termux

Jalankan perintah ini satu-satu di Termux (ganti `USERNAME-KAMU` dengan username GitHub kamu):

```
cd AIChatbot
git init
git add .
git commit -m "Upload awal Alpha chatbot"
git branch -M main
git remote add origin https://github.com/USERNAME-KAMU/alpha-chatbot.git
git push -u origin main
```

Nanti diminta login GitHub (username + **Personal Access Token**, bukan password biasa — cara bikin token: di GitHub buka **Settings → Developer settings → Personal access tokens → Generate new token**, centang scope "repo", copy tokennya, dipakai sebagai password saat `git push`).

## Langkah 5: Tunggu GitHub Build Otomatis

1. Buka repo kamu di GitHub → klik tab **Actions** di atas
2. Akan muncul proses "Build APK" yang jalan otomatis (ada ikon kuning berputar = sedang proses, ijo centang = selesai)
3. Tunggu sekitar 3-7 menit

## Langkah 6: Download APK Hasilnya

1. Masih di tab **Actions**, klik run yang sudah selesai (centang hijau)
2. Scroll ke bawah, ada bagian **Artifacts**
3. Klik **Alpha-APK** untuk download (hasilnya file `.zip` kecil berisi `app-debug.apk`)
4. Extract, dapat file `app-debug.apk` — ini aplikasi jadi kamu! 🎉

## Langkah 7: Upload ke MediaFire

1. Buka mediafire.com → login/daftar → **Upload** → pilih `app-debug.apk`
2. Setelah selesai, MediaFire kasih link download
3. Share link itu ke siapa aja — mereka tinggal download & install APK-nya
4. HP mereka mungkin muncul peringatan "sumber tidak dikenal" — itu normal untuk APK di luar Play Store, tinggal pilih **"Tetap install"**

---

## 🔁 Kalau Nanti Mau Update Aplikasi

Ubah kode di HP (lewat Termux atau text editor), lalu ulangi:
```
git add .
git commit -m "Update fitur X"
git push
```
GitHub otomatis build ulang APK terbaru — tinggal download lagi dari tab Actions.



---

## 🚀 Cara Share Lewat MediaFire (biar semua orang bisa download)

Karena gak lewat Play Store, ini lebih simpel:

1. Build APK **release** (lebih optimal daripada debug) — di Android Studio: **Build → Generate Signed Bundle/APK → APK**
   - Pas diminta "signing key", klik **Create new...**, isi data apa aja (password, nama, dll), simpan filenya baik-baik (dipakai lagi kalau nanti update aplikasi)
   - Pilih build variant **release**
   - Hasilnya ada di: `AIChatbot/app/release/app-release.apk`
   - Kalau males bikin signing key, pakai aja hasil build **debug** (`app-debug.apk`) — tetap bisa diinstall normal, cuma kurang optimal untuk performa
2. Buka **mediafire.com** → daftar/login → **Upload** → pilih file `.apk` tadi
3. Setelah upload selesai, MediaFire kasih **link download**
4. Share link itu ke siapa aja — mereka tinggal klik, download APK-nya, lalu install di HP Android masing-masing

**Catatan buat orang yang download:**
- HP mereka mungkin nampilin peringatan "Install dari sumber tidak dikenal" atau "Play Protect memblokir" — itu wajar untuk APK di luar Play Store, mereka tinggal pilih **"Tetap install"** / **"Izinkan"**
- Setiap orang yang install wajib masukin **API key Anthropic mereka sendiri** (klik ikon ⚙️ di app) supaya chatbotnya bisa jalan

---

## ⚙️ Cara Kerja Aplikasi Ini

- Setiap user yang install aplikasi ini akan diminta memasukkan **API Key Anthropic mereka sendiri** (klik ikon ⚙️ di app)
- API key disimpan **hanya di HP masing-masing user** (tidak dikirim ke server manapun kecuali langsung ke Anthropic)
- Kamu (sebagai pembuat app) **tidak perlu menanggung biaya** pemakaian AI orang lain
- User bisa dapat API key gratis (dengan kredit awal) di: https://console.anthropic.com

## ✏️ Kalau Mau Ubah Sesuatu

- Nama aplikasi sekarang: **Alpha** → kalau mau ganti lagi, edit `app/src/main/res/values/strings.xml`
- Logo sudah diganti pakai logo yang kamu kasih (ada di folder `mipmap-*`)
- Ganti warna tema → edit `MainActivity.kt`, cari baris warna di bagian atas file (`BgDark`, `AccentPurple`, dll)
- Ganti instruksi default AI / kepribadian Alpha → edit `SettingsStore.kt`, bagian `DEFAULT_PROMPT`
- Ganti tombol quick-reply (Jam berapa, Kasih ide, dll) → edit `MainActivity.kt`, cari `quickReplies`

---

Kalau ada error waktu build, screenshot aja errornya dan tanya saya lagi — saya bisa bantu debug! 😊
