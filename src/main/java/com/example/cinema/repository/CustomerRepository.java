package com.example.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cinema.model.Customer;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
