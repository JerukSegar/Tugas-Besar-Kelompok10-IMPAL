package com.example.eventhub;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tiket")
public class Tiket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pendaftaran_id", nullable = false)
    private Long pendaftaranId;

    @Column(name = "kode_qr", nullable = false, unique = true)
    private String kodeQr;

    @Column(name = "status_tiket", nullable = false)
    private String statusTiket = "aktif";

    @Column(name = "diterbitkan_at")
    private LocalDateTime diterbitkanAt;

    @PrePersist
    protected void onCreate() {
        this.diterbitkanAt = LocalDateTime.now();
        if (this.statusTiket == null) this.statusTiket = "aktif";
    }

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public Long getPendaftaranId()               { return pendaftaranId; }
    public void setPendaftaranId(Long v)         { this.pendaftaranId = v; }
    public String getKodeQr()                    { return kodeQr; }
    public void setKodeQr(String v)              { this.kodeQr = v; }
    public String getStatusTiket()               { return statusTiket; }
    public void setStatusTiket(String v)         { this.statusTiket = v; }
    public LocalDateTime getDiterbitkanAt()      { return diterbitkanAt; }
    public void setDiterbitkanAt(LocalDateTime v){ this.diterbitkanAt = v; }
}