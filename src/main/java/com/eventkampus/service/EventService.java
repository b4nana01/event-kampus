package com.eventkampus.service;

import com.eventkampus.Event;
import com.eventkampus.dao.EventDAO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Logika CRUD Event (bagian Ana).
 * Aturan validasi sama dengan prototype HTML:
 * nama/kategori/lokasi wajib, selesai tidak boleh sebelum mulai,
 * biaya dan kuota tidak boleh negatif.
 * Pesan error berupa IllegalArgumentException berbahasa Indonesia,
 * siap ditampilkan oleh lapisan web nanti.
 */
public class EventService {

    // Nilai status di database. Ubah di sini saja kalau tabel memakai huruf besar
    // ("Aktif"/"Nonaktif"). Contoh data di HibernateTest memakai "aktif".
    public static final String STATUS_AKTIF = "Aktif";
    public static final String STATUS_NONAKTIF = "Nonaktif";

    private final EventDAO eventDAO = new EventDAO();

    // ---------- CREATE ----------
    public Event tambahEvent(Event event) {
        validasi(event);
        event.setStatus(normalisasiStatus(event.getStatus()));
        return eventDAO.save(event);
    }

    // ---------- READ ----------
    public List<Event> daftarEvent() {
        return eventDAO.findAll();
    }

    public List<Event> cariEvent(String keyword) {
        return eventDAO.searchByName(keyword);
    }

    public Event detailEvent(Long id) {
        return ambilAtauGagal(id);
    }

    // ---------- UPDATE ----------
    /** Menyalin isi dataBaru ke event dengan id tersebut. */
    public Event ubahEvent(Long id, Event dataBaru) {
        validasi(dataBaru);
        Event lama = ambilAtauGagal(id);

        lama.setNamaEvent(dataBaru.getNamaEvent().trim());
        lama.setKategori(dataBaru.getKategori().trim());
        lama.setDeskripsi(dataBaru.getDeskripsi());
        lama.setTanggalMulai(dataBaru.getTanggalMulai());
        lama.setTanggalSelesai(dataBaru.getTanggalSelesai());
        lama.setLokasi(dataBaru.getLokasi().trim());
        lama.setBiaya(dataBaru.getBiaya());
        lama.setKuota(dataBaru.getKuota());
        lama.setStatus(normalisasiStatus(dataBaru.getStatus()));

        return eventDAO.update(lama);
    }

    // ---------- "DELETE" = nonaktifkan ----------
    public void nonaktifkanEvent(Long id) {
        ubahStatus(id, STATUS_NONAKTIF);
    }

    public void aktifkanEvent(Long id) {
        ubahStatus(id, STATUS_AKTIF);
    }

    public static boolean isAktif(Event event) {
        return STATUS_AKTIF.equalsIgnoreCase(event.getStatus());
    }

    // ---------- helper ----------
    private void ubahStatus(Long id, String status) {
        if (!eventDAO.updateStatus(id, status)) {
            throw new IllegalArgumentException("Event dengan id " + id + " tidak ditemukan.");
        }
    }

    private Event ambilAtauGagal(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id event wajib diisi.");
        }
        Event event = eventDAO.findById(id);
        if (event == null) {
            throw new IllegalArgumentException("Event dengan id " + id + " tidak ditemukan.");
        }
        return event;
    }

    private String normalisasiStatus(String status) {
        if (status == null || status.isBlank()) {
            return STATUS_AKTIF;
        }
        if (status.trim().equalsIgnoreCase(STATUS_NONAKTIF)) {
            return STATUS_NONAKTIF;
        }
        if (status.trim().equalsIgnoreCase(STATUS_AKTIF)) {
            return STATUS_AKTIF;
        }
        throw new IllegalArgumentException("Status harus Aktif atau Nonaktif.");
    }

    private void validasi(Event e) {
        if (e == null) {
            throw new IllegalArgumentException("Data event tidak boleh kosong.");
        }
        wajibIsi(e.getNamaEvent(), "Nama event", 150);
        wajibIsi(e.getKategori(), "Kategori", 50);
        wajibIsi(e.getLokasi(), "Lokasi", 150);

        if (e.getTanggalMulai() == null || e.getTanggalSelesai() == null) {
            throw new IllegalArgumentException("Tanggal mulai dan tanggal selesai wajib diisi.");
        }
        if (e.getTanggalSelesai().isBefore(e.getTanggalMulai())) {
            throw new IllegalArgumentException("Tanggal selesai tidak boleh sebelum tanggal mulai.");
        }
        if (e.getBiaya() == null || e.getBiaya().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Biaya wajib diisi dan tidak boleh negatif.");
        }
        if (e.getKuota() == null || e.getKuota() < 0) {
            throw new IllegalArgumentException("Kuota wajib diisi dan tidak boleh negatif.");
        }
    }

    private void wajibIsi(String nilai, String label, int maks) {
        if (nilai == null || nilai.isBlank()) {
            throw new IllegalArgumentException(label + " wajib diisi.");
        }
        if (nilai.trim().length() > maks) {
            throw new IllegalArgumentException(label + " maksimal " + maks + " karakter.");
        }
    }
}
