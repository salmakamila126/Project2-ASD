
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
        /** Prasyarat belum lulus, termasuk prasyarat dari prasyarat (contoh PAW: "SO, IMK"). */
        String yangBelumLulus(String kodeMK, DaftarKode lulus) {
            DaftarKode hasil = new DaftarKode();
            kumpulkanBelumLulus(kodeMK, lulus, hasil);
            return hasil.gabung();
        }
        /** Penelusuran rekursif; prasyarat paling awal dicatat lebih dulu, tanpa duplikat. */
        void kumpulkanBelumLulus(String kodeMK, DaftarKode lulus, DaftarKode hasil) {
            for (NodePrasyarat p = head; p != null; p = p.next) {
                String kodeSyarat = p.data.kodeSyarat;
                if (p.data.kodeTujuan.equalsIgnoreCase(kodeMK)
                        && !lulus.ada(kodeSyarat) && !hasil.ada(kodeSyarat)) {
                    kumpulkanBelumLulus(kodeSyarat, lulus, hasil);
                    hasil.tambah(kodeSyarat);
                }
            }
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
        String gabung() {
            StringBuilder sb = new StringBuilder();
            for (NodeKode p = head; p != null; p = p.next) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(p.kode);
            }
            return sb.toString();
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
                if (asal.equalsIgnoreCase(tujuan))
                    throw new IOException("MK tidak boleh menjadi prasyarat dirinya sendiri, baris " + nomor);
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

    /** Prasyarat belum lulus. MK biasa: lewat prasyarat.csv. SKR: seluruh MK wajib lain harus lulus. */
    static String syaratBelumLulus(MataKuliah mk, DaftarMK daftar, DaftarPrasyarat syarat, DaftarKode lulus) {
        if (!mk.kode.equalsIgnoreCase("SKR")) return syarat.yangBelumLulus(mk.kode, lulus);
        String hasil = "";
        for (NodeMK p = daftar.head; p != null; p = p.next) {
            if (p.data.kode.equalsIgnoreCase("SKR")) continue;
            if (!lulus.ada(p.data.kode)) {
                if (!hasil.isEmpty()) hasil += ", ";
                hasil += p.data.kode;
            }
        }
        return hasil;
    }

    /** Satu kali penelusuran: MK bisa diambil langsung dicetak, alasan MK belum bisa ditampung dulu. */
    static void rekomendasi(DaftarMK daftar, DaftarPrasyarat syarat, Mahasiswa mhs) {
        StringBuilder belum = new StringBuilder();
        int nomor = 0;
        System.out.println("Daftar MK yang bisa diambil:");
        for (NodeMK p = daftar.head; p != null; p = p.next) {
            MataKuliah mk = p.data;
            if (mhs.lulus.ada(mk.kode) || !ditawarkan(mk, mhs.periode)) continue;
            String kurang = syaratBelumLulus(mk, daftar, syarat, mhs.lulus);
            if (kurang.isEmpty())
                System.out.println(++nomor + ". " + mk.kode + " " + mk.nama + " " + mk.sks + " SKS");
            else
                belum.append(mk.kode).append(": belum lulus ").append(kurang).append(System.lineSeparator());
        }
        if (nomor == 0) System.out.println("(Tidak ada)");

        System.out.println("\nDaftar MK yang belum bisa diambil semester depan:");
        System.out.print(belum.length() == 0 ? "(Tidak ada)" + System.lineSeparator() : belum.toString());
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
