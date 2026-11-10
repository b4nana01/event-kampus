package com.eventkampus.dao;

import com.eventkampus.Event;
import com.eventkampus.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.function.Function;

/**
 * Akses database untuk tabel events.
 * Hanya berisi operasi database; aturan/validasi ada di EventService.
 * Sengaja TIDAK ada hapus permanen: event yang sudah punya pendaftaran
 * tidak boleh hilang, jadi "hapus" dilakukan dengan menonaktifkan (status).
 */
public class EventDAO {

    /** Menjalankan satu transaksi; rollback otomatis kalau gagal. */
    private <T> T inTransaction(Function<Session, T> work) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T result = work.apply(session);
                tx.commit();
                return result;
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    /** CREATE - setelah selesai, event.getId() sudah terisi. */
    public Event save(Event event) {
        return inTransaction(session -> {
            session.persist(event);
            return event;
        });
    }

    /** READ satu event; null jika tidak ada. */
    public Event findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Event.class, id);
        }
    }

    /** READ semua event, yang terdekat tanggalnya di atas. */
    public List<Event> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Event e order by e.tanggalMulai asc, e.id asc", Event.class)
                    .getResultList();
        }
    }

    /** SEARCH berdasarkan nama (tidak peka huruf besar/kecil). */
    public List<Event> searchByName(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Event e where lower(e.namaEvent) like :kw "
                            + "order by e.tanggalMulai asc, e.id asc", Event.class)
                    .setParameter("kw", "%" + kw + "%")
                    .getResultList();
        }
    }

    /** UPDATE - event harus sudah punya id (hasil findById/save). */
    public Event update(Event event) {
        return inTransaction(session -> session.merge(event));
    }

    /** Ubah status saja. Mengembalikan false jika id tidak ditemukan. */
    public boolean updateStatus(Long id, String status) {
        return inTransaction(session -> {
            Event event = session.find(Event.class, id);
            if (event == null) {
                return false;
            }
            event.setStatus(status);
            return true;
        });
    }
}
