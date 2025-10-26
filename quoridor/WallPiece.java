package puzzles.quoridor;

import game.core.Piece;

/** Not placed on cells, but used for clarity (e.g., renders/tooling). */
public final class WallPiece implements Piece {
    private final int id; // 1 = horizontal, 2 = vertical
    private final String label;
    public WallPiece(int id, String label) { this.id = id; this.label = label; }
    @Override public int id() { return id; }
    @Override public String label() { return label; }
    public static WallPiece H() { return new WallPiece(1, "—"); }
    public static WallPiece V() { return new WallPiece(2, "|"); }
}

