package com.example.eventhub;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotifikasiRepository extends JpaRepository<Notifikasi, Long> {
    List<Notifikasi> findByEventId(Long eventId);
    List<Notifikasi> findByPenyelenggaraId(Long penyelenggaraId);
}