package com.fypgrading.notificationservice.activity;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository extends JpaRepository<ActivityEntry, UUID> {

    Page<ActivityEntry> findAllByOrderByAtDesc(Pageable pageable);

    /** One team's history (plan 3.4): every feed line linked to the team. */
    Page<ActivityEntry> findByLink_TeamIdOrderByAtDesc(UUID teamId, Pageable pageable);

    @Modifying
    @Query(value = "delete from activity where demo_session = :session", nativeQuery = true)
    int deleteCopy(@Param("session") String session);

    @Modifying
    @Query(value = "delete from activity", nativeQuery = true)
    int deleteAllCopies();
}
