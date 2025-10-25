package com.example.cinema.controller;

import com.example.cinema.model.Hall;
import com.example.cinema.repository.HallRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
public class HallController {
    private final HallRepository repo;
    public HallController(HallRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Hall> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public Hall get(@PathVariable Long id){
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Hall create(@RequestBody Hall hall){ return repo.save(hall); }

    @PutMapping("/{id}")
    public Hall update(@PathVariable Long id, @RequestBody Hall hall){
        Hall ex = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ex.setName(hall.getName());
        ex.setCapacity(hall.getCapacity());
        return repo.save(ex);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}
