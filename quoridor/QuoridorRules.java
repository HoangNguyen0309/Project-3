package puzzles.quoridor;

import game.core.Rules;
import game.core.Position;
import java.util.*;

/** Rules for Quoridor (validation + state transition). */
public final class QuoridorRules implements Rules<QuoridorState, QuoridorAction> {
    private static int mid(int a, int b) {
        return (a + b) / 2;
    }

    private static boolean blocked(QuoridorState s, int er, int ec, int nr, int nc) {
        int mr = mid(er, nr), mc = mid(ec, nc);
        if (er == nr) { // left/right => vertical segment at (even, odd)
            return s.v[er][mc];
        } else { // up/down => horizontal segment at (odd, even)
            return s.h[mr][ec];
        }
    }

    private static List<Position> neighbors(QuoridorState s, int er, int ec) {
        List<Position> out = new ArrayList<>();
        int[][] del = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        for (int[] d : del) {
            int nr = er + d[0], nc = ec + d[1];
            if (!s.inGrid(nr, nc) || !QuoridorState.isCell(nr, nc)) continue;
            if (!blocked(s, er, ec, nr, nc)) out.add(new Position(nr, nc));
        }
        return out;
    }

    public List<Position> legalPawnMoves(QuoridorState s, boolean p1Turn) {
        Position me = p1Turn ? s.p1 : s.p2;
        Position you = p1Turn ? s.p2 : s.p1;
        List<Position> res = new ArrayList<>();
        int[][] dirs = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        for (int[] d : dirs) {
            int nr = me.r + d[0], nc = me.c + d[1];
            if (!s.inGrid(nr, nc) || !QuoridorState.isCell(nr, nc)) continue;
            if (blocked(s, me.r, me.c, nr, nc)) continue;
            Position adj = new Position(nr, nc);
            if (!(adj.r == you.r && adj.c == you.c)) {
                res.add(adj); // simple step
            } else {
                // straight jump if open
                int jr = you.r + d[0], jc = you.c + d[1];
                boolean jumped = false;
                if (s.inGrid(jr, jc) && QuoridorState.isCell(jr, jc) && !blocked(s, you.r, you.c, jr, jc)) {
                    res.add(new Position(jr, jc));
                    jumped = true;
                }
                if (!jumped) {
                    // diagonal side steps
                    int[][] sides = (d[0] != 0) ? new int[][]{{0, 2}, {0, -2}} : new int[][]{{2, 0}, {-2, 0}};
                    for (int[] sd : sides) {
                        int sr = you.r + sd[0], sc = you.c + sd[1];
                        if (!s.inGrid(sr, sc) || !QuoridorState.isCell(sr, sc)) continue;
                        if (!blocked(s, you.r, you.c, sr, sc)) res.add(new Position(sr, sc));
                    }
                }
            }
        }
        return res;
    }

    // ---- Walls (logical r,c anchors) ----
    public static boolean canPlaceWallH(QuoridorState s, int r, int c) {
        if (r < 0 || r > s.n - 2 || c < 0 || c > s.m - 2) return false;
        int rr = 2 * r + 1, cc1 = 2 * c, cc2 = 2 * c + 2;
        if (s.h[rr][cc1] || s.h[rr][cc2]) return false;
        if (c > 0 && s.h[rr][cc1 - 2]) return false; // avoid length-3 run
        if (c < s.m - 2 && s.h[rr][cc2 + 2]) return false;
        return true;
    }

    public static void placeWallH(QuoridorState s, int r, int c) {
        int rr = 2 * r + 1, cc1 = 2 * c, cc2 = 2 * c + 2;
        s.h[rr][cc1] = true;
        s.h[rr][cc2] = true;
    }

    public static boolean canPlaceWallV(QuoridorState s, int r, int c) {
        if (r < 0 || r > s.n - 2 || c < 0 || c > s.m - 2) return false;
        int cc = 2 * c + 1, rr1 = 2 * r, rr2 = 2 * r + 2;
        if (s.v[rr1][cc] || s.v[rr2][cc]) return false;
        if (r > 0 && s.v[rr1 - 2][cc]) return false;
        if (r < s.n - 2 && s.v[rr2 + 2][cc]) return false;
        return true;
    }

    public static void placeWallV(QuoridorState s, int r, int c) {
        int cc = 2 * c + 1, rr1 = 2 * r, rr2 = 2 * r + 2;
        s.v[rr1][cc] = true;
        s.v[rr2][cc] = true;
    }

