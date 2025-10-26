package puzzles.quoridor;

import game.core.Position;

/** Union type for player actions. */
public final class QuoridorAction {
    public enum Type { MOVE, WALL_H, WALL_V }

    public final Type type;
    public final Position from; // only for MOVE (current pawn pos)
    public final Position to;   // only for MOVE (destination cell)
    public final int r, c;      // top-left cell of wall anchor

    private QuoridorAction(Type t, Position from, Position to, int r, int c) {
        this.type = t; this.from = from; this.to = to; this.r = r; this.c = c;
    }

    public static QuoridorAction move(Position from, Position to) {
        return new QuoridorAction(Type.MOVE, from, to, -1, -1);
    }
    public static QuoridorAction wallH(int r, int c) {
        return new QuoridorAction(Type.WALL_H, null, null, r, c);
    }
    public static QuoridorAction wallV(int r, int c) {
        return new QuoridorAction(Type.WALL_V, null, null, r, c);
    }

    @Override public String toString() {
        switch (type) {
            case MOVE:   return "move " + from + "->" + to;
            case WALL_H: return "wall h " + r + " " + c;
            case WALL_V: return "wall v " + r + " " + c;
            default:     return "action(?)";
        }
    }
}
