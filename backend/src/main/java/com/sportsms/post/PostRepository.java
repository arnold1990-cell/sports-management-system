package com.sportsms.post;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query("select p from Post p where p.status = 'PUBLISHED' " +
            "and (:keyword is null or lower(p.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(cast(p.content as string)) like lower(concat('%', :keyword, '%'))) " +
            "and (:fromDate is null or p.createdAt >= :fromDate) " +
            "and (:toDate is null or p.createdAt <= :toDate)")
    Page<Post> searchPublished(@Param("keyword") String keyword,
                               @Param("fromDate") Instant fromDate,
                               @Param("toDate") Instant toDate,
                               Pageable pageable);

    @Query("select p from Post p where (:status is null or p.status = :status) " +
            "and (:keyword is null or lower(p.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(cast(p.content as string)) like lower(concat('%', :keyword, '%')))")
    Page<Post> searchAll(@Param("keyword") String keyword,
                         @Param("status") PostStatus status,
                         Pageable pageable);
}
