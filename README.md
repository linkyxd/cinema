# Cinema Spring Boot project

How to run:
1. Open in IntelliJ IDEA (or use terminal)
2. Run `mvn spring-boot:run` or run `CinemaApplication` from IDE
3. H2 console available at http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:cinema)

REST endpoints (base `/api`):
- /api/movies
- /api/halls
- /api/customers
- /api/screenings
- /api/tickets

Notes:
- Creating a ticket checks hall capacity (tickets sold <= hall.capacity).
- Refund allowed only before screening start time: POST /api/tickets/refund/{id}
