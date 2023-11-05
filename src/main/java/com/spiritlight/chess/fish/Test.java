package com.spiritlight.chess.fish;

import com.google.gson.JsonArray;
import com.spiritlight.chess.fish.game.FEN;
import com.spiritlight.chess.fish.game.Piece;
import com.spiritlight.chess.fish.game.utils.board.BoardMap;
import com.spiritlight.chess.fish.game.utils.game.Move;
import com.spiritlight.chess.fish.internal.utils.resources.Resources;
import com.spiritlight.fishutils.misc.ThrowingRunnable;
import com.spiritlight.fishutils.utils.Stopwatch;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static com.spiritlight.chess.fish.game.Piece.*;
import static com.spiritlight.chess.fish.game.utils.GameConstants.*;

public class Test {
    public static void main(String[] args) {
        System.out.printf("9=%s, 17=%s\n", Piece.asString(9), Piece.asString(17));
        Stopwatch timer = new Stopwatch();
        timer.start();
        testFenString(timer); // Contains I/O to receive off-heap data, fence internally instead.
        long fen = timer.get("fen");
        timer.fence("gen");
        generateBoard(FEN.getInitialPosition());
        long gen = timer.get("gen");
        timer.fence("parse");
        testParsing();
        long parse = timer.get("parse");
        timer.fence("mask");
        testMasking();
        long mask = timer.get("mask");
        timer.fence("move");
        testMove();
        long move = timer.get("move");
        timer.fence("board");
        testBoard();
        long board = timer.get("board");
        long elapsed = timer.stop();
        System.out.printf("""
                Test finished.
                
                Total time elapsed: %dns (~%.2fms)
                FEN time elapsed: %dns (~%.2fms)
                Board generation time elapsed: %dns (~%.2fms)
                FEN parse time elapsed: %dns (~%.2fms)
                Masking time elapsed: %dns (~%.2fms)
                Move time elapsed: %dns (~%.2fms)
                Board parsing time elapsed: %dns (~%.2fms)
                """,
                elapsed, elapsed / 1000000d,
                fen, fen / 1000000d,
                gen, gen / 1000000d,
                parse, parse / 1000000d,
                mask, mask / 1000000d,
                move, move / 1000000d,
                board, board / 1000000d);
    }

    private static void testBoard() {
        BoardMap board = BoardMap.initialize();
        assertEquals(FEN.INITIAL_POSITION, board.toFENString(), "Board conversion failed");
    }

    private static void testMove() {
        Move move = Move.of("a4", "b3");
        assertEquals(move.toString(), "Move[a4, b3]", "Move mismatch 1");
        assertEquals(move.up().toString(), "Move[a4, b4]", "Move mismatch up");
        assertEquals(move.down().toString(), "Move[a4, b2]", "Move mismatch down");
        assertEquals(move.left().toString(), "Move[a4, a3]", "Move mismatch left");
        assertEquals(move.right().toString(), "Move[a4, c3]", "Move mismatch right");

        System.out.println("Move test successful 1 creation, 5 tests.");
    }

    private static void testMasking() {
        assertEquals((WHITE | QUEEN) & PIECE_MASK, QUEEN, "Masking Mismatch Q");
        assertEquals((BLACK | KING) & PIECE_MASK, KING, "Masking Mismatch k");
        assertEquals((WHITE | QUEEN) & COLOR_MASK, WHITE, "Masking Mismatch W");
        assertEquals((BLACK | KING) & COLOR_MASK, BLACK, "Masking Mismatch B");

        assertEquals((WHITE_CASTLE_KING_SIDE | WHITE_CASTLE_QUEEN_SIDE) & ~WHITE_CASTLE_MASK, 0, "Masking Mismatch O-O");
        assertEquals((BLACK_CASTLE_KING_SIDE | BLACK_CASTLE_QUEEN_SIDE) & ~BLACK_CASTLE_MASK, 0, "Masking Mismatch o-o");

        System.out.println("Mask test successful. 6 tests");
    }

    private static void testParsing() {
        /* Position */
        String position = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        String expected = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0";
        assertEquals(FEN.get(FEN.load(position)), expected, "FEN Position Mismatch");
        /* Who to play */
        assertEquals(FEN.parseTurn(WHITE_TURN), "w", "Turn Parsing Mismatch Ww");
        assertEquals(FEN.parseTurn(BLACK_TURN), "b", "Turn Parsing Mismatch Bb");
        assertEquals(FEN.parseTurn("w"), WHITE_TURN, "Turn Parsing Mismatch wW");
        assertEquals(FEN.parseTurn("b"), BLACK_TURN, "Turn Parsing Mismatch bB");
        /* Castle rights */
        assertEquals(FEN.parseCastle("KQkq"),
                WHITE_CASTLE_KING_SIDE | WHITE_CASTLE_QUEEN_SIDE | BLACK_CASTLE_KING_SIDE | BLACK_CASTLE_QUEEN_SIDE,
                "Castle right mismatch");
        /* En passant square */
        assertEquals(FEN.parseEnPassant("d6"), 0x35, "En passant mismatch dL");
        assertEquals(FEN.parseEnPassant(0x35), "d6", "En passant mismatch Ld");
        // We do not test for half-move/full-move as those are literally calling parseInt.
        System.out.println("Parsing test complete. Multiple tests.");
    }

