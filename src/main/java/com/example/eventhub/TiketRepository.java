package com.example.eventhub;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TiketRepository extends JpaRepository<Tiket, Long> {
    List<Tiket> findByPendaftaranId(Long pendaftaranId);
    Optional<Tiket> findByKodeQr(String kodeQr);
    boolean existsByKodeQr(String kodeQr);
}