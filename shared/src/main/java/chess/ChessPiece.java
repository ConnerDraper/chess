package chess;
import chess.ChessGame.TeamColor;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
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
        throw new RuntimeException("Still need to finish this method (ChessPiece potentialPieceMoves");

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
            if (board.getPiece(row, column + forward) == null) {
                ChessPosition forwardSpace = ChessPosition(row, column + forward);
                // if forward space is a promotion...
                if (forwardSpace == promotionRow) {
                    for (ChessPiece p : PieceType.values()) {
                        if (p != PieceType.PAWN) {
                            ChessMove m = new ChessMove(myPosition, forwardSpace, p);
                            moves.add(m);
                        }
                    }

                }
                // if forward space is NOT a promotion...
                else {
                    ChessMove m = new ChessMove(myPosition, forwardSpace);
                    moves.add(m);
                }
            }


        }
        else if (this.type == PieceType.ROOK) {
            // positive direction of row
            for (int i = row; i <= 8; i++) {
                if (board.getPiece(i, column) != null) {
                    ChessPosition newPosition = ChessPosition(i, column);
                    ChessMove m = new ChessMove(myPosition, newPosition);
                    moves.add(m);
                }
                else {
                    if (board.getPiece(i, column).getTeamColor() == otherColor) {
                        ChessPosition newPosition = ChessPosition(i, column);
                        ChessMove m = new ChessMove(myPosition, newPosition);
                        moves.add(m);
                    }
                    break;
                }
            }

            // negative direction of row
            for (int i = row; i >= 1; i--) {
                if (board.getPiece(i, column) != null) {
                    ChessPosition newPosition = ChessPosition(i, column);
                    ChessMove m = new ChessMove(myPosition, newPosition);
                    moves.add(m);
                }
                else {
                    if (board.getPiece(i, column).getTeamColor() == otherColor) {
                        ChessPosition newPosition = ChessPosition(i, column);
                        ChessMove m = new ChessMove(myPosition, newPosition);
                        moves.add(m);
                    }
                    break;
                }
            }

            // positive direction of column
            for (int i = column; i <= 8; i++) {
                if (board.getPiece(row, i) != null) {
                    ChessPosition newPosition = ChessPosition(row, i);
                    ChessMove m = new ChessMove(myPosition, newPosition);
                    moves.add(m);
                }
                else {
                    if (board.getPiece(row, i).getTeamColor() == otherColor) {
                        ChessPosition newPosition = ChessPosition(row, i);
                        ChessMove m = new ChessMove(myPosition, newPosition);
                        moves.add(m);
                    }
                    break;
                }
            }

            // negative direction of column
            for (int i = column; i >= 1; i--) {
                if (board.getPiece(row, i) != null) {
                    ChessPosition newPosition = ChessPosition(row, i);
                    ChessMove m = new ChessMove(myPosition, newPosition);
                    moves.add(m);
                }
                else {
                    if (board.getPiece(row, i).getTeamColor() == otherColor) {
                        ChessPosition newPosition = ChessPosition(row, i);
                        ChessMove m = new ChessMove(myPosition, newPosition);
                        moves.add(m);
                    }
                    break;
                }
            }
        }
        else if (this.type == PieceType.KNIGHT) {
            throw new RuntimeException("Error: Not yet implemented.");
        }
        else if (this.type == PieceType.BISHOP) {
            throw new RuntimeException("Error: Not yet implemented.");
        }
        else if (this.type == PieceType.QUEEN) {
            throw new RuntimeException("Error: Not yet implemented.");
        }
        else if (this.type == PieceType.KING) {
            throw new RuntimeException("Error: Not yet implemented.");
        }
    }
}
