package com.example.eventhub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/sertifikat")
@CrossOrigin(origins = "*")
public class SertifikatController {

    @Autowired private SertifikatRepository sertifikatRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private TiketRepository tiketRepository;
    @Autowired private PendaftaranRepository pendaftaranRepository;

    // ===== GENERATE sertifikat setelah check-in =====
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody Map<String, Object> body) {
        Long userId  = Long.parseLong(body.get("userId").toString());
        Long eventId = Long.parseLong(body.get("eventId").toString());

        // Cek apakah sudah punya sertifikat
        if (sertifikatRepository.existsByUserIdAndEventId(userId, eventId)) {
            Optional<Sertifikat> existing = sertifikatRepository.findByUserIdAndEventId(userId, eventId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Sertifikat sudah ada!");
            result.put("sertifikat", existing.get());
            return result;
        }

        // Cek apakah sudah check-in
        List<Pendaftaran> pendList = pendaftaranRepository.findByUserId(userId)
            .stream().filter(p -> p.getEventId().equals(eventId)).toList();

        if (pendList.isEmpty()) {
            return Map.of("success", false, "message", "Kamu belum terdaftar di event ini!");
        }

        Pendaftaran pend = pendList.get(0);
        List<Tiket> tikets = tiketRepository.findByPendaftaranId(pend.getId());
        boolean sudahCheckIn = tikets.stream()
            .anyMatch(t -> t.getStatusTiket().equals("digunakan"));

        if (!sudahCheckIn) {
            return Map.of("success", false, "message", "Sertifikat hanya tersedia setelah check-in!");
        }

        // Ambil data user dan event
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Event> evOpt  = eventRepository.findById(eventId);

        if (userOpt.isEmpty() || evOpt.isEmpty()) {
            return Map.of("success", false, "message", "Data tidak ditemukan!");
        }

        User user = userOpt.get();
        Event ev  = evOpt.get();

        // Generate kode sertifikat unik
        String kode = "CERT-" + ev.getId() + "-" + userId + "-"
            + java.time.Year.now().getValue();

        Sertifikat sert = new Sertifikat();
        sert.setUserId(userId);
        sert.setEventId(eventId);
        sert.setKodeSertifikat(kode);
        sert.setNamaPeserta(user.getNama());
        sert.setNamaEvent(ev.getNama());
        sert.setTanggalEvent(ev.getTanggal());
        sert.setPenyelenggara(ev.getPenyelenggara());
        sertifikatRepository.save(sert);

        Map<String, Object> result = new HashMap<>();
        result.put("success",    true);
        result.put("message",    "Sertifikat berhasil digenerate!");
        result.put("sertifikat", sert);
        return result;
    }

    // ===== GET sertifikat milik user =====
    @GetMapping("/user/{userId}")
    public List<Sertifikat> getByUser(@PathVariable Long userId) {
        return sertifikatRepository.findByUserId(userId);
    }

    // ===== GET sertifikat by kode =====
    @GetMapping("/kode/{kode}")
    public Map<String, Object> getByKode(@PathVariable String kode) {
        Optional<Sertifikat> sertOpt = sertifikatRepository.findByKodeSertifikat(kode);
        if (sertOpt.isEmpty()) {
            return Map.of("success", false, "message", "Sertifikat tidak ditemukan!");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success",    true);
        result.put("sertifikat", sertOpt.get());
        return result;
    }

    // ===== GET sertifikat by event (untuk penyelenggara) =====
    @GetMapping("/event/{eventId}")
    public List<Sertifikat> getByEvent(@PathVariable Long eventId) {
        return sertifikatRepository.findByEventId(eventId);
    }
}