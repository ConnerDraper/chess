package chess;
import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final TeamColor pieceColor;
    private PieceType type;

    public ChessPiece(TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
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
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        TeamColor otherColor;
        if (this.pieceColor == TeamColor.WHITE) otherColor = TeamColor.BLACK;
        else if (this.pieceColor == TeamColor.BLACK) otherColor = TeamColor.WHITE;
        else throw new RuntimeException("Error: no pieceColor assigned to piece!");

        int row = myPosition.getRow();
        int column = myPosition.getColumn();

        if (this.type == PieceType.PAWN) {
            int forward;
            int promotionRow;
            if (this.pieceColor == TeamColor.WHITE) {
                forward = 1;
                promotionRow = 8;
            }
            else {
                forward = -1;
                promotionRow = 1;
            }

            // it is impossible for a pawn to be on the edge - so we don't check for it.
            // if forward space is available...
            if (board.getPiece(row + forward, column) == null) {
                ChessPosition forwardSpace = new ChessPosition(row + forward, column);
                // if forward space is a promotion...
                if (row + forward == promotionRow) {
                    for (PieceType p : PieceType.values()) {
                        if ((p != PieceType.PAWN) && (p!= PieceType.KING)) {
                            ChessMove m = new ChessMove(myPosition, forwardSpace, p);
                            moves.add(m);
                        }
                    }
                }
                // if forward space is NOT a promotion...
                else {
                    ChessMove m = new ChessMove(myPosition, forwardSpace, null);
                    moves.add(m);
                }
            }

            if (column != 8) {
                ChessPiece takenPiece = board.getPiece(row + forward, column + 1);
                if ((takenPiece != null) && (takenPiece.getTeamColor() != this.getTeamColor())) {
                    ChessPosition newPosition = new ChessPosition(row + forward, column + 1);
                    if (row + forward == promotionRow) {
                        for (PieceType p : PieceType.values()) {
                            if ((p != PieceType.PAWN) && (p != PieceType.KING)) {
                                ChessMove m = new ChessMove(myPosition, newPosition, p);
                                moves.add(m);
                            }
                        }
                    }
                    else {
                        ChessMove m = new ChessMove(myPosition, newPosition, null);
                        moves.add(m);
                    }
                }
            }
            if (column != 1) {
                ChessPiece takenPiece = board.getPiece(row + forward, column - 1);
                if ((takenPiece != null) && (takenPiece.getTeamColor() != this.getTeamColor())) {
                    ChessPosition newPosition = new ChessPosition(row + forward, column - 1);
                    if (row + forward == promotionRow) {
                        for (PieceType p : PieceType.values()) {
                            if ((p != PieceType.PAWN) && (p != PieceType.KING)) {
                                ChessMove m = new ChessMove(myPosition, newPosition, p);
                                moves.add(m);
                            }
                        }
                    }
                    else {
                        ChessMove m = new ChessMove(myPosition, newPosition, null);
                        moves.add(m);
                    }
                }
            }
            
            int startingRow;
            if (this.getTeamColor() == TeamColor.WHITE) {startingRow = 2;}
            else {startingRow = 7;}

            if (myPosition.getRow() == startingRow) {
                if ((board.getPiece(row + forward, column) == null) && 
                    (board.getPiece(row + (2 * forward), column) == null)) {
                        ChessPosition doubleForward = new ChessPosition(row + (2 * forward), column);
                        ChessMove m = new ChessMove(myPosition, doubleForward, null);
                        moves.add(m);
                }
            }
        }
        else if (this.type == PieceType.ROOK) {
            int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            moves = getMovesForDirections(board, myPosition, directions, true);
        }
        else if (this.type == PieceType.KNIGHT) {
            int[][] directions = {
                {1, 2}, {2, 1},
                {1, -2}, {2, -1},
                {-1, 2}, {-2, 1},
                {-1, -2}, {-2, -1}
            };
            moves = getMovesForDirections(board, myPosition, directions, false);
        }
        else if (this.type == PieceType.BISHOP) {
            int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            moves  = getMovesForDirections(board, myPosition, directions, true);
        }
        else if (this.type == PieceType.QUEEN) {
            int[][] directions = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            };
            moves = getMovesForDirections(board, myPosition, directions, true);
        }
        else if (this.type == PieceType.KING) {
            int[][] directions = {
                {-1, 1},  {0, 1},  {1, 1},
                {-1, 0},           {1, 0},
                {-1, -1}, {0, -1}, {1, -1}
            };
            moves = getMovesForDirections(board, myPosition, directions, false);
        }

    return moves;
    }

    /*
    Helper function that returns the moves for all directions.
    */
    public Collection<ChessMove> getMovesForDirections(
        ChessBoard board, ChessPosition myPosition, 
        int[][] directions, boolean repeat) {
        Collection<ChessMove> moves = new ArrayList<>();

        int row = myPosition.getRow();
        int column = myPosition.getColumn();

        for (int[] dir : directions) {
            int i = row + dir[0];
            int j = column + dir[1];

            while (i >= 1 && i <= 8 && j >= 1 && j <=8) {
                ChessPiece otherPiece = board.getPiece(i, j);
                ChessPosition newPosition = new ChessPosition(i, j);
                ChessMove m = new ChessMove(myPosition, newPosition, null);
                if (otherPiece != null) {
                    if (otherPiece.getTeamColor() != this.pieceColor) {
                        moves.add(m);
                    }
                    break;
                }
                moves.add(m);
                if (!repeat) break;
                i += dir[0];
                j += dir[1];
            }
        }
        return moves;
    }
}
