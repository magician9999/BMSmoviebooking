package com.jts.movie.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.jts.movie.entities.*;
import com.jts.movie.exceptions.BookingConflictException;
import com.jts.movie.repositories.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jts.movie.convertor.TicketConvertor;
import com.jts.movie.exceptions.SeatsNotAvailable;
import com.jts.movie.exceptions.ShowDoesNotExists;
import com.jts.movie.exceptions.UserDoesNotExists;
import com.jts.movie.repositories.ShowRepository;
import com.jts.movie.repositories.TicketRepository;
import com.jts.movie.repositories.UserRepository;
import com.jts.movie.request.TicketRequest;
import com.jts.movie.response.TicketResponse;

@Service
public class TicketService {

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private ShowRepository showRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WaitlistRepository waitlistRepository;

	public TicketResponse ticketBooking(TicketRequest ticketRequest) {
		Optional<Show> showOpt = showRepository.findById(ticketRequest.getShowId());

		if (showOpt.isEmpty()) {
			throw new ShowDoesNotExists();
		}

		Optional<User> userOpt = userRepository.findById(ticketRequest.getUserId());

		if (userOpt.isEmpty()) {
			throw new UserDoesNotExists();
		}

		User user = userOpt.get();
		Show show = showOpt.get();

		//Check for conflicting show at same time
		List<Ticket> existingTickets = ticketRepository.findTicketsByUserAndDateAndTime(
				user.getUserId(), show.getDate(), show.getTime());

		if (!existingTickets.isEmpty()) {
			throw new BookingConflictException("User already has a booking for another show at the same time.");
		}

		Boolean isSeatAvailable = isSeatAvailable(show.getShowSeatList(), ticketRequest.getRequestSeats());

		if (!isSeatAvailable) {
			// Add to waitlist
			Waitlist waitlist = new Waitlist();
			waitlist.setShow(show);
			waitlist.setUser(user);
			waitlist.setRequestedSeats(ticketRequest.getRequestSeats().size());
			waitlistRepository.save(waitlist);

			throw new SeatsNotAvailable();
		}

		// count price
		Integer getPriceAndAssignSeats = getPriceAndAssignSeats(show.getShowSeatList(),	ticketRequest.getRequestSeats());

		String seats = listToString(ticketRequest.getRequestSeats());

		Ticket ticket = new Ticket();
		ticket.setTotalTicketsPrice(getPriceAndAssignSeats);
		ticket.setBookedSeats(seats);
		ticket.setUser(user);
		ticket.setShow(show);

		ticket = ticketRepository.save(ticket);

		user.getTicketList().add(ticket);
		show.getTicketList().add(ticket);
		userRepository.save(user);
		showRepository.save(show);

		return TicketConvertor.returnTicket(show, ticket);
	}

	private Boolean isSeatAvailable(List<ShowSeat> showSeatList, List<String> requestSeats) {
		for (ShowSeat showSeat : showSeatList) {
			String seatNo = showSeat.getSeatNo();

			if (requestSeats.contains(seatNo) && !showSeat.getIsAvailable()) {
				return false;
			}
		}

		return true;
	}

	private Integer getPriceAndAssignSeats(List<ShowSeat> showSeatList, List<String> requestSeats) {
		Integer totalAmount = 0;

		for (ShowSeat showSeat : showSeatList) {
			if (requestSeats.contains(showSeat.getSeatNo())) {
				totalAmount += showSeat.getPrice();
				showSeat.setIsAvailable(Boolean.FALSE);
			}
		}

		return totalAmount;
	}

	private String listToString(List<String> requestSeats) {
		StringBuilder sb = new StringBuilder();

		for (String s : requestSeats) {
			sb.append(s).append(",");
		}

		return sb.toString();
	}

	public void cancelTicket(Integer ticketId) {
		Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);

		Ticket ticket = ticketOpt.get();

		// Free the seats
		Show show = ticket.getShow();
		List<String> cancelledSeats = Arrays.asList(ticket.getBookedSeats().split(","));
		for (ShowSeat seat : show.getShowSeatList()) {
			if (cancelledSeats.contains(seat.getSeatNo())) {
				seat.setAvailable(true);
			}
		}

		// Remove ticket from user and show
		User user = ticket.getUser();
		user.getTicketList().remove(ticket);
		show.getTicketList().remove(ticket);

		ticketRepository.delete(ticket);
		userRepository.save(user);
		showRepository.save(show);

		// ✅ Notify waitlist if any
		processWaitlistForShow(show, cancelledSeats.size());
	}

	private void processWaitlistForShow(Show show, int seatsFreed) {
		List<Waitlist> waitlisted = waitlistRepository.findByShowAndStatusOrderByCreatedAtAsc(show, "WAITING");

		for (Waitlist w : waitlisted) {
			if (w.getRequestedSeats() <= seatsFreed) {
				List<String> availableSeats = getAvailableSeats(show.getShowSeatList(), w.getRequestedSeats());
				if (availableSeats.size() < w.getRequestedSeats()) continue;

				Ticket ticket = new Ticket();
				ticket.setBookedSeats(String.join(",", availableSeats));
				ticket.setTotalTicketsPrice(getPriceAndAssignSeats(show.getShowSeatList(), availableSeats));
				ticket.setShow(show);
				ticket.setUser(w.getUser());

				ticket = ticketRepository.save(ticket);

				for (ShowSeat seat : show.getShowSeatList()) {
					if (availableSeats.contains(seat.getSeatNo())) {
						seat.setAvailable(false);
					}
				}

				w.setStatus("FULFILLED");
				waitlistRepository.save(w);
				break; // only one user is fulfilled per cancellation
			}
		}

		showRepository.save(show);
	}

	private List<String> getAvailableSeats(List<ShowSeat> seats, int count) {
		List<String> available = new ArrayList<>();
		for (ShowSeat seat : seats) {
			if (seat.isAvailable()) {
				available.add(seat.getSeatNo());
			}
			if (available.size() == count) break;
		}
		return available;
	}
}
