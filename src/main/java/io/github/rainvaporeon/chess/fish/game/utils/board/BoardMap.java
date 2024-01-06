package io.github.rainvaporeon.chess.fish.game.utils.board;

import io.github.rainvaporeon.chess.fish.game.FEN;
import io.github.rainvaporeon.chess.fish.game.Piece;
import io.github.rainvaporeon.chess.fish.game.utils.EndType;
import io.github.rainvaporeon.chess.fish.game.utils.GameState;
import io.github.rainvaporeon.chess.fish.game.utils.MoveGenerator;
import io.github.rainvaporeon.chess.fish.game.utils.game.Move;
import io.github.rainvaporeon.chess.fish.game.utils.game.MovementEvent;
import io.github.rainvaporeon.chess.fish.internal.InternLogger;
import io.github.rainvaporeon.chess.fish.internal.annotation.Mask;
import io.github.rainvaporeon.chess.fish.internal.annotation.MaskType;
import io.github.rainvaporeon.chess.fish.internal.exceptions.SystemError;
import io.github.rainvaporeon.chess.fish.internal.utils.Bits;
import com.spiritlight.fishutils.collections.Pair;
import com.spiritlight.fishutils.logging.ILogger;
import com.spiritlight.fishutils.misc.annotations.Modifies;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static io.github.rainvaporeon.chess.fish.game.FEN.*;
import static io.github.rainvaporeon.chess.fish.game.Piece.*;
import static io.github.rainvaporeon.chess.fish.game.utils.GameConstants.*;
import static io.github.rainvaporeon.chess.fish.game.utils.game.Move.BACKWARD_OFFSET;
import static io.github.rainvaporeon.chess.fish.game.utils.game.Move.FORWARD_OFFSET;

// TODO: Separate en passant masks again, may need to modify FEN
// TODO: or not allow piece to capture same type on en passant

// TODO: Add castling to King moves.
public class BoardMap implements Cloneable {
    private static final long PAWN_MASK   = 0xFF00;
    private static final long BISHOP_MASK = 0b00100100;
    private static final long KNIGHT_MASK = 0b01000010;
    private static final long ROOK_MASK   = 0b10000001;
    private static final long KING_MASK   = 0b00010000;
    private static final long QUEEN_MASK  = 0b00001000;
    private static final int CASTLE_K_MASK = 0xF0;
    private static final int CASTLE_Q_MASK = 0x0F;

    // board to represent piece status
    private long pawn;
    private long bishop;
    private long knight;
    private long rook;
    private long queen;
    private long king;

    private final int color;
    // Update: Deprecated the use of StableField and removed final
    //  to make cloning easier.
    private BoardMap enemyBoard;
    // Consider this for castling, ~0xF0/0x0F to cancel one
    private int castle = 0xFF; // K=F0, Q=0F
    private int pawnAdvance = 0x0;
    private BoardInfo info;

    private CheckMethod checkMethod;

    private BoardMap(long pawn, long bishop, long knight, long rook, long queen, long king, int color) {
        this.pawn = pawn;
        this.bishop = bishop;
        this.knight = knight;
        this.rook = rook;
        this.queen = queen;
        this.king = king;
        this.color = color;
        this.checkMethod = CheckMethod.MANUAL;
        this.enemyBoard = null;
    }

    public int getColor() {
        return color;
    }

    public BoardMap getEnemyBoard() {
        return enemyBoard;
    }

    // TODO: Un-making a castle move may be disastrous.
    public MovementEvent unmake(String move) {
        return forceUpdate(Move.ofInverse(move));
    }

    public MovementEvent unmake(Move move) {
        return forceUpdate(move.invert());
    }

    public MovementEvent forceUpdate(String move) {
        return forceUpdate(Move.of(move));
    }

    /**
     * Forces a movement update, ignoring the info.turn to move.
     * @param move the move
     * @return event denoting the move
     * @apiNote this will also change the info.turn to play.
     */
    public MovementEvent forceUpdate(Move move) {
        int src  = move.sourcePos();
        int dest = move.destPos();

        int srcPiece  = this.getPieceAt(src);
        int destPiece = this.getPieceAt(dest);

        if(info.turn == WHITE_TURN) {
            return handleMove(srcPiece, src, dest, destPiece, move, true);
        } else {
            return this.enemyBoard.handleMove(srcPiece, src, dest, destPiece, move, true);
        }
    }

    public MovementEvent update(String move) {
        return this.update(Move.of(move));
    }

