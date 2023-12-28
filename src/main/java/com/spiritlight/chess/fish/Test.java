package com.spiritlight.chess.fish;

import com.google.gson.JsonArray;
import com.spiritlight.chess.fish.game.FEN;
import com.spiritlight.chess.fish.game.Piece;
import com.spiritlight.chess.fish.game.utils.GameState;
import com.spiritlight.chess.fish.game.utils.board.AttackTable;
import com.spiritlight.chess.fish.game.utils.board.BoardMap;
import com.spiritlight.chess.fish.game.utils.board.Magic;
import com.spiritlight.chess.fish.game.utils.game.BoardEvaluator;
import com.spiritlight.chess.fish.game.utils.game.Move;
import com.spiritlight.chess.fish.game.utils.game.MovementEvent;
import com.spiritlight.chess.fish.internal.InternLogger;
import com.spiritlight.chess.fish.internal.utils.board.GameBitboard;
import com.spiritlight.chess.fish.internal.utils.resources.Resources;
import com.spiritlight.chess.fish.test.TestComponent;
import com.spiritlight.chess.fish.test.Tests;
import com.spiritlight.fishutils.misc.ThrowingRunnable;
import com.spiritlight.fishutils.utils.Stopwatch;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.stream.StreamSupport;

import static com.spiritlight.chess.fish.game.Piece.*;
import static com.spiritlight.chess.fish.game.utils.GameConstants.*;

/**
 * Makeshift class about testing basic functionalities and make sure
 * the fundamentals do not fail.
 */
public class Test {
    public static void main(String[] args) {
        InternLogger.setEnabled(true);

        Stopwatch timer = new Stopwatch();
        timer.start();
        testFenString(timer); // Contains I/O to receive off-heap data, fence internally instead.
        timer.record("fen");
        timer.fence("fen.parse");
        testParsing();
        timer.record("fen.parse");
        timer.fence("bit.mask");
        testMasking();
        timer.record("bit.mask");
        timer.fence("move.create");
        testMove();
        timer.record("move.create");
        timer.fence("boardmap.initialize");
        testBoard();
        timer.record("boardmap.initialize");
        timer.fence("boardmap.play");
        testBoardMove();
        timer.record("boardmap.play");
        timer.fence("bitboard.load");
        testBitboardLoad();
        timer.record("bitboard.load");
        timer.fence("piece.sliding");
        testGenerateSlidingMoves();
        timer.record("piece.sliding");
        timer.fence("boardmap.play");
        testPlay();
        timer.record("boardmap.play");
        timer.fence("boardmap.fen");
        testFENSetup();
        timer.record("boardmap.fen");
        timer.fence("boardmap.misc.moves");
        testMisc();
        timer.record("boardmap.misc.moves");
        timer.fence("table.attack.gen");
        testAttackMoveGen();
        timer.record("table.attack.gen");
        timer.fence("table.attack.getall");
        testAttackMoveGetAll();
        timer.record("table.attack.getall");
        timer.fence("test.misc");
        testTest();
        timer.record("test.misc");
        timer.fence("map.clone");
        testCloneDistinct();
        timer.record("map.clone");
        timer.fence("map.eval");
        testMapEvaluation();
        timer.record("map.eval");
        timer.fence("map.castles");
        testCastle();
        timer.record("map.castles");
        System.out.println(timer.getRecordString());
        System.out.println("All test case passed! Congratulations!");
    }

