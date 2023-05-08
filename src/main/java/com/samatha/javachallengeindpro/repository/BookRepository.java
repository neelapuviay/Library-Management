package com.samatha.javachallengeindpro.repository;

import com.samatha.javachallengeindpro.dto.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByCategoryContainingAndAuthorContainingAndIsbnContainingAndTitleContainingAndPublishingCompanyContaining(
            String category, String author, String isbn, String title, String publishingCompany);
}
