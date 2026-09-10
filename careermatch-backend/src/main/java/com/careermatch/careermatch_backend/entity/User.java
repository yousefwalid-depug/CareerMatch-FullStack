package com.careermatch.careermatch_backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 320) private String email;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;

    protected User() {}
    public User(UUID id, String email, Instant createdAt) { this.id = id; this.email = email; this.createdAt = createdAt; }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
}
