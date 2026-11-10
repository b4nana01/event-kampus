package com.eventkampus;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nama_event", length = 150)
    private String namaEvent;

    @Column(name = "kategori", length = 50)
    private String kategori;

    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;

    @Column(name = "gambar", length = 255)
    private String gambar;

    @Column(name = "tanggal_mulai")
    private LocalDateTime tanggalMulai;

    @Column(name = "tanggal_selesai")
    private LocalDateTime tanggalSelesai;

    @Column(name = "lokasi", length = 150)
    private String lokasi;

    @Column(name = "biaya", precision = 12, scale = 2)
    private BigDecimal biaya;

    @Column(name = "kuota")
    private Integer kuota;

    @Column(name = "status", length = 20)
    private String status;

    @OneToMany(mappedBy = "event")
    private List<Registration> registrations = new ArrayList<>();

    public Event() {
    }

    public Long getId() {
        return id;
    }

    public String getNamaEvent() {
        return namaEvent;
    }

    public String getKategori() {
        return kategori;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getGambar() {
        return gambar;
    }

    public LocalDateTime getTanggalMulai() {
        return tanggalMulai;
    }

    public LocalDateTime getTanggalSelesai() {
        return tanggalSelesai;
    }

    public String getLokasi() {
        return lokasi;
    }

    public BigDecimal getBiaya() {
        return biaya;
    }

    public Integer getKuota() {
        return kuota;
    }

    public String getStatus() {
        return status;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setNamaEvent(String namaEvent) {
        this.namaEvent = namaEvent;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public void setBiaya(BigDecimal biaya) {
        this.biaya = biaya;
    }

    public void setKuota(Integer kuota) {
        this.kuota = kuota;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTanggalMulai(LocalDateTime tanggalMulai) {
        this.tanggalMulai = tanggalMulai;
    }

    public void setTanggalSelesai(LocalDateTime tanggalSelesai) {
        this.tanggalSelesai = tanggalSelesai;
    }
}
