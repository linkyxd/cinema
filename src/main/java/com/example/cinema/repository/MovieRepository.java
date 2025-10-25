package com.example.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cinema.model.Movie;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
