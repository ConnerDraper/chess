package ui;

import chess.ChessBoard;

/**
 * Helper class to render the board without having
 * to initialize a SQL server.
 */
public class BoardPreview {
    public static void main(String[] args) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        BoardRenderer.drawBoard(board, true);
        System.out.println();
        BoardRenderer.drawBoard(board, false);
    }
}
