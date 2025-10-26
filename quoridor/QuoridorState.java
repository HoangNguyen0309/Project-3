package puzzles.quoridor;

import game.core.Board;
import game.core.Piece;
import game.core.Position;
import game.core.Tile;

import java.util.Arrays;

/**
 * Quoridor state with dynamic rectangular size (rows x cols).
 * Cells are rows x cols.
 * Horizontal walls h: (rows-1) x cols   (between (r,c) and (r+1,c))
 * Vertical walls   v: rows x (cols-1)   (between (r,c) and (r,c+1))
 */
public final class QuoridorState implements Board<Piece> {
    public final int rows;
    public final int cols;

    private final Piece[][] cells; // for rendering pawns only
    public final boolean[][] h;    // horizontal segments
    public final boolean[][] v;    // vertical segments

    public Position p1;            // P1 pawn
    public Position p2;            // P2 pawn
    public int walls1;             // P1 remaining walls
    public int walls2;             // P2 remaining walls
    public int turn = 1;           // 1 or 2

    // Tile lattice for pathfinding (one Tile per cell)
    public final Tile[][] lattice;

    /** Default 9x9. */
    public QuoridorState() { this(9, 9); }

    public QuoridorState(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new Piece[rows][cols];
        this.h = new boolean[Math.max(rows - 1, 0)][cols];
        this.v = new boolean[rows][Math.max(cols - 1, 0)];
        this.lattice = new Tile[rows][cols];

        // Start pawns centered in the top/bottom rows
        this.p1 = new Position(0, cols / 2);
        this.p2 = new Position(rows - 1, cols / 2);

        // Simple rule-of-thumb for walls per player: min(rows, cols) + 1 (9x9 → 10)
        int w = Math.min(rows, cols) + 1;
        this.walls1 = w;
        this.walls2 = w;

        initLatticeFresh();

        // Place pawn pieces for rendering
        set(p1.r, p1.c, new PawnPiece(1, "1"));
        set(p2.r, p2.c, new PawnPiece(2, "2"));

        rebuildGraphNeighbors();
    }

    /** Recreate Tile objects (clears neighbor edges safely). */
    private void initLatticeFresh() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                lattice[r][c] = new Tile(0, new Position(r, c));
            }
        }
    }

    /** Rebuild graph adjacency in the Tile lattice based on current walls. */
    public void rebuildGraphNeighbors() {
        initLatticeFresh(); // clears all edges

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Up
                if (r > 0 && !h[r - 1][c]) {
                    lattice[r][c].addEdge(new Position(r - 1, c));
                }
                // Down
                if (r < rows - 1 && !h[r][c]) {
                    lattice[r][c].addEdge(new Position(r + 1, c));
                }
                // Left
                if (c > 0 && !v[r][c - 1]) {
                    lattice[r][c].addEdge(new Position(r, c - 1));
                }
                // Right
                if (c < cols - 1 && !v[r][c]) {
                    lattice[r][c].addEdge(new Position(r, c + 1));
                }
            }
        }
    }

    // --- Board<Piece> (render-only for pawns) ---
    @Override public int rows() { return rows; }
    @Override public int cols() { return cols; }
    @Override public Piece get(int r, int c) { return cells[r][c]; }
    @Override public void set(int r, int c, Piece value) { cells[r][c] = value; }

    public Position currentPawn() { return (turn == 1) ? p1 : p2; }
    public Position otherPawn()   { return (turn == 1) ? p2 : p1; }

    public boolean inBounds(int r, int c) { return r >= 0 && r < rows && c >= 0 && c < cols; }

    public QuoridorState copy() {
        QuoridorState s = new QuoridorState(rows, cols);
        // copy walls
        for (int r = 0; r < Math.max(rows - 1, 0); r++) s.h[r] = Arrays.copyOf(h[r], cols);
        for (int r = 0; r < rows; r++)                 s.v[r] = Arrays.copyOf(v[r], Math.max(cols - 1, 0));

        // copy pawns/walls/turn
        s.clearCells();
        s.p1 = new Position(p1.r, p1.c);
        s.p2 = new Position(p2.r, p2.c);
        s.walls1 = walls1;
        s.walls2 = walls2;
        s.turn = turn;

        s.set(s.p1.r, s.p1.c, new PawnPiece(1, "1"));
        s.set(s.p2.r, s.p2.c, new PawnPiece(2, "2"));

        s.rebuildGraphNeighbors();
        return s;
    }

    private void clearCells() {
        for (int r = 0; r < rows; r++) Arrays.fill(cells[r], null);
    }

    /** Store wall pieces **/
    public WallPiece hPiece(int r, int c) { // horizontal segment under row r, col c
        return (r >= 0 && r < rows - 1 && c >= 0 && c < cols && h[r][c]) ? WallPiece.H() : null;
    }
    public WallPiece vPiece(int r, int c) { // vertical segment right of row r, col c
        return (r >= 0 && r < rows && c >= 0 && c < cols - 1 && v[r][c]) ? WallPiece.V() : null;
    }
}
