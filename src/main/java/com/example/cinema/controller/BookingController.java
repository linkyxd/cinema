package com.example.cinema.controller;

import com.example.cinema.model.Screening;
import com.example.cinema.model.Ticket;
import com.example.cinema.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** POST /api/booking/buy — купить один билет */
    @PostMapping("/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public Ticket buyTicket(@RequestBody Map<String, Object> body) {
        Long customerId = longFrom(body, "customerId");
        Long screeningId = longFrom(body, "screeningId");
        double price = doubleFrom(body, "price");
        return bookingService.buyTicket(customerId, screeningId, price);
    }

    /** POST /api/booking/refund/{ticketId} — вернуть билет */
    @PostMapping("/refund/{ticketId}")
    public Ticket refundTicket(@PathVariable Long ticketId) {
        return bookingService.refundTicket(ticketId);
    }

    /** GET /api/booking/screenings?movieId=1 — сеансы по фильму */
    @GetMapping("/screenings")
    public List<Screening> getScreeningsByMovie(@RequestParam Long movieId) {
        return bookingService.getScreeningsByMovie(movieId);
    }

    /** GET /api/booking/available-seats?screeningId=1 — свободные места */
    @GetMapping("/available-seats")
    public Map<String, Integer> getAvailableSeats(@RequestParam Long screeningId) {
        int seats = bookingService.getAvailableSeats(screeningId);
        return Map.of("availableSeats", seats);
    }

    /** POST /api/booking/bulk — купить несколько билетов */
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Ticket> bulkPurchase(@RequestBody Map<String, Object> body) {
        Long customerId = longFrom(body, "customerId");
        Long screeningId = longFrom(body, "screeningId");
        int quantity = intFrom(body, "quantity");
        double pricePerTicket = doubleFrom(body, "pricePerTicket");
        return bookingService.bulkPurchase(customerId, screeningId, quantity, pricePerTicket);
    }

    /** GET /api/booking/customer/{customerId}/tickets — билеты клиента */
    @GetMapping("/customer/{customerId}/tickets")
    public List<Ticket> getCustomerTickets(@PathVariable Long customerId) {
        return bookingService.getCustomerTickets(customerId);
    }

    private static long longFrom(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s) return Long.parseLong(s);
        throw new IllegalArgumentException("Missing or invalid: " + key);
    }

    private static int intFrom(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) return Integer.parseInt(s);
        throw new IllegalArgumentException("Missing or invalid: " + key);
    }

    private static double doubleFrom(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) return Double.parseDouble(s);
        throw new IllegalArgumentException("Missing or invalid: " + key);
    }
}
