package com.example.demo.entity;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UnlockId implements Serializable {

    private Long userId;
    private Long cdId;

    public UnlockId(Long userId, Long cdId) {
        this.userId = userId;
        this.cdId = cdId;
    }
}