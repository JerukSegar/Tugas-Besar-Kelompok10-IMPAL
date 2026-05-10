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
    @Autowired private TiketRepository tiketRepository;
    @Autowired private PendaftaranRepository pendaftaranRepository;

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody Map<String, Object> body) {
        Long userId  = Long.parseLong(body.get("userId").toString());
        Long eventId = Long.parseLong(body.get("eventId").toString());

        if (sertifikatRepository.existsByUserIdAndEventId(userId, eventId)) {
            Optional<Sertifikat> existing = sertifikatRepository.findByUserIdAndEventId(userId, eventId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Sertifikat sudah ada!");
            result.put("sertifikat", existing.get());
            return result;
        }

        List<Pendaftaran> pendList = pendaftaranRepository.findByUserId(userId)
            .stream().filter(p -> p.getEventId().equals(eventId)).toList();

        if (pendList.isEmpty()) {
            return Map.of("success", false, "message", "Kamu belum terdaftar di event ini!");
        }

        boolean sudahCheckIn = false;
        for (Pendaftaran pend : pendList) {
            List<Tiket> tikets = tiketRepository.findByPendaftaranId(pend.getId());
            if (tikets.stream().anyMatch(t -> "digunakan".equals(t.getStatusTiket()))) {
                sudahCheckIn = true;
                break;
            }
        }

        if (!sudahCheckIn) {
            return Map.of("success", false, "message", "Sertifikat hanya tersedia setelah check-in!");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Event> evOpt  = eventRepository.findById(eventId);

        if (userOpt.isEmpty() || evOpt.isEmpty()) {
            return Map.of("success", false, "message", "Data user atau event tidak ditemukan!");
        }

        User user = userOpt.get();
        Event ev  = evOpt.get();

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

    @GetMapping("/user/{userId}")
    public List<Sertifikat> getByUser(@PathVariable Long userId) {
        return sertifikatRepository.findByUserId(userId);
    }

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

    @GetMapping("/event/{eventId}")
    public List<Sertifikat> getByEvent(@PathVariable Long eventId) {
        return sertifikatRepository.findByEventId(eventId);
    }
}