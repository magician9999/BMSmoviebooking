package com.jts.movie.exceptions;

public class BookingConflictException extends RuntimeException {
  public BookingConflictException(String message) {
    super(message);
  }
}
