package service;

import dataAccess.*;
import dataAccess.Exceptions.DataAccessException;

public class DBService {
    UserDAO userDAO = new SQLUserDAO();
    AuthDAO authDAO = new MemoryAuthDAO();
    GameDAO gameDAO = new MemoryGameDAO();
    public void clearDB() throws DataAccessException{
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }
}
