package ui;

import ServerFacade.ServerFacade;
import model.AuthData;

import static java.lang.System.exit;
import static ui.UIUtils.input;

public class LoginMenu {
    private static ServerFacade serverFacade;
    public LoginMenu(ServerFacade sF) {
        serverFacade = sF;
    }
    public AuthData display() {
        boolean loggedIn = false;
        AuthData authData = null;
        while (!loggedIn) {
            String line = input("Would you like to (l)ogin, (r)egister, or (q)uit? Type (h) for help.");
            switch (line) {
                case "l" -> {
                    String username = input("Username: ");
                    String password = input("Password: ");
                    try {
                        authData = serverFacade.login(username, password);
                        loggedIn = true;
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case "r" -> {
                    String username = input("Username: ");
                    String password = input("Password: ");
                    String email = input("Email: ");
                    try {
                        authData = serverFacade.register(username, password, email);
                        loggedIn = true;
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case "h" -> {
                    System.out.println("(r)egister -> Prompts for a username, password, and email. Registers your account with the server.");
                    System.out.println("(l)ogin -> Prompts for a username and password. Logs you in to the server.");
                    System.out.println("(q)uit -> Quits the chess client.");
                    System.out.println("(h)elp -> Displays this help message.");
                }
                case "q" -> exit(0);
                case null, default -> System.out.println("Invalid option, please try again");
            }
        }
        return authData;
    }
}
