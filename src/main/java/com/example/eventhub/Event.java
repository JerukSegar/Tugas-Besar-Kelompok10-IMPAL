package com.example.eventhub;

import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @Column(nullable = false)
    private String kategori;

    @Column(nullable = false)
    private String tanggal;

    @Column(nullable = false)
    private String waktu;

    @Column(nullable = false)
    private String lokasi;

    @Column(nullable = false)
    private String venue;

    @Column(length = 1000)
    private String deskripsi;

    @Column(nullable = false)
    private String penyelenggara;

    @Column(nullable = false)
    private String harga;

    @Column(nullable = false)
    private String tipeHarga;

    @Column(nullable = false)
    private int kapasitas;

    @Column(nullable = false)
    private int terisi;

    @Column(nullable = false)
    private Long createdBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
    public String getWaktu() { return waktu; }
    public void setWaktu(String waktu) { this.waktu = waktu; }
    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public String getPenyelenggara() { return penyelenggara; }
    public void setPenyelenggara(String penyelenggara) { this.penyelenggara = penyelenggara; }
    public String getHarga() { return harga; }
    public void setHarga(String harga) { this.harga = harga; }
    public String getTipeHarga() { return tipeHarga; }
    public void setTipeHarga(String tipeHarga) { this.tipeHarga = tipeHarga; }
    public int getKapasitas() { return kapasitas; }
    public void setKapasitas(int kapasitas) { this.kapasitas = kapasitas; }
    public int getTerisi() { return terisi; }
    public void setTerisi(int terisi) { this.terisi = terisi; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}