package puzzles.quoridor;

import game.core.Piece;
import game.core.Renderer;

/**
 * Pretty Quoridor renderer (Unicode/ASCII + optional ANSI colors) that matches the current state:
 *   - State fields used: rows, cols, h (rows-1 x cols), v (rows x cols-1), p1, p2, walls1, walls2, turn
 *   - Shows aligned column headers and junctions between horizontal segments
 *   - Java 8 compatible (no String.repeat)
 */
public final class QuoridorRenderer implements Renderer<QuoridorState> {

    // style knobs
    private final boolean useUnicode;
    private final boolean useColor;
    private final boolean tintWalls;

    // glyphs (computed from mode)
    private final String DOT;         // empty cell marker
    private final String VERT;        // vertical wall (single char)
    private final String CROSS;       // junction char between horizontal segments
    private final String SP3;         // three spaces (gap between cells)

    // ANSI (used only when useColor==true)
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String DIM   = "\u001B[2m";
    private static final String FG_CYAN    = "\u001B[36m";
    private static final String FG_MAGENTA = "\u001B[35m";
    private static final String FG_YELLOW  = "\u001B[33m";
    private static final String FG_GRAY    = "\u001B[90m";

    public QuoridorRenderer() { this(true, false, false); }

    /**
     * @param preferUnicode show Unicode lines/box chars unless we're on Windows console
     * @param useColor      enable ANSI colors (off on Windows cmd by default)
     * @param tintWalls     dim/yellow tint for wall glyphs when useColor is true
     */
    public QuoridorRenderer(boolean preferUnicode, boolean useColor, boolean tintWalls) {
        boolean isWindows = System.getProperty("os.name","").toLowerCase().contains("win");
        this.useUnicode = preferUnicode && !isWindows; // safer default: ASCII on Windows
        this.useColor   = useColor;
        this.tintWalls  = useColor && tintWalls;

        if (this.useUnicode) {
            DOT   = "·";
            VERT  = "│";
            CROSS = "┼";
            SP3   = "   ";
        } else {
            DOT   = ".";
            VERT  = "|";
            CROSS = "+";
            SP3   = "   ";
        }
    }

    private String c(String s, String color) { return useColor ? color + s + RESET : s; }
    private String bold(String s)            { return useColor ? BOLD + s + RESET : s; }
    private String dim(String s)             { return useColor ? DIM + s + RESET  : s; }

    // --- helpers (Java 8 friendly) ---
    private static String repeat(String s, int n) {
        if (n <= 0) return "";
        StringBuilder b = new StringBuilder(s.length() * n);
        for (int i = 0; i < n; i++) b.append(s);
        return b.toString();
    }
    private static String center(String text, int width) {
        if (text == null) text = "";
        int len = text.length();
        if (len >= width) return text;
        int left = (width - len) / 2;
        int right = width - len - left;
        return repeat(" ", left) + text + repeat(" ", right);
    }

    @Override
    public String render(QuoridorState b) {
        final String NL = System.lineSeparator();

        // --- layout widths (like your first renderer) ---
        final int maxCol = Math.max(0, b.cols - 1);
        final int digits = String.valueOf(maxCol).length();
        final int cellW  = Math.max(3, digits + 1);     // width of each cell
        final int gutterW = 1;                          // spaces between cells
        final int wallW   = 1;                          // one char for a vertical wall

        // horizontal segment sized to cell width (Unicode or ASCII)
        final String HSEG = useUnicode ? repeat("─", cellW) : repeat("-", cellW);

        StringBuilder sb = new StringBuilder(4096);

        // ===== HUD =====
        sb.append(bold("=== Quoridor ===")).append(NL);
        sb.append(c("P1@", FG_CYAN))
                .append(b.p1.r).append(",").append(b.p1.c).append("   ")
                .append(c("P2@", FG_MAGENTA))
                .append(b.p2.r).append(",").append(b.p2.c).append("   ")
                .append("Walls ")
                .append(c("P1:"+b.walls1, FG_CYAN)).append(" ")
                .append(c("P2:"+b.walls2, FG_MAGENTA)).append(NL);
        sb.append(c("Commands: ", FG_GRAY))
                .append("move r c  |  wall h r c  |  wall v r c  |  help | quit")
                .append(NL).append(NL);

        // ===== Column header =====
        sb.append(repeat(" ", 2 + 2)); // room for row label (e.g., "%2d" + two spaces)
        for (int c = 0; c < b.cols; c++) {
            sb.append(center(String.valueOf(c), cellW));
            if (c < b.cols - 1) sb.append(repeat(" ", gutterW + wallW));
        }
        sb.append(NL);

        // ===== Rows =====
        for (int r = 0; r < b.rows; r++) {
            // row label + cells + vertical walls
            sb.append(String.format("%2d  ", r));
            for (int c = 0; c < b.cols; c++) {
                String cellText = DOT;

                // prefer board piece label if present
                Piece pc = b.get(r, c);
                if (pc != null && pc.label() != null && pc.label().length() > 0) {
                    cellText = pc.label();
                } else if (b.p1.r == r && b.p1.c == c) {
                    cellText = c("1", FG_CYAN);
                } else if (b.p2.r == r && b.p2.c == c) {
                    cellText = c("2", FG_MAGENTA);
                }

                sb.append(center(cellText, cellW));

                // vertical wall slot between this cell and the next cell
                if (c < b.cols - 1) {
                    String vw = b.v[r][c] ? VERT : " ";
                    if (b.v[r][c] && tintWalls) vw = c(dim(vw), FG_YELLOW);
                    sb.append(repeat(" ", gutterW)).append(vw);
                }
            }
            sb.append(NL);

            // horizontal wall row between r and r+1
            if (r < b.rows - 1) {
                sb.append(repeat(" ", 2 + 2));
                for (int c = 0; c < b.cols; c++) {
                    boolean leftSeg  = b.h[r][c];
                    boolean rightSeg = (c < b.cols - 1) && b.h[r][c+1];

                    String h = leftSeg ? HSEG : repeat(" ", cellW);
                    if (leftSeg && tintWalls) h = c(dim(h), FG_YELLOW);
                    sb.append(h);

                    if (c < b.cols - 1) {
                        // junction between segments
                        String j = (leftSeg && rightSeg) ? CROSS : " ";
                        if ((leftSeg || rightSeg) && tintWalls) j = c(dim(j), FG_YELLOW);
                        sb.append(repeat(" ", gutterW)).append(j);
                    }
                }
                sb.append(NL);
            }
        }

        // ===== Footer =====
        sb.append(NL)
                .append("Turn: ")
                .append(b.turn == 1 ? c("P1", FG_CYAN) : c("P2", FG_MAGENTA))
                .append("   Walls: P1=").append(b.walls1).append(", P2=").append(b.walls2)
                .append(NL);

        return sb.toString();
    }
}