    public MovementEvent update(Move move) {
        int src  = move.sourcePos();
        int dest = move.destPos();

        int srcPiece  = this.getPieceAt(src);
        int destPiece = this.getPieceAt(dest);
        // Turn checking
        if(Piece.color(srcPiece) != (info.turn == WHITE_TURN ? WHITE : BLACK)) {
            // InternLogger.getLogger().debug(STR."Piece \{Piece.asString(srcPiece)} is not of color \{Piece.asString(color)}");
            return info.turn == WHITE_TURN ? MovementEvent.WHITE_TO_PLAY : MovementEvent.BLACK_TO_PLAY;
        }
        // Destination checking; cannot capture pieces of the same color
        if(Piece.color(srcPiece) == Piece.color(destPiece)) {
            if(!Piece.is(srcPiece, KING) || !Piece.is(destPiece, ROOK)) return MovementEvent.CAPTURING_SAME;
        }
        // Move checking, cannot move from nothing.
        if(srcPiece == NONE) return MovementEvent.ILLEGAL;

        if(info.turn == WHITE_TURN && this.color == WHITE) {
            return handleMove(srcPiece, src, dest, destPiece, move, false);
        } else {
            return this.enemyBoard.handleMove(srcPiece, src, dest, destPiece, move, false);
        }
    }

    public Pair<GameState, EndType> getGameState() {
        if(info.halfMove >= 100) return Pair.of(GameState.GAME_END, EndType.DRAW_50_MOVE);
        if(this.isCheckmate()) {
            return Pair.of(GameState.GAME_END, this.color == WHITE ? EndType.BLACK_WIN_CHECKMATE : EndType.WHITE_WIN_CHECKMATE);
        }
        if(enemyBoard.isCheckmate()) {
            return Pair.of(GameState.GAME_END, this.color == WHITE ? EndType.BLACK_WIN_CHECKMATE : EndType.WHITE_WIN_CHECKMATE);
        }
        if(this.isStalemate()) {
            return Pair.of(GameState.GAME_END, EndType.DRAW_STALEMATE);
        }
        if(enemyBoard.isStalemate()) {
            return Pair.of(GameState.GAME_END, EndType.DRAW_STALEMATE);
        }
        GameState state;
        if(this.info.fullMove <= 10) {
            state = GameState.EARLY_GAME;
        } else {
            state = this.queen == 0 && enemyBoard.queen == 0 ? GameState.END_GAME : GameState.MIDDLE_GAME;
        }
        return Pair.of(state, EndType.IN_PROGRESS);
    }

