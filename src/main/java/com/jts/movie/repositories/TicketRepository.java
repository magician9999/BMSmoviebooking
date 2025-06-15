package com.jts.movie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jts.movie.entities.Ticket;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Integer> {
    @Query("SELECT t FROM Ticket t WHERE t.user.id = :userId AND t.show.date = :date AND t.show.time = :time")
    List<Ticket> findTicketsByUserAndDateAndTime(@Param("userId") Integer userId,
                                                 @Param("date") Date date,
                                                 @Param("time") Time time);
}