    private static void testFenString(Stopwatch timer) {
        JsonArray arr = Resources.getAsJson("fen_test.json").getAsJsonArray();
        String[] VALID_FEN_STRINGS = StreamSupport.stream(arr.spliterator(), false)
                .map(element -> element.getAsJsonObject().get("fen").getAsString()).toArray(String[]::new);

        String[] INVALID_FEN_STRINGS = new String[] {
                "3r1rk1/1pp1bpp1/6p1/pP1npqPn/8/4N2P/P2PP3/1B2BP2/R2QK2R",
                "5n1k/1p3r1qp/p3p3/2p1N2Q/2P1R3/2P5/P2r1PP1/4R1K1",
                "6k1/pp3ppp/4p3/2P3b1/bPP3P1/3K4/P3Q1q1"
        };
        int success = 0, fail = 0;

        timer.fence("fen");
        for (String fen : VALID_FEN_STRINGS) {
            try {
                int[] generated = FEN.load(fen);
                String saved = FEN.get(generated);
                if (!fen.equals(saved)) {
                    System.err.println("Unexpected FEN position: ");
                    System.err.println(fen);
                    System.err.println(saved);
                    fail++;
                } else {
                    System.out.println("Correct FEN position: " + saved);
                    success++;
                }
            } catch (Exception ex) {
                System.err.println("Unexpected exception during loading FEN string " + fen);
                throw new RuntimeException(ex);
            }
        }

        for(String fen : INVALID_FEN_STRINGS) {
            assertFail(() -> FEN.load(fen), "String " + fen + " parsed successfully unexpectedly.");
        }

        System.out.println("FEN parsing test finished, " + success + " success, " + fail + " fail.");
    }

    //║♖│♘│♗│♕│♔│♗│♘║1
    //║♜│♞│♝│♛│♚│♝│♞║8

    /**
     * Generates the board layout
     * @param board the board
     */
    private static void generateBoard(int[] board) {
        for (int i = 0; i < 64;) {
            final String s = getPiece(board, i);
            System.out.print(s);
            if(++i % 8 == 0) System.out.println();
        }
        System.out.printf("%s to play | Castle status: %s | En passant: %s | Half-move: %s | Full-move: %s\n",
                board[64] == WHITE_TURN ? "White" : "Black",
                FEN.parseCastle(board[65]),
                board[66] == 0 ? "-" : String.valueOf((char) ((board[66] >> 4) + 'a')) + ((char) ((board[66] & 0x0F) + '1')),
                board[67],
                board[68]);

        System.out.println("Board generation successful.");
    }

    /**
     * Helper method to retrieve a piece's icon by board index
     * @param board the board
     * @param i the index
     * @return the value
     */
    private static String getPiece(int[] board, int i) {
        char charValue = (char) board[i];
        return switch (charValue) {
            case NONE -> "⬛";
            case WHITE | PAWN -> "♙";
            case BLACK | PAWN -> "♟";
            case WHITE | KNIGHT -> "♘";
            case BLACK | KNIGHT -> "♞";
            case WHITE | BISHOP -> "♗";
            case BLACK | BISHOP -> "♝";
            case WHITE | ROOK -> "♖";
            case BLACK | ROOK -> "♜";
            case WHITE | QUEEN -> "♕";
            case BLACK | QUEEN -> "♛";
            case WHITE | KING -> "♔";
            case BLACK | KING -> "♚";
            default -> throw new IllegalArgumentException("for value " + board[i] + " in pattern " + Arrays.toString(board));
        };
    }

    private static void assertEquals(Object o1, Object o2, String message) {
        if(!Objects.equals(o1, o2)) {
            System.err.printf("""
                    Two values does not match:
                    %s
                    %s
                    """, o1, o2);
            throw new AssertionError(message);
        }
    }

    private static void assertFail(ThrowingRunnable tr, String message, Throwable... type) {
        try {
            tr.run();
        } catch (Throwable t) {
            if(type == null || type.length == 0) {
                System.out.println("Fail assertion passed, caught " + t.getClass().getCanonicalName() + ": " + t.getMessage());
                return;
            }
            for(Throwable expect : type) {
                if(expect.getClass().isAssignableFrom(t.getClass()))  {
                    System.out.println("Fail assertion passed, caught " + t.getClass().getCanonicalName() + ": " + t.getMessage());
                    return;
                }
            }
        }
        throw new AssertionError(message);
    }
}
