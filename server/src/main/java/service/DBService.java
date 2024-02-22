package service;

import dataAccess.*;

public class DBService {
    UserDAO userDAO = new MemoryUserDAO();
    AuthDAO authDAO = new MemoryAuthDAO();
    GameDAO gameDAO = new MemoryGameDAO();
    public void clearDB() {
       userDAO.clear();
       authDAO.clear();
       gameDAO.clear();
    }
}
