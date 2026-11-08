# Proyek 2 — Algoritma dan Struktur Data

## Sistem Rekomendasi Pengambilan Mata Kuliah Menggunakan Singly Linked List

Program berbasis **Java** untuk menentukan mata kuliah yang dapat diambil pada semester berikutnya berdasarkan periode penawaran, mata kuliah yang sudah lulus, dan prasyarat yang tercatat pada dataset. Program juga menjelaskan mata kuliah yang belum dapat diambil karena prasyaratnya belum terpenuhi.

### Anggota kelompok

| Nama | NIM |
| --- | --- |
| Joshua Maximillian Cahaya | 255150201111053 |
| Nasywa Tifa Alzena | 255150207111113 |
| Salma Kamila | 255150207111115 |
| Dipo Hadi Bawono | 255150207111118 |

## 1. Struktur folder

```text

Project2-/
├── README.md                   # Dokumentasi utama repository
└── Proyek2_ASD/
    ├── .gitignore              # Konfigurasi file yang diabaikan Git
    ├── Main.java               # Source code program (semua class dalam satu file)
    ├── matakuliah.csv          # Data seluruh mata kuliah
    ├── prasyarat.csv           # Data relasi prasyarat
    ├── mhs_01.txt              # Contoh mahasiswa 1
    ├── mhs_02.txt              # Contoh mahasiswa 2
    ├── mhs_03.txt              # Contoh mahasiswa 3
    ├── output_mhs_01.txt       # Hasil pengujian mahasiswa 1
    ├── output_mhs_02.txt       # Hasil pengujian mahasiswa 2
    ├── output_mhs_03.txt       # Hasil pengujian mahasiswa 3
    └── Laporan_Proyek2_ASD.pdf # Laporan struktur data dan algoritma
```

**Penting:** `Main.java` memang hanya satu file agar mudah dikompilasi, tetapi file **CSV dan TXT harus tetap terpisah**. Program tidak menanamkan isi CSV/TXT langsung ke dalam kode. File-file tersebut dibaca ketika program dijalankan.

## 2. Kebutuhan

- **JDK 17** atau lebih baru (menggunakan fitur Java dasar).
- Terminal / CMD / PowerShell, atau terminal di VS Code/IntelliJ IDEA.
- Semua file input berada di folder yang sama, atau ditentukan menggunakan path yang sesuai.

Cek instalasi Java:

```bash
java -version
javac -version
```

## 3. Cara menjalankan

Buka terminal **di dalam folder `Proyek2_ASD`**, kemudian:

```bash
javac Main.java
java Main matakuliah.csv prasyarat.csv mhs_01.txt
```

Coba data mahasiswa lainnya:

```bash
java Main matakuliah.csv prasyarat.csv mhs_02.txt
java Main matakuliah.csv prasyarat.csv mhs_03.txt
```

Perintah `javac Main.java` mengompilasi source code menjadi berkas `.class`. Perintah `java Main ...` menjalankan program, sedangkan tiga argumen di belakangnya adalah **nama/path file input**. Java tidak otomatis mencari file tersebut jika lokasinya berbeda dari direktori kerja saat ini.

Bentuk umum:

```text
java Main <lokasi_matakuliah.csv> <lokasi_prasyarat.csv> <lokasi_mhs.txt>
```

Contoh Windows PowerShell bila menjalankan dari folder induk:

```powershell
javac .\Proyek2_ASD\Main.java
cd .\Proyek2_ASD
java Main matakuliah.csv prasyarat.csv mhs_01.txt
```

Jika muncul `FileNotFoundException` atau `Gagal memproses berkas`, periksa bahwa nama/path file sesuai dan terminal sedang berada di folder yang benar.

## 4. Format dan peran file input

### `matakuliah.csv` — daftar mata kuliah

File ini berisi lima kolom yang **dipisahkan oleh titik koma (`;`)**, bukan koma.

```csv
Kode;Nama;sks;semester;ganjil/genap
RPL;Rekayasa Perangkat Lunak;4;1;Ganjil
PDAS;Pemrograman Dasar;5;1;Ganjil
```

Makna kolom: `Kode` = identitas mata kuliah, `Nama` = nama mata kuliah, `sks` = bobot, `semester` = semester pada peta kurikulum, `ganjil/genap` = periode penawaran. Program membaca periode penawaran, **bukan menggunakan nomor semester sebagai batas pengambilan**.

### `prasyarat.csv` — relasi prasyarat

```csv
prasyarat;matakuliah
PDAS;PLAN
PLAN;ASD
ASD;DAA
```

Setiap baris dibaca sebagai **`prasyarat → mata kuliah tujuan`**. Misalnya `PDAS;PLAN` berarti PDAS harus sudah lulus sebelum mengambil PLAN. Jika satu mata kuliah memiliki beberapa prasyarat langsung, **semuanya harus lulus**.

### `mhs_01.txt` — kondisi mahasiswa

```text
semester_berikutnya=genap
lulus=RPL,PDAS,PKK,AOK,KAL,STP,AGM
```

