package com.jts.movie.entities;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Getter
@Entity
public class Waitlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Date createdAt = new Date();

    @ManyToOne
    private Show show;

    @ManyToOne
    private User user;

    private Integer requestedSeats;

    // status: WAITING, FULFILLED, CANCELLED
    private String status = "WAITING";

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRequestedSeats(Integer requestedSeats) {
        this.requestedSeats = requestedSeats;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
