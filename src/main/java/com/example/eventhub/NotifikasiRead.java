package com.example.eventhub;
import jakarta.persistence.*;

@Entity
@Table(name = "notifikasi_read",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "notifikasi_id"}))
public class NotifikasiRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "notifikasi_id", nullable = false)
    private Long notifikasiId;

    // true = sudah dibaca
    @Column(name = "sudah_dibaca")
    private boolean sudahDibaca = false;

    // true = dihapus oleh user ini (soft delete per user)
    @Column(name = "dihapus")
    private boolean dihapus = false;

    public Long getId()                   { return id; }
    public void setId(Long id)            { this.id = id; }
    public Long getUserId()               { return userId; }
    public void setUserId(Long v)         { this.userId = v; }
    public Long getNotifikasiId()         { return notifikasiId; }
    public void setNotifikasiId(Long v)   { this.notifikasiId = v; }
    public boolean isSudahDibaca()        { return sudahDibaca; }
    public void setSudahDibaca(boolean v) { this.sudahDibaca = v; }
    public boolean isDihapus()            { return dihapus; }
    public void setDihapus(boolean v)     { this.dihapus = v; }
}