package com.eventkampus.util;

import com.eventkampus.Event;
import com.eventkampus.Participant;
import com.eventkampus.Registration;
import com.eventkampus.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Satu SessionFactory untuk seluruh aplikasi.
 * Memakai hibernate.cfg.xml milik project (tidak diubah) dan mendaftarkan
 * entity yang sama seperti di HibernateTest.java.
 */
public final class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        StandardServiceRegistry registry =
                new StandardServiceRegistryBuilder().configure().build();
        try {
            return new MetadataSources(registry)
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Event.class)
                    .addAnnotatedClass(Participant.class)
                    .addAnnotatedClass(Registration.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        SESSION_FACTORY.close();
    }
}
