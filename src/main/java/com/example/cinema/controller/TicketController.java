package com.example.cinema.controller;

import com.example.cinema.model.Ticket;
import com.example.cinema.model.Screening;
import com.example.cinema.repository.TicketRepository;
import com.example.cinema.repository.ScreeningRepository;
import com.example.cinema.repository.CustomerRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketRepository repo;
    private final ScreeningRepository screeningRepo;
    private final CustomerRepository customerRepo;

    public TicketController(TicketRepository repo, ScreeningRepository screeningRepo, CustomerRepository customerRepo) {
        this.repo = repo;
        this.screeningRepo = screeningRepo;
        this.customerRepo = customerRepo;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Ticket> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Ticket get(@PathVariable Long id){
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public Ticket create(@RequestBody Ticket ticket){
        if (ticket.getScreening() == null || ticket.getScreening().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Screening required");
        }
        if (ticket.getCustomer() == null || ticket.getCustomer().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer required");
        }

        Screening screening = screeningRepo.findById(ticket.getScreening().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid screening"));
        // check capacity
        long sold = repo.countByScreeningAndRefundedFalse(screening);
        if (screening.getHall() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Screening has no hall assigned");
        }
        if (sold >= screening.getHall().getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hall is full");
        }

        // ensure customer exists
        ticket.setCustomer(customerRepo.findById(ticket.getCustomer().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customer")));
        ticket.setScreening(screening);
        return repo.save(ticket);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Ticket update(@PathVariable Long id, @RequestBody Ticket t){
        Ticket ex = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        // allow updating price only
        ex.setPrice(t.getPrice());
        return repo.save(ex);
    }

    @PostMapping("/refund/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Ticket refund(@PathVariable Long id){
        Ticket ex = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Screening screening = ex.getScreening();
        if (screening.getStartTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Screening has no start time");
        }
        if (screening.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot refund after screening start");
        }
        ex.setRefunded(true);
        return repo.save(ex);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}
