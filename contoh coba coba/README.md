# simple YARA Analyzer (Java / NetBeans)

Aplikasi GUI Java sederhana untuk analisis file (mis. sampel malware) menggunakan YARA. Menyediakan hexdump, ekstraksi printable strings, perhitungan entropi sederhana, dan wrapper untuk memanggil yara CLI. Termasuk kelas Sandbox berbasis SecurityManager untuk pembatasan runtime (opsional).

# PENTING (KEAMANAN)
Aplikasi ini membaca file yang mungkin berbahaya. Jangan mengandalkan aplikasi ini sendiri untuk mengisolasi malware — jalankan di lingkungan terkontrol (VM / container). SecurityManager bersifat terbatas dan mulai deprecated pada JDK modern. Untuk analisis aman gunakan isolasi OS-level.

Daftar Isi

Fitur

Struktur Proyek

Persyaratan

Instalasi & Buka di NetBeans

Menjalankan (Run) Aplikasi

# Cara Pakai (GUI)

Sandbox (opsional)

Contoh File YARA

Tweak: Mengizinkan yara saat Sandbox Aktif (Whitelist Exec)

Tips Pengembangan / Peningkatan

Troubleshooting Cepat

Catatan Keamanan & Keterbatasan

Lisensi & Kontak

Fitur

GUI Swing (NetBeans-compatible) dengan panel:

Panel kontrol: pilih sample, pilih YARA rule, Run Analysis.

Hex viewer: hexdump (batas byte agar tidak memblok UI).

Result panel: output YARA, printable strings, nilai entropi.

YaraEngine — wrapper sederhana memanggil yara -s.

Utils — hex dump, ekstrak strings, kalkulasi entropy.

Sandbox — SecurityManager-based runtime restrictions (file I/O, exec, network, process, exit).

# Semua kelas berada dalam package javaapplication3.

Struktur Proyek
src/javaapplication3/
│
├── JavaApplication3.java       // entry point
├── MainFrame.java              // JFrame utama
├── ControlPanel.java           // tombol pilih file & run
├── HexViewPanel.java           // tampil hexdump
├── ResultPanel.java            // tampil output YARA, strings, entropy
├── YaraEngine.java             // wrapper panggil `yara`
├── Utils.java                  // helper: hexDump, strings, entropy
└── Sandbox.java                // optional SecurityManager sandbox

Persyaratan

JDK 8+ (direkomendasikan JDK 11 atau 17 untuk kestabilan Swing)

Perhatian: SecurityManager deprecated pada JDK terbaru — fitur sandbox mungkin terbatas.

NetBeans IDE (direkomendasikan) atau build via javac/jar.

(Opsional) YARA terinstal dan tersedia di PATH jika ingin gunakan fitur YaraEngine yang memanggil yara CLI.

Instalasi & Buka di NetBeans

Salin/letakkan folder proyek (yang berisi nbproject) ke disk lokal.

Buka NetBeans → File → Open Project → pilih folder proyek.

Jika belum ada file sumber, buat file Java baru di package javaapplication3 dan paste kode masing‑masing kelas.

Build → Run (atau tekan F6).

Menjalankan (Run) Aplikasi
Di NetBeans

Run seperti biasa (F6).

Jika ingin mengaktifkan sandbox via VM options, buka: Project → Properties → Run → VM Options dan tambahkan VM options (lihat bagian Sandbox).

Dari command line (opsional)

Compile & jalankan:

