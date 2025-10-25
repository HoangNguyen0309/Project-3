package puzzles.quoridor;

/** Action for Quoridor: either a pawn MOVE to a logical cell (r,c),
 * or a WALL placement with direction 'H'/'V' at logical top-left (r,c). */
public final class QuoridorAction {
    public enum Type { MOVE, WALL }
    public enum WallDir { H, V }


    public final Type type;
    public final int r, c; // for MOVE: logical target cell; for WALL: logical anchor
    public final WallDir dir; // only for WALL (null for MOVE)


    private QuoridorAction(Type type, int r, int c, WallDir dir) {
        this.type = type; this.r = r; this.c = c; this.dir = dir;
    }


    public static QuoridorAction move(int r, int c) { return new QuoridorAction(Type.MOVE, r, c, null); }
    public static QuoridorAction wallH(int r, int c) { return new QuoridorAction(Type.WALL, r, c, WallDir.H); }
    public static QuoridorAction wallV(int r, int c) { return new QuoridorAction(Type.WALL, r, c, WallDir.V); }
}
