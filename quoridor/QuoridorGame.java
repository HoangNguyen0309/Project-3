package puzzles.quoridor;

import game.core.ConsoleIO;
import game.core.Game;
import game.core.Position;

public final class QuoridorGame implements Game {
    private final ConsoleIO io;
    private final QuoridorRules rules = new QuoridorRules();
    private QuoridorState state; // create after asking for size

    public QuoridorGame(ConsoleIO io) { this.io = io; }

    @Override
    public void run() {
        // Ask user for board size: n m (rows cols)
        int n = 9, m = 9; // defaults
        io.print("Enter board size 'n m' (rows cols, >=5 recommended; default 9 9): ");
        String sizeLine = io.nextLine();
        if (sizeLine != null) {
            sizeLine = sizeLine.trim();
            try {
                String[] parts = sizeLine.split("\\s+");
                if (parts.length == 1) {
                    int parsed = Integer.parseInt(parts[0]);
                    if (parsed >= 5) { n = parsed; m = parsed; }
                } else if (parts.length >= 2) {
                    int nParsed = Integer.parseInt(parts[0]);
                    int mParsed = Integer.parseInt(parts[1]);
                    n = nParsed;
                    m = mParsed;
                }
            } catch (Exception ignore) { /* keep defaults */ }
        }
        state = new QuoridorState(n, m);

        io.println(new QuoridorRenderer().render(state));
        while (true) {
            if (rules.isTerminal(state)) {
                int winner = (state.p1.r == state.rows - 1) ? 1 : 2;
                io.println("Game over! Winner: P" + winner);
                return;
            }

            io.print("P" + state.turn + "> ");
            String line = io.nextLine();
            if (line == null) return;
            line = line.trim();
            if (line.equalsIgnoreCase("quit")) return;
            if (line.equalsIgnoreCase("help")) {
                io.println("move r c  |  wall h r c  |  wall v r c  |  quit");
                continue;
            }

            try {
                String[] t = line.split("\\s+");
                QuoridorAction a;
                String cmd = t[0].toLowerCase();
                if ("move".equals(cmd)) {
                    int r = Integer.parseInt(t[1]), c = Integer.parseInt(t[2]);
                    a = QuoridorAction.move(state.currentPawn(), new Position(r, c));
                } else if ("wall".equals(cmd)) {
                    String hv = t[1].toLowerCase();
                    int r = Integer.parseInt(t[2]), c = Integer.parseInt(t[3]);
                    a = "h".equals(hv) ? QuoridorAction.wallH(r, c) : QuoridorAction.wallV(r, c);
                } else {
                    io.println("Unknown command");
                    continue;
                }

                String err = rules.validationError(state, a);
                if (err != null) {
                    io.println("Invalid: " + err);
                } else {
                    state = rules.apply(state, a);
                    io.println(new QuoridorRenderer().render(state));
                }
            } catch (Exception ex) {
                io.println("Parse error. Try: move r c | wall h r c | wall v r c");
            }
        }
    }
}
