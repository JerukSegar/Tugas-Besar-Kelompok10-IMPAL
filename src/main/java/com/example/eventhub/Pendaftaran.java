package com.example.eventhub;
import jakarta.persistence.*;

@Entity
@Table(name = "pendaftaran")
public class Pendaftaran {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false, unique = true)
    private String kodeTiket;

    @Column(nullable = false)
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getKodeTiket() { return kodeTiket; }
    public void setKodeTiket(String kodeTiket) { this.kodeTiket = kodeTiket; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
