package com.bankingSystem.models;

import com.bankingSystem.encryption.AttributeEncryptor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "app_users")
public class Users {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "name", nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String userName;

    @Column(name = "id_number", nullable = false, unique = true)
    @Convert(converter = AttributeEncryptor.class)
    private String idNumber;

    @Column(name = "email", nullable = false, unique = true)
    @Convert(converter = AttributeEncryptor.class)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    @Convert(converter = AttributeEncryptor.class)
    private String phoneNumber;

    @Column(nullable = false)
    @CreatedDate
    LocalDateTime time;

    public Users() {}

    @PrePersist
    protected void onCreate() {
        this.userId = UUID.randomUUID();
    }
}
