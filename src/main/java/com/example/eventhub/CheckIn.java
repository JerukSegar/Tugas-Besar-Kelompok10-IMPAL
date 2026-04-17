package com.example.eventhub;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tiket_id", nullable = false)
    private Long tiketId;

    @Column(name = "penyelenggara_id", nullable = false)
    private Long penyelenggaraId;

    @Column(name = "waktu_checkin")
    private LocalDateTime waktuCheckin;

    @PrePersist
    protected void onCreate() {
        this.waktuCheckin = LocalDateTime.now();
    }

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public Long getTiketId()                     { return tiketId; }
    public void setTiketId(Long v)               { this.tiketId = v; }
    public Long getPenyelenggaraId()             { return penyelenggaraId; }
    public void setPenyelenggaraId(Long v)       { this.penyelenggaraId = v; }
    public LocalDateTime getWaktuCheckin()       { return waktuCheckin; }
    public void setWaktuCheckin(LocalDateTime v) { this.waktuCheckin = v; }
}