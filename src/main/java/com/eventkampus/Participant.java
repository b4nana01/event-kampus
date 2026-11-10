package com.eventkampus;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "participants")
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nim")
    private String nim;

    @Column(name = "nama")
    private String nama;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "telepon", nullable = false)
    private String telepon;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.PERSIST)
    private List<Registration> registrations = new ArrayList<>();

    public Participant() {
    }

    public Participant(String nim, String nama, String email, String telepon) {
        this.nim = nim;
        this.nama = nama;
        this.email = email;
        this.telepon = telepon;
    }

    public Long getId() {
        return id;
    }

    public String getNim() {
        return nim;
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    public String getTelepon() {
        return telepon;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void addRegistration(Registration registration) {
        registrations.add(registration);
        registration.setParticipant(this);
    }
}