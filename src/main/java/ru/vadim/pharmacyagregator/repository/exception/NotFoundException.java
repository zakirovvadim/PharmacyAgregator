package ru.vadim.pharmacyagregator.repository.exception;

public class NotFoundException extends Exception {
    public <T> NotFoundException(T id) {
        super(String.format("Entity with id='%s' not found!", id.toString()));
    }
    public NotFoundException(String message) {
        super(message);
    }
}
