package com.example.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cinema.model.Hall;
import java.util.List;

public interface HallRepository extends JpaRepository<Hall, Long> {
}
