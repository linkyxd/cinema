package com.example.cinema.controller;

import com.example.cinema.model.Movie;
import com.example.cinema.repository.MovieRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieRepository repo;
    public MovieController(MovieRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Movie> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public Movie get(@PathVariable Long id){
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movie create(@Valid @RequestBody Movie movie){ return repo.save(movie); }

    @PutMapping("/{id}")
    public Movie update(@PathVariable Long id, @Valid @RequestBody Movie movie){
        Movie ex = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ex.setTitle(movie.getTitle());
        ex.setGenre(movie.getGenre());
        ex.setDurationMinutes(movie.getDurationMinutes());
        return repo.save(ex);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}
