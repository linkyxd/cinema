package com.example.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cinema.model.Ticket;
import com.example.cinema.model.Screening;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByScreening(Screening screening);
}
