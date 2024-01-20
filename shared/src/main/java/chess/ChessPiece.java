package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor teamColor;
    private final ChessPiece.PieceType pieceType;


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.pieceType = type;
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "teamColor=" + teamColor +
                ", pieceType=" + pieceType +
                '}';
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() { return this.pieceType; }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        switch (getPieceType()){
            case KING:
                for(int i = -1; i < 2; i++) {
                    for(int j = -1; j < 2; j++) {
                        checkAndAddMove(board, moves, myPosition, i,j);
                    }
                }
                break;

            case BISHOP:
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, i, i) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, -i, i) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, i, -i) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, -i, -i) == 1) break;
                }
                break;
            case QUEEN:
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, 0, i) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, 0, -i) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, i, 0) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, -i, 0) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, i, i) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, -i, i) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, i, -i) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, -i, -i) == 1) break;
                }
                break;
            case ROOK:
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, 0, i) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, 0, -i) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, i, 0) == 1) break;
                }
                for(int i = 1; i < 8; i++) {
                    if (checkAndAddMove(board, moves, myPosition, -i, 0) == 1) break;
                }
                break;
            case KNIGHT:
                checkAndAddMove(board, moves, myPosition, 1, 2);
                checkAndAddMove(board, moves, myPosition, 2, 1);
                checkAndAddMove(board, moves, myPosition, -1, 2);
                checkAndAddMove(board, moves, myPosition, -2, 1);
                checkAndAddMove(board, moves, myPosition, 1, -2);
                checkAndAddMove(board, moves, myPosition, 2, -1);
                checkAndAddMove(board, moves, myPosition, -1, -2);
                checkAndAddMove(board, moves, myPosition, -2, -1);
                break;
            case PAWN:
                int dir = 1;
                if(teamColor == ChessGame.TeamColor.BLACK) { dir = -1; }
                // moving forward
                ChessPosition targetPos = new ChessPosition(myPosition.getRow()+dir, myPosition.getColumn());
                if (board.getPiece(targetPos) == null) {
                    //promotion
                    if (teamColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7
                            || teamColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2)
                    {
                        targetPos = new ChessPosition(myPosition.getRow()+dir, myPosition.getColumn());
                        if (board.getPiece(targetPos) == null) {
                            moves.add(new ChessMove(myPosition, targetPos, PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, targetPos, PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, targetPos, PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, targetPos, PieceType.QUEEN));
                        }
                    }
                    //not promotion
                    else {
                        moves.add(new ChessMove(myPosition, targetPos, null));
                        if (teamColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2
                                || teamColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7) {
                            targetPos = new ChessPosition(myPosition.getRow() + dir * 2, myPosition.getColumn());
                            if (board.getPiece(targetPos) == null) {
                                moves.add(new ChessMove(myPosition, targetPos, null));
                            }
                        }
                    }
                }
                //capturing diagonal
                targetPos = new ChessPosition(myPosition.getRow() + dir, myPosition.getColumn() + 1);
                if (board.getPiece(targetPos) != null && board.getPiece(targetPos).teamColor != teamColor) {
                    if (teamColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7
                            || teamColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2) {
                        moves.add(new ChessMove(myPosition, targetPos, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, targetPos, PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, targetPos, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, targetPos, PieceType.QUEEN));
                    } else {
                        moves.add(new ChessMove(myPosition, targetPos, null));
                    }
                }
                targetPos = new ChessPosition(myPosition.getRow() + dir, myPosition.getColumn() - 1);
                if (board.getPiece(targetPos) != null && board.getPiece(targetPos).teamColor != teamColor) {
                    if (teamColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 7
                            || teamColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 2) {
                        moves.add(new ChessMove(myPosition, targetPos, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, targetPos, PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, targetPos, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, targetPos, PieceType.QUEEN));
                    }
                    else {
                        moves.add(new ChessMove(myPosition, targetPos, null));
                    }
                }
                break;
            default:
                break;
        }
        return moves;
    }

    private int checkAndAddMove(ChessBoard board, HashSet<ChessMove> moves, ChessPosition myPosition, int x, int y)
    {
        if(myPosition.getRow()+y >=1 && myPosition.getRow()+y <= 8 && myPosition.getColumn()+x >= 1 && myPosition.getColumn()+x <= 8)
        {
            ChessPiece targetPiece = board.getPiece(new ChessPosition(myPosition.getRow()+y, myPosition.getColumn()+x));
            if(targetPiece == null || targetPiece.getTeamColor() != teamColor)
            {
                moves.add(new ChessMove(myPosition, new ChessPosition(myPosition.getRow()+y, myPosition.getColumn()+x), null));
                if (targetPiece != null) {return 1;}
                else {return 0;}
            }
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return teamColor == that.teamColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, pieceType);
    }
}
