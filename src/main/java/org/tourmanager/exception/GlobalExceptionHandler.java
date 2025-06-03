package org.tourmanager.exception;

import org.springframework.stereotype.Component;

@Component
public class GlobalExceptionHandler {

    public void handleException(String operation, Exception e) {
        System.err.println("Błąd podczas " + operation + ": " + e.getMessage());

        if (isDebugMode()) {
            e.printStackTrace();
        }

        notifyUser("Wystąpił błąd podczas " + operation);
    }

    private boolean isDebugMode() {
        return "true".equals(System.getProperty("debug.mode"));
    }

    private void notifyUser(String message) {
        System.out.println("POWIADOMIENIE: " + message);
    }
}