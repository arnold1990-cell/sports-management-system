package com.sportsms.post;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query(value = """
            select p.*
            from posts p
            where p.status = 'PUBLISHED'
              and (:keyword is null
                   or p.title ilike concat('%', :keyword, '%')
                   or p.content ilike concat('%', :keyword, '%'))
              and (:fromDate is null or p.created_at >= :fromDate)
              and (:toDate is null or p.created_at <= :toDate)
            """,
            countQuery = """
                    select count(*)
                    from posts p
                    where p.status = 'PUBLISHED'
                      and (:keyword is null
                           or p.title ilike concat('%', :keyword, '%')
                           or p.content ilike concat('%', :keyword, '%'))
                      and (:fromDate is null or p.created_at >= :fromDate)
                      and (:toDate is null or p.created_at <= :toDate)
                    """,
            nativeQuery = true)
    Page<Post> searchPublished(@Param("keyword") String keyword,
                               @Param("fromDate") Instant fromDate,
                               @Param("toDate") Instant toDate,
                               Pageable pageable);

    @Query(value = """
            select p.*
            from posts p
            where (:status is null or p.status = :status)
              and (:keyword is null
                   or p.title ilike concat('%', :keyword, '%')
                   or p.content ilike concat('%', :keyword, '%'))
            """,
            countQuery = """
                    select count(*)
                    from posts p
                    where (:status is null or p.status = :status)
                      and (:keyword is null
                           or p.title ilike concat('%', :keyword, '%')
                           or p.content ilike concat('%', :keyword, '%'))
                    """,
            nativeQuery = true)
    Page<Post> searchAll(@Param("keyword") String keyword,
                         @Param("status") String status,
                         Pageable pageable);
}
