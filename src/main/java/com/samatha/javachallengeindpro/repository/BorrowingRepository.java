package com.samatha.javachallengeindpro.repository;

import com.samatha.javachallengeindpro.dto.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
//    @Query("SELECT 1 FROM borrowings b WHERE b.book_id = :bookId AND b.returned_date IS NULL")
    boolean existsByBookIdAndReturnedDateIsNull(Long bookId);

}
