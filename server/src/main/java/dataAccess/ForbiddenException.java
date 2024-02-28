package dataAccess;

/**
 * Indicates there was an error connecting to the database
 */
public class ForbiddenException extends DataAccessException{
    public ForbiddenException(String message) {
        super(message);
    }
}
