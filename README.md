# 🎭 Live Show Booking System - Spring Boot Project

This is a backend application built with **Java Spring Boot** that manages **live show scheduling and ticket booking** for organizers and users. It handles show creation, slot-based scheduling, ticket booking with constraints, cancellation, waitlisting, and more.

---

## 🚀 Features

### 🎫 Booking Functionality (User-Side)
- Search live shows by genre (Comedy, Theatre, Tech, Singing, etc.)
- View show timings ranked by start time (pluggable ranking strategy)
- Book a ticket for any available show slot
- Book one ticket for **multiple persons** (only if enough seats are available)
- Prevents double booking in overlapping slots for the same user
- Cancel a booking → slot becomes available
- View all bookings made for the day
- **Waitlist system**:
  - If slot is full, user is waitlisted
  - If an active booking is cancelled, the first user in waitlist gets the spot automatically

### 🎟️ Show Management (Organizer-Side)
- Register new **live shows** with unique names and genres
- Add shows in specific 1-hour time slots (e.g., 9–10AM, 10–11AM, etc.)
- Prevents overlapping time slots for the same show
- Add theaters and define their seating capacity per slot
- View bookings made by users for the day

---

## 🧩 Project Modules

| Module       | Description                                       |
|--------------|---------------------------------------------------|
| `MovieController` | Admin/organizer functionality to add live shows (name, genre) |
| `ShowController`  | Add show timings and schedule slots            |
| `TheatreController` | Manage seating capacity and theaters         |
| `UserController`   | User registration (optional)                  |
| `TicketController` | Booking & cancellation of tickets             |

---

## ⚙️ Technologies Used

- Java 17
- Spring Boot
- Spring MVC
- Maven
- (Optional) Spring Security / JWT (if login is implemented)
- H2 / MySQL / PostgreSQL (pluggable DB - current setup uses in-memory DB for dev)

---

## 🧪 How to Run

```bash
git clone https://github.com/magician9999/BMSClone.git
cd BMSClone
./mvnw spring-boot:run
