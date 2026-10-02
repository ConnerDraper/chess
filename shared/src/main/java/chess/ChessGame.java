package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    
    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(getBoard(), chessGame.getBoard()) && getTeamTurn() == chessGame.getTeamTurn();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBoard(), getTeamTurn());
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = this.board.getPiece(startPosition);
        if (piece == null) {return null;}
        else {
            throw new RuntimeException("Not implemented");
        }
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        TeamColor otherTeam;
        if (teamColor == TeamColor.WHITE) {
            otherTeam = TeamColor.BLACK;
        }
        else {
            otherTeam = TeamColor.WHITE;
        }

        Collection<ChessMove> validOppMoves = new ArrayList<>();
        Collection<ChessMove> validKingMoves = new ArrayList<>();
        ChessPosition currKingPosition;
        ChessPiece currPiece;

        if (this.isInCheck(teamColor)) {
            // Store all OPP valid moves
            // Store all MY king moves
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    currPiece = this.board.getPiece(row, col);
                    if (currPiece.getTeamColor() == otherTeam) {
                        validOppMoves.addAll(validMoves(new ChessPosition(row, col)));
                    }
                    else {
                        if (currPiece.getPieceType() == ChessPiece.PieceType.KING) {
                            currKingPosition = new ChessPosition(row, col);
                            validKingMoves.addAll(validMoves(currKingPosition));
                        }
                    }
                }
            }
        }
        // For kingMove -> if not in allValidOppMoves return false;
        for (ChessMove kingMove : validKingMoves) {
            if (!validOppMoves.contains(kingMove)) {
                return false;
            }
        }
        return true; // otherwise: return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard to a given board
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
