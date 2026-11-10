package com.eventkampus;

import com.eventkampus.service.EventService;
import com.eventkampus.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Uji CRUD Event lewat EventService. Jalankan main() ini (bukan Main.java).
 * Data uji dihapus lagi di akhir supaya tabel events tidak kotor.
 */
public class EventCrudTest {

    public static void main(String[] args) {
        EventService service = new EventService();
        Long idUji = null;

        try {
            // CREATE
            Event e = new Event();
            e.setNamaEvent("[TEST] Workshop Hibernate");
            e.setKategori("Workshop");
            e.setDeskripsi("Event uji CRUD");
            e.setLokasi("Lab Komputer 1");
            e.setBiaya(new BigDecimal("25000"));
            e.setKuota(30);
            e.setTanggalMulai(LocalDateTime.of(2026, 12, 20, 9, 0));
            e.setTanggalSelesai(LocalDateTime.of(2026, 12, 20, 12, 0));
            e.setStatus("Aktif");
            idUji = service.tambahEvent(e).getId();
            cek("CREATE: id terisi", idUji != null);
            final Long id = idUji;

            // READ
            Event baca = service.detailEvent(idUji);
            cek("READ: nama sesuai", "[TEST] Workshop Hibernate".equals(baca.getNamaEvent()));
            cek("READ: ada di daftar", service.daftarEvent().stream()
                    .anyMatch(x -> id.equals(x.getId())));

            // SEARCH
            List<Event> hasil = service.cariEvent("hibernate");
            cek("SEARCH: huruf kecil ketemu", hasil.stream().anyMatch(x -> id.equals(x.getId())));
            cek("SEARCH: kata ngawur kosong", service.cariEvent("zzzzxxxx").isEmpty());

            // UPDATE
            Event baru = new Event();
            baru.setNamaEvent("[TEST] Workshop Hibernate (Revisi)");
            baru.setKategori("Workshop");
            baru.setDeskripsi("Sudah direvisi");
            baru.setLokasi("Lab Komputer 2");
            baru.setBiaya(new BigDecimal("30000"));
            baru.setKuota(25);
            baru.setTanggalMulai(LocalDateTime.of(2026, 12, 21, 9, 0));
            baru.setTanggalSelesai(LocalDateTime.of(2026, 12, 21, 12, 0));
            baru.setStatus("Aktif");
            service.ubahEvent(idUji, baru);
            Event setelahUbah = service.detailEvent(idUji);
            cek("UPDATE: nama berubah", setelahUbah.getNamaEvent().endsWith("(Revisi)"));
            cek("UPDATE: kuota berubah", setelahUbah.getKuota() == 25);

            // NONAKTIFKAN / AKTIFKAN
            service.nonaktifkanEvent(idUji);
            cek("NONAKTIF: status berubah", !EventService.isAktif(service.detailEvent(idUji)));
            service.aktifkanEvent(idUji);
            cek("AKTIF: status kembali", EventService.isAktif(service.detailEvent(idUji)));

            // VALIDASI
            Event salah = new Event();
            salah.setNamaEvent("x");
            salah.setKategori("x");
            salah.setLokasi("x");
            salah.setBiaya(new BigDecimal("0"));
            salah.setKuota(1);
            salah.setTanggalMulai(LocalDateTime.of(2026, 12, 2, 9, 0));
            salah.setTanggalSelesai(LocalDateTime.of(2026, 12, 1, 9, 0));
            cekError("VALIDASI: selesai sebelum mulai ditolak", () -> service.tambahEvent(salah));
            salah.setTanggalSelesai(LocalDateTime.of(2026, 12, 3, 9, 0));
            salah.setKuota(-1);
            cekError("VALIDASI: kuota negatif ditolak", () -> service.tambahEvent(salah));
            cekError("VALIDASI: id tidak ada ditolak", () -> service.nonaktifkanEvent(-999L));

            System.out.println("\nSEMUA PENGUJIAN SELESAI.");
        } finally {
            if (idUji != null) {
                hapusDataUji(idUji);
            }
            HibernateUtil.shutdown();
        }
    }

    private static void hapusDataUji(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Event e = session.find(Event.class, id);
            if (e != null) {
                session.remove(e);
            }
            tx.commit();
            System.out.println("Data uji dibersihkan.");
        }
    }

    private static void cek(String nama, boolean ok) {
        System.out.println((ok ? "[LULUS] " : "[GAGAL] ") + nama);
        if (!ok) {
            throw new AssertionError(nama);
        }
    }

    private static void cekError(String nama, Runnable aksi) {
        try {
            aksi.run();
        } catch (IllegalArgumentException ex) {
            System.out.println("[LULUS] " + nama + " -> \"" + ex.getMessage() + "\"");
            return;
        }
        throw new AssertionError(nama + " (seharusnya error)");
    }
}
