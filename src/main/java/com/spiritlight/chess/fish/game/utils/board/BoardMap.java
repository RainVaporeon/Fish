package com.spiritlight.chess.fish.game.utils.board;

import com.spiritlight.chess.fish.game.FEN;
import com.spiritlight.chess.fish.game.Piece;
import com.spiritlight.chess.fish.game.utils.EndType;
import com.spiritlight.chess.fish.game.utils.GameState;
import com.spiritlight.chess.fish.game.utils.game.BoardEvaluator;
import com.spiritlight.chess.fish.game.utils.game.Move;
import com.spiritlight.chess.fish.game.utils.game.MovementEvent;
import com.spiritlight.chess.fish.internal.InternLogger;
import com.spiritlight.chess.fish.internal.exceptions.SystemError;
import com.spiritlight.chess.fish.internal.utils.StableField;
import com.spiritlight.fishutils.collections.Pair;
import com.spiritlight.fishutils.misc.annotations.Modifies;
import com.spiritlight.fishutils.misc.arrays.primitive.CharacterArray;
import com.spiritlight.fishutils.misc.arrays.primitive.IntArray;

import java.util.Arrays;
import java.util.Iterator;

import static com.spiritlight.chess.fish.game.FEN.*;
import static com.spiritlight.chess.fish.game.Piece.*;
import static com.spiritlight.chess.fish.game.utils.GameConstants.*;
import static com.spiritlight.chess.fish.game.utils.game.Move.FORWARD_OFFSET;

public class BoardMap {
    private static final long PAWN_MASK   = 0xFF00;
    private static final long BISHOP_MASK = 0b00100100;
    private static final long KNIGHT_MASK = 0b01000010;
    private static final long ROOK_MASK   = 0b10000001;
    private static final long KING_MASK   = 0b00010000;
    private static final long QUEEN_MASK  = 0b00001000;

    private long pawn;
    private long bishop;
    private long knight;
    private long rook;
    private long queen;
    private long king;
    private final int color;
    private final StableField<BoardMap> enemyBoard;
    // Consider this for castling, ~0xF0/0x0F to cancel one
    private int castle = 0xFF;
    private int pawnAdvance = 0b11111111;
    private int enPassantSquare = 0;
    private int halfMove = 0;
    private int fullMove = 1;
    private int turn = WHITE_TURN;

    private BoardMap(long pawn, long bishop, long knight, long rook, long queen, long king, int color) {
        this.pawn = pawn;
        this.bishop = bishop;
        this.knight = knight;
        this.rook = rook;
        this.queen = queen;
        this.king = king;
        this.color = color;
        this.enemyBoard = new StableField<>(null);
    }

    public int getColor() {
        return color;
    }

    public BoardMap getEnemyBoard() {
        return enemyBoard.get();
    }

    public MovementEvent update(String move) {
        return this.update(Move.of(move));
    }

    public MovementEvent update(Move move) {
        int src  = move.sourcePos();
        int dest = move.destPos();

        int srcPiece  = this.getPieceAt(src);
        int destPiece = this.getPieceAt(dest);
        InternLogger.getLogger().debug("Move " + move + " extracts src=" + Piece.asString(srcPiece) + ", dest=" + Piece.asString(destPiece));
        // Turn checking
        if(Piece.color(srcPiece) != (turn == WHITE_TURN ? WHITE : BLACK)) {
            InternLogger.getLogger().debug("Piece " + Piece.asString(srcPiece) + " is not of color " + Piece.asString(color));
            return turn == WHITE_TURN ? MovementEvent.WHITE_TO_PLAY : MovementEvent.BLACK_TO_PLAY;
        }
        // Destination checking; cannot capture pieces of the same color
        if(Piece.color(srcPiece) == Piece.color(destPiece)) return MovementEvent.CAPTURING_SAME;
        // Move checking, cannot move from nothing.
        if(srcPiece == NONE) return MovementEvent.ILLEGAL;

        if(turn == WHITE_TURN) {
            return handleMove(srcPiece, src, dest, destPiece, enemyBoard.get(), move);
        } else {
            return this.enemyBoard.get().handleMove(srcPiece, src, dest, destPiece, this, move);
        }
    }

