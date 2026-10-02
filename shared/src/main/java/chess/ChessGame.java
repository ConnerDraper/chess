package chess;

import java.sql.Array;
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
    private boolean whiteKingMoved = false;
    private boolean whiteRook1Moved = false;
    private boolean whiteRook8Moved = false;
    private boolean blackKingMoved = false;
    private boolean blackRook1Moved = false;
    private boolean blackRook8Moved = false;
    private ChessMove lastMove = null;

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
        Collection<ChessMove> allValidMoves = new ArrayList<>();
        Collection<ChessMove> proposedMoves = piece.pieceMoves(this.board, startPosition);
        TeamColor myTeamColor = piece.getTeamColor();

        for (ChessMove move : proposedMoves) {
            ChessBoard hypotheticalBoard = new ChessBoard(board);
            this.makeValidMove(move, hypotheticalBoard);
            if (!isInCheck(myTeamColor, hypotheticalBoard)) {
                allValidMoves.add(move);
            }
        }
        return allValidMoves;
    }
    /**
     * Private helper function
     * Makes a move that has already been confirmed as valid.
     */
    private void makeValidMove(ChessMove move, ChessBoard board) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (promotionPiece != null) {piece = new ChessPiece(piece.getTeamColor(), promotionPiece);}
        board.addPiece(endPosition, piece);
        board.addPiece(startPosition, null);
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("Piece is null at " + move.getStartPosition());
        }
        TeamColor myTeamColor = piece.getTeamColor();
        TeamColor currentTurn = this.getTeamTurn();
        ChessPosition myStartPosition = move.getStartPosition();
        ChessPosition myEndPosition = move.getEndPosition();
        if (myTeamColor != currentTurn) {
            throw new InvalidMoveException("It is not the turn of " + myTeamColor);
        }
        Collection<ChessMove> allValidMoves = validMoves(myStartPosition);
        if (!allValidMoves.contains(move)) {
            throw new InvalidMoveException("Illegal move: " + move);
        }

        // update moving tracker
        updateMoveTracker(move.getStartPosition());
        updateMoveTracker(move.getEndPosition());
        this.lastMove = move;

        // if no error was thrown, the move is valid
        this.makeValidMove(move, board);
        if (currentTurn == TeamColor.WHITE) {this.setTeamTurn(TeamColor.BLACK);}
        else {this.setTeamTurn(TeamColor.WHITE);}
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, this.board);
    }

    private boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        ChessPosition myKingPos = findKingPos(teamColor, board);
        TeamColor otherTeam;
        if (teamColor == TeamColor.WHITE) {otherTeam = TeamColor.BLACK;}
        else {otherTeam = TeamColor.WHITE;}

        Collection<ChessMove> validOppMoves = new ArrayList<>();
        Collection<ChessPosition> validOppSpaces = new ArrayList<>();

        ChessPiece currPiece;
        ChessPosition currPosition;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                currPosition = new ChessPosition(row, col);
                currPiece = board.getPiece(row, col);
                if (currPiece == null) continue;
                if (currPiece.getTeamColor() == otherTeam) {
                    validOppMoves.addAll(currPiece.pieceMoves(board, currPosition));
                }
            }
        }
        ChessPosition endPosition;
        for (ChessMove oppMove : validOppMoves) {
            endPosition = oppMove.getEndPosition();
            validOppSpaces.add(endPosition);
        }
        return validOppSpaces.contains(myKingPos);
    }

    /**
     * Helper function
     * Finds the king of a given team
     */
    public ChessPosition findKingPos(TeamColor teamColor) {
        return findKingPos(teamColor, this.board);
    }
    public ChessPosition findKingPos(TeamColor teamColor, ChessBoard board) {
        ChessPiece currPiece;
        ChessPosition kingPosition;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                currPiece = board.getPiece(row, col);
                if (currPiece == null) continue;
                if (currPiece.getTeamColor() == teamColor) {
                    if (currPiece.getPieceType() == ChessPiece.PieceType.KING) {
                        kingPosition = new ChessPosition(row, col);
                        return kingPosition;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if the current team has any valid moves
     * Useful for stalemate & checkmate
     */
    private boolean hasValidMoves(TeamColor teamColor) {
        ChessPosition currPosition;
        ChessPiece currPiece;
        Collection<ChessMove> currValidMoves;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                currPosition = new ChessPosition(row, col);
                currPiece = this.board.getPiece(row, col);
                if (currPiece != null) {
                    if (currPiece.getTeamColor() == teamColor) {
                        currValidMoves = this.validMoves(currPosition);
                        if (!currValidMoves.isEmpty()) {return true;}
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return (!hasValidMoves(teamColor));
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return !hasValidMoves(teamColor);
        }
        return false;
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        this.whiteKingMoved = false;
        this.whiteRook1Moved = false;
        this.whiteRook8Moved = false;
        this.blackKingMoved = false;
        this.blackRook1Moved = false;
        this.blackRook8Moved = false;
        this.lastMove = null;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    /**
     * Helper functions to check if the king, rook 1, or rook 8 have moved.
     * Helps with castling.
     * @param teamColor
     * @return
     */
    private boolean kingMoved(TeamColor teamColor) {
        if (teamColor == TeamColor.WHITE) {return this.whiteKingMoved;}
        else {return this.blackKingMoved;}
    }
    private boolean rook1Moved(TeamColor teamColor) {
        if (teamColor == TeamColor.WHITE) {return this.whiteRook1Moved;}
        else {return this.blackRook1Moved;}
    }
    private boolean rook8Moved(TeamColor teamColor) {
        if (teamColor == TeamColor.WHITE) {return this.whiteRook8Moved;}
        else {return this.blackRook8Moved;}
    }

    /**
     * Private helper function
     * Updates move tracker to ensure that castling is valid.
     * @param position
     */
    private void updateMoveTracker(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();

        if (row == 1 && col == 5) {this.whiteKingMoved = true;}
        if (row == 1 && col == 1) {this.whiteRook1Moved = true;}
        if (row == 1 && col == 8) {this.whiteRook8Moved = true;}
        if (row == 8 && col == 5) {this.blackKingMoved = true;}
        if (row == 8 && col == 1) {this.blackRook1Moved = true;}
        if (row == 8 && col == 8) {this.blackRook8Moved = true;}
    }

    private boolean canCastle(TeamColor teamColor, int rookIndex) {
        if (kingMoved(teamColor)) {return false;}
        if (rookIndex == 1) {
            if (this.rook1Moved(teamColor)) {return false;}
        }
        if (rookIndex == 8) {
            if (this.rook8Moved(teamColor)) {return false;}
        }


    }
}
