package com.samatha.javachallengeindpro.controller;

import com.samatha.javachallengeindpro.dto.Book;
import com.samatha.javachallengeindpro.dto.User;
import com.samatha.javachallengeindpro.exceptions.BookNotFoundException;
import com.samatha.javachallengeindpro.exceptions.UserAccessException;
import com.samatha.javachallengeindpro.model.CustomPrincipal;
import com.samatha.javachallengeindpro.repository.BookRepository;
import com.samatha.javachallengeindpro.repository.BorrowingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/book")
public class BookController {
    private static final String ADMIN = "ADMIN";
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    // POST endpoint to add a new book (Only admins can add books)
    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book,Authentication authentication) {
        Book savedBook =null;
        // Perform necessary validations and authorization checks for admins
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));

            if (isAdmin) {
                savedBook = bookRepository.save(book);
            }else {
                throw new UserAccessException("User Doesn't have permission to add an book");
            }
        }
        // Save the book to the database
        return ResponseEntity.ok(savedBook);
    }

    // PUT endpoint to edit the details of a book (Only admins can edit)
    @PutMapping("/{id}")
    public ResponseEntity<Book> editBook(@PathVariable Long id, @RequestBody Book updatedBook,Authentication authentication) {
        Book book =null;
        Book updatedBookEntity =null;
        // Perform necessary validations and authorization checks for admins
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));

            if (isAdmin) {
                // Retrieve the existing book from the database
                book = bookRepository.findById(id)
                        .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));


                // Update the book details
                book.setTitle(updatedBook.getTitle());
                book.setAuthor(updatedBook.getAuthor());
                book.setCategory(updatedBook.getCategory());
                book.setIsbn(updatedBook.getIsbn());
                book.setPublishingCompany(updatedBook.getPublishingCompany());

                // Save the updated book to the database
                updatedBookEntity = bookRepository.save(book);
            }
        }
        return ResponseEntity.ok(updatedBookEntity);
    }

    // GET endpoint to retrieve the details of a book by its identifier
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        // Retrieve the book from the database by its identifier
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));

        return ResponseEntity.ok(book);
    }

    // GET endpoint to fetch all the books with search options
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "isbn", required = false) String isbn,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "publishingCompany", required = false) String publishingCompany
    ) {
        if(category==null)
            category = "";
        if(author ==null)
            author="";
        if (title == null) {
            title = "";
        }
        if(isbn == null){
            isbn = "";
        }
        if (publishingCompany == null) {
            publishingCompany = "";
        }
        // Perform necessary search options and fetch books from the database
        List<Book> books = bookRepository.findByCategoryContainingAndAuthorContainingAndIsbnContainingAndTitleContainingAndPublishingCompanyContaining(category, author, isbn, title, publishingCompany);

        return ResponseEntity.ok(books);
    }

    // DELETE endpoint to delete a book (Only admins can delete)
/*    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));

            if (isAdmin) {
                // Check if the book has been borrowed by someone
                boolean isBorrowed = borrowingRepository.existsByBookIdAndIsBorrowedTrue(id);

                if (isBorrowed) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Book is currently borrowed and cannot be deleted.");
                }

                // Delete the book from the database
                bookRepository.deleteById(id);

                return ResponseEntity.noContent().build();
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Return forbidden status if not authorized
    }*/
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));

            if (isAdmin) {
                // Check if the book has been borrowed by someone
                boolean isBorrowed = borrowingRepository.existsByBookIdAndIsBorrowedTrue(id);

                if (isBorrowed) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Book is currently borrowed and cannot be deleted.");
                }

                // Update the book's status to 'deleted' instead of deleting the record
                Book book = bookRepository.findById(id).orElse(null);
                if (book != null) {
                    book.setIsDeleted(true);
                    bookRepository.save(book);
                    return ResponseEntity.noContent().build();
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Return forbidden status if not authorized
    }

}


