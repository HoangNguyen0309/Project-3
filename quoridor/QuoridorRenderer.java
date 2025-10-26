package puzzles.quoridor;

import game.core.Piece;
import game.core.Renderer;

/**
 * Multiline Quoridor renderer with aligned column headers (matches current state):
 * - Works with QuoridorState { rows, cols, h, v, p1, p2, walls1, walls2, turn }.
 * - Horizontal walls: h[r][c] between (r,c) and (r+1,c).
 * - Vertical walls:   v[r][c] between (r,c) and (r,c+1).
 * - Java 8 compatible (no String.repeat).
 */
public final class QuoridorRenderer implements Renderer<QuoridorState> {
    private final boolean asciiMode;

    public QuoridorRenderer() { this(false); }
    public QuoridorRenderer(boolean asciiMode) { this.asciiMode = asciiMode; }

    @Override
    public String render(QuoridorState b) {
        final String NL = System.lineSeparator();

        // --- Layout parameters ---
        final int maxCol = Math.max(0, b.cols - 1);
        final int digits = String.valueOf(maxCol).length(); // width for largest col index
        final int cellW  = Math.max(3, digits + 1);         // cell width (>=3 looks nicer)
        final int gutterW = 1;                              // space between cells
        final int wallW   = 1;                              // one char for a vertical wall

        // Visual tokens sized to cellW
        final String V = asciiMode ? "|" : WallPiece.V().label();
        final String H = asciiMode ? repeat("-", cellW)
                : repeat(WallPiece.H().label(), cellW);
        final String SP = repeat(" ", cellW);                                // blank segment
        final String DOT = "·";                                              // empty cell marker

        StringBuilder sb = new StringBuilder();

        // ── Column header (aligned) ───────────────────────────────────────────
        sb.append(repeat(" ", 2 + 2)); // room for row label like "%2d" plus two spaces
        for (int c = 0; c < b.cols; c++) {
            sb.append(center(String.valueOf(c), cellW));
            if (c < b.cols - 1) {
                sb.append(repeat(" ", gutterW + wallW)); // leave slot for vertical wall between cells
            }
        }
        sb.append(NL);

        // ── Rows ─────────────────────────────────────────────────────────────
        for (int r = 0; r < b.rows; r++) {
            // Cells + vertical walls
            sb.append(String.format("%2d  ", r));
            for (int c = 0; c < b.cols; c++) {
                // Prefer board piece label if present; otherwise place 1/2 via positions
                String cell = DOT;
                Piece pc = b.get(r, c);
                if (pc != null && pc.label() != null && pc.label().length() > 0) {
                    cell = pc.label();
                } else if (b.p1.r == r && b.p1.c == c) {
                    cell = "1";
                } else if (b.p2.r == r && b.p2.c == c) {
                    cell = "2";
                }

                sb.append(center(cell, cellW));

                if (c < b.cols - 1) {
                    sb.append(repeat(" ", gutterW))
                            .append(b.v[r][c] ? V : " ");
                }
            }
            sb.append(NL);

            // Horizontal walls between row r and r+1
            if (r < b.rows - 1) {
                sb.append(repeat(" ", 2 + 2)); // under the row label
                for (int c = 0; c < b.cols; c++) {
                    boolean hLeft  = b.h[r][c];                       // segment under column c
                    boolean hRight = (c < b.cols - 1) && b.h[r][c+1]; // next segment to the right

                    sb.append(hLeft ? H : SP);

                    if (c < b.cols - 1) {
                        // between segments: gutter + junction/space
                        sb.append(repeat(" ", gutterW))
                                .append(hLeft && hRight ? (asciiMode ? "+" : "╋") : " ");
                    }
                }
                sb.append(NL);
            }
        }

        // ── Footer ───────────────────────────────────────────────────────────
        sb.append(NL)
                .append("Turn: ")
                .append(b.turn == 1 ? (asciiMode ? "P1 (1)" : "P1 ⒈")
                        : (asciiMode ? "P2 (2)" : "P2 ⒉"))
                .append("   Walls: P1=").append(b.walls1).append(", P2=").append(b.walls2)
                .append(NL)
                .append("Commands: move r c | wall h r c | wall v r c | help | quit")
                .append(NL);

        return sb.toString();
    }

    // --- Helpers (Java 8 friendly) ---
    private static String repeat(String s, int n) {
        if (n <= 0) return "";
        StringBuilder b = new StringBuilder(s.length() * n);
        for (int i = 0; i < n; i++) b.append(s);
        return b.toString();
    }

    private static String center(String text, int width) {
        if (text == null) text = "";
        int len = text.length();
        if (len >= width) return text; // if it overflows, just return as-is
        int left = (width - len) / 2;
        int right = width - len - left;
        return repeat(" ", left) + text + repeat(" ", right);
    }
}
