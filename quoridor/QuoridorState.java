package puzzles.quoridor;

import game.core.Position;


/** Game state for Quoridor. Uses a doubled grid (2n×2m) internally so
 * cells are at even-even coords and wall segments sit between cells.
 * No external API exposes doubled coordinates. */
public final class QuoridorState {
    // Logical size (cells)
    public final int n, m;
    // Doubled size (internal grid)
    public final int rows, cols;


    // Pawn positions (even-even doubled coords)
    public final Position p1, p2;


    // Remaining walls
    public final int p1Walls, p2Walls;


    // Turn: true = P1, false = P2
    public final boolean p1Turn;


    // Segment arrays (sized to doubled grid). Horizontal at (odd, even), vertical at (even, odd)
    public final boolean[][] h; // horizontal segments
    public final boolean[][] v; // vertical segments


    public QuoridorState(int n, int m) {
        if (n < 3 || m < 3) throw new IllegalArgumentException("Minimum board 3x3");
        this.n = n; this.m = m;
        this.rows = 2*n; this.cols = 2*m;
        this.h = new boolean[rows][cols];
        this.v = new boolean[rows][cols];
        int midC = 2*(m/2);
        this.p1 = new Position(0, midC);
        this.p2 = new Position(2*(n-1), midC);
        this.p1Walls = 10; this.p2Walls = 10;
        this.p1Turn = true;
    }


    QuoridorState(int n, int m, int rows, int cols,
                  Position p1, Position p2, int p1Walls, int p2Walls, boolean p1Turn,
                  boolean[][] h, boolean[][] v) {
        this.n = n; this.m = m; this.rows = rows; this.cols = cols;
        this.p1 = p1; this.p2 = p2; this.p1Walls = p1Walls; this.p2Walls = p2Walls; this.p1Turn = p1Turn;
        this.h = h; this.v = v;
    }


    public QuoridorState copy() {
        boolean[][] nh = new boolean[rows][cols];
        boolean[][] nv = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(h[r], 0, nh[r], 0, cols);
            System.arraycopy(v[r], 0, nv[r], 0, cols);
        }
        return new QuoridorState(n, m, rows, cols,
                new Position(p1.r, p1.c), new Position(p2.r, p2.c),
                p1Walls, p2Walls, p1Turn, nh, nv);
    }


    // Helpers (internal)
    static boolean isCell(int r, int c) { return (r % 2 == 0) && (c % 2 == 0); }
    boolean inGrid(int r, int c) { return r >= 0 && r < rows && c >= 0 && c < cols; }
    static int gr(int r) { return 2*r; } // logical -> doubled row
    static int gc(int c) { return 2*c; } // logical -> doubled col
}
