import ServerFacade.ServerFacade;
import chess.*;
import model.AuthData;
import model.GameData;
import server.ListGame.ListGameResponse;
import ui.EscapeSequences;
import ui.LoginMenu;
import ui.MainMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.Math.abs;
import static java.lang.System.exit;
import static ui.EscapeSequences.*;
import static ui.UIUtils.input;

public class Main {
    private static final ServerFacade serverFacade = new ServerFacade("http://localhost:3676");
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