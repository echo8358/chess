package service;

import dataAccess.*;
import dataAccess.Exceptions.DataAccessException;

public class DBService {
    UserDAO userDAO = new SQLUserDAO();
    AuthDAO authDAO = new SQLAuthDAO();
    GameDAO gameDAO = new SQLGameDAO();
    public void clearDB() throws DataAccessException{
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }
}
