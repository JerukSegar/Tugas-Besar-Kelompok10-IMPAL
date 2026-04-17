package com.example.eventhub;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "laporan")
public class Laporan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "total_pendaftar", nullable = false)
    private int totalPendaftar = 0;

    @Column(name = "total_hadir", nullable = false)
    private int totalHadir = 0;

    @Column(name = "dibuat_at")
    private LocalDateTime dibuatAt;

    @PrePersist
    protected void onCreate() {
        this.dibuatAt = LocalDateTime.now();
    }

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }
    public Long getEventId()                 { return eventId; }
    public void setEventId(Long v)           { this.eventId = v; }
    public int getTotalPendaftar()           { return totalPendaftar; }
    public void setTotalPendaftar(int v)     { this.totalPendaftar = v; }
    public int getTotalHadir()               { return totalHadir; }
    public void setTotalHadir(int v)         { this.totalHadir = v; }
    public LocalDateTime getDibuatAt()       { return dibuatAt; }
    public void setDibuatAt(LocalDateTime v) { this.dibuatAt = v; }
}
