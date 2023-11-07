package com.spiritlight.chess.fish.game;

import static com.spiritlight.chess.fish.game.Piece.*;
import static com.spiritlight.chess.fish.game.utils.GameConstants.*;

public class FEN {

    public static final String INITIAL_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static int[] getInitialPosition() {
        return load(INITIAL_POSITION);
    }

    public static int[] load(String setup) {
        // Five fields reserved for: Move, Castle flag, En passant Squares, Half-move Clock, Full Move
        // Nice.
        int[] board = new int[69];
        int boardCursor = 0;
        int mode = 0; // 0: Parse board, 1: Parse turn, 2: Parse castle, 3: Parse En Passant,
                      // 4: Half-move Clock, 5: Full move (How many times black has moved)
        int count = 0;
        String[] params = setup.split(" ");
        for(String str : params) {
            if(mode == 0) {
                char[] boardSetup = str.toCharArray();
                for(char c : boardSetup) {
                    if(c == '/') {
                        count = 0;
                        continue;
                    }
                    if(boardCursor % 8 - count < 0) throw new IllegalArgumentException("FEN String illegal: excepted 8 ranks, found " + count);
                    if(Character.isDigit(c)) {
                        int advances = c - '0';
                        boardCursor += advances; // Skip these stuffs
                    } else {
                        int piece = from(c);
                        board[boardCursor++] = piece;
                    }
                    count++;
                }
                if(boardCursor != 64) throw new IllegalArgumentException("board position mismatch: expected 64, got " + boardCursor);
            } else {
                // No need to worry about any IOOBs as we are iterating the string itself
                switch (mode) {
                    case 1: board[64] = parseTurn(str); break;
                    case 2: board[65] = parseCastle(str); break;
                    case 3: board[66] = parseEnPassant(str); break;
                    case 4: board[67] = parseHalfMove(str); break;
                    case 5: board[68] = parseFullMove(str); break;
                    default: throw new IllegalArgumentException("unexpected mode: " + mode + " for input FEN string " + setup);
                }
            }
            mode++;
        }
        return board;
    }

    public static final int TURN = 64;
    public static final int CASTLE = 65;
    public static final int EN_PASSANT = 66;
    public static final int HALF_MOVE = 67;
    public static final int FULL_MOVE = 68;
    public static String get(int[] board) {
        if(board.length != 69) throw new IllegalArgumentException();
        StringBuilder builder = new StringBuilder();
        int cursor = 0;
        int carry = 0;
        for(int setup : board) {
            if(cursor >= 64) {
                if(carry != 0) {
                    builder.append(carry);
                }
                break;
            }
            if(cursor != 0 && cursor % 8 == 0) {
                if(carry != 0) {
                    builder.append(carry);
                    carry = 0;
                }
                builder.append("/");
            }
            char value = fen(setup);
            if(value == ' ') {
                carry++;
            } else {
                if(carry != 0) {
                    builder.append(carry);
                    carry = 0;
                }
                builder.append(value);
            }
            cursor++;
        }
        int turnState = board[64];
        int castleState = board[65];
        int enPassant = board[66];
        int halfMove = board[67];
        int fullMove = board[68];
        builder.append(' ').append(parseTurn(turnState));
        builder.append(' ').append(parseCastle(castleState));
        builder.append(' ').append(parseEnPassant(enPassant));
        builder.append(' ').append(halfMove);
        builder.append(' ').append(fullMove);
        return builder.toString();
    }

    public static int from(char c) {
        if(c == '/') return NONE;
        /* The mask to apply on the given character */
        int colorMask = Character.isUpperCase(c) ? WHITE : BLACK;
        int value = switch (Character.toUpperCase(c)) {
            case 'P' -> PAWN;
            case 'N' -> KNIGHT;
            case 'B' -> BISHOP;
            case 'R' -> ROOK;
            case 'Q' -> QUEEN;
            case 'K' -> KING;
            default -> throw new IllegalArgumentException("for input character: " + (int) c);
        };
        return colorMask | value;
    }

    public static char fen(int piece) {
        int pieceType = piece & PIECE_MASK;
        char value = switch (pieceType) {
            case NONE -> ' ';
            case PAWN -> 'p';
            case KNIGHT -> 'n';
            case BISHOP -> 'b';
            case ROOK -> 'r';
            case QUEEN -> 'q';
            case KING -> 'k';
            default -> throw new IllegalArgumentException("unexpected value " + piece + " (Masked value: " + pieceType + ")");
        };
        if(value == ' ') return value;

        // ASCII Table: (W) A=65, (B) a=97
        return (piece & WHITE) == 0 ? /* black */ value : /* white */ (char) (value - 32);
    }

    public static int parseTurn(String in) {
        return switch (in) {
            case "w" -> WHITE_TURN;
            case "b" -> BLACK_TURN;
            default -> throw new IllegalArgumentException("Parsing turn, for input: " + in);
        };
    }

    public static String parseTurn(int in) {
        return in == WHITE_TURN ? "w" : "b";
    }

    public static int parseCastle(String in) {
        int value = 0;
        if(in.equals("-")) return value;
        if(in.contains("Q")) value |= WHITE_CASTLE_QUEEN_SIDE;
        if(in.contains("q")) value |= BLACK_CASTLE_QUEEN_SIDE;
        if(in.contains("K")) value |= WHITE_CASTLE_KING_SIDE;
        if(in.contains("k")) value |= BLACK_CASTLE_KING_SIDE;
        return value;
    }

    public static String parseCastle(int in) {
        StringBuilder builder = new StringBuilder();
        if((in & WHITE_CASTLE_KING_SIDE) != 0) builder.append("K");
        if((in & WHITE_CASTLE_QUEEN_SIDE) != 0) builder.append("Q");
        if((in & BLACK_CASTLE_KING_SIDE) != 0) builder.append("k");
        if((in & BLACK_CASTLE_QUEEN_SIDE) != 0) builder.append("q");
        if(builder.isEmpty()) return "-";
        return builder.toString();
    }

    public static int parseEnPassant(String in) {
        // Theoretically speaking, this is probably challenging to parse.
        // To make it simple, int has 32 bits, and there are 16 pawns in total,
        // so we allocate two bits to each possibly capture square
        // (even though theoretically we can really just have simplified the process.)

        // This was typed pretty late at night (Around 10:30PM), and I just thought that
        // there are really just one square that can have the move played at any time
        // of the moment, so it isn't really that complicated.

        // To make this simple, we'll go with 0x000000{FILE}{RANK}
        if(in.equals("-")) return 0; // No en passant to process
        char[] cs = in.toCharArray();
        int first = cs[0] - 'a';
        int second = cs[1] - '1';
        // Mask 0xF0 to get the file, 0x0F for the rank, 0xFF for both
        // Theoretically the value should be max at 0x77 so there's this.
        return (first << 4) + second;
    }

    public static String parseEnPassant(int in) {
        if(in == 0) return "-";
        return String.valueOf((char) ((in >> 4) + 'a')) + ((char) ((in & 0x0F) + '1'));
    }

    public static int parseHalfMove(String in) {
        return Integer.parseInt(in);
    }

    public static int parseFullMove(String in) {
        return Integer.parseInt(in);
    }
}
