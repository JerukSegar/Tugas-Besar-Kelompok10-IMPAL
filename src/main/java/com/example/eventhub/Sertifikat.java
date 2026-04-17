package com.example.eventhub;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sertifikat")
public class Sertifikat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "kode_sertifikat", nullable = false, unique = true)
    private String kodeSertifikat;

    @Column(name = "nama_peserta", nullable = false)
    private String namaPeserta;

    @Column(name = "nama_event", nullable = false)
    private String namaEvent;

    @Column(name = "tanggal_event", nullable = false)
    private String tanggalEvent;

    @Column(name = "penyelenggara", nullable = false)
    private String penyelenggara;

    @Column(name = "diterbitkan_at")
    private LocalDateTime diterbitkanAt;

    @PrePersist
    protected void onCreate() {
        this.diterbitkanAt = LocalDateTime.now();
    }

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public Long getUserId()                      { return userId; }
    public void setUserId(Long v)                { this.userId = v; }
    public Long getEventId()                     { return eventId; }
    public void setEventId(Long v)               { this.eventId = v; }
    public String getKodeSertifikat()            { return kodeSertifikat; }
    public void setKodeSertifikat(String v)      { this.kodeSertifikat = v; }
    public String getNamaPeserta()               { return namaPeserta; }
    public void setNamaPeserta(String v)         { this.namaPeserta = v; }
    public String getNamaEvent()                 { return namaEvent; }
    public void setNamaEvent(String v)           { this.namaEvent = v; }
    public String getTanggalEvent()              { return tanggalEvent; }
    public void setTanggalEvent(String v)        { this.tanggalEvent = v; }
    public String getPenyelenggara()             { return penyelenggara; }
    public void setPenyelenggara(String v)       { this.penyelenggara = v; }
    public LocalDateTime getDiterbitkanAt()      { return diterbitkanAt; }
    public void setDiterbitkanAt(LocalDateTime v){ this.diterbitkanAt = v; }
}