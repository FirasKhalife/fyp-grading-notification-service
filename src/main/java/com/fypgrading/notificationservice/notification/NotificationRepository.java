package com.fypgrading.notificationservice.notification;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Scoped to the current demo session by Hibernate; the native deletes manage whole copies. */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDescIdDesc(UUID recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDescIdDesc(UUID recipientId, Pageable pageable);

    long countByRecipientIdAndReadFalse(UUID recipientId);

    Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId);

    boolean existsByEventKeyAndRecipientId(String eventKey, UUID recipientId);

    @Modifying
    @Query("update Notification n set n.read = true where n.recipientId = :recipient and n.read = false")
    int markAllRead(@Param("recipient") UUID recipient);

    @Modifying
    @Query(value = "delete from notification where demo_session = :session", nativeQuery = true)
    int deleteCopy(@Param("session") String session);

    @Modifying
    @Query(value = "delete from notification", nativeQuery = true)
    int deleteAllCopies();
}
