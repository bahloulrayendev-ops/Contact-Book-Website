package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="unlock")
@Entity
@Getter
@Setter
public class Unlock {


    @EmbeddedId
    private UnlockId id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private Client client;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cdId")
    @JoinColumn(name = "cd_id")
    private ContactDetails contactDetail;


    @Column(nullable = false)
    private int tokenSpent;

    @Column(nullable = false)
    private LocalDateTime unlockedAt;
}