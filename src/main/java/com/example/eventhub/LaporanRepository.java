package com.example.eventhub;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LaporanRepository extends JpaRepository<Laporan, Long> {
    List<Laporan> findByEventId(Long eventId);
    Optional<Laporan> findFirstByEventIdOrderByDibuatAtDesc(Long eventId);
}
