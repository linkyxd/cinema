package com.example.cinema.service;

import com.example.cinema.model.*;
import com.example.cinema.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {
    private final CustomerRepository customerRepo;
    private final ScreeningRepository screeningRepo;
    private final TicketRepository ticketRepo;

    public BookingService(CustomerRepository customerRepo, ScreeningRepository screeningRepo, TicketRepository ticketRepo) {
        this.customerRepo = customerRepo;
        this.screeningRepo = screeningRepo;
        this.ticketRepo = ticketRepo;
    }

    /** 1. Покупка билета — проверка мест, создание билета в транзакции */
    @Transactional
    public Ticket buyTicket(Long customerId, Long screeningId, double price) {
        Customer customer = customerRepo.findById(customerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        Screening screening = screeningRepo.findById(screeningId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screening not found"));

        long sold = ticketRepo.countByScreeningAndRefundedFalse(screening);
        if (sold >= screening.getHall().getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нет свободных мест");
        }
        if (screening.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Сеанс уже начался");
        }

        Ticket t = new Ticket();
        t.setCustomer(customer);
        t.setScreening(screening);
        t.setPrice(price);
        return ticketRepo.save(t);
    }

    /** 2. Возврат билета — только до начала сеанса */
    @Transactional
    public Ticket refundTicket(Long ticketId) {
        Ticket t = ticketRepo.findById(ticketId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        if (t.isRefunded()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Билет уже возвращён");
        }
        if (t.getScreening().getStartTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Возврат после начала сеанса невозможен");
        }
        t.setRefunded(true);
        return ticketRepo.save(t);
    }

    /** 3. Сеансы по фильму */
    public List<Screening> getScreeningsByMovie(Long movieId) {
        return screeningRepo.findByMovieIdOrderByStartTimeAsc(movieId);
    }

    /** 4. Количество свободных мест на сеанс */
    public int getAvailableSeats(Long screeningId) {
        Screening screening = screeningRepo.findById(screeningId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screening not found"));
        long sold = ticketRepo.countByScreeningAndRefundedFalse(screening);
        return Math.max(0, screening.getHall().getCapacity() - (int) sold);
    }

    /** 5. Покупка нескольких билетов одной транзакцией */
    @Transactional
    public List<Ticket> bulkPurchase(Long customerId, Long screeningId, int quantity, double pricePerTicket) {
        Customer customer = customerRepo.findById(customerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        Screening screening = screeningRepo.findById(screeningId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screening not found"));

        if (quantity < 1 || quantity > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Количество от 1 до 10");
        }

        long sold = ticketRepo.countByScreeningAndRefundedFalse(screening);
        int available = screening.getHall().getCapacity() - (int) sold;
        if (quantity > available) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно мест. Свободно: " + available);
        }
        if (screening.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Сеанс уже начался");
        }

        return java.util.stream.IntStream.range(0, quantity)
            .mapToObj(i -> {
                Ticket t = new Ticket();
                t.setCustomer(customer);
                t.setScreening(screening);
                t.setPrice(pricePerTicket);
                return ticketRepo.save(t);
            })
            .toList();
    }

    /** Доп: История бронирований клиента */
    public List<Ticket> getCustomerTickets(Long customerId) {
        return ticketRepo.findByCustomerIdAndRefundedFalse(customerId);
    }
}
