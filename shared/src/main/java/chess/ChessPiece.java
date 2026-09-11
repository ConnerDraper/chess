package chess;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;
    private ChessPosition position;

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
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gives potential moves for a given piece.
     * Does not account for occupied squares or game state.
     * Only returns squares that a piece could physically move to.
     * Helper function for pieceMoves.
     * @param board
     * @param myPosition
     * @return
     */
    public Collection<ChessMove> potentialPieceMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] moves = new int[8][8];
        TeamColor otherColor;
        if (this.pieceColor == teamColor.WHITE) otherColor = teamColor.BLACK;
        else if (this.pieceColor == teamColor.BLACK) otherColor = teamColor.WHITE;
        else throw new RuntimeException("Error: no pieceColor assigned to piece!");

        throw new RuntimeException("Still need to finish this method (ChessPiece potentialPieceMoves");

        if (this.type == PieceType.PAWN) {
            
        }
        else if (this.type == PieceType.ROOK) {

        }
    }
}
