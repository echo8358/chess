package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

import static java.lang.Math.abs;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;
    private final Stack<HistoricalMove> tryStack;
    private static final Stack<HistoricalMove> historyStack = new Stack<>();
    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();

        teamTurn = TeamColor.WHITE;

        tryStack = new Stack<>();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() { return teamTurn; }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) { teamTurn = team; }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece targetPiece = board.getPiece(startPosition);
        if(targetPiece != null ) {
            Collection<ChessMove> possibleMoves = targetPiece.pieceMoves(board, startPosition);
            HashSet<ChessMove> validMoveSet = new HashSet<>();
            for (ChessMove possibleMove : possibleMoves) {
                tryMove(possibleMove);
                if (isInCheck(targetPiece.getTeamColor())) {
                    untryMove();
                    continue;
                }
                validMoveSet.add(possibleMove);
                untryMove();
            }
            //if(validMoveSet.isEmpty()) { return null;}
            return validMoveSet;
        }
        return null;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if(board.getPiece(move.getStartPosition()) != null && board.getPiece(move.getStartPosition()).getTeamColor() == getTeamTurn()
                && validMoves(move.getStartPosition()) != null && validMoves(move.getStartPosition()).contains(move)) {
            ChessMove lastMove = getLastMove();
            historyStack.push(new HistoricalMove(board.getPiece(move.getStartPosition()), move, board.getPiece(move.getEndPosition())));

            //normal moves
            if(move.getPromotionPiece() == null) {
                board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
            } else {
                board.addPiece(move.getEndPosition(), new ChessPiece(getTeamTurn(), move.getPromotionPiece()));
            }
            board.addPiece(move.getStartPosition(), null);

            //en passant
            if(lastMove != null && abs(lastMove.getStartPosition().getRow()-lastMove.getEndPosition().getRow()) > 1
                    && board.getPiece(lastMove.getEndPosition()).getPieceType() == ChessPiece.PieceType.PAWN
                    && board.getPiece(move.getEndPosition()).getPieceType() == ChessPiece.PieceType.PAWN)
            {
                if(getTeamTurn() == TeamColor.WHITE && move.getEndPosition().getRow()-1 == lastMove.getEndPosition().getRow()
                        && move.getEndPosition().getColumn() == lastMove.getEndPosition().getColumn()) {
                    board.addPiece(lastMove.getEndPosition(),null);
                }
                if(getTeamTurn() == TeamColor.BLACK && move.getEndPosition().getRow()+1 == lastMove.getEndPosition().getRow()
                        && move.getEndPosition().getColumn() == lastMove.getEndPosition().getColumn()) {
                    board.addPiece(lastMove.getEndPosition(),null);
                }
            }
            //switch turns
            if(getTeamTurn() == TeamColor.WHITE) { setTeamTurn(TeamColor.BLACK); } else { setTeamTurn(TeamColor.WHITE); }
        } else {
            throw new InvalidMoveException("Invalid Move!");
        }
    }

    public static ChessMove getLastMove() {
        if(historyStack.empty()) {
            return null;
        }
        return historyStack.peek().move();
    }

    public void tryMove(ChessMove move) {
        tryStack.push(new HistoricalMove(board.getPiece(move.getStartPosition()), move, board.getPiece(move.getEndPosition())));
        board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
        board.addPiece(move.getStartPosition(), null);
    }

    public void untryMove() {
        HistoricalMove lastMove = tryStack.pop();
        if(lastMove != null) {
            board.addPiece(lastMove.move().getStartPosition(), lastMove.movedPiece());
            board.addPiece(lastMove.move().getEndPosition(), lastMove.targetPiece());
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = getKingPos(teamColor);
        if(kingPos != null) { //should never be false, but just in case
            for(int y = 1; y <= 8; y++) {
                for(int x = 1; x <= 8; x++) {
                    ChessPiece targetPiece = board.getPiece(new ChessPosition(y, x));
                    if( targetPiece != null && targetPiece.getTeamColor()!=teamColor ) {
                        for(ChessMove move: targetPiece.pieceMoves(board, new ChessPosition(y, x))) {
                            if(move.getEndPosition().getColumn() == kingPos.getColumn() && move.getEndPosition().getRow() == kingPos.getRow()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition getKingPos(TeamColor teamColor)
    {
        ChessPiece tempPiece;
        ChessPosition targetPos;
        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                targetPos = new ChessPosition(y, x);
                tempPiece = board.getPiece(new ChessPosition(y, x));
                if( tempPiece != null && tempPiece.getTeamColor() == teamColor &&
                        tempPiece.getPieceType() == ChessPiece.PieceType.KING){
                    return targetPos;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        //no available moves
        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                ChessPiece targetPiece = board.getPiece(new ChessPosition(y, x));
                if (targetPiece != null && targetPiece.getTeamColor() == teamColor && !validMoves(new ChessPosition(y, x)).isEmpty()) {
                    return false;
                }
            }
        }
        //and in check
        return isInCheck(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        //no available moves
        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                ChessPiece targetPiece = board.getPiece(new ChessPosition(y, x));
                if (targetPiece != null && targetPiece.getTeamColor() == teamColor && !validMoves(new ChessPosition(y, x)).isEmpty()) {
                    return false;
                }
            }
        }
        //and not in check and is your turn
        return !isInCheck(teamColor) && teamTurn==teamColor;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
