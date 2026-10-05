package com.example.demo.repository;

import com.example.demo.entity.Unlock;
import com.example.demo.entity.UnlockId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnlockRepository extends JpaRepository<Unlock, UnlockId> {
    boolean existsByClient_ClientIdAndContactDetail_CdId(Long clientId, Long cdId);

    Page<Unlock> findByClient_ClientId(Long clientId, Pageable pageable);
}
