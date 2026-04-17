package com.example.eventhub;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SertifikatRepository extends JpaRepository<Sertifikat, Long> {

    List<Sertifikat> findByUserId(Long userId);

    List<Sertifikat> findByEventId(Long eventId);

    Optional<Sertifikat> findByUserIdAndEventId(Long userId, Long eventId);

    Optional<Sertifikat> findByKodeSertifikat(String kodeSertifikat);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}   