# compile (contoh sederhana)
javac -d out/production/MyApp src/javaapplication3/*.java

# buat jar (opsional)
jar cfe SimpleYaraAnalyzer.jar javaapplication3.JavaApplication3 -C out/production/MyApp .

# jalankan
java -jar SimpleYaraAnalyzer.jar

Cara Pakai (GUI)

Jalankan aplikasi.

Pada Control Panel:

Klik Pilih File Sample... → pilih file yang ingin dianalisis (binary, dokumen, dsb).

Klik Pilih File Rule YARA... → pilih file rule .yar.

Klik Run Analysis:

Panel kiri: akan menampilkan hexdump (batas byte).

Panel kanan atas: hasil output yara -s ....

Panel kanan bawah: printable strings hasil ekstraksi.

Label entropy di atas panel kanan menampilkan nilai entropi (bits per byte).

Baca output YARA untuk mengetahui rule yang cocok dan offset/strings yang cocok.

Catatan: Project saat ini memanggil YaraEngine.runYara(...) secara sinkron. Untuk file besar atau aturan kompleks, disarankan menjalankan analisis di background (lihat Tips Pengembangan).

Sandbox (opsional)

Sandbox.java merupakan kelas standalone yang memakai SecurityManager untuk membatasi operasi JVM. Kamu tidak perlu mengubah file lain untuk menggunakan sandbox — cukup tambahkan file Sandbox.java ke project.

Aktifkan tanpa mengubah kode (VM options)

Tambahkan VM options berikut di NetBeans Project → Properties → Run → VM Options:

-Denable.sandbox=true
-Dsandbox.allowed.sample=C:\path\to\sample.bin
-Dsandbox.allowed.rule=C:\path\to\rules.yar


Sesuaikan C:\path\to\... dengan path file sample dan rule yang ingin diizinkan dibaca. Sandbox akan:

Mengizinkan read hanya pada file/dir yang ditentukan (file sample dan rule serta parent directory).

Menolak write/delete/exec/network/process/exit.

Mengizinkan read dari JRE dan working dir agar Swing tetap berjalan.

Aktifkan secara programatis (opsional)

Jika ingin aktifkan dari kode (harus memodifikasi kode lain), panggil:

Sandbox.enableFor(samplePath, rulePath);


Untuk menonaktifkan:

Sandbox.disable();

Batasan Sandbox

SecurityManager dapat dibatasi/deprecated di JDK terbaru — efektivitas mungkin berkurang.

Sandbox memblokir ProcessBuilder exec secara default, sehingga memanggil yara eksternal akan dilempar SecurityException. Jika ingin tetap memanggil yara, gunakan whitelist exec (lihat bagian selanjutnya) atau jangan aktifkan sandbox.

Contoh File YARA

Simpan sebagai example_rule.yar:

rule ExampleStringRule {
    strings:
        $s1 = "HelloWorld"
    condition:
        $s1
}


Jika sample mengandung string HelloWorld, yara -s akan melaporkan match dan offset.

Tweak: Mengizinkan yara saat Sandbox Aktif (Whitelist Exec)

Jika ingin agar sandbox tetap aktif tetapi mengizinkan eksekusi yara tertentu (kurang aman — gunakan hati-hati), kamu bisa modifikasi Sandbox.java sedikit untuk menambahkan whitelist exec.

Contoh penyesuaian singkat (tambahkan fields & pengecekan di RestrictiveSecurityManager):

// tambahkan field di Sandbox:
private static final Set<String> allowedExecCommands = new HashSet<>();

// di addAllowed atau enableFor, bisa tambahkan:
allowedExecCommands.add("yara");
allowedExecCommands.add("C:\\path\\to\\yara.exe");

// kemudian ubah checkExec:
@Override
public void checkExec(String cmd) {
    // ambil nama executable sederhana
    String lower = cmd.toLowerCase();
    for (String allowed : allowedExecCommands) {
        if (lower.contains(allowed.toLowerCase())) {
            return; // izinkan
        }
    }
    throw new SecurityException("Execution of external commands denied: " + cmd);
}


Perhatian: whitelist exec mengurangi keamanan sandbox — hanya gunakan jika kamu memahami risikonya dan tetap menjalankan di lingkungan terisolasi.

Tips Pengembangan / Peningkatan

Jangan jalankan analisis besar di EDT (UI thread) — gunakan SwingWorker agar UI tetap responsif.

Scroll-to-offset: jika output YARA memuat offset, parse offset dan buat hex viewer scroll ke offset tersebut.

JSON output YARA: jika build YARA mendukung JSON, gunakan output JSON untuk parsing terstruktur.

Integrasi libyara: menggunakan libyara (bindings native) bisa menghindari eksekusi process eksternal, memberikan kontrol lebih baik.

Pemisahan process: jalankan YARA dan analisis berat di proses terpisah (child process) di VM terisolasi untuk keamanan.

Troubleshooting Cepat

yara not found
Pastikan YARA terinstal dan yara ada di PATH. Di Windows, pastikan folder berisi yara.exe ditambahkan ke PATH Environment Variable.

UI hang saat Run Analysis
Analisis sedang berjalan di Event Dispatch Thread — ubah agar menggunakan SwingWorker.

Sandbox memblokir yara
Sandbox default memblokir exec. Untuk tetap gunakan sandbox, tambahkan whitelist exec (lihat bagian sebelumnya) atau nonaktifkan sandbox saat memanggil yara.

Error baca file / permission denied
Jika sandbox aktif, pastikan path sample & rule ditambahkan ke VM options (sandbox.allowed.*) atau dipassing via Sandbox.enableFor(...).

Catatan Keamanan & Keterbatasan

Aplikasi membaca file berbahaya — selalu gunakan VM/container terisolasi untuk analisis nyata.

SecurityManager tidak sama dengan isolasi OS-level; tidak menjamin perlindungan penuh terhadap malware.

Sandbox dapat memblokir fitur yang dibutuhkan aplikasi; testing dan konfigurasi diperlukan.

Hati‑hati saat menambahkan whitelist exec atau mengizinkan write/exec.

Lisensi & Kontak

Contoh ini dibuat untuk edukasi/prototyping. Tidak ada lisensi resmi terlampir — tambahkan LICENSE sesuai kebutuhan (mis. MIT/Apache 2.0) jika kamu ingin mendistribusikannya.

Untuk bantuan tambahan (menambahkan SwingWorker, whitelist aman, parsing JSON YARA, atau contoh rule/sample), beri tahu dan aku bantu!

Contoh Perintah VM Options (ringkasan)

Di NetBeans → Project Properties → Run → VM Options:

-Denable.sandbox=true -Dsandbox.allowed.sample=C:\path\to\sample.bin -Dsandbox.allowed.rule=C:\path\to\rules.yar


Ganti C:\path\to\... sesuai path di sistem.