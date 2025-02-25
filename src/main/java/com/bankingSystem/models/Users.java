package com.bankingSystem.models;

import com.bankingSystem.encryption.AttributeEncryptor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

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
    LocalDateTime time;

    public Users() {}

    public Users(String userName, String email, String idNumber, String phoneNumber) {
        this.userName = userName;
        this.email = email;
        this.idNumber = idNumber;
        this.phoneNumber = phoneNumber;
        this.time = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.time = LocalDateTime.now();
        this.userId = UUID.randomUUID();
    }

    public String getUserName() {
        return userName;
    }
    public UUID getUserId(){
        return userId;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getIdNumber() {
        return idNumber;
    }
    public String getEmail() {
        return email;
    }
    public LocalDateTime getTime(){
        return time;
    }
    public void setUserName(String userName){

        this.userName = userName;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public void setIdNumber(String ID){
        this.idNumber = ID;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
}
