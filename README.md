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
Project2-ASD/
├── README.md
└── Proyek2_ASD/
    ├── .gitignore
    ├── Main.java
    ├── matakuliah.csv
    ├── prasyarat.csv
    ├── mhs_01.txt
    ├── mhs_02.txt
    ├── mhs_03.txt
    ├── mhs_04.txt
    ├── mhs_05.txt
    ├── mhs_06.txt
    ├── output_mhs_01.txt
    ├── output_mhs_02.txt
    ├── output_mhs_03.txt
    ├── output_mhs_04.txt
    ├── output_mhs_05.txt
    ├── output_mhs_06.txt
    └── Laporan_Proyek2_ASD.pdf
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

Buka terminal di root repository, kemudian:

```bash
cd Proyek2_ASD
javac Main.java
java -cp . Main matakuliah.csv prasyarat.csv mhs_01.txt
```

Coba data mahasiswa lainnya:

```bash
java -cp . Main matakuliah.csv prasyarat.csv mhs_02.txt
java -cp . Main matakuliah.csv prasyarat.csv mhs_03.txt
java -cp . Main matakuliah.csv prasyarat.csv mhs_04.txt
java -cp . Main matakuliah.csv prasyarat.csv mhs_05.txt
java -cp . Main matakuliah.csv prasyarat.csv mhs_06.txt
```

Perintah `javac Main.java` mengompilasi source code menjadi berkas `.class`. Perintah `java Main ...` menjalankan program, sedangkan tiga argumen di belakangnya adalah **nama/path file input**. Java tidak otomatis mencari file tersebut jika lokasinya berbeda dari direktori kerja saat ini.

Bentuk umum:

```text
java -cp . Main <lokasi_matakuliah.csv> <lokasi_prasyarat.csv> <lokasi_mhs.txt>
```

Contoh Windows PowerShell bila menjalankan dari folder induk:

```powershell
javac .\Proyek2_ASD\Main.java
cd .\Proyek2_ASD
java -cp . Main matakuliah.csv prasyarat.csv mhs_01.txt
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
5. `rekomendasi(...)` melakukan **satu penelusuran utama kandidat mata kuliah**, mengecek periode penawaran, status kelulusan, serta prasyarat. Pemeriksaan khusus SKR menelusuri daftar MK wajib secara terpisah.
6. MK yang bisa diambil langsung dicetak; alasan MK yang belum bisa diambil ditampung dalam `StringBuilder` dan dicetak setelah penelusuran utama selesai.

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
3. Panggil `syaratBelumLulus()` satu kali untuk setiap kandidat. Untuk MK biasa, `yangBelumLulus()` memanggil `kumpulkanBelumLulus()` secara rekursif melalui linked list relasi, mengumpulkan prasyarat yang belum lulus tanpa duplikasi, lalu `gabung()` menyusun kode-kodenya menjadi string (contoh: `PAW: belum lulus SO, IMK`). Khusus `SKR`, periksa seluruh MK wajib selain `SKR` pada `DaftarMK`, tanpa membatasi periode penawarannya. Karena TXT hanya menyimpan daftar lulus, kelulusan digunakan sebagai indikator MK telah diselesaikan.
4. Jika `syaratBelumLulus()` menghasilkan string kosong, tampilkan MK pada daftar **bisa diambil**.
5. Jika string tidak kosong, tampung kode MK dan alasan **belum bisa diambil** dalam `StringBuilder`, lalu cetak setelah penelusuran utama selesai.

Urutan hasil mengikuti urutan baris `matakuliah.csv`.

## 7. Hasil pengujian

| Input mahasiswa | Periode | Bisa diambil | Terkendala prasyarat | Total SKS (bisa) | Keterangan |
| --- | --- | ---: | ---: | ---: | --- |
| `mhs_01.txt` | Genap | 11 MK | 5 MK | 30 | 7 MK lulus; SKR ditolak |
| `mhs_02.txt` | Ganjil | 4 MK | 7 MK | 12 | 20 MK lulus; SKR ditolak |
| `mhs_03.txt` | Ganjil | 8 MK | 1 MK | 22 | 28 MK lulus; SKR ditolak |
| `mhs_04.txt` | Genap | 1 MK | 0 MK | 6 | 36 MK wajib lulus; SKR diterima |
| `mhs_05.txt` | Ganjil | 0 MK | 1 MK | 0 | semua lulus kecuali CAP; SKR ditolak |
| `mhs_06.txt` | Ganjil | 1 MK | 0 MK | 6 | 36 MK wajib lulus; SKR diterima |

Total SKS pada tabel dihitung dari MK yang dapat diambil, bukan dicetak sebagai total oleh program.

Output lengkap masing-masing pengujian terdapat pada berkas `output_mhs_01.txt` sampai `output_mhs_06.txt`. `mhs_01`-`mhs_03` adalah data dari soal; `mhs_04`-`mhs_06` adalah data uji tambahan untuk aturan khusus Skripsi.

## 8. Batasan

- Sistem mengikuti data yang tersedia pada file pendukung; tidak menentukan batas maksimal SKS KRS, IP semester, atau syarat akademik tambahan di luar aturan khusus Skripsi.
- Dataset tidak mencantumkan prasyarat untuk beberapa MK seperti `CAP` dan `PKL`. Keduanya tetap mengikuti periode penawaran dan data prasyarat. Untuk `SKR`, aturan khusus instruksi terbaru mewajibkan seluruh MK wajib lain di daftar sudah lulus.
- Mata kuliah yang sudah lulus atau tidak ditawarkan pada periode berikutnya tidak masuk ke dua daftar hasil.
- Program mengasumsikan relasi prasyarat tidak bersiklus dan riwayat kelulusan mahasiswa konsisten. Rekursi berhenti pada prasyarat yang sudah lulus; kode menolak relasi MK terhadap dirinya sendiri, tetapi belum mendeteksi siklus antar-MK.
- Oleh karena itu, output adalah **simulasi rekomendasi berdasarkan dataset**, bukan persetujuan KRS resmi.

Pengujian tambahan (`mhs_04`-`mhs_06`): Skripsi dapat diambil pada Ganjil maupun Genap jika 36 MK wajib lainnya sudah lulus; jika CAP saja belum lulus, Skripsi tetap ditolak.

## 9. Pengumpulan

[Laporan Proyek 2 ASD](Proyek2_ASD/Laporan_Proyek2_ASD.pdf)

Unggah `Main.java`, CSV, TXT, `README.md`, dan `Laporan_Proyek2_ASD.pdf` ke repository GitHub/GitLab kelompok. Sertakan hasil pengujian untuk membantu pemeriksaan. Kumpulkan **link repository** di Brone sesuai instruksi tugas.
