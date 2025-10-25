package com.example.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cinema.model.Screening;
import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
}
