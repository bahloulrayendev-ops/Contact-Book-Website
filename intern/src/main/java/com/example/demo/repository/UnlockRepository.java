package com.example.demo.repository;

import com.example.demo.entity.Unlock;
import com.example.demo.entity.UnlockId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UnlockRepository extends JpaRepository<Unlock, UnlockId> {

        @EntityGraph(attributePaths = {"contactDetail", "contactDetail.employee", "contactDetail.company"})
        Page<Unlock> findByClientClientIdOrderByUnlockedAtDesc(Long clientId, Pageable pageable);

    // Given a client and a batch of contact-detail ids, returns only the ids that client
    // already paid for. Used to compute "locked" per contact without an N+1 (one query per page).
    @Query("select u.id.cdId from Unlock u where u.id.userId = :userId and u.id.cdId in :cdIds " +
            "and not exists (select n.notificationId from ContactUpdateNotification n " +
            "where n.client.clientId = :userId and n.contactDetail.cdId = u.id.cdId " +
            "and n.requiresRepurchase = true and n.repurchaseCompleted = false)")
    List<Long> findUnlockedCdIds(@Param("userId") Long userId, @Param("cdIds") List<Long> cdIds);

    @Query("select case when count(u) > 0 then true else false end from Unlock u " +
            "where u.id.userId = :clientId and u.id.cdId = :contactDetailId")
    boolean existsForClientAndContact(@Param("clientId") Long clientId,
                                      @Param("contactDetailId") Long contactDetailId);
}