    private static void testCastle() {
        BoardMap map = BoardMap.fromFENString("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        Move kingCastle = Move.of("e1,h1");
        map.update(kingCastle);
        assertEquals(map.toFENString(), "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R4RK1 b kq - 1 1", "Kingside castle incorrect");

        BoardMap mapq = BoardMap.fromFENString("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        Move queenCastle = Move.of("e1,a1");
        mapq.update(queenCastle);
        assertEquals(mapq.toFENString(), "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/2KR3R b kq - 1 1", "Queenside castle incorrect");

        BoardMap mapbk = BoardMap.fromFENString("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b KQkq - 0 1");
        Move bkCastle = Move.of("e8,h8");
        mapbk.update(bkCastle);
        assertEquals(mapbk.toFENString(), "r4rk1/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQ - 1 2", "BKingside castle incorrect");

        BoardMap mapbq = BoardMap.fromFENString("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b KQkq - 0 1");
        Move bqCastle = Move.of("e8,a8");
        mapbq.update(bqCastle);
        assertEquals(mapbq.toFENString(), "2kr3r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQ - 1 2", "BQueenside castle incorrect");

    }

    private static void testMapEvaluation() {
        BoardMap map = BoardMap.initialize();

        double rating = BoardEvaluator.evaluate(map, GameState.EARLY_GAME);
        System.out.println(STR."Evaluate rating = \{rating}");
    }

    private static void testCloneDistinct() {
        BoardMap map = BoardMap.initialize();

        BoardMap copy = map.fork();

        assertFalse(map == copy, "reference equality after cloning: curr");
        assertFalse(map.getEnemyBoard() == copy.getEnemyBoard(), "reference equality after cloning: enemy");
    }

    private static void testTest() {
        BoardMap board = BoardMap.initialize();
        System.out.println(board.flatBoardView());
    }

    private static void testAttackMoveGetAll() {
        AttackTable.getBishop().iterator().forEachRemaining(table -> System.out.println(Magic.visualize(table)));
        AttackTable.getRook().iterator().forEachRemaining(table -> System.out.println(Magic.visualize(table)));
        AttackTable.getQueen().iterator().forEachRemaining(table -> System.out.println(Magic.visualize(table)));
        AttackTable.getKnight().iterator().forEachRemaining(table -> System.out.println(Magic.visualize(table)));
        AttackTable.getKing().iterator().forEachRemaining(table -> System.out.println(Magic.visualize(table)));
    }

    private static void testAttackMoveGen() {
        AttackTable.init();
    }

    private static void testMisc() {
        BoardMap knight = BoardMap.fromFENString("k7/8/2p1p3/1p3p2/3N4/1p3p2/2p1p3/7K w - - 0 1");
        int knightSource = Move.parseLocation("d4");
        String[] locations = {"c2", "e2", "b3", "f3", "b5", "f5", "c6", "e6"};
        for(String dest : locations) {
            assertTrue(knight.canMove(knightSource, Move.parseLocation(dest)), STR."Invalid moveset d4 \{dest}");
        }
        System.out.println("Knight move passed");
        BoardMap rook = BoardMap.fromFENString("4r3/2k5/4R3/8/rr2R1Rr/8/2K5/4r3 w - - 0 1");
        int rookSource = Move.parseLocation("e4");
        String[] okLocations = {"e1", "e2", "e3", "b4", "c4", "d4", "f4", "e5"};
        String[] noOkLocations = {"a4", "g4", "e6", "h4", "e7", "e8"};
        for(String dest : okLocations) {
            assertTrue(rook.canMove(rookSource, Move.parseLocation(dest)), STR."Invalid moveset e4 \{dest}");
        }
        for(String dest : noOkLocations) {
            assertFalse(rook.canMove(rookSource, Move.parseLocation(dest)), STR."Valid invalid moveset e4 \{dest}");
        }
        System.out.println("Rook move passed");
    }

    private static void testFENSetup() {
        BoardMap map = BoardMap.fromFENString(FEN.INITIAL_POSITION);
        assertEquals(map.toFENString(), FEN.INITIAL_POSITION, "FEN position not equal");
        BoardMap map2 = BoardMap.fromFENString("2kr1b2/p4p1p/2pp4/1p2p3/8/1BPp4/PP1B1P2/RN1QK1q1 w - - 0 24");
        assertEquals(map2.toFENString(), "2kr1b2/p4p1p/2pp4/1p2p3/8/1BPp4/PP1B1P2/RN1QK1q1 w - - 0 24", "FEN position not equal 2");

    }

    private static void testPlay() {
        assertSuccess(() -> {
            BoardMap map = BoardMap.initialize();
            System.out.println(STR."Evaluation: \{BoardEvaluator.evaluateFormatted(map, GameState.EARLY_GAME)}");

            System.out.println("-".repeat(32));
            System.out.println(map.boardView());
            System.out.println(map.flatBoardView());

            System.out.println("Playing e4");
            map.update("e2, e4");
            System.out.println("Playing e5");
            map.update("e7, e5");

            System.out.println(STR."Evaluation: \{BoardEvaluator.evaluateFormatted(map, GameState.EARLY_GAME)}");

            System.out.println("-".repeat(32));
            System.out.println(map.boardView());
            System.out.println(map.flatBoardView());

            System.out.println("Playing Bf6 and bxa6");

            map.update("f1, a6");
            MovementEvent captureCheck = map.update("b7, a6");

            System.out.println(captureCheck);
            assertEquals(captureCheck.capturedPiece(), WHITE | BISHOP, "Capture failed");

            System.out.println("-".repeat(32));
            System.out.println(map.boardView());
            System.out.println(map.flatBoardView());

            System.out.println(STR."Evaluation: \{BoardEvaluator.evaluateFormatted(map, GameState.MIDDLE_GAME)}");
        }, "Unexpected error whilst evaluating position");
    }

    private static void testGenerateSlidingMoves() {
        assertEquals(sliding(BISHOP, 0).length, 7, "Bishop sliding length wrong");
        assertEquals(sliding(ROOK, 0).length, 14, "Rook sliding length wrong");
        assertEquals(sliding(QUEEN, 0).length, 21, "Queen sliding length wrong");
        assertEquals(sliding(BISHOP, 63).length, 7, "Bishop sliding length wrong");
        assertEquals(sliding(ROOK, 63).length, 14, "Rook sliding length wrong");
        assertEquals(sliding(QUEEN, 63).length, 21, "Queen sliding length wrong");
        assertEquals(sliding(BISHOP, 28).length, 13, "Bishop sliding length wrong 13");
        assertEquals(sliding(ROOK, 28).length, 14, "Rook sliding length wrong 14");
        assertEquals(sliding(QUEEN, 28).length, 27, "Queen sliding length wrong 27");
    }

    private static void testBitboardLoad() {
        GameBitboard.init();
    }

    private static void testBoardMove() {
        BoardMap board = BoardMap.initialize();
        Move move = Move.of("e2", "e4");
        MovementEvent event = board.update(move);
        System.out.println(board.boardView());
        System.out.println(board.flatBoardView());
        System.out.println(board.toFENString());
        assertFalse(event::illegal, STR."Illegal movement for legal move: for move \{move}: \{event.toString()}");
        assertEquals(event.capturedPiece(), NONE, STR."Unexpected capture: \{event.capturedPiece()}");
        assertEquals(event.capturingPiece(), WHITE | PAWN, STR."Unexpected source: \{event.capturingPiece()}");
        assertEquals(board.toFENString(), "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", "Unexpected FEN");
        Move illegal = Move.of("e4, e6");
        MovementEvent illegalEvent = board.update(illegal);
        assertTrue(illegalEvent::illegal, STR."Illegal movement was legal: for move \{illegalEvent}");
        assertEquals(board.toFENString(), "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", "Unexpected FEN after illegal move");
    }

    private static void testBoard() {
        BoardMap board = BoardMap.initialize();
        assertEquals(board.toFENString(), FEN.INITIAL_POSITION, "Board conversion failed");
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
                    System.out.println(STR."Correct FEN position: \{saved}");
                    success++;
                }
            } catch (Exception ex) {
                System.err.println(STR."Unexpected exception during loading FEN string \{fen}");
                throw new RuntimeException(ex);
            }
        }

