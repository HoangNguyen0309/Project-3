package puzzles.quoridor;

import game.core.Renderer;

/**
 * Multiline Quoridor renderer with aligned column headers.
 * - Chooses a cell width based on the largest column index (>=3).
 * - Centers header digits and cell contents in the same width.
 * - Horizontal wall segments match cell width for perfect alignment.
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
        final int maxCol = Math.max(0, b.m - 1);
        final int digits = String.valueOf(maxCol).length(); // width needed for largest index
        final int cellW  = Math.max(3, digits + 1);         // cell width (>=3 looks nicest)
        final int gutterW = 1;                              // single-space gutter between cells
        final int wallW   = 1;                              // one char for a vertical wall

        // Visual tokens sized to cellW
        final String V = asciiMode ? "|" : "┃";             // vertical wall, single char
        final String H = asciiMode ? repeat("-", cellW) : repeat("━", cellW); // horizontal segment
        final String SP = repeat(" ", cellW);               // blank segment (under a cell)
        final String DOT = "·";                             // empty cell marker

        StringBuilder sb = new StringBuilder();

        // ── Column header (aligned) ───────────────────────────────────────────
        sb.append(repeat(" ", 2 + 2)); // room for row label like "%2d" plus two spaces
        for (int c = 0; c < b.m; c++) {
            sb.append(center(String.valueOf(c), cellW));
            if (c < b.m - 1) {
                sb.append(repeat(" ", gutterW + wallW)); // space for gutter + wall slot
            }
        }
        sb.append(NL);

        // ── Rows ─────────────────────────────────────────────────────────────
        for (int r = 0; r < b.n; r++) {
            final int er = QuoridorState.gr(r);

            // Cells + vertical walls
            sb.append(String.format("%2d  ", r));
            for (int c = 0; c < b.m; c++) {
                final int ec = QuoridorState.gc(c);

                String cell = DOT;
                if (b.p1.r == er && b.p1.c == ec) cell = "1";
                else if (b.p2.r == er && b.p2.c == ec) cell = "2";

                sb.append(center(cell, cellW));

                if (c < b.m - 1) {
                    final int vcs = ec + 1; // (even, odd) column for vertical wall slot
                    sb.append(repeat(" ", gutterW))
                            .append(b.v[er][vcs] ? V : " ");
                }
            }
            sb.append(NL);

            // Horizontal walls between row r and r+1
            if (r < b.n - 1) {
                sb.append(repeat(" ", 2 + 2)); // under the row label
                final int hrs = er + 1; // (odd, even)
                for (int c = 0; c < b.m; c++) {
                    final int ec = QuoridorState.gc(c);
                    final boolean hLeft  = b.h[hrs][ec];
                    final boolean hRight = (c < b.m - 1) && b.h[hrs][ec + 2];

                    sb.append(hLeft ? H : SP);

                    if (c < b.m - 1) {
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
                .append(b.p1Turn ? (asciiMode ? "P1 (1)" : "P1 ⒈") : (asciiMode ? "P2 (2)" : "P2 ⒉"))
                .append("   Walls: P1=").append(b.p1Walls).append(", P2=").append(b.p2Walls)
                .append(NL)
                .append("Commands: move r c | wall h r c | wall v r c | size n m | help | quit")
                .append(NL);

        return sb.toString();
    }

    // --- Helpers (Java 8 friendly) ---
    private static String repeat(String s, int n) {
        StringBuilder b = new StringBuilder(s.length() * Math.max(0, n));
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
