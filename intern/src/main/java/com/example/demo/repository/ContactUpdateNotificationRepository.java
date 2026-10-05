package com.example.demo.repository;

import com.example.demo.entity.ContactUpdateNotification;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface ContactUpdateNotificationRepository extends JpaRepository<ContactUpdateNotification, Long> {

    @EntityGraph(attributePaths = {"client", "contactDetail"})
    Page<ContactUpdateNotification> findByClientClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"client", "contactDetail"})
    @Query("select notification from ContactUpdateNotification notification " +
            "where notification.notificationId = :notificationId " +
            "and notification.client.clientId = :clientId")
    Optional<ContactUpdateNotification> findForClientForUpdate(
            @Param("notificationId") Long notificationId,
            @Param("clientId") Long clientId);

    long countByClientClientIdAndReadAtIsNull(Long clientId);

    boolean existsByClientClientIdAndContactDetailCdIdAndRequiresRepurchaseTrueAndRepurchaseCompletedFalse(
            Long clientId, Long contactDetailId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"client", "contactDetail"})
    List<ContactUpdateNotification>
    findByClientClientIdAndContactDetailCdIdAndRequiresRepurchaseTrueAndRepurchaseCompletedFalse(
            Long clientId, Long contactDetailId);
}