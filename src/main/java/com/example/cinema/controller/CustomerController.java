package com.example.cinema.controller;

import com.example.cinema.model.Customer;
import com.example.cinema.repository.CustomerRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerRepository repo;
    public CustomerController(CustomerRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Customer> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public Customer get(@PathVariable Long id){
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@RequestBody Customer customer){ return repo.save(customer); }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @RequestBody Customer customer){
        Customer ex = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ex.setFullName(customer.getFullName());
        ex.setEmail(customer.getEmail());
        return repo.save(ex);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}
