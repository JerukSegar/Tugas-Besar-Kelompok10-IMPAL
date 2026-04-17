package com.example.eventhub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/tiket")
@CrossOrigin(origins = "*")
public class TiketController {

    @Autowired private TiketRepository tiketRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private PendaftaranRepository pendaftaranRepository;
    @Autowired private SertifikatRepository sertifikatRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping("/pendaftaran/{pendaftaranId}")
    public Map<String, Object> getTiketByPendaftaran(@PathVariable Long pendaftaranId) {
        List<Tiket> list = tiketRepository.findByPendaftaranId(pendaftaranId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tiket", list);
        return result;
    }

    @GetMapping("/qr/{kodeQr}")
    public Map<String, Object> getTiketByQr(@PathVariable String kodeQr) {
        Optional<Tiket> tiketOpt = tiketRepository.findByKodeQr(kodeQr);
        if (tiketOpt.isEmpty()) {
            return Map.of("success", false, "message", "Tiket tidak ditemukan!");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tiket", tiketOpt.get());
        result.put("sudahCheckIn", checkInRepository.existsByTiketId(tiketOpt.get().getId()));
        return result;
    }

    @PostMapping("/buat")
    public Map<String, Object> buatTiket(@RequestBody Map<String, Object> body) {
        Long pendaftaranId = Long.parseLong(body.get("pendaftaranId").toString());
        String kodeTiket   = body.get("kodeTiket").toString();

        List<Tiket> existing = tiketRepository.findByPendaftaranId(pendaftaranId);
        if (!existing.isEmpty()) {
            return Map.of("success", false, "message", "Tiket sudah pernah dibuat!");
        }

        Tiket tiket = new Tiket();
        tiket.setPendaftaranId(pendaftaranId);
        tiket.setKodeQr("QR-" + kodeTiket);
        tiket.setStatusTiket("aktif");
        tiketRepository.save(tiket);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Tiket berhasil dibuat!");
        result.put("kodeQr",  tiket.getKodeQr());
        return result;
    }

    @PostMapping("/checkin")
    public Map<String, Object> checkIn(@RequestBody Map<String, Object> body) {
        String kodeQr        = body.get("kodeQr").toString();
        Long penyelenggaraId = Long.parseLong(body.get("penyelenggaraId").toString());

        Optional<Tiket> tiketOpt = tiketRepository.findByKodeQr(kodeQr);
        if (tiketOpt.isEmpty()) {
            return Map.of("success", false, "message", "Tiket tidak ditemukan!");
        }

        Tiket tiket = tiketOpt.get();

        if (tiket.getStatusTiket().equals("digunakan")) {
            return Map.of("success", false, "message", "Tiket sudah digunakan!");
        }
        if (tiket.getStatusTiket().equals("kadaluarsa")) {
            return Map.of("success", false, "message", "Tiket sudah kadaluarsa!");
        }
        if (checkInRepository.existsByTiketId(tiket.getId())) {
            return Map.of("success", false, "message", "Peserta sudah check-in!");
        }

        // Simpan check-in
        CheckIn checkIn = new CheckIn();
        checkIn.setTiketId(tiket.getId());
        checkIn.setPenyelenggaraId(penyelenggaraId);
        checkInRepository.save(checkIn);

        // Update status tiket
        tiket.setStatusTiket("digunakan");
        tiketRepository.save(tiket);

        // AUTO GENERATE SERTIFIKAT setelah check-in
        Optional<Pendaftaran> pendOpt = pendaftaranRepository.findById(tiket.getPendaftaranId());
        String kodeSertifikat = null;

        if (pendOpt.isPresent()) {
            Pendaftaran pend = pendOpt.get();
            Long userId  = pend.getUserId();
            Long eventId = pend.getEventId();

            if (!sertifikatRepository.existsByUserIdAndEventId(userId, eventId)) {
                Optional<User>  userOpt = userRepository.findById(userId);
                Optional<Event> evOpt   = eventRepository.findById(eventId);

                if (userOpt.isPresent() && evOpt.isPresent()) {
                    User  user = userOpt.get();
                    Event ev   = evOpt.get();
                    String kode = "CERT-" + eventId + "-" + userId + "-"
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
                    kodeSertifikat = kode;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Check-in berhasil! Sertifikat telah digenerate.");
        if (kodeSertifikat != null) result.put("kodeSertifikat", kodeSertifikat);
        return result;
    }

    @GetMapping("/semua")
    public List<Tiket> getSemuaTiket() {
        return tiketRepository.findAll();
    }
}