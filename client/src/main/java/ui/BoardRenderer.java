package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class BoardRenderer {

    private static final String[] FILES = {
            " \u200Aa\u2003", " \u200Ab\u2003", " \u200Ac\u2003", " \u200Ad\u2003",
            " \u200Ae\u2003", " \u200Af\u2003", " \u200Ag\u2003", " \u200Ah\u2003"
    };
    private static final String[] RANKS = {
            " \u200A1\u2003", " \u200A2\u2003", " \u200A3\u2003", " \u200A4\u2003",
            " \u200A5\u2003", " \u200A6\u2003", " \u200A7\u2003", " \u200A8\u2003"
    };

    private static final String[] WHITE_PIECES = {WHITE_KING, WHITE_QUEEN, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK, WHITE_PAWN};
    private static final String[] BLACK_PIECES = {BLACK_KING, BLACK_QUEEN, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK, BLACK_PAWN};

    private static final String LIGHT_SQUARE = SET_BG_COLOR_WHITE;
    private static final String DARK_SQUARE = SET_BG_COLOR_LIGHT_GREY;
    private static final String BORDER = SET_BG_COLOR_BLACK + SET_TEXT_COLOR_YELLOW;
    private static final String RESET = RESET_BG_COLOR + RESET_TEXT_COLOR + RESET_TEXT_BOLD_FAINT;

    public static void drawBoard(ChessBoard board, boolean isWhite) {
        printFileLabels(isWhite);

        if (isWhite) {
            for (int rank = 8; rank >= 1; rank--) {
                printRank(board, rank, isWhite);
            }
        } else {
            for (int rank = 1; rank <= 8; rank++) {
                printRank(board, rank, isWhite);
            }
        }

        printFileLabels(isWhite);
    }

    private static void printFileLabels(boolean isWhite) {
        System.out.print(BORDER + EMPTY);
        if (isWhite) {
            for (int file = 1; file <= 8; file++) {
                System.out.print(FILES[file - 1]);
            }
        } else {
            for (int file = 8; file >= 1; file--) {
                System.out.print(FILES[file - 1]);
            }
        }
        System.out.println(EMPTY + RESET);
    }

    private static void printRank(ChessBoard board, int rank, boolean isWhite) {
        String label = BORDER + RANKS[rank - 1];
        System.out.print(label);

        if (isWhite) {
            for (int file = 1; file <= 8; file++) {
                printSquare(board, rank, file);
            }
        } else {
            for (int file = 8; file >= 1; file--) {
                printSquare(board, rank, file);
            }
        }

        System.out.println(RESET + label + RESET);
    }

    private static void printSquare(ChessBoard board, int rank, int file) {
        boolean rankIsEven = rank % 2 == 0;
        boolean fileIsEven = file % 2 == 0;

        String background = DARK_SQUARE;
        if (rankIsEven != fileIsEven) {
            background = LIGHT_SQUARE;
        }

        ChessPiece piece = board.getPiece(new ChessPosition(rank, file));
        System.out.print(background + SET_TEXT_BOLD + pieceSymbol(piece));
    }

    private static String pieceSymbol(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }

        int index = piece.getPieceType().ordinal();
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return SET_TEXT_COLOR_RED + WHITE_PIECES[index];
        }
        return SET_TEXT_COLOR_BLACK + BLACK_PIECES[index];
    }
}
