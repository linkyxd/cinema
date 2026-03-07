package com.example.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cinema.model.Ticket;
import com.example.cinema.model.Screening;
import com.example.cinema.model.Customer;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByScreeningAndRefundedFalse(Screening screening);
    List<Ticket> findByCustomerIdAndRefundedFalse(Long customerId);
}