    // ---- Path existence (BFS on even-even cells) ----
    private static boolean hasPathToGoal(QuoridorState s, boolean forP1) {
        int startR = forP1 ? s.p1.r : s.p2.r;
        int startC = forP1 ? s.p1.c : s.p2.c;
        int goalRow = forP1 ? 2 * (s.n - 1) : 0;
        boolean[][] seen = new boolean[s.rows][s.cols];
        ArrayDeque<Position> dq = new ArrayDeque<>();
        dq.add(new Position(startR, startC));
        seen[startR][startC] = true;
        while (!dq.isEmpty()) {
            Position cur = dq.poll();
            if (cur.r == goalRow) return true;
            for (Position nxt : neighbors(s, cur.r, cur.c)) {
                if (!seen[nxt.r][nxt.c]) { seen[nxt.r][nxt.c] = true; dq.add(nxt); }
            }
        }
        return false;
    }

    // ---- Simulate a wall and report who gets blocked ----
    // returns 0=ok, 1=blocks P1, 2=blocks P2, 3=blocks both
    private static int blocksPathIfPlaced(QuoridorState s, QuoridorAction.WallDir dir, int r, int c) {
        QuoridorState sim = s.copy();
        if (dir == QuoridorAction.WallDir.H) placeWallH(sim, r, c); else placeWallV(sim, r, c);
        boolean p1ok = hasPathToGoal(sim, true);
        boolean p2ok = hasPathToGoal(sim, false);
        int mask = 0;
        if (!p1ok) mask |= 1;
        if (!p2ok) mask |= 2;
        return mask;
    }

    // ---- Rules interface ----
    @Override public boolean isTerminal(QuoridorState s) {
        return s.p1.r == 2 * (s.n - 1) || s.p2.r == 0;
    }

    @Override public boolean isValid(QuoridorState s, QuoridorAction a) {
        if (a == null) return false;
        if (a.type == QuoridorAction.Type.MOVE) {
            int er = QuoridorState.gr(a.r), ec = QuoridorState.gc(a.c);
            if (!s.inGrid(er, ec) || !QuoridorState.isCell(er, ec)) return false;
            for (Position d : legalPawnMoves(s, s.p1Turn)) if (d.r == er && d.c == ec) return true;
            return false;
        } else { // WALL
            if (s.p1Turn && s.p1Walls <= 0) return false;
            if (!s.p1Turn && s.p2Walls <= 0) return false;
            boolean ok = (a.dir == QuoridorAction.WallDir.H) ? canPlaceWallH(s, a.r, a.c)
                    : canPlaceWallV(s, a.r, a.c);
            if (!ok) return false;
            // special: would it block a path?
            return blocksPathIfPlaced(s, a.dir, a.r, a.c) == 0;
        }
    }

    @Override public QuoridorState apply(QuoridorState s, QuoridorAction a) {
        if (!isValid(s, a)) return s; // defensive
        QuoridorState ns = s.copy();
        if (a.type == QuoridorAction.Type.MOVE) {
            int er = QuoridorState.gr(a.r), ec = QuoridorState.gc(a.c);
            ns = new QuoridorState(ns.n, ns.m, ns.rows, ns.cols,
                    s.p1Turn ? new Position(er, ec) : ns.p1,
                    s.p1Turn ? ns.p2 : new Position(er, ec),
                    ns.p1Walls, ns.p2Walls, ns.p1Turn, ns.h, ns.v);
        } else {
            if (a.dir == QuoridorAction.WallDir.H) placeWallH(ns, a.r, a.c); else placeWallV(ns, a.r, a.c);
            ns = new QuoridorState(ns.n, ns.m, ns.rows, ns.cols, ns.p1, ns.p2,
                    s.p1Turn ? ns.p1Walls - 1 : ns.p1Walls,
                    s.p1Turn ? ns.p2Walls : ns.p2Walls - 1,
                    ns.p1Turn, ns.h, ns.v);
        }
        // toggle turn
        return new QuoridorState(ns.n, ns.m, ns.rows, ns.cols, ns.p1, ns.p2, ns.p1Walls, ns.p2Walls, !s.p1Turn, ns.h, ns.v);
    }

    @Override public String validationError(QuoridorState s, QuoridorAction a) {
        if (a == null) return "No action";
        if (a.type == QuoridorAction.Type.MOVE) {
            int er = QuoridorState.gr(a.r), ec = QuoridorState.gc(a.c);
            if (!s.inGrid(er, ec) || !QuoridorState.isCell(er, ec)) return "Target out of bounds";
            return "Illegal move";
        } else {
            if (s.p1Turn && s.p1Walls <= 0) return "No walls left";
            if (!s.p1Turn && s.p2Walls <= 0) return "No walls left";
            boolean ok = (a.dir == QuoridorAction.WallDir.H) ? canPlaceWallH(s, a.r, a.c)
                    : canPlaceWallV(s, a.r, a.c);
            if (!ok) return "Overlaps/adjacent violation";

            int mask = blocksPathIfPlaced(s, a.dir, a.r, a.c);
            if (mask == 1) return "Wall blocks P1's path";
            if (mask == 2) return "Wall blocks P2's path";
            if (mask == 3) return "Wall blocks both players' paths";

            // generic fallback (shouldn't happen if isValid was checked first)
            return "Invalid";
        }
    }
}