    public int getPieceAt(int src) {
        // InternLogger.getLogger().debug("Debugging piece at board " + color + ": " + src);
        int self = this.getSelfPieceAt(src, false);
        // InternLogger.getLogger().debug("Self piece: " + Piece.asString(self));
        if(self == NONE) return this.enemyBoard.getSelfPieceAt(src, false);
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

    public int enPassantSquare() {
        return info.enPassantSquare;
    }

    /**
     * Reconstructs the int array representation of this board and converts it
     * to a FEN string.
     * @return the fen string
     */
    public String toFENString() {
        if(this.color == BLACK && this.enemyBoard.color == BLACK) {
            throw new SystemError(STR."unexpected color: this and enemy board color is black. dump: \{this}, enemy=\{this.enemyBoard}");
        }
        if(this.color == BLACK) return this.enemyBoard.toFENString(); // Convenience purposes only.
        int[] positions = new int[69]; // pos:color
        int arrIdx = 0;
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 7; file >= 0; file--) {
                int pos = (rank * 8) + file;
                int piece = this.getSelfPieceAt(pos, true);
                int enemy = enemyBoard.getSelfPieceAt(pos, true);
                if(piece == NONE && enemy == NONE) positions[arrIdx] = NONE;
                if(piece == NONE) positions[arrIdx] = enemy;
                if(enemy == NONE) positions[arrIdx] = piece;
                if(enemy != NONE && piece != NONE) throw new SystemError(String.format("duplicate position: array=%s, pos=%d, type=%d,%d", Arrays.toString(positions), pos, piece, enemy));
                // InternLogger.getLogger().debug("Index " + pos + " (" + Move.parseLocation(pos) + ": " + Piece.asString(positions[arrIdx]) + ") has piece ID " + positions[arrIdx]);
                arrIdx++;
            }
        }
        int castleState = 0;
        if((this.castle & 0xF0) != 0) castleState |= WHITE_CASTLE_KING_SIDE;
        if((this.castle & 0x0F) != 0) castleState |= WHITE_CASTLE_QUEEN_SIDE;
        if((enemyBoard.castle & 0xF0) != 0) castleState |= BLACK_CASTLE_KING_SIDE;
        if((enemyBoard.castle & 0x0F) != 0) castleState |= BLACK_CASTLE_QUEEN_SIDE;
        positions[TURN] = info.turn;
        positions[CASTLE] = castleState;
        positions[EN_PASSANT] = BoardHelper.getFENPosition(info.enPassantSquare);
        positions[HALF_MOVE] = info.halfMove;
        positions[FULL_MOVE] = info.fullMove; // Synchronized
        return FEN.get(positions);
    }

    public static BoardMap fromFENString(String fen) {
        int[] layout = FEN.load(fen);
        int arrIdx = 0;
        BoardMap boardMap = BoardMap.clearBoard(WHITE);
        BoardMap enemyBoardMap = BoardMap.clearBoard(BLACK);
        boardMap.enemyBoard = enemyBoardMap;
        enemyBoardMap.enemyBoard = boardMap;
        boardMap.castle = 0;
        enemyBoardMap.castle = 0;
        for(int rank = 0; rank < 8; rank++) {
            for(int file = 7; file >= 0; file--) {
                long mask = BoardMap.getFENMask((rank * 8) + file);
                if((layout[arrIdx] & COLOR_MASK) == WHITE) {
                    if(rank == 1 && (layout[arrIdx] & PIECE_MASK) == PAWN) {
                        boardMap.pawnAdvance |= BoardMap.getByteMask(file);
                    }
                    boardMap.retain(layout[arrIdx] & PIECE_MASK, mask);
                } else {
                    if(rank == 6 && (layout[arrIdx] & PIECE_MASK) == PAWN) {
                        enemyBoardMap.pawnAdvance |= BoardMap.getByteMask(file);
                    }
                    enemyBoardMap.retain(layout[arrIdx] & PIECE_MASK, mask);
                }
                arrIdx++;
            }
        }
        BoardInfo info = new BoardInfo();
        info.turn = layout[TURN];
        int castle = layout[CASTLE];
        int wCastle = castle & WHITE_CASTLE_MASK;
        int bCastle = castle & BLACK_CASTLE_MASK;
        if((wCastle & WHITE_CASTLE_KING_SIDE) != 0) boardMap.castle |= 0xF0;
        if((wCastle & WHITE_CASTLE_QUEEN_SIDE) != 0) boardMap.castle |= 0x0F;
        if((bCastle & BLACK_CASTLE_KING_SIDE) != 0) enemyBoardMap.castle |= 0xF0;
        if((bCastle & BLACK_CASTLE_QUEEN_SIDE) != 0) enemyBoardMap.castle |= 0x0F;
        int enPassant = layout[EN_PASSANT];
        info.enPassantSquare = BoardHelper.fromFENPosition(enPassant);
        info.halfMove = layout[HALF_MOVE];
        info.fullMove = layout[FULL_MOVE];
        boardMap.info = info;
        enemyBoardMap.info = info;
        if(boardMap.inCheck() && info.turn == BLACK_TURN) throw new IllegalArgumentException("white in check, but it's black's turn");
        if(enemyBoardMap.inCheck() && info.turn == WHITE_TURN) throw new IllegalArgumentException("black in check, but it's white's turn");
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

    public void setCheckMethod(CheckMethod checkMethod) {
        this.checkMethod = checkMethod;
        this.getEnemyBoard().checkMethod = checkMethod;
    }

    public BoardItr itr() {
        return new BoardItr();
    }

    /**
     * Gets all pieces mapped to a 64-bit value.
     * @return
     */
    public long getBlocker() {
        return pawn | bishop | knight | rook | queen | king;
    }

    /**
     * Gets all pieced from both sides of the board to a 64-bit value.
     * @return
     */
    public long getBlockers() {
        return this.getBlocker() | this.getEnemyBoard().getBlocker();
    }

    public boolean canMove(int srcPos, int destPos) {
        return canMove(srcPos, destPos, false);
    }

    public boolean canMove(int srcPos, int destPos, boolean respectTurn) {
        int srcPiece = this.getPieceAt(srcPos);
        int destPiece = this.getPieceAt(destPos);

        if(Piece.is(srcPiece, NONE)) return false; // no piece = moves to nowhere
        if(Piece.color(srcPiece) != this.color) {
            // avoiding recursion
            if(Piece.color(srcPiece) != enemyBoard.color) throw new SystemError(STR."invalid src piece \{Integer.toHexString(srcPiece)}=\{Piece.asString(srcPiece)} (dest \{Integer.toHexString(destPiece)}=\{Piece.asString(destPiece)}) and color, this board: \n\{this}, enemy: \n\{enemyBoard}");
            return enemyBoard.canMove(srcPos, destPos, respectTurn);
        }

        if(respectTurn) {
            if(Piece.color(this.getPieceAt(srcPos)) != (this.info.turn == WHITE_TURN ? WHITE : BLACK)) return false;
            if(Piece.is(srcPiece, NONE)) return false;
        }

        if(Piece.color(srcPiece) == Piece.color(destPiece)) {
            // castling input is valid, checked later on, so same-color non-castling moves are
            // then canceled.
            if (!Piece.is(srcPiece, KING) || !Piece.is(destPiece, ROOK)) {
                return false;
            }
        }
        int handlerCode = switch (srcPiece & ~COLOR_MASK) {
            case PAWN -> verifyPawn(srcPos, destPos, destPiece, true);
            case BISHOP -> verifyBishop(srcPos, destPos);
            case KNIGHT -> verifyKnight(srcPos, destPos);
            case ROOK -> verifyRook(srcPos, destPos);
            case QUEEN -> verifyQueen(srcPos, destPos);
            case KING -> verifyKing(srcPos, destPos, false);
            default -> throw new IllegalStateException(STR."Unexpected value: \{Integer.toHexString(srcPiece)}");
        };
        if(this.revealsCheck(srcPiece, srcPos, destPos)) return false;
        return (handlerCode & ~PIECE_MASK) == 0;
    }

    public BoardMap fork() {
        BoardMap current = this.clone();
        BoardMap enemy = this.enemyBoard.clone();
        current.enemyBoard = enemy;
        enemy.enemyBoard = current;
        current.info = info.clone();
        enemy.info = enemyBoard.info.clone();
        return current;
    }

    public int getTurn() {
        return info.turn;
    }

    public void setTurn(int turn) {
        info.turn = turn;
    }

    @Override
    public BoardMap clone() {
        try {
            final BoardMap clone = (BoardMap) super.clone();
            clone.info = info.clone();
            clone.enemyBoard.info = enemyBoard.info.clone();
            return clone;
        } catch (CloneNotSupportedException what) {
            throw new AssertionError(what);
        }
    }

    private static final int CASTLE_FLAG = 1, PROMOTION_FLAG = 2;

    /**
     * handles the move
     * @param srcPiece the source piece
     * @param srcPos the source position
     * @param destPos the destination position
     * @param destPiece the captured piece
     * @param forced whether this move should ignore all validity checks
     */
    private MovementEvent handleMove(int srcPiece, int srcPos, int destPos, int destPiece, Move move, boolean forced) {
        InternLogger.getLogger().debug(STR."Source: \{srcPos}, Destination: \{destPos} (Origin: \{srcPos + 1}, \{destPos + 1})");
        InternLogger.getLogger().debug(STR."State: \{BoardHelper.getPositionString(srcPos + 1)}, \{BoardHelper.getPositionString(destPos + 1)}");
        InternLogger.getLogger().debug(STR."Has: \{Piece.asString(srcPiece)}, To: \{Piece.asString(destPiece)}");
        long srcMask = getMask(srcPos);
        long destMask = getMask(destPos);

        /* Verifying the result, 0 is OK
        * Check should include path checks and other processing,
        * but should not involve color/piece checking, as we've
        * checked it before masks were even retrieved.       */

        int handlerCode = switch (srcPiece & ~COLOR_MASK) {
            case PAWN -> verifyPawn(srcPos, destPos, destPiece, false);
            case BISHOP -> verifyBishop(srcPos, destPos);
            case KNIGHT -> verifyKnight(srcPos, destPos);
            case ROOK -> verifyRook(srcPos, destPos);
            case QUEEN -> verifyQueen(srcPos, destPos);
            case KING -> verifyKing(srcPos, destPos, true);
            default -> throw new IllegalStateException(STR."Unexpected value: \{srcPiece & ~COLOR_MASK}");
        };
        boolean castleFlag = handlerCode == CASTLE_FLAG;
        boolean promoFlag = handlerCode == PROMOTION_FLAG;
        MovementEvent error = translate(handlerCode);
        // error.code() & 0x07 is not 0
        if(error != null && !forced) return error;

        if(this.revealsCheck(srcPiece, srcPos, destPos)) return MovementEvent.REVEALS_CHECK;

        // Anything past this line is not going to be interrupted.

        info.halfMove++;

        boolean enpFlag = false;
        /* Calculating 50-move rule and en passant */
        if(Piece.is(srcPiece, PAWN)) {
            info.halfMove = 0;

            if(destPos == info.enPassantSquare) {
                if(this.color == WHITE) {
                    this.enemyBoard.clear(PAWN, 1L << (info.enPassantSquare + BACKWARD_OFFSET));
                } else {
                    this.enemyBoard.clear(PAWN, 1L << (info.enPassantSquare + FORWARD_OFFSET));
                }
                enpFlag = true;
            }

            if(Math.abs(BoardHelper.getRank(srcPos) - BoardHelper.getRank(destPos)) != 2) {
                this.info.enPassantSquare = -1;
            }
        }
        /* Castle rights too */
        if(Piece.is(srcPiece, KING)) {
            castle &= ~0xFF;
        }

        /* Castle rights */
        // moved rook
        if(Piece.is(srcPiece, ROOK)) {
            if(BoardHelper.getFile(srcPos) == 0) {
                castle &= ~0xF0; // king side
            } else {
                castle &= ~0x0F; // queen side
            }
        }
        // captured rook
        if(Piece.is(destPiece, ROOK) && !castleFlag) {
            if(BoardHelper.getFile(destPos) == 0) {
                enemyBoard.castle &= ~0xF0;
            } else {
                enemyBoard.castle &= ~0x0F;
            }
        }

        // Past this line is when all moves are confirmed to be valid

        // If we castled, it has already been handled internally via doCastle
        if(!(castleFlag || promoFlag)) {
            clear(srcPiece & ~COLOR_MASK, srcMask);
            retain(srcPiece & ~COLOR_MASK, destMask);
        }

        if(info.turn == BLACK_TURN) {
            info.fullMove++;
        }

        // has a piece at destination, and en passant is not in place,
        // handle here.
        // en passant captures are handled earlier.
        if(destPiece != NONE && !enpFlag && !castleFlag) {
            info.halfMove = 0;
            enemyBoard.clear(destPiece & ~COLOR_MASK, destMask);
        }

        info.turn ^= TURN_MASK;

        return new MovementEvent(srcPiece, destPiece, move);
    }

    private boolean revealsCheck(int piece, int srcPos, int destPos) {
        long sourceMask = getMask(srcPos);
        long destMask = getMask(destPos);
        int enemyPiece = enemyBoard.getSelfPieceAt(destPos, false) & PIECE_MASK;
        boolean flag = enemyPiece != NONE;
        try {
            this.clear(piece, sourceMask);
            this.retain(piece, destMask);
            if(flag) {
                enemyBoard.clear(enemyPiece, destMask);
            }
            return this.inCheck();
        } finally {
            this.retain(piece, sourceMask);
            this.clear(piece, destMask);
            if(flag) {
                enemyBoard.retain(enemyPiece, destMask);
            }
        }
    }

    public boolean isCheckmate() {
        InternLogger.getLogger().debug(STR."king:\{this.king};other:\{this.isStalemate()},\{this.inCheck()}");
        InternLogger.getLogger().debug(STR."bking:\{enemyBoard.king};other:\{enemyBoard.isStalemate()},\{enemyBoard.inCheck()}");
        return this.king == 0 || (this.isStalemate() && this.inCheck());
    }

    public boolean isStalemate() {
        List<Move> moves = new LinkedList<>();
        MoveGenerator gen = MoveGenerator.create(this);
        for(int i = 0; i < 64; i++) {
            moves.addAll(gen.getValidMovesFor(i, false));
        }
        return moves.isEmpty();
    }

    // Section to verify the validity of a movement.
    // If it mutates board structure in the process,
    // methods are annotated with the @Modifies annotation
    // with what parameter in this class is mutated.

    // Side note, I kind of forgot why I made the translating
    // method return null (denoting success) if &~0b111 returns
    // 0. Leaving it as is though.
    // TODO: This is too complex for our own goods.
    // Simplifications may be needed.
    // Would having 4 if-statement to check for advancing and captures be better?
    @Modifies({"info.enPassantSquare", "pawnAdvance"})
    private int verifyPawn(int srcPos, int destPos, int destPiece, boolean verify) {
        int file = BoardHelper.getFile(srcPos);
        int destFile = BoardHelper.getFile(destPos);
        int rank = BoardHelper.getRank(srcPos);
        int destRank = BoardHelper.getRank(destPos);
        // The minimum tile a pawn can move is 7 (Forward + left/right offset)
        if (Math.abs(file - destFile) > 1 || Math.abs(srcPos - destPos) < 7) {
            // InternLogger.getLogger().debug(STR."Distance too far horizontally: From \{file} to \{destFile} / Unexpected distance");
            return MovementEvent.ILLEGAL.code();
        }
        if (destRank - rank == (color == WHITE ? -1 : 1)) {
            // backwards check
            return MovementEvent.ILLEGAL.code();
        }
        if (Math.abs(srcPos - destPos) > 16) {
            // Moves way too far forward
            return MovementEvent.ILLEGAL.code();
        }
        if ((Math.abs(file - destFile) == 1 && destPiece == NONE) || (file == destFile && destPiece != NONE)) {
            // InternLogger.getLogger().debug("Captures nothing / Advances into something");
            if(destPos == info.enPassantSquare) {
                int en = BoardHelper.getRank(info.enPassantSquare);
                if((this.color == WHITE && en == 2) || (this.color == BLACK && en == 5)) {
                    return MovementEvent.ILLEGAL.code();
                }
            } else {
                return MovementEvent.ILLEGAL.code();
            }
        }

        if(Math.abs(rank - destRank) == 2) {
            // tried to capture and move two squares forward
            if(file != destFile) return MovementEvent.ILLEGAL.code();
            // check if pawn can advance two squares forward
            if((pawnAdvance & getByteMask(file)) != 0) {
                if(BoardHelper.getRank(srcPos) != (this.color == WHITE ? 1 : 6)) {
                    return MovementEvent.ILLEGAL.code();
                }
            } else {
                return MovementEvent.ILLEGAL.code();
            }
            // tried to capture two squares forward
            if(this.getPieceAt(srcPos + (info.turn == WHITE_TURN ? FORWARD_OFFSET : -FORWARD_OFFSET)) != NONE) {
                // InternLogger.getLogger().debug("Pawn advances past something");
                return MovementEvent.ILLEGAL.code();
            }
            if(!verify) {
                info.enPassantSquare = srcPos + (info.turn == WHITE_TURN ? FORWARD_OFFSET : -FORWARD_OFFSET);
            }
        }
        if(!verify) {
            // promotion logic
            // TODO: Ask what piece to move
            if(this.color == WHITE && destRank == 7) {
                long mask = getMask(srcPos);
                long clearMask = getMask(destPos);
                clear(PAWN, mask);
                retain(QUEEN, clearMask);
                enemyBoard.clear(destPiece & PIECE_MASK, clearMask);
                return PROMOTION_FLAG;
            }
            if(this.color == BLACK && destRank == 0) {
                long mask = getMask(srcPos);
                long clearMask = getMask(destPos);
                clear(PAWN, mask);
                retain(QUEEN, clearMask);
                enemyBoard.clear(destPiece & PIECE_MASK, clearMask);
                return PROMOTION_FLAG;
            }
            // A patch for when a pawn changes file on its next move
            if(BoardHelper.getRank(srcPos) == (this.color == WHITE ? 1 : 6)) {
                pawnAdvance &= ~getByteMask(file);
            }
        }
        return 0;
    }

    private int verifyKnight(int srcPos, int destPos) {
        if(this.checkMethod.useBits()) {
            if(destPos == 0) return this.getSelfPieceAt(destPos, false) == 0 ? 0 : MovementEvent.ILLEGAL.code();
            return (BoardMap.getMask(destPos) & (AttackTable.getMaskAt(KNIGHT, srcPos) & ~this.getBlockers())) == 0 ? 0 : MovementEvent.ILLEGAL.code();
        }
        // Honestly somewhat easy to check on this one
        // as knights skip over pieces
        int rDiff = Math.abs(BoardHelper.getRank(srcPos) - BoardHelper.getRank(destPos));
        int fDiff = Math.abs(BoardHelper.getFile(srcPos) - BoardHelper.getFile(destPos));
        if(Math.abs(rDiff - fDiff) == 1) {
            if(rDiff + fDiff == 3) return 0;
        }
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
            for(int i = Math.min(srcRank, destRank) + 1; i < Math.max(srcRank, destRank); i++) {
                if(this.getPieceAt(i * 8 + srcFile) != NONE) return MovementEvent.ILLEGAL.code();
            }
        } else {
            // Check that sliding range of file
            for(int i = Math.min(srcFile, destFile) + 1; i < Math.max(srcFile, destFile); i++) {
                if(this.getPieceAt(i + srcRank * 8) != NONE) return MovementEvent.ILLEGAL.code();
            }
        }
        return 0;
    }

    private int verifyQueen(int srcPos, int destPos) {
        return Math.max(verifyBishop(srcPos, destPos), verifyRook(srcPos, destPos));
    }

    private static final int[] VALID_OFFSETS = {1, -1, 7, -7, 9, -9, 8, -8};
    @Special
    private int verifyKing(int srcPos, int destPos, boolean shouldCastle) {
        if(this.getSelfPieceAt(destPos, false) == (color | ROOK)) {
            int kq = BoardHelper.getFile(destPos) == 7 ? CASTLE_K_MASK : BoardHelper.getFile(destPos) == 0 ? CASTLE_Q_MASK : 0;
            if(!doCastle(kq, shouldCastle)) {
                return MovementEvent.ILLEGAL.code();
            } else {
                return CASTLE_FLAG;
            }
        }
        if(this.checkMethod.useBits()) {
            for(int i : VALID_OFFSETS) {
                if(srcPos - destPos == i) {
                    // if 0, means the destination got cleared by the attack mask, hence illegal
                    return (BoardMap.getMask(destPos) & ~this.getAttackMask(this.getEnemyBoard().color)) != 0 ? 0 : MovementEvent.REVEALS_CHECK.code();
                }
            }
            return MovementEvent.ILLEGAL.code();
        }
        int file = BoardHelper.getFile(srcPos);
        int rank = BoardHelper.getRank(srcPos);
        int destFile = BoardHelper.getFile(destPos);
        int destRank = BoardHelper.getRank(destPos);
        if(Math.abs(file - destFile) > 1) return MovementEvent.ILLEGAL.code();
        if(Math.abs(rank - destRank) > 1) return MovementEvent.ILLEGAL.code();
        if(((1L << destPos) & ~this.getAttackMask(enemyBoard.color)) == 0) return MovementEvent.REVEALS_CHECK.code();
        return 0;
    }

    // 0xF0=King, 0x0F=Queen
    @MaskType(Mask.CLEAR)
    private static final long WHITE_CASTLE_K_BLOCKER_MASK = 0b01100000;
    private static final long WHITE_CASTLE_K_ATTACKER_MASK = 0b01100000;
    private static final long WHITE_CASTLE_Q_BLOCKER_MASK = 0b00001110;
    private static final long WHITE_CASTLE_Q_ATTACKER_MASK = 0b00001100;

    @MaskType(Mask.CLEAR)
    private static final long K_PRESERVE_MASK = 0b10000000;
    private static final long Q_PRESERVE_MASK = 0b00000001;
    private static final long BLACK_K_PRESERVE_MASK = K_PRESERVE_MASK << 56;
    private static final long BLACK_Q_PRESERVE_MASK = Q_PRESERVE_MASK << 56;

    @MaskType(Mask.CLEAR)
    private static final long BLACK_CASTLE_K_ATTACKER_MASK = WHITE_CASTLE_K_ATTACKER_MASK << 56;
    private static final long BLACK_CASTLE_K_BLOCKER_MASK = WHITE_CASTLE_K_BLOCKER_MASK << 56;
    private static final long BLACK_CASTLE_Q_ATTACKER_MASK = WHITE_CASTLE_Q_ATTACKER_MASK << 56;
    private static final long BLACK_CASTLE_Q_BLOCKER_MASK = WHITE_CASTLE_Q_BLOCKER_MASK << 56;
    @Special // Probably also the most bloated one
    private boolean doCastle(int side, boolean shouldAttempt) {
        if((this.castle & side) == 0) return false;
        if(this.color == WHITE) {
            long enemyAttackMask = this.getAttackMask(BLACK);
            if(side == CASTLE_K_MASK) {
                if((WHITE_CASTLE_K_ATTACKER_MASK & ~enemyAttackMask) == 0 || this.inCheck()) return false; // bit cleared; square was attacked (or in check)
                if((WHITE_CASTLE_K_BLOCKER_MASK & this.getBlockers()) != 0) return false; // has blockers
                if(!shouldAttempt) return true;
                this.king = this.king << 2;
                this.clear(ROOK, K_PRESERVE_MASK);
                this.retain(ROOK, this.king >>> 1);
            } else {
                if((WHITE_CASTLE_Q_ATTACKER_MASK & ~enemyAttackMask) == 0 || this.inCheck()) return false; // bit cleared; square was attacked (or in check)
                if((WHITE_CASTLE_Q_BLOCKER_MASK & this.getBlockers()) != 0) return false; // has blockers
                if(!shouldAttempt) return true;
                this.king = this.king >>> 2;
                this.clear(ROOK, Q_PRESERVE_MASK);
                this.retain(ROOK, this.king << 1);
            }
        } else {
            long enemyAttackMask = this.getAttackMask(WHITE);
            if(side == CASTLE_K_MASK) {
                if((BLACK_CASTLE_K_ATTACKER_MASK & ~enemyAttackMask) == 0 || this.inCheck()) return false; // bit cleared; square was attacked (or in check)
                if((BLACK_CASTLE_K_BLOCKER_MASK & this.getBlockers()) != 0) return false; // has blockers
                if(!shouldAttempt) return true;
                this.king = this.king << 2;
                this.clear(ROOK, BLACK_K_PRESERVE_MASK);
                this.retain(ROOK, this.king >>> 1);
            } else {
                if((BLACK_CASTLE_Q_ATTACKER_MASK & ~enemyAttackMask) == 0 || this.inCheck()) return false; // bit cleared; square was attacked (or in check)
                if((BLACK_CASTLE_Q_BLOCKER_MASK & this.getBlockers()) != 0) return false; // has blockers
                if(!shouldAttempt) return true;
                this.king = this.king >>> 2;
                this.clear(ROOK, BLACK_Q_PRESERVE_MASK);
                this.retain(ROOK, this.king << 1);
            }
        }
        this.castle = 0; // castled
        return true;
    }

    /**
     * Returns whether the current king gets cleared with the enemy
     * attack mask
     */
    public boolean inCheck() {
        return ((king & ~this.getAttackMask(enemyBoard.color)) == 0);
    }

    public CheckMethod getCheckMethod() {
        return checkMethod;
    }

    /**
     * Gets all the current attacked squares for given side
     * @param side the side
     * @return the attack mask
     */
    private long getAttackMask(int side) {
        long ll = 0L;
        long blockers = this.getBlockers();
        BoardMap map = side == this.color ? this : enemyBoard;
        long captureClearMask = map.getBlocker();
        for(int i = 0; i < 64; i++) {
            int piece = map.getSelfPieceAt(i, false);
            long maskedBlocker = blockers & Bits.getRayAttack(i, piece);
            if(color != side) continue;
            if(Piece.isSlidingPiece(piece)) {
                ll |= Bits.getRayAttackMagic(maskedBlocker, i, piece) & ~captureClearMask;
            } else {
                ll |= AttackTable.getMaskAt(piece, i);
            }
        }
        return ll;
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
            default -> {} // throw new SystemError(String.format("unexpected clear call with piece type %d (%s), mask: %s", srcPiece, Long.toBinaryString(mask)));
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
            default -> {} // throw new SystemError(String.format("unexpected retain call with piece type %d, mask: %s", srcPiece, Long.toBinaryString(mask)));
        }
    }

    private long mergePieceMask() {
        long ll = 0;
        for (BoardItr it = this.itr(); it.hasNext(); ) {
            ll |= it.nextLong();
        }
        return ll;
    }

    private long mergeAllPieces() {
        return this.mergePieceMask() | this.getEnemyBoard().mergePieceMask();
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

        BoardMap white = new BoardMap( PAWN_MASK, BISHOP_MASK, KNIGHT_MASK, ROOK_MASK, QUEEN_MASK, KING_MASK, WHITE);
        BoardMap black = new BoardMap(blackPawns, blackBishop, blackKnight, blackRook, blackQueen, blackKing, BLACK);
        white.enemyBoard = black;
        black.enemyBoard = white;
        white.pawnAdvance = 0xFF;
        black.pawnAdvance = 0xFF;
        BoardInfo sharedBoardInfo = new BoardInfo();
        white.info = sharedBoardInfo;
        black.info = sharedBoardInfo;
        return white;
    }

    // Translates the given code to the event
    private static MovementEvent translate(int code) {
        if((code & ~PIECE_MASK) == 0) return null;
        return MovementEvent.getTranslationCode(code);
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
                Board Color: %s; Castle flag: %s; Pawn advance mask: %s
                Board Info: %s
                """, pawn, knight, bishop, rook, queen, king, this.color, Integer.toHexString(this.castle), Integer.toBinaryString(pawnAdvance) , this.info);
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
                default -> throw new RuntimeException(STR."internal error: unexpected cursor \{cursor}");
            };
        }

        @Override
        public boolean hasNext() {
            return cursor < 6;
        }

        public long nextLong() {
            return switch (cursor++) {
                case 0 -> pawn;
                case 1 -> bishop;
                case 2 -> knight;
                case 3 -> rook;
                case 4 -> queen;
                case 5 -> king;
                default -> throw new RuntimeException(STR."internal exception: unexpected cursor reached: \{cursor}");
            };
        }

        @Override
        public Long next() {
            return nextLong();
        }
    }

    public enum CheckMethod {
        MANUAL,
        BITS
        ;

        public boolean useBits() {
            return this == BITS;
        }

        public boolean manual() {
            return this == MANUAL;
        }
    }

    /**
     * Indicates that this method is impure or that it behaves
     * differently than other methods.
     * The call hierarchy should
     * contain this annotation all the way up to the caller.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    @Documented
    private @interface Special {
    }
}
