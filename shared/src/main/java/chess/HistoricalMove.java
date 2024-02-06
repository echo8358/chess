package chess;

public record HistoricalMove(ChessPiece movedPiece, ChessMove move, ChessPiece targetPiece) { }