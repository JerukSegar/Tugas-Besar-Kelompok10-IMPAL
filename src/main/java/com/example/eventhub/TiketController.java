package com.example.eventhub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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

    // ── Helper: parse waktu selesai dari format "13.00–14.00 WIB" ─────────────
    private LocalTime parseWaktuSelesai(String waktu) {
        try {
            if (waktu == null || waktu.isBlank()) return null;
            String[] parts = waktu.split("[\\u2013\\-]");
            if (parts.length < 2) return null;
            String selesaiStr = parts[1].trim()
                    .replace(".", ":")
                    .replaceAll("[^0-9:]", "")
                    .trim();
            return LocalTime.parse(selesaiStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return null;
        }
    }

    // ── Helper: parse tanggal dari format "2026-05-10" ────────────────────────
    private LocalDate parseTanggal(String tanggal) {
        try {
            if (tanggal == null || tanggal.isBlank()) return null;
            return LocalDate.parse(tanggal.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return null;
        }
    }

    // ── Helper: cek apakah event sudah selesai ────────────────────────────────
    private boolean isEventSudahSelesai(Event ev) {
        try {
            LocalDate tglEvent = parseTanggal(ev.getTanggal());
            LocalTime selesai  = parseWaktuSelesai(ev.getWaktu());
            if (tglEvent == null || selesai == null) return false;
            LocalDate today    = LocalDate.now();
            LocalTime sekarang = LocalTime.now();
            if (today.isAfter(tglEvent)) return true;
            if (today.isEqual(tglEvent) && sekarang.isAfter(selesai)) return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

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

        // ── BLOKIR check-in jika event sudah selesai ──────────────────────────
        Optional<Pendaftaran> pendOpt = pendaftaranRepository.findById(tiket.getPendaftaranId());
        if (pendOpt.isPresent()) {
            Long eventId = pendOpt.get().getEventId();
            Optional<Event> evOpt = eventRepository.findById(eventId);
            if (evOpt.isPresent()) {
                Event ev = evOpt.get();
                if (isEventSudahSelesai(ev)) {
                    return Map.of("success", false,
                        "message", "Check-in gagal! Waktu event sudah selesai (" + ev.getWaktu() + ").");
                }
            }
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
        String kodeSertifikat = null;
        if (pendOpt.isPresent()) {
            Pendaftaran pend = pendOpt.get();
            Long userId  = pend.getUserId();
            Long eventId = pend.getEventId();

            if (!sertifikatRepository.existsByUserIdAndEventId(userId, eventId)) {
                Optional<User>  userOpt = userRepository.findById(userId);
                Optional<Event> evOpt2  = eventRepository.findById(eventId);

                if (userOpt.isPresent() && evOpt2.isPresent()) {
                    User  user = userOpt.get();
                    Event ev   = evOpt2.get();
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