    public Pair<GameState, EndType> getGameState() {
        int clock = this.halfMove + enemyBoard.get().halfMove;
        if(clock >= 100) return Pair.of(GameState.GAME_END, EndType.DRAW_50_MOVE);
        // TODO: Either rework or change this
        throw new UnsupportedOperationException("not implemented");
    }

    public int getPieceAt(int src) {
        // InternLogger.getLogger().debug("Debugging piece at board " + color + ": " + src);
        int self = this.getSelfPieceAt(src, false);
        // InternLogger.getLogger().debug("Self piece: " + Piece.asString(self));
        if(self == NONE) return this.enemyBoard.get().getSelfPieceAt(src, false);
        return self;
    }

    public int getSelfPieceAt(int src, boolean fen) {
        BoardItr itr = itr();
        long mask = fen ? getFENMask(src) : getMask(src);
        while (itr.hasNext()) {
            long value = itr.next();
            if((value & mask) != 0) {
                // InternLogger.getLogger().debug("Self piece at board " + color + " at " + src + " with fen " + fen + ": " + Piece.asString(itr.cursorPiece() | color));
                return itr.cursorPiece() | color;
            }
        }
        // InternLogger.getLogger().debug("Self piece at board " + color + " at " + src + " with fen " + fen + ": none");
        return NONE;
    }

