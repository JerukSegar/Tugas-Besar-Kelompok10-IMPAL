package com.example.eventhub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/register")
     public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (userRepository.findByEmail(email).isPresent()) {
            return Map.of("success", false, "message", "Email sudah terdaftar!");
        }
        User user = new User();
        user.setNama(body.get("nama"));
        user.setEmail(email);
        user.setPassword(body.get("password"));
        user.setRole(body.get("role"));
        userRepository.save(user);
        return Map.of("success", true);
    }

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        Optional<User> user = userRepository.findByEmailAndPassword(email, password);
        if (user.isPresent()) {
            return Map.of("success", true);
        } else {
            return Map.of("success", false);
        }
    }
}