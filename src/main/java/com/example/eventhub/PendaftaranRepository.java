package com.example.eventhub;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PendaftaranRepository extends JpaRepository<Pendaftaran, Long> {
    List<Pendaftaran> findByUserId(Long userId);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    void deleteByUserIdAndEventId(Long userId, Long eventId);
    Pendaftaran findByUserIdAndEventId(Long userId, Long eventId);
}