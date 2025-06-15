package com.jts.movie.repositories;

import com.jts.movie.entities.Show;
import com.jts.movie.entities.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaitlistRepository extends JpaRepository<Waitlist, Integer> {
    List<Waitlist> findByShowAndStatusOrderByCreatedAtAsc(Show show, String status);
}
