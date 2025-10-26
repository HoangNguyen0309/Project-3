package puzzles.quoridor;

import game.core.Piece;

public final class PawnPiece implements Piece {
    private final int id;
    private final String label;

    public PawnPiece(int id, String label) {
        this.id = id; this.label = label;
    }
    @Override public int id() { return id; }
    @Override public String label() { return label; }
}

