package com.spiritlight.chess.fish.game.utils.game;

public record MovementEvent(int capturingPiece, int capturedPiece, Move move) { }
