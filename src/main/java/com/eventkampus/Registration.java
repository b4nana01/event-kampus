package com.eventkampus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "tanggal_daftar")
    private LocalDateTime tanggalDaftar;

    @Column(name = "status_pendaftaran")
    private String statusPendaftaran;

    @Column(name = "status_pembayaran")
    private String statusPembayaran;

    @Column(name = "status_kehadiran")
    private String statusKehadiran;

    public Registration() {
    }

    public Long getId() {
        return id;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}