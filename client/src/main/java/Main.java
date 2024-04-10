import ServerFacade.HttpCommunicator;
import ServerFacade.ServerFacade;
import chess.*;
import model.AuthData;
import ui.LoginMenu;
import ui.MainMenu;

import static java.lang.Math.abs;

public class Main {
    private static final ServerFacade serverFacade = new ServerFacade("localhost:3676");
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        AuthData auth;
        MainMenu mainMenu = new MainMenu(serverFacade);
        LoginMenu loginMenu = new LoginMenu(serverFacade);
        while (true) {
            auth = loginMenu.display();
            mainMenu.display(auth);
        }
    }


}