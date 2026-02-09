package dev.rudrade.chat.exception;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException() {
        super("User not found");
    }

}
