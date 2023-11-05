package com.spiritlight.chess.fish.game.utils.board;

import com.spiritlight.chess.fish.game.FEN;
import com.spiritlight.chess.fish.game.Piece;
import com.spiritlight.chess.fish.game.utils.EndType;
import com.spiritlight.chess.fish.game.utils.GameState;
import com.spiritlight.chess.fish.game.utils.game.Move;
import com.spiritlight.chess.fish.game.utils.game.MovementEvent;
import com.spiritlight.chess.fish.internal.exceptions.SystemError;
import com.spiritlight.chess.fish.internal.utils.StableField;
import com.spiritlight.fishutils.collections.Pair;

import java.util.Arrays;
import java.util.Iterator;

import static com.spiritlight.chess.fish.game.FEN.*;
import static com.spiritlight.chess.fish.game.Piece.*;
import static com.spiritlight.chess.fish.game.utils.game.Move.*;
import static com.spiritlight.chess.fish.game.utils.GameConstants.*;

public class BoardMap {
    private static final long PAWN_MASK   = 0xFF00;
    private static final long BISHOP_MASK = 0b00100100;
    private static final long KNIGHT_MASK = 0b01000010;
    private static final long ROOK_MASK   = 0b10000001;
    private static final long KING_MASK   = 0b00001000;
    private static final long QUEEN_MASK  = 0b00010000;

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
    private int pawnAdvance = 0xFF;
    private int enPassantSquare = 0;
    private int halfMove = 0;
    private int fullMove = 0;

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

    public MovementEvent update(Move move) {
        int src  = move.sourcePos();
        int dest = move.destPos();

        int sourcePiece = this.getPieceAt(src);
        int destPiece   = this.getPieceAt(dest);
        handleMove(sourcePiece, src, dest, destPiece, enemyBoard.get());
        return new MovementEvent(sourcePiece, destPiece, move);
    }

    public Pair<GameState, EndType> getGameState() {
        int clock = this.halfMove + enemyBoard.get().halfMove;
        if(clock >= 100) return Pair.of(GameState.GAME_END, EndType.DRAW_50_MOVE);
        // TODO: Either rework or change this
        throw new UnsupportedOperationException("not implemented");
    }

    public int getPieceAt(int src) {
        BoardItr itr = itr();
        long mask = getMask(src);
        while (itr.hasNext()) {
            long value = itr.next();
            if((value & mask) != 0) return itr.cursorPiece() | color;
        }
        BoardItr otherItr = enemyBoard.get().itr();
        while (otherItr.hasNext()) {
            long value = otherItr.next();
            if((value & mask) != 0) return otherItr.cursorPiece() | (color ^ COLOR_MASK);
        }
        return NONE;
    }

    public int getSelfPieceAt(int src) {
        BoardItr itr = itr();
        long mask = getMask(src);
        while (itr.hasNext()) {
            long value = itr.next();
            if((value & mask) != 0) return itr.cursorPiece() | color;
        }
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
        for(int i = 0; i < 64; i++) {
            int piece = this.getSelfPieceAt(i);
            int enemy = enemyBoard.get().getSelfPieceAt(i);
            if(piece == NONE && enemy == NONE) positions[i] = NONE;
            if(piece == NONE) positions[i] = enemy;
            if(enemy == NONE) positions[i] = piece;
            if(enemy != NONE && piece != NONE) throw new SystemError(String.format("duplicate position: array=%s, pos=%d, type=%d,%d", Arrays.toString(positions), i, piece, enemy));
        }
        int castleState = 0;
        if((this.castle & 0x0F) != 0) castleState |= WHITE_CASTLE_KING_SIDE;
        if((this.castle & 0xF0) != 0) castleState |= WHITE_CASTLE_QUEEN_SIDE;
        if((enemyBoard.get().castle & 0x0F) != 0) castleState |= BLACK_CASTLE_KING_SIDE;
        if((enemyBoard.get().castle & 0xF0) != 0) castleState |= BLACK_CASTLE_QUEEN_SIDE;
        positions[TURN] = halfMove % 2 == 0 ? WHITE_TURN : BLACK_TURN;
        positions[CASTLE] = castleState;
        positions[EN_PASSANT] = enPassantSquare;
        positions[HALF_MOVE] = (this.halfMove + enemyBoard.get().halfMove);
        positions[FULL_MOVE] = this.fullMove; // Synchronized
        return FEN.get(positions);
    }

    private BoardItr itr() {
        return new BoardItr();
    }

    /**
     * Retrieves a mask with all bits but the specified location turned on.
     * @param location the bit position to be turned off
     * @return a mask with all bits but the location turned on.
     */
    private long getMask(int location) {
        return 0x8000_0000_0000_0000L >>> location;
        // return ~(1L << location);
    }

    /**
     * @see BoardMap#getMask(int)
     */
    private int getIntMask(int position) {
        return 0x8000_0000 >>> position;
        // return ~(1 << position);
    }

    /**
     * handles the move
     * @param srcPiece the source piece
     * @param srcPos the source position
     * @param destPos the destination position
     * @param capturedPiece the captured piece
     * @param enemyMap the enemy BoardMap to update if {@code capturedPiece != null}
     */
    private void handleMove(int srcPiece, int srcPos, int destPos, int capturedPiece, BoardMap enemyMap) {
        long srcMask = this.getMask(srcPos);
        long destMask = this.getMask(destPos);
        enPassantSquare = 0; // reset after each move
        // Inverting destination mask as we want to turn on that specific bit.
        halfMove++;
        if(color == BLACK) {
            fullMove++;
            enemyBoard.get().fullMove++;
        }
        /* Calculating 50-move rule and en passant */
        if(Piece.is(srcPiece, PAWN)) {
            halfMove = 0;
            if(Math.abs(BoardHelper.getRank(srcPos) - BoardHelper.getRank(destPos)) == 2) {
                enPassantSquare = srcPos + FORWARD_OFFSET;
            }
            int file = BoardHelper.getFile(srcPos);
            pawnAdvance &= getIntMask(file);
        }
        /* Castle rights too */
        if(Piece.is(srcPiece, KING)) {
            castle &= ~0xFF;
        }
        /* Castle rights */
        if(Piece.is(srcPiece, ROOK)) {
            if(BoardHelper.getFile(srcPos) == 0) {
                castle &= ~0xF0;
            } else {
                castle &= ~0x0F;
            }
        }
        clear(srcPiece & ~COLOR_MASK, srcMask);
        retain(srcPiece & ~COLOR_MASK, destMask);

        if(capturedPiece != NONE && enemyMap != null) {
            halfMove = 0;
            enemyMap.clear(capturedPiece & ~COLOR_MASK, destMask);
        }
    }

    /**
     * Clears the bits specified in the source mask.
     * @param srcPiece source piece
     * @param mask source bit mask
     */
    private void clear(int srcPiece, long mask) {
        switch (srcPiece) {
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

    private class BoardItr implements Iterator<Long> {
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
