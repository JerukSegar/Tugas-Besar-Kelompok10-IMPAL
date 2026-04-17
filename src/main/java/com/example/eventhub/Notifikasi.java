package com.example.eventhub;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifikasi")
public class Notifikasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "penyelenggara_id", nullable = false)
    private Long penyelenggaraId;

    @Column(nullable = false)
    private String judul;

    @Column(length = 1000, nullable = false)
    private String pesan;

    @Column(name = "dibuat_at")
    private LocalDateTime dibuatAt;

    @PrePersist
    protected void onCreate() {
        this.dibuatAt = LocalDateTime.now();
    }

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public Long getEventId()                   { return eventId; }
    public void setEventId(Long v)             { this.eventId = v; }
    public Long getPenyelenggaraId()           { return penyelenggaraId; }
    public void setPenyelenggaraId(Long v)     { this.penyelenggaraId = v; }
    public String getJudul()                   { return judul; }
    public void setJudul(String v)             { this.judul = v; }
    public String getPesan()                   { return pesan; }
    public void setPesan(String v)             { this.pesan = v; }
    public LocalDateTime getDibuatAt()         { return dibuatAt; }
    public void setDibuatAt(LocalDateTime v)   { this.dibuatAt = v; }
}