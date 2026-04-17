package com.example.eventhub;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column
    private String telp;

    @Column
    private String kota;

    @Column
    private String institusi;

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public String getNama()                    { return nama; }
    public void setNama(String nama)           { this.nama = nama; }
    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }
    public String getPassword()                { return password; }
    public void setPassword(String password)   { this.password = password; }
    public String getRole()                    { return role; }
    public void setRole(String role)           { this.role = role; }
    public String getTelp()                    { return telp != null ? telp : ""; }
    public void setTelp(String telp)           { this.telp = telp; }
    public String getKota()                    { return kota != null ? kota : ""; }
    public void setKota(String kota)           { this.kota = kota; }
    public String getInstitusi()               { return institusi != null ? institusi : ""; }
    public void setInstitusi(String institusi) { this.institusi = institusi; }
}