        for(String fen : INVALID_FEN_STRINGS) {
            assertFail(() -> FEN.load(fen), STR."String \{fen} parsed successfully unexpectedly.");
        }

        System.out.println(STR."FEN parsing test finished, \{success} success, \{fail} fail.");
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
            default -> throw new IllegalArgumentException(STR."for value \{board[i]} in pattern \{Arrays.toString(board)}");
        };
    }

    private static void assertInRange(double value, double min, double max, String message) {
        if(value < min || value > max) throw new AssertionError(message);
        System.out.printf("%f is in %f and %f\n", value, min, max);
    }

    private static void assertNotEquals(Object o1, Object o2, String message) {
        System.out.print(STR."Assertion check: \{o1} not equals \{o2}...");
        if(Objects.equals(o1, o2)) {
            System.err.printf("""
                    Two values matches:
                    %s
                    %s
                    """, o1, o2);
            System.err.println("If the objects are numeric, here's the parsed piece type:");
            System.err.println(STR."\{Piece.asString(String.valueOf(o1))}, \{Piece.asString(String.valueOf(o2))}");
            System.out.println();
            throw new AssertionError(message);
        }
        System.out.println(" OK");
    }

    private static void assertFalse(boolean bool, String message) {
        assertFalse(() -> bool, message);
    }

    private static void assertFalse(BooleanSupplier sup, String message) {
        if(sup.getAsBoolean()) throw new AssertionError(message);
    }

    private static void assertTrue(boolean bool, String message) {
        assertTrue(() -> bool, message);
    }

    private static void assertTrue(BooleanSupplier sup, String message) {
        if(!sup.getAsBoolean()) throw new AssertionError(message);
    }

    private static void assertEquals(Object o1, Object o2, String message) {
        System.out.print(STR."Assertion check: \{o1} equals \{o2}...");
        if(!Objects.equals(o1, o2)) {
            System.err.printf("""
                    Two values does not match:
                    %s [LHS]
                    %s [RHS]
                    """, o1, o2);
            System.err.println("If the objects are numeric, here's the parsed piece type:");
            System.err.println(STR."\{Piece.asString(String.valueOf(o1))}, \{Piece.asString(String.valueOf(o2))}");
            System.out.println();
            throw new AssertionError(message);
        }
        System.out.println(" OK");
    }

    private static void assertFail(ThrowingRunnable tr, String message, Throwable... type) {
        try {
            tr.run();
        } catch (Throwable t) {
            if(type == null || type.length == 0) {
                System.out.println(STR."Fail assertion passed, caught \{t.getClass().getCanonicalName()}: \{t.getMessage()}");
                return;
            }
            for(Throwable expect : type) {
                if(expect.getClass().isAssignableFrom(t.getClass()))  {
                    System.out.println(STR."Fail assertion passed, caught \{t.getClass().getCanonicalName()}: \{t.getMessage()}");
                    return;
                }
            }
        }
        throw new AssertionError(message);
    }

    private static void assertSuccess(ThrowingRunnable tr, String message) {
        try {
            tr.run();
            System.out.println("Success assertion passed");
        } catch (Throwable t) {
            throw new AssertionError(message, t);
        }
    }
}