    /**
     * Reconstructs the int array representation of this board and converts it
     * to a FEN string.
     * @return the fen string
     */
    public String toFENString() {
        if(this.color == BLACK && this.enemyBoard.get().color == BLACK) {
            throw new SystemError("unexpected color: this and enemy board color is black. dump: " + this + ", enemy=" + this.enemyBoard.get());
        }
        if(this.color == BLACK) return this.enemyBoard.get().toFENString(); // Convenience purposes only.
        int[] positions = new int[69]; // pos:color
        int arrIdx = 0;
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 7; file >= 0; file--) {
                int pos = (rank * 8) + file;
                int piece = this.getSelfPieceAt(pos, true);
                int enemy = enemyBoard.get().getSelfPieceAt(pos, true);
                if(piece == NONE && enemy == NONE) positions[arrIdx] = NONE;
                if(piece == NONE) positions[arrIdx] = enemy;
                if(enemy == NONE) positions[arrIdx] = piece;
                if(enemy != NONE && piece != NONE) throw new SystemError(String.format("duplicate position: array=%s, pos=%d, type=%d,%d", Arrays.toString(positions), pos, piece, enemy));
                // InternLogger.getLogger().debug("Index " + pos + " (" + Move.parseLocation(pos) + ": " + Piece.asString(positions[arrIdx]) + ") has piece ID " + positions[arrIdx]);
                arrIdx++;
            }
        }
        int castleState = 0;
        if((this.castle & 0x0F) != 0) castleState |= WHITE_CASTLE_KING_SIDE;
        if((this.castle & 0xF0) != 0) castleState |= WHITE_CASTLE_QUEEN_SIDE;
        if((enemyBoard.get().castle & 0x0F) != 0) castleState |= BLACK_CASTLE_KING_SIDE;
        if((enemyBoard.get().castle & 0xF0) != 0) castleState |= BLACK_CASTLE_QUEEN_SIDE;
        positions[TURN] = turn;
        positions[CASTLE] = castleState;
        positions[EN_PASSANT] = BoardHelper.getFENPosition(enPassantSquare);
        positions[HALF_MOVE] = (this.halfMove + enemyBoard.get().halfMove);
        positions[FULL_MOVE] = this.fullMove; // Synchronized
        return FEN.get(positions);
    }

    public static BoardMap fromFENString(String fen) {
        int[] layout = FEN.load(fen);
        int arrIdx = 0;
        BoardMap boardMap = BoardMap.clearBoard(WHITE);
        BoardMap enemyBoardMap = BoardMap.clearBoard(BLACK);
        boardMap.enemyBoard.set(enemyBoardMap);
        enemyBoardMap.enemyBoard.set(boardMap);
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 7; file >= 0; file--) {
                long mask = BoardMap.getFENMask((rank * 8) + file);
                if((layout[arrIdx] & COLOR_MASK) == WHITE) {
                    boardMap.retain(layout[arrIdx] & PIECE_MASK, mask);
                } else {
                    enemyBoardMap.retain(layout[arrIdx] & PIECE_MASK, mask);
                }
                arrIdx++;
            }
        }
        int turn = layout[TURN];
        boardMap.turn = turn;
        enemyBoardMap.turn = turn;
        int castle = layout[CASTLE];
        int wCastle = castle & WHITE_CASTLE_MASK;
        int bCastle = castle & BLACK_CASTLE_MASK;
        if((wCastle & WHITE_CASTLE_KING_SIDE) != 0) boardMap.castle |= 0xF0;
        if((wCastle & WHITE_CASTLE_QUEEN_SIDE) != 0) boardMap.castle |= 0x0F;
        if((bCastle & BLACK_CASTLE_KING_SIDE) != 0) enemyBoardMap.castle |= 0xF0;
        if((bCastle & BLACK_CASTLE_QUEEN_SIDE) != 0) enemyBoardMap.castle |= 0x0F;
        int enPassant = layout[EN_PASSANT];
        boardMap.enPassantSquare = BoardHelper.fromFENPosition(enPassant);
        enemyBoardMap.enPassantSquare = BoardHelper.fromFENPosition(enPassant);
        int half = layout[HALF_MOVE];
        boardMap.halfMove = half;
        enemyBoardMap.halfMove = half;
        int full = layout[FULL_MOVE];
        boardMap.fullMove = full;
        enemyBoardMap.fullMove = full;
        return boardMap;
    }

    private static BoardMap clearBoard(int color) {
        return new BoardMap(0,0,0,0,0,0,color);
    }

    public String boardView() {
        return BoardHelper.viewBoard(this.toFENString());
    }

    public String flatBoardView() {
        String str = "0123456789".repeat(7).substring(0, 64).concat("\n");
        StringBuilder builder = new StringBuilder(64);
        for(int i = 0; i < 64; i++) {
            builder.append(Piece.asCharacter(this.getPieceAt(i)));
        }
        String other = "\n1bcdefgh2bcdefgh3bcdefgh4bcdefgh5bcdefgh6bcdefgh7bcdefgh8bcdefgh";
        return str + builder + other;
    }

    public BoardItr itr() {
        return new BoardItr();
    }

    /**
     * Retrieves a mask with all bits but the specified location turned off.
     * This shifts from the largest bit, whereas {@link BoardMap#getByteMask(int)}
     * shifts from the smallest bit.
     * @param location the bit position to be turned off
     * @return a mask with all bits but the location turned off.
     */
    private static long getMask(int location) {
        return 1L << location;
        // return 0x8000_0000_0000_0000L >>> location;
        // return ~(1L << location);
    }

    // Retrieves the mask used for FEN
    private static long getFENMask(int location) {
        return Long.MIN_VALUE >>> location;
    }

    /**
     * Retrieves a bitmask with the specified position turned on.
     * This shifts from the smallest bit, whereas {@link BoardMap#getMask(int)}
     * shifts from the largest bit.
     * @see BoardMap#getMask(int)
     */
    private static int getByteMask(int position) {
        return 0b10000000 >>> position;
        // return ~(1 << position);
    }

    /**
     * handles the move
     * @param srcPiece the source piece
     * @param srcPos the source position
     * @param destPos the destination position
     * @param destPiece the captured piece
     * @param enemyMap the enemy BoardMap to update if {@code capturedPiece != null}
     */
    private MovementEvent handleMove(int srcPiece, int srcPos, int destPos, int destPiece, BoardMap enemyMap, Move move) {
        InternLogger.getLogger().debug("Source: " + srcPos + ", Destination: " + destPos + " (Origin: " + (srcPos + 1) + ", " + (destPos + 1) + ")");
        InternLogger.getLogger().debug("State: " + BoardHelper.getPositionString(srcPos + 1) + ", " + BoardHelper.getPositionString(destPos + 1));
        InternLogger.getLogger().debug("Has: " + Piece.asString(srcPiece) + ", To: " + Piece.asString(destPiece));
        long srcMask = getMask(srcPos);
        long destMask = getMask(destPos);

        /* Verifying the result, 0 is OK
        * Check should include path checks and other processing,
        * but should not involve color/piece checking, as we've
        * checked it before masks were even retrieved.       */

        int handlerCode = switch (srcPiece & ~COLOR_MASK) {
            case PAWN -> verifyPawn(srcPos, destPos, destPiece);
            case BISHOP -> verifyBishop(srcPos, destPos);
            case KNIGHT -> verifyKnight(srcPos, destPos);
            case ROOK -> verifyRook(srcPos, destPos);
            case QUEEN -> verifyQueen(srcPos, destPos);
            case KING -> verifyKing(srcPos, destPos);
            default -> throw new IllegalStateException("Unexpected value: " + (srcPiece & ~COLOR_MASK));
        };

        MovementEvent error = translate(handlerCode);
        // error.code() & 0x07 is not 0
        if(error != null) return error;

        // Anything past this line is not going to be interrupted.

        halfMove++;

        /* Calculating 50-move rule and en passant */
        if(Piece.is(srcPiece, PAWN)) {
            halfMove = 0;
        }
        /* Castle rights too */
        if(Piece.is(srcPiece, KING)) {
            castle &= ~0xFF;
        }

        /* Castle rights */
        if(Piece.is(srcPiece, ROOK)) {
            if(this.getFile(srcPos) == 0) {
                castle &= ~0xF0;
            } else {
                castle &= ~0x0F;
            }
        }

        // Past this line is when all moves are confirmed to be valid

        clear(srcPiece & ~COLOR_MASK, srcMask);
        retain(srcPiece & ~COLOR_MASK, destMask);

        if(turn == BLACK_TURN) {
            fullMove++;
            enemyBoard.get().fullMove++;
        }

        if(destPiece != NONE && enemyMap != null) {
            halfMove = 0;
            enemyMap.clear(destPiece & ~COLOR_MASK, destMask);
        }

        turn ^= COLOR_MASK;
        enemyBoard.get().turn ^= COLOR_MASK;

        return new MovementEvent(srcPiece, destPiece, move);
    }

    // Translates the given code to the event
    private static MovementEvent translate(int code) {
        if((code & ~PIECE_MASK) == 0) return null;
        return MovementEvent.getTranslationCode(code);
    }

    // Section to verify the validity of a movement.
    // If it mutates board structure in the process,
    // methods are annotated with the @Modifies annotation
    // with what parameter in this class is mutated.

    // Side note, I kind of forgot why I made the translating
    // method return null (denoting success) if &~0b111 returns
    // 0. Leaving it as is though.

    @Modifies({"enPassantSquare", "pawnAdvance"})
    private int verifyPawn(int srcPos, int destPos, int destPiece) {
        int file = this.getFile(srcPos);
        int destFile = this.getFile(destPos);
        if (Math.abs(file - destFile) > 1) {
            InternLogger.getLogger().debug("Distance too far horizontally: From " + file + " to " + destFile);
            return MovementEvent.ILLEGAL.code();
        }
        if ((Math.abs(file - destFile) == 1 && destPiece == NONE) || (file - destFile == 0 && destPiece != NONE)) {
            InternLogger.getLogger().debug("Captures nothing");
            return MovementEvent.ILLEGAL.code();
        }

        if(Math.abs(this.getRank(srcPos) - this.getRank(destPos)) == 2) {
            if((pawnAdvance & getByteMask(file)) == 0) {
                InternLogger.getLogger().debug("Pawn advance does not match with byte mask");
                return MovementEvent.ILLEGAL.code();
            }
            if(this.getPieceAt(srcPos + (this.turn == WHITE_TURN ? FORWARD_OFFSET : -FORWARD_OFFSET)) != NONE) {
                InternLogger.getLogger().debug("Pawn advances into something");
                return MovementEvent.ILLEGAL.code();
            }
            // We add one here to restore it back onto its actual represented location
            enPassantSquare = srcPos + (this.turn == WHITE_TURN ? FORWARD_OFFSET : -FORWARD_OFFSET);
        }
        pawnAdvance &= ~getByteMask(file);
        return 0;
    }

    private int verifyKnight(int srcPos, int destPos) {
        // Honestly somewhat easy to check on this one
        // as knights skip over pieces
        int diff = Math.abs(srcPos - destPos);
        boolean valid = Math.abs(BoardHelper.getRank(diff) - BoardHelper.getFile(diff)) == 1;
        if(valid) return 0;
        return MovementEvent.ILLEGAL.code();
    }

    private int verifyBishop(int srcPos, int destPos) {
        int srcFile = BoardHelper.getFile(srcPos);
        int srcRank = BoardHelper.getRank(srcPos);
        int destFile = BoardHelper.getFile(destPos);
        int destRank = BoardHelper.getRank(destPos);
        // Bishop tried to move funny
        if(srcFile == destFile || srcRank == destRank) return MovementEvent.ILLEGAL.code();
        if(Math.abs(srcFile - destFile) != Math.abs(srcRank - destRank)) return MovementEvent.ILLEGAL.code();
        int range = Math.abs(srcFile - destFile);
        // Now we know it's just sliding on a diagonal, though it's a bit tricky to manage this
        if(srcFile < destFile && srcRank < destRank) { // top right
            for(int i = 1; i < range; i++) {
                int pos = (srcFile + i) + ((srcRank + i) * 8);
                if(this.getPieceAt(pos) != NONE) return MovementEvent.ILLEGAL.code();
            }
            return 0;
        }
        if(srcFile < destFile) { // top left
            for(int i = 1; i < range; i++) {
                int pos = (srcFile + i) + ((srcRank - i) * 8);
                if(this.getPieceAt(pos) != NONE) return MovementEvent.ILLEGAL.code();
            }
            return 0;
        }
        if(srcRank > destRank) { // bottom left
            for(int i = 1; i < range; i++) {
                int pos = (srcFile - i) + ((srcRank - i) * 8);
                if(this.getPieceAt(pos) != NONE) return MovementEvent.ILLEGAL.code();
            }
            return 0;
        }
        for(int i = 1; i < range; i++) {
            int pos = (srcFile - i) + ((srcRank + i) * 8);
            if(this.getPieceAt(pos) != NONE) return MovementEvent.ILLEGAL.code();
        }
        return 0;
    }

    private int verifyRook(int srcPos, int destPos) {
        int srcFile = BoardHelper.getFile(srcPos);
        int srcRank = BoardHelper.getRank(srcPos);
        int destFile = BoardHelper.getFile(destPos);
        int destRank = BoardHelper.getRank(destPos);
        // Rook tried to move funny
        if(srcFile != destFile && srcRank != destRank) return MovementEvent.ILLEGAL.code();
        if(srcFile == destFile) {
            // Check that sliding range of rank
            for(int i = Math.min(srcRank, destRank) + 1; i < Math.max(srcRank, destRank) - 1; i++) {
                if(this.getPieceAt(i * 8 + srcFile) != NONE) return MovementEvent.ILLEGAL.code();
            }
        } else {
            // Check that sliding range of file
            for(int i = Math.min(srcFile, destFile) + 1; i < Math.max(srcFile, destFile) - 1; i++) {
                if(this.getPieceAt(i + srcRank * 8) != NONE) return MovementEvent.ILLEGAL.code();
            }
        }
        return 0;
    }

    private int verifyQueen(int srcPos, int destPos) {
        return Math.min(verifyBishop(srcPos, destPos), verifyRook(srcPos, destPos));
    }

    private int verifyKing(int srcPos, int destPos) {
        // 1 square, check if it gets put in check after some time
        return 0;
    }

    // These methods exist to adapt with the aforementioned issue
    // in #handleMove()

    private int getRank(int src) {
        return BoardHelper.getRank(src);
    }

    private int getFile(int src) {
        return BoardHelper.getFile(src);
    }

    /**
     * Clears the bits specified in the source mask.
     * @param srcPiece source piece
     * @param mask source bit mask
     */
    private void clear(int srcPiece, long mask) {
        switch (srcPiece) {
            case NONE -> {} // no-op clearing nothing
            case PAWN -> pawn &= ~mask;
            case BISHOP -> bishop &= ~mask;
            case KNIGHT -> knight &= ~mask;
            case ROOK -> rook &= ~mask;
            case QUEEN -> queen &= ~mask;
            case KING -> king &= ~mask;
            default -> throw new SystemError(String.format("unexpected clear call with piece type %d, mask: %s", srcPiece, Long.toBinaryString(mask)));
        }
    }

    /**
     * Turns on all bits specified by this mask
     * @param srcPiece the piece type
     * @param mask the mask
     */
    private void retain(int srcPiece, long mask) {
        switch (srcPiece) {
            case NONE -> {} // no-op
            case PAWN -> pawn |= mask;
            case BISHOP -> bishop |= mask;
            case KNIGHT -> knight |= mask;
            case ROOK -> rook |= mask;
            case QUEEN -> queen |= mask;
            case KING -> king |= mask;
            default -> throw new SystemError(String.format("unexpected retain call with piece type %d, mask: %s", srcPiece, Long.toBinaryString(mask)));
        }
    }

    /**
     * Initializes the board map
     * @return the board, with the returned type being white
     */
    public static BoardMap initialize() {
        long blackPawns = PAWN_MASK << 40;
        long blackKnight = KNIGHT_MASK << 56;
        long blackBishop = BISHOP_MASK << 56;
        long blackRook = ROOK_MASK << 56;
        long blackQueen = QUEEN_MASK << 56;
        long blackKing = KING_MASK << 56;

        BoardMap white = new BoardMap(PAWN_MASK, BISHOP_MASK, KNIGHT_MASK, ROOK_MASK, QUEEN_MASK, KING_MASK, WHITE);
        BoardMap black = new BoardMap(blackPawns, blackBishop, blackKnight, blackRook, blackQueen, blackKing, BLACK);
        white.enemyBoard.set(black);
        black.enemyBoard.set(white);
        return white;
    }

    @Override
    public String toString() {
        String pawn = String.format("%64s", Long.toBinaryString(this.pawn)).replace(" ", "0");
        String knight = String.format("%64s", Long.toBinaryString(this.knight)).replace(" ", "0");
        String bishop = String.format("%64s", Long.toBinaryString(this.bishop)).replace(" ", "0");
        String rook = String.format("%64s", Long.toBinaryString(this.rook)).replace(" ", "0");
        String queen = String.format("%64s", Long.toBinaryString(this.queen)).replace(" ", "0");
        String king = String.format("%64s", Long.toBinaryString(this.king)).replace(" ", "0");
        return String.format("""
                Pawn  :%s
                Knight:%s
                Bishop:%s
                Rook  :%s
                Queen :%s
                King  :%s
                """, pawn, knight, bishop, rook, queen, king);
    }

    public class BoardItr implements Iterator<Long> {
        private int cursor = 0;

        // Retrieves the latest iterated piece type according to this board
        public int cursorPiece() {
            return switch (cursor) {
                case 0, 1 -> PAWN;
                case 2 -> BISHOP;
                case 3 -> KNIGHT;
                case 4 -> ROOK;
                case 5 -> QUEEN;
                case 6 -> KING;
                default -> throw new RuntimeException("internal error: unexpected cursor " + cursor);
            };
        }

        @Override
        public boolean hasNext() {
            return cursor < 6;
        }

        @Override
        public Long next() {
            return switch (cursor++) {
                case 0 -> pawn;
                case 1 -> bishop;
                case 2 -> knight;
                case 3 -> rook;
                case 4 -> queen;
                case 5 -> king;
                default -> throw new RuntimeException("internal exception: unexpected cursor reached: " + cursor);
            };
        }
    }
}
