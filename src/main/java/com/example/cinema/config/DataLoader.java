package com.example.cinema.config;

import com.example.cinema.model.*;
import com.example.cinema.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {
    private final CustomerRepository customerRepo;
    private final MovieRepository movieRepo;
    private final HallRepository hallRepo;
    private final ScreeningRepository screeningRepo;
    private final TicketRepository ticketRepo;

    public DataLoader(CustomerRepository customerRepo, MovieRepository movieRepo,
                      HallRepository hallRepo, ScreeningRepository screeningRepo,
                      TicketRepository ticketRepo) {
        this.customerRepo = customerRepo;
        this.movieRepo = movieRepo;
        this.hallRepo = hallRepo;
        this.screeningRepo = screeningRepo;
        this.ticketRepo = ticketRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (customerRepo.count() > 0) return;

        Customer c1 = new Customer(); c1.setFullName("Иван Петров"); c1.setEmail("ivan@mail.ru"); c1 = customerRepo.save(c1);
        Customer c2 = new Customer(); c2.setFullName("Мария Сидорова"); c2.setEmail("maria@gmail.com"); c2 = customerRepo.save(c2);
        Customer c3 = new Customer(); c3.setFullName("Алексей Козлов"); c3.setEmail("alex@yandex.ru"); c3 = customerRepo.save(c3);

        Movie m1 = new Movie(); m1.setTitle("Матрица"); m1.setGenre("Фантастика"); m1.setDurationMinutes(136); m1 = movieRepo.save(m1);
        Movie m2 = new Movie(); m2.setTitle("Побег из Шоушенка"); m2.setGenre("Драма"); m2.setDurationMinutes(142); m2 = movieRepo.save(m2);
        Movie m3 = new Movie(); m3.setTitle("Король Лев"); m3.setGenre("Мультфильм"); m3.setDurationMinutes(88); m3 = movieRepo.save(m3);

        Hall h1 = new Hall(); h1.setName("Зал 1"); h1.setCapacity(100); h1 = hallRepo.save(h1);
        Hall h2 = new Hall(); h2.setName("Зал 2"); h2.setCapacity(50); h2 = hallRepo.save(h2);
        Hall h3 = new Hall(); h3.setName("VIP зал"); h3.setCapacity(20); h3 = hallRepo.save(h3);

        Screening s1 = new Screening(); s1.setMovie(m1); s1.setHall(h1); s1.setStartTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0)); s1 = screeningRepo.save(s1);
        Screening s2 = new Screening(); s2.setMovie(m2); s2.setHall(h1); s2.setStartTime(LocalDateTime.now().plusDays(1).withHour(21).withMinute(0)); s2 = screeningRepo.save(s2);
        Screening s3 = new Screening(); s3.setMovie(m3); s3.setHall(h2); s3.setStartTime(LocalDateTime.now().plusDays(2).withHour(12).withMinute(0)); s3 = screeningRepo.save(s3);

        Ticket t1 = new Ticket(); t1.setScreening(s1); t1.setCustomer(c1); t1.setPrice(350.0); ticketRepo.save(t1);
        Ticket t2 = new Ticket(); t2.setScreening(s1); t2.setCustomer(c2); t2.setPrice(350.0); ticketRepo.save(t2);
    }
}
