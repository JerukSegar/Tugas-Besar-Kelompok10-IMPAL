package com.example.eventhub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String nama  = body.get("nama");
        String pass  = body.get("password");
        String role  = body.get("role");

        if (email == null || nama == null || pass == null || role == null
                || email.isBlank() || nama.isBlank() || pass.isBlank()) {
            return Map.of("success", false, "message", "Semua field wajib diisi!");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return Map.of("success", false, "message", "Email sudah terdaftar!");
        }

        User user = new User();
        user.setNama(nama);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(pass));
        user.setRole(role);
        userRepository.save(user);

        return Map.of("success", true, "message", "Akun berhasil dibuat!");
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String pass  = body.get("password");

        if (email == null || pass == null || email.isBlank() || pass.isBlank()) {
            return Map.of("success", false, "message", "Email dan password wajib diisi!");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Map.of("success", false, "message", "Email atau password salah!");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(pass, user.getPassword())) {
            return Map.of("success", false, "message", "Email atau password salah!");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success",   true);
        result.put("token",     UUID.randomUUID().toString());
        result.put("id",        user.getId());
        result.put("nama",      user.getNama());
        result.put("email",     user.getEmail());
        result.put("role",      user.getRole());
        result.put("telp",      user.getTelp());
        result.put("kota",      user.getKota());
        result.put("institusi", user.getInstitusi());
        return result;
    }

    @PutMapping("/update/{id}")
    public Map<String, Object> updateProfile(@PathVariable Long id,
                                             @RequestBody Map<String, String> body) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return Map.of("success", false, "message", "User tidak ditemukan!");
        }

        User user = userOpt.get();

        if (body.containsKey("nama") && body.get("nama") != null && !body.get("nama").isBlank()) {
            user.setNama(body.get("nama").trim());
        }

        if (body.containsKey("password") && body.get("password") != null && !body.get("password").isBlank()) {
            if (body.get("password").length() < 6) {
                return Map.of("success", false, "message", "Password minimal 6 karakter!");
            }
            user.setPassword(passwordEncoder.encode(body.get("password")));
        }

        if (body.containsKey("telp"))      { user.setTelp(body.get("telp")); }
        if (body.containsKey("kota"))      { user.setKota(body.get("kota")); }
        if (body.containsKey("institusi")) { user.setInstitusi(body.get("institusi")); }

        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success",   true);
        result.put("message",   "Profil berhasil diperbarui!");
        result.put("nama",      user.getNama());
        result.put("telp",      user.getTelp());
        result.put("kota",      user.getKota());
        result.put("institusi", user.getInstitusi());
        return result;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteAccount(@PathVariable Long id) {
        if (userRepository.findById(id).isEmpty()) {
            return Map.of("success", false, "message", "User tidak ditemukan!");
        }
        userRepository.deleteById(id);
        return Map.of("success", true, "message", "Akun berhasil dihapus!");
    }
}