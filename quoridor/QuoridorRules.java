package puzzles.quoridor;

import game.core.Position;
import game.core.Rules;

import java.util.ArrayDeque;

public final class QuoridorRules implements Rules<QuoridorState, QuoridorAction> {

    @Override
    public boolean isTerminal(QuoridorState s) {
        return s.p1.r == s.rows - 1 || s.p2.r == 0;
    }

    @Override
    public boolean isValid(QuoridorState s, QuoridorAction a) {
        return validationError(s, a) == null;
    }

    @Override
    public QuoridorState apply(QuoridorState s, QuoridorAction a) {
        QuoridorState next = s.copy();

        switch (a.type) {
            case MOVE: {
                if (next.turn == 1) next.p1 = a.to; else next.p2 = a.to;

                // refresh visible cells
                for (int r = 0; r < next.rows; r++) {
                    for (int c = 0; c < next.cols; c++) next.set(r, c, null);
                }
                next.set(next.p1.r, next.p1.c, new PawnPiece(1, "1"));
                next.set(next.p2.r, next.p2.c, new PawnPiece(2, "2"));

                next.turn = 3 - next.turn;
                break;
            }
            case WALL_H: {
                next.h[a.r][a.c] = true;
                if (a.c + 1 < next.cols) next.h[a.r][a.c + 1] = true;
                if (next.turn == 1) next.walls1--; else next.walls2--;
                next.rebuildGraphNeighbors();
                next.turn = 3 - next.turn;
                break;
            }
            case WALL_V: {
                next.v[a.r][a.c] = true;
                if (a.r + 1 < next.rows) next.v[a.r + 1][a.c] = true;
                if (next.turn == 1) next.walls1--; else next.walls2--;
                next.rebuildGraphNeighbors();
                next.turn = 3 - next.turn;
                break;
            }
            default: break;
        }
        return next;
    }

    @Override
    public String validationError(QuoridorState s, QuoridorAction a) {
        int me = s.turn;
        Position my = s.currentPawn();
        Position opp = s.otherPawn();

        switch (a.type) {
            case MOVE: {
                if (!s.inBounds(a.to.r, a.to.c)) return "Move out of bounds";
                if (a.to.equals(opp)) return "Cannot move onto opponent";
                if (!isReachableStep(s, my, opp, a.to)) return "Illegal move (blocked or not adjacent/jump)";
                return null;
            }
            case WALL_H: {
                if (me == 1 && s.walls1 <= 0) return "P1 has no walls left";
                if (me == 2 && s.walls2 <= 0) return "P2 has no walls left";
                if (a.r < 0 || a.r >= s.rows - 1 || a.c < 0 || a.c >= s.cols - 1)
                    return "Wall anchor out of bounds";
                if (s.h[a.r][a.c] || s.h[a.r][a.c + 1]) return "Wall overlaps existing horizontal wall";

                QuoridorState sim = s.copy();
                sim.h[a.r][a.c] = true;
                sim.h[a.r][a.c + 1] = true;
                sim.rebuildGraphNeighbors();
                if (!hasPath(sim, sim.p1, sim.rows - 1)) return "Wall blocks P1 path";
                if (!hasPath(sim, sim.p2, 0))            return "Wall blocks P2 path";
                return null;
            }
            case WALL_V: {
                if (me == 1 && s.walls1 <= 0) return "P1 has no walls left";
                if (me == 2 && s.walls2 <= 0) return "P2 has no walls left";
                if (a.r < 0 || a.r >= s.rows - 1 || a.c < 0 || a.c >= s.cols - 1)
                    return "Wall anchor out of bounds";
                if (s.v[a.r][a.c] || s.v[a.r + 1][a.c]) return "Wall overlaps existing vertical wall";

                QuoridorState sim = s.copy();
                sim.v[a.r][a.c] = true;
                sim.v[a.r + 1][a.c] = true;
                sim.rebuildGraphNeighbors();
                if (!hasPath(sim, sim.p1, sim.rows - 1)) return "Wall blocks P1 path";
                if (!hasPath(sim, sim.p2, 0))            return "Wall blocks P2 path";
                return null;
            }
            default: return "Unknown action";
        }
    }

    /** One-step move validity, including the Quoridor jump rules. */
    private boolean isReachableStep(QuoridorState s, Position me, Position opp, Position target) {
        for (Position n : s.lattice[me.r][me.c].neighbors()) {
            if (n.equals(target) && !n.equals(opp)) return true;

            if (n.equals(opp)) {
                // Straight-over jump:
                int dr = opp.r - me.r, dc = opp.c - me.c;
                Position straight = new Position(opp.r + dr, opp.c + dc);
                if (s.inBounds(straight.r, straight.c)
                        && s.lattice[opp.r][opp.c].neighbors().contains(straight)
                        && straight.equals(target)) return true;

                // Side-steps if straight is blocked:
                for (Position nn : s.lattice[opp.r][opp.c].neighbors()) {
                    if (!(nn.r == me.r && nn.c == me.c)) {
                        if (nn.equals(target)) return true;
                    }
                }
            }
        }
        return false;
    }

    /** BFS to check that a pawn has at least one path to its goal row. */
    private boolean hasPath(QuoridorState s, Position start, int goalRow) {
        boolean[][] vis = new boolean[s.rows][s.cols];
        ArrayDeque<Position> dq = new ArrayDeque<Position>();
        dq.add(start); vis[start.r][start.c] = true;
        while (!dq.isEmpty()) {
            Position p = dq.poll();
            if (p.r == goalRow) return true;
            for (Position n : s.lattice[p.r][p.c].neighbors()) {
                if (!vis[n.r][n.c]) { vis[n.r][n.c] = true; dq.add(n); }
            }
        }
        return false;
    }
}
