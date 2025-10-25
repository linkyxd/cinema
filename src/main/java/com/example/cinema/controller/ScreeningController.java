package com.example.cinema.controller;

import com.example.cinema.model.Screening;
import com.example.cinema.repository.ScreeningRepository;
import com.example.cinema.repository.MovieRepository;
import com.example.cinema.repository.HallRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {
    private final ScreeningRepository repo;
    private final MovieRepository movieRepo;
    private final HallRepository hallRepo;

    public ScreeningController(ScreeningRepository repo, MovieRepository movieRepo, HallRepository hallRepo) {
        this.repo = repo;
        this.movieRepo = movieRepo;
        this.hallRepo = hallRepo;
    }

    @GetMapping
    public List<Screening> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public Screening get(@PathVariable Long id){
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Screening create(@RequestBody Screening screening){
        // Basic validation: referenced movie and hall must exist
        if (screening.getMovie() == null || screening.getMovie().getId() == null ||
            movieRepo.findById(screening.getMovie().getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid movie reference");
        }
        if (screening.getHall() == null || screening.getHall().getId() == null ||
            hallRepo.findById(screening.getHall().getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid hall reference");
        }
        return repo.save(screening);
    }

    @PutMapping("/{id}")
    public Screening update(@PathVariable Long id, @RequestBody Screening screening){
        Screening ex = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ex.setStartTime(screening.getStartTime());
        // allow changing movie/hall by id
        if (screening.getMovie() != null && screening.getMovie().getId() != null) {
            ex.setMovie(movieRepo.findById(screening.getMovie().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST)));
        }
        if (screening.getHall() != null && screening.getHall().getId() != null) {
            ex.setHall(hallRepo.findById(screening.getHall().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST)));
        }
        return repo.save(ex);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}
