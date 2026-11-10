package com.eventkampus;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class HibernateTest {

    public static void main(String[] args) {

        StandardServiceRegistry registry =
                new StandardServiceRegistryBuilder()
                        .configure()
                        .build();

        try {
            SessionFactory sessionFactory =
                    new MetadataSources(registry)
                            .addAnnotatedClass(User.class)
                            .addAnnotatedClass(Event.class)
                            .addAnnotatedClass(Participant.class)
                            .addAnnotatedClass(Registration.class)
                            .buildMetadata()
                            .buildSessionFactory();

            System.out.println("Hibernate berhasil terhubung ke MySQL!");

            try (var session = sessionFactory.openSession()) {

                // CREATE
                Transaction transaction = session.beginTransaction();

                Event event = new Event();
                event.setNamaEvent("Event Percobaan");
                event.setKategori("Seminar");
                event.setDeskripsi("Event untuk pengujian Hibernate");
                event.setLokasi("Jakarta");
                event.setBiaya(new BigDecimal("50000"));
                event.setTanggalMulai(
                        LocalDateTime.of(2026, 12, 1, 9, 0));
                event.setTanggalSelesai(
                        LocalDateTime.of(2026, 12, 1, 12, 0));
                event.setKuota(100);
                event.setStatus("aktif");

                session.persist(event);
                transaction.commit();

                System.out.println("CREATE berhasil!");

                // READ
                Event eventDitemukan =
                        session.find(Event.class, event.getId());

                System.out.println(
                        "Nama Event: " + eventDitemukan.getNamaEvent());
                System.out.println(
                        "Kategori: " + eventDitemukan.getKategori());

                // UPDATE
                Transaction updateTransaction =
                        session.beginTransaction();

                eventDitemukan.setNamaEvent(
                        "Event Percobaan Updated");

                updateTransaction.commit();

                System.out.println("UPDATE berhasil!");

                // DELETE
                Transaction deleteTransaction =
                        session.beginTransaction();

                session.remove(eventDitemukan);
                deleteTransaction.commit();

                System.out.println("DELETE berhasil!");
            }

            sessionFactory.close();

        } catch (Exception e) {
            System.out.println("Pengujian Hibernate gagal!");
            e.printStackTrace();

        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}