`semester_berikutnya` menentukan periode yang akan diperiksa (`ganjil` atau `genap`). `lulus` adalah daftar kode mata kuliah yang telah lulus, dipisahkan koma. Untuk membuat data mahasiswa sendiri, salin contoh TXT ke `mhs_baru.txt`, lalu ubah nilai dua baris tersebut. **Jangan ubah format `key=value`.**

## 5. Cara program membaca file

Alur program saat menerima tiga argumen:

1. `main(String[] args)` menerima nama/path **tiga file**.
2. `bacaMataKuliah(args[0])` membuka CSV pertama menggunakan `FileReader` dan `BufferedReader`; setiap baris dipecah dengan `split(";", -1)`, lalu dimasukkan ke *linked list* mata kuliah buatan sendiri.
3. `bacaPrasyarat(args[1], daftar)` membuka CSV kedua; setiap relasi prasyarat disimpan dalam *linked list* relasi.
4. `bacaMahasiswa(args[2], daftar)` membuka TXT; memisahkan `key=value`, membaca periode, dan menyimpan kode mata kuliah lulus dalam *linked list* kode.
5. `rekomendasi(...)` menelusuri seluruh mata kuliah dan mengecek periode penawaran, status kelulusan, serta prasyarat.
6. Program mencetak dua kelompok hasil: **bisa diambil** dan **belum bisa diambil karena prasyarat**.

Cuplikan pembacaan dari source code:

```java
try (BufferedReader br = new BufferedReader(new FileReader(lokasi))) {
    br.readLine(); // lewati header CSV
    String baris;
    while ((baris = br.readLine()) != null) {
        String[] kolom = baris.split(";", -1);
        // tiap baris diproses dan disimpan ke linked list buatan sendiri
    }
}
```

`FileReader`/`BufferedReader` adalah fasilitas **I/O** untuk membaca berkas, **bukan struktur data koleksi bawaan**. Penggunaan `String[]` hanya sebagai hasil pemisahan satu baris, bukan penyimpanan utama kumpulan data.

## 6. Struktur data dan algoritma

Sesuai **instruksi nomor 3**, penyimpanan utama menggunakan *singly linked list* buatan sendiri:

| Elemen | Struktur | Fungsi |
| --- | --- | --- |
| Mata kuliah | `MataKuliah`, `NodeMK`, `DaftarMK` | Menyimpan seluruh mata kuliah dan mencari kode |
| Prasyarat | `Prasyarat`, `NodePrasyarat`, `DaftarPrasyarat` | Menyimpan relasi syarat → tujuan dan mencari syarat yang belum lulus |
| MK lulus | `NodeKode`, `DaftarKode` | Menyimpan kode yang sudah lulus dan mengecek keanggotaan |
| Mahasiswa | `Mahasiswa` | Menyimpan periode berikutnya dan daftar kelulusan |

Setiap node memiliki `next` yang menunjuk node berikutnya. Daftar menggunakan `head` dan `tail`, dengan operasi penambahan node di akhir dan penelusuran manual menggunakan `for`/`while`. Tidak menggunakan `ArrayList`, `LinkedList` bawaan Java, `HashMap`, `TreeMap`, `TreeSet`, ataupun library graph.

Aturan keputusan untuk satu mata kuliah:

1. Jika **sudah lulus**, lewati (tidak direkomendasikan kembali).
2. Jika **tidak ditawarkan** pada periode semester berikutnya, lewati.
3. Jika **semua prasyarat langsung** di `prasyarat.csv` sudah lulus, tampilkan pada daftar **bisa diambil**.
4. Jika **ada prasyarat yang belum lulus**, tampilkan pada daftar **belum bisa diambil**, disertai kode mata kuliah prasyarat yang kurang.

Urutan hasil mengikuti urutan baris `matakuliah.csv`.

## 7. Hasil pengujian

| Input mahasiswa | Bisa diambil | Terkendala prasyarat |
| --- | ---: | ---: |
| `mhs_01.txt` | 12 MK | 4 MK |
| `mhs_02.txt` | 5 MK | 6 MK |
| `mhs_03.txt` | 9 MK | 0 MK |

Output lengkap masing-masing pengujian terdapat pada berkas `output_mhs_01.txt`, `output_mhs_02.txt`, dan `output_mhs_03.txt`.

## 8. Batasan

- Sistem mengikuti data yang tersedia pada file pendukung; tidak menentukan batas maksimal SKS KRS, IP semester, atau syarat akademik tambahan.
- Dataset tidak mencantumkan prasyarat untuk beberapa MK seperti `CAP`, `PKL`, dan `SKR`. Sistem tidak mengarang prasyarat baru sehingga MK tersebut bisa muncul sebagai rekomendasi sesuai periodenya.
- Mata kuliah yang sudah lulus atau tidak ditawarkan pada periode berikutnya tidak masuk ke dua daftar hasil.
- Oleh karena itu, output adalah **simulasi rekomendasi berdasarkan dataset**, bukan persetujuan KRS resmi.

## 9. Pengumpulan

Unggah `Main.java`, CSV, TXT, `README.md`, dan `Laporan_Proyek2_ASD.pdf` ke repository GitHub/GitLab kelompok. Sertakan hasil pengujian untuk membantu pemeriksaan. Kumpulkan **link repository** di Brone sesuai instruksi tugas.
