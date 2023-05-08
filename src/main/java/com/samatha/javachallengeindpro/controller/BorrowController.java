package com.samatha.javachallengeindpro.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import com.samatha.javachallengeindpro.dto.Book;
import com.samatha.javachallengeindpro.dto.Borrowing;
import com.samatha.javachallengeindpro.exceptions.BorrowingNotFoundException;
import com.samatha.javachallengeindpro.model.Report;
import com.samatha.javachallengeindpro.repository.BookRepository;
import com.samatha.javachallengeindpro.repository.BorrowingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/book")
public class BorrowController {
    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private BookRepository bookRepository;

    // POST endpoint to allow a user with borrowing rights to borrow a book
    @PostMapping("/borrow")
    public ResponseEntity<Borrowing> borrowBook(@RequestBody Borrowing borrowing, Authentication authentication) {
        // Perform necessary validations and checks for user's borrowing rights
                // Retrieve the book from the database
                Long bookId = borrowing.getBook().getId();
                Optional<Book> optionalBook = bookRepository.findById(bookId);
                if (optionalBook.isPresent()) {
                    Book book = optionalBook.get();
                    borrowing.setBorrowedDate(LocalDate.now());
                    borrowing.setReturnedDate(LocalDate.now().plusDays(10));
                    borrowing.setDueDate(LocalDate.now().plusDays(15));
                    borrowing.setIsBorrowed(true);

                    // Perform any necessary updates to the book object
                    // For example, set the book as borrowed or update its availability status

                    // Set the updated book object in the borrowing
                    borrowing.setBook(book);

                    // Set the borrowed date to the current date
                    borrowing.setBorrowedDate(LocalDate.now());

                    // Save the borrowing details to the database
                    Borrowing savedBorrowing = borrowingRepository.save(borrowing);

                    return ResponseEntity.ok(savedBorrowing);
                }



        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


    // POST endpoint to enable a user to return a book
    @PostMapping("/return")
    public ResponseEntity<Borrowing> returntheBook(@RequestBody Borrowing borrowing) {
// Perform necessary validations and checks for user and book
        // Update the borrowing details
        Borrowing existingBorrowing = borrowingRepository.findById(borrowing.getId())
                .orElseThrow(() -> new BorrowingNotFoundException("Borrowing not found with ID: " + borrowing.getId()));

        // Update the returned date and calculate late fee if applicable
        existingBorrowing.setReturnedDate(LocalDate.now());
        if (existingBorrowing.getReturnedDate().isAfter(existingBorrowing.getDueDate())) {
            // Calculate late fee and update
            long daysLate = ChronoUnit.DAYS.between(existingBorrowing.getDueDate(), existingBorrowing.getReturnedDate());
            BigDecimal lateFee = calculateLateFee(daysLate);
            existingBorrowing.setLateFee(lateFee);
        }

        // Save the updated borrowing details to the database
        Borrowing updatedBorrowing = borrowingRepository.save(existingBorrowing);

        return ResponseEntity.ok(updatedBorrowing);
    }

    // GET endpoint to allow the admin to view/download reports from the system
    @GetMapping("/library/report")
    public ResponseEntity<Report> generateLibraryReport(@RequestParam("format") String format,Authentication authentication) {
        Report report=null;
        // Perform necessary validations and checks for admin
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
            if (isAdmin) {
                // Generate the report based on the requested format
                report = generateReport(format);
            }
        }
        // Return the report in the requested format
        return ResponseEntity.ok(report);
    }

    // Helper method to calculate late fee based on the number of days late
    private BigDecimal calculateLateFee(long daysLate) {
        // Implement the logic to calculate the late fee
        // Define the late fee rate per day
        BigDecimal lateFeeRate = new BigDecimal("1.00"); // Assuming the late fee is $1.00 per day

        // Calculate the late fee
        BigDecimal lateFee = lateFeeRate.multiply(new BigDecimal(daysLate));

        return lateFee;
    }

    // Helper method to generate the library report in the requested format
    private Report generateReport(String format) {
        // Generate the report title
        String title = "Library Report";

        // Generate the report entries
        List<Report.ReportEntry> entries = new ArrayList<>();

        // Perform the necessary database queries or calculations to populate the report entries
        if (format.equalsIgnoreCase("CSV")) {
            // Generate CSV format report entries
            List<Object[]> borrowedBooks = borrowingRepository.countBorrowedBooks();
            for (Object[] book : borrowedBooks) {
                Report.ReportEntry entry = new Report.ReportEntry();
                entry.setLabel((String) book[0]);
                entry.setCount(Math.toIntExact((Long) book[1]));
                entries.add(entry);
            }
        } else if (format.equalsIgnoreCase("PDF")) {

        }
        else {
            // Generate default format report entries
            List<Object[]> borrowedBooks = borrowingRepository.countBorrowedBooks();
            for (Object[] book : borrowedBooks) {
                Report.ReportEntry entry = new Report.ReportEntry();
                entry.setLabel((String) book[0]);
                entry.setCount(((BigInteger) book[1]).intValue());
                entries.add(entry);
            }
        }

        // Create and populate the report object
        Report report = new Report();
        report.setTitle(title);
        report.setEntries(entries);

        return report;
    }



}