package com.bankingSystem.exceptions;

/**
 * Exception thrown when a requested user cannot be found in the system.
 * This is a checked exception as user lookup failures should be explicitly handled.
 */
public class UserNotFoundException extends Exception {
    private final String userIdentifier;
    private final String searchCriteria;

    /**
     * Constructs a new exception with detail message and user identifier.
     * @param message the detail message
     * @param userIdentifier the identifier used to search for the user (email, ID, etc.)
     */
    public UserNotFoundException(String message, String userIdentifier) {
        super(message);
        this.userIdentifier = userIdentifier;
        this.searchCriteria = null;
    }

    /**
     * Constructs a new exception with detail message, user identifier and search criteria.
     * @param message the detail message
     * @param userIdentifier the identifier used to search for the user
     * @param searchCriteria the type of search performed (email, userId, phone, etc.)
     */
    public UserNotFoundException(String message, String userIdentifier, String searchCriteria) {
        super(message);
        this.userIdentifier = userIdentifier;
        this.searchCriteria = searchCriteria;
    }

    /**
     * @return the user identifier that caused the exception
     */
    public String getUserIdentifier() {
        return userIdentifier;
    }

    /**
     * @return the type of search criteria used
     */
    public String getSearchCriteria() {
        return searchCriteria;
    }

    /**
     * Constructs a standard error message including the search details
     * @return formatted error message
     */
    @Override
    public String getMessage() {
        if (searchCriteria != null) {
            return super.getMessage() +
                    " [Search Criteria: " + searchCriteria +
                    ", Identifier: " + userIdentifier + "]";
        }
        return super.getMessage() + " [Identifier: " + userIdentifier + "]";
    }
}
