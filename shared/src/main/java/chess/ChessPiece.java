package chess;

import chess.ChessGame.TeamColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
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

        if (this.type == PieceType.PAWN) {
            moves = pawnMoves(board, myPosition);
        } else if (this.type == PieceType.ROOK) {
            int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            moves = getMovesForDirections(board, myPosition, directions, true);
        } else if (this.type == PieceType.KNIGHT) {
            int[][] directions = {
                    {1, 2}, {2, 1},
                    {1, -2}, {2, -1},
                    {-1, 2}, {-2, 1},
                    {-1, -2}, {-2, -1}
            };
            moves = getMovesForDirections(board, myPosition, directions, false);
        } else if (this.type == PieceType.BISHOP) {
            int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            moves = getMovesForDirections(board, myPosition, directions, true);
        } else if (this.type == PieceType.QUEEN) {
            int[][] directions = {
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            };
            moves = getMovesForDirections(board, myPosition, directions, true);
        } else if (this.type == PieceType.KING) {
            int[][] directions = {
                    {-1, 1}, {0, 1}, {1, 1},
                    {-1, 0}, {1, 0},
                    {-1, -1}, {0, -1}, {1, -1}
            };
            moves = getMovesForDirections(board, myPosition, directions, false);
        }

        return moves;
    }

    /**
     * Helper function: Pawn moves
     * Pawns don't move like the other pieces, so they get their own method
     * @param board
     * @param myPosition
     * @return Collection of pawn moves
     */
    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        int row = myPosition.getRow();
        int column = myPosition.getColumn();

        int forward;
        int promotionRow;
        int startingRow;
        if (this.pieceColor == TeamColor.WHITE) {
            forward = 1;
            promotionRow = 8;
            startingRow = 2;
        } else {
            forward = -1;
            promotionRow = 1;
            startingRow = 7;
        }

        // it is impossible for a pawn to be on the edge - so we don't check for it.
        // if forward space is available...
        if (board.getPiece(row + forward, column) == null) {
            ChessPosition forwardSpace = new ChessPosition(row + forward, column);
            addPawnMove(moves, myPosition, forwardSpace, promotionRow);

            // if pawn hasn't moved yet and the space two ahead is also available...
            if ((row == startingRow) && (board.getPiece(row + (2 * forward), column) == null)) {
                ChessPosition doubleForward = new ChessPosition(row + (2 * forward), column);
                moves.add(new ChessMove(myPosition, doubleForward, null));
            }
        }

        // check both diagonals for an enemy piece to take
        for (int side : new int[]{1, -1}) {
            int newColumn = column + side;
            if ((newColumn < 1) || (newColumn > 8)) {
                continue;
            }
            ChessPiece takenPiece = board.getPiece(row + forward, newColumn);
            if ((takenPiece != null) && (takenPiece.getTeamColor() != this.pieceColor)) {
                ChessPosition newPosition = new ChessPosition(row + forward, newColumn);
                addPawnMove(moves, myPosition, newPosition, promotionRow);
            }
        }

        return moves;
    }

    /**
     * Helper function: Add pawn move
     * Keeps pieceMoves from being too large of a method
     * @param moves
     * @param start
     * @param end
     * @param promotionRow
     */
    private void addPawnMove(Collection<ChessMove> moves, ChessPosition start,
                             ChessPosition end, int promotionRow) {
        if (end.getRow() != promotionRow) {
            moves.add(new ChessMove(start, end, null));
            return;
        }
        for (PieceType piece : PieceType.values()) {
            if ((piece != PieceType.PAWN) && (piece != PieceType.KING)) {
                moves.add(new ChessMove(start, end, piece));
            }
        }
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

            while (i >= 1 && i <= 8 && j >= 1 && j <= 8) {
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
                if (!repeat) {
                    break;
                }
                i += dir[0];
                j += dir[1];
            }
        }
        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
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

}
