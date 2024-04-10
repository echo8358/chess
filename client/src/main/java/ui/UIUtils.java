package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.Math.abs;
import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_BG_COLOR_BLACK;

public class UIUtils {
    public static String input(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(prompt);
        return scanner.nextLine();
    }

     public static void displayGame(ChessGame game, ChessGame.TeamColor color) {

        ChessBoard board = game.getBoard();
        String squareColor = SET_BG_COLOR_WHITE;
        ChessPiece target = null;
        int yInt = 8;
        int yDir = -1;
        if (Objects.equals(color, ChessGame.TeamColor.BLACK)) { yInt = 1; yDir = 1; }

        System.out.println(SET_TEXT_COLOR_BLACK+SET_BG_COLOR_LIGHT_GREY);
        if (color == ChessGame.TeamColor.WHITE) System.out.println("  h  g  f  e  d  c  b  a  ");
        else System.out.println("  a  b  c  d  e  f  g  h  ");

        for (int y = yInt; abs(y-yInt) < 8; y+=yDir){
            System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + Integer.toString(9-y));
            for (int x = 1; x < 9; x++) {
                target = board.getPiece(new ChessPosition(y,x));

                if (target != null) {
                    System.out.print(squareColor+getTextColorFromPieceColor(target.getTeamColor()));
                    System.out.print(" "+getCharFromPieceType(target.getPieceType())+" ");
                }
                if (target == null) System.out.print(squareColor+"   ");

                if (squareColor.equals(SET_BG_COLOR_WHITE)) {
                    squareColor = SET_BG_COLOR_DARK_GREY;
                } else if (squareColor.equals(SET_BG_COLOR_DARK_GREY)) {
                    squareColor = SET_BG_COLOR_WHITE;
                }
            }
            System.out.println(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + Integer.toString(9-y));
            if (squareColor.equals(SET_BG_COLOR_WHITE)) {
                squareColor = SET_BG_COLOR_DARK_GREY;
            } else if (squareColor.equals(SET_BG_COLOR_DARK_GREY)) {
                squareColor = SET_BG_COLOR_WHITE;
            }
        }
        if (color == ChessGame.TeamColor.WHITE) System.out.println("  h  g  f  e  d  c  b  a  ");
        else System.out.println("  a  b  c  d  e  f  g  h  ");

    }

    public static String getCharFromPieceType(ChessPiece.PieceType type){
        HashMap<ChessPiece.PieceType, String> charMap = new HashMap<>();
        charMap.put(ChessPiece.PieceType.PAWN, "P");
        charMap.put(ChessPiece.PieceType.ROOK, "R");
        charMap.put(ChessPiece.PieceType.KNIGHT, "N");
        charMap.put(ChessPiece.PieceType.BISHOP, "B");
        charMap.put(ChessPiece.PieceType.KING, "K");
        charMap.put(ChessPiece.PieceType.QUEEN, "Q");
        return charMap.get(type);
    }

    public static String getTextColorFromPieceColor(ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.BLACK) return SET_TEXT_COLOR_RED;
        if (color == ChessGame.TeamColor.WHITE) return SET_TEXT_COLOR_BLUE;
        else return SET_BG_COLOR_BLACK;
    }
}
