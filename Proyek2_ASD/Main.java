
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/** Proyek 2 ASD - seluruh penyimpanan data menggunakan linked list buatan sendiri. */
public class Main {
    static class MataKuliah {
        String kode, nama, periode;
        int sks, semester;
        MataKuliah(String kode, String nama, int sks, int semester, String periode) {
            this.kode = kode;
            this.nama = nama;
            this.sks = sks;
            this.semester = semester;
            this.periode = periode;
        }
    }

    static class NodeMK {
        MataKuliah data;
        NodeMK next;
        NodeMK(MataKuliah data) { this.data = data; }
    }

    static class DaftarMK {
        NodeMK head, tail;
        void tambah(MataKuliah mk) {
            NodeMK baru = new NodeMK(mk);
            if (head == null) head = tail = baru;
            else { tail.next = baru; tail = baru; }
        }
        MataKuliah cari(String kode) {
            for (NodeMK p = head; p != null; p = p.next)
                if (p.data.kode.equalsIgnoreCase(kode)) return p.data;
            return null;
        }
    }

    static class Prasyarat {
        String kodeSyarat, kodeTujuan;
        Prasyarat(String syarat, String tujuan) {
            kodeSyarat = syarat;
            kodeTujuan = tujuan;
        }
    }
    static class NodePrasyarat {
        Prasyarat data;
        NodePrasyarat next;
        NodePrasyarat(Prasyarat data) { this.data = data; }
    }
    static class DaftarPrasyarat {
        NodePrasyarat head, tail;
        void tambah(String syarat, String tujuan) {
            NodePrasyarat baru = new NodePrasyarat(new Prasyarat(syarat, tujuan));
            if (head == null) head = tail = baru;
            else { tail.next = baru; tail = baru; }
        }
        String yangBelumLulus(String kodeMK, DaftarKode lulus) {
            String hasil = "";
            for (NodePrasyarat p = head; p != null; p = p.next) {
                if (p.data.kodeTujuan.equalsIgnoreCase(kodeMK)
                        && !lulus.ada(p.data.kodeSyarat)) {
                    if (!hasil.isEmpty()) hasil += ", ";
                    hasil += p.data.kodeSyarat;
                }
            }
            return hasil;
        }
    }

    static class NodeKode {
        String kode;
        NodeKode next;
        NodeKode(String kode) { this.kode = kode; }
    }
    static class DaftarKode {
        NodeKode head, tail;
        void tambah(String kode) {
            if (kode.isEmpty() || ada(kode)) return;
            NodeKode baru = new NodeKode(kode);
            if (head == null) head = tail = baru;
            else { tail.next = baru; tail = baru; }
        }
        boolean ada(String kode) {
            for (NodeKode p = head; p != null; p = p.next)
                if (p.kode.equalsIgnoreCase(kode)) return true;
            return false;
        }
    }

    static class Mahasiswa {
        String periode;
        DaftarKode lulus = new DaftarKode();
    }

    static DaftarMK bacaMataKuliah(String lokasi) throws IOException {
        DaftarMK daftar = new DaftarMK();
        try (BufferedReader br = new BufferedReader(new FileReader(lokasi))) {
            br.readLine(); // lewati header
            String baris;
            int nomor = 1;
            while ((baris = br.readLine()) != null) {
                nomor++;
                if (baris.trim().isEmpty()) continue;
                String[] kolom = baris.split(";", -1);
                if (kolom.length != 5) throw new IOException("Format mata kuliah salah pada baris " + nomor);
                String kode = kolom[0].trim();
                if (kode.isEmpty() || daftar.cari(kode) != null)
                    throw new IOException("Kode MK kosong/duplikat pada baris " + nomor);
                try {
                    daftar.tambah(new MataKuliah(kode, kolom[1].trim(),
                        Integer.parseInt(kolom[2].trim()), Integer.parseInt(kolom[3].trim()),
                        kolom[4].trim()));
                } catch (NumberFormatException e) {
                    throw new IOException("SKS/semester tidak valid pada baris " + nomor, e);
                }
            }
        }
        return daftar;
    }

    static DaftarPrasyarat bacaPrasyarat(String lokasi, DaftarMK daftar) throws IOException {
        DaftarPrasyarat syarat = new DaftarPrasyarat();
        try (BufferedReader br = new BufferedReader(new FileReader(lokasi))) {
            br.readLine();
            String baris;
            int nomor = 1;
            while ((baris = br.readLine()) != null) {
                nomor++;
                if (baris.trim().isEmpty()) continue;
                String[] kolom = baris.split(";", -1);
                if (kolom.length != 2) throw new IOException("Format prasyarat salah pada baris " + nomor);
                String asal = kolom[0].trim(), tujuan = kolom[1].trim();
                if (daftar.cari(asal) == null || daftar.cari(tujuan) == null)
                    throw new IOException("Kode prasyarat tidak dikenal pada baris " + nomor);
                syarat.tambah(asal, tujuan);
            }
        }
        return syarat;
    }

    static Mahasiswa bacaMahasiswa(String lokasi, DaftarMK daftar) throws IOException {
        Mahasiswa mhs = new Mahasiswa();
        try (BufferedReader br = new BufferedReader(new FileReader(lokasi))) {
            String baris;
            while ((baris = br.readLine()) != null) {
                baris = baris.trim();
                if (baris.isEmpty()) continue;
                int samaDengan = baris.indexOf('=');
                if (samaDengan < 0) throw new IOException("Format mahasiswa salah: " + baris);
                String kunci = baris.substring(0, samaDengan).trim();
                String nilai = baris.substring(samaDengan + 1).trim();
                if (kunci.equalsIgnoreCase("semester_berikutnya")) {
                    mhs.periode = nilai;
                } else if (kunci.equalsIgnoreCase("lulus")) {
                    if (!nilai.isEmpty()) {
                        String[] kode = nilai.split(",");
                        for (String k : kode) {
                            String bersih = k.trim();
                            if (daftar.cari(bersih) == null)
                                throw new IOException("Kode MK lulus tidak dikenal: " + bersih);
                            mhs.lulus.tambah(bersih);
                        }
                    }
                }
            }
        }
        if (mhs.periode == null ||
            (!mhs.periode.equalsIgnoreCase("ganjil") && !mhs.periode.equalsIgnoreCase("genap")))
            throw new IOException("semester_berikutnya harus Ganjil atau Genap");
        return mhs;
    }

    static boolean ditawarkan(MataKuliah mk, String periode) {
        return mk.periode.equalsIgnoreCase(periode)
                || mk.periode.equalsIgnoreCase("Keduanya");
    }

    static void rekomendasi(DaftarMK daftar, DaftarPrasyarat syarat, Mahasiswa mhs) {
        System.out.println("SEMESTER BERIKUTNYA: " + mhs.periode.toUpperCase());
        System.out.println("\nDaftar MK yang bisa diambil:");
        int jumlahBisa = 0, sksBisa = 0;
        for (NodeMK p = daftar.head; p != null; p = p.next) {
            MataKuliah mk = p.data;
            if (mhs.lulus.ada(mk.kode) || !ditawarkan(mk, mhs.periode)) continue;
            if (syarat.yangBelumLulus(mk.kode, mhs.lulus).isEmpty()) {
                jumlahBisa++;
                sksBisa += mk.sks;
                System.out.printf("%2d. %-5s %-47s %d SKS%n",
                        jumlahBisa, mk.kode, mk.nama, mk.sks);
            }
        }
        if (jumlahBisa == 0) System.out.println("(Tidak ada)");
        System.out.println("Total: " + jumlahBisa + " MK, " + sksBisa + " SKS (belum memperhitungkan batas SKS/KRS).");

        System.out.println("\nDaftar MK yang belum bisa diambil semester depan:");
        int jumlahBelum = 0;
        for (NodeMK p = daftar.head; p != null; p = p.next) {
            MataKuliah mk = p.data;
            if (mhs.lulus.ada(mk.kode) || !ditawarkan(mk, mhs.periode)) continue;
            String kurang = syarat.yangBelumLulus(mk.kode, mhs.lulus);
            if (!kurang.isEmpty()) {
                jumlahBelum++;
                System.out.println(mk.kode + " (" + mk.nama + "): belum lulus " + kurang);
            }
        }
        if (jumlahBelum == 0) System.out.println("(Tidak ada)");
        System.out.println("\nCatatan: mata kuliah yang sudah lulus atau tidak ditawarkan pada periode ini tidak ditampilkan.");
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Penggunaan: java Main matakuliah.csv prasyarat.csv mhs_01.txt");
            return;
        }
        try {
            DaftarMK daftar = bacaMataKuliah(args[0]);
            DaftarPrasyarat syarat = bacaPrasyarat(args[1], daftar);
            Mahasiswa mhs = bacaMahasiswa(args[2], daftar);
            rekomendasi(daftar, syarat, mhs);
        } catch (IOException e) {
            System.err.println("Gagal memproses berkas: " + e.getMessage());
        }
    }
}
