package puzzles.quoridor;

import game.core.ConsoleIO;
import game.core.Game;
import game.core.Rules;
import game.core.Renderer;


public final class QuoridorGame implements Game {
    private final ConsoleIO io;
    private final Rules<QuoridorState, QuoridorAction> rules = new QuoridorRules();
    private final Renderer<QuoridorState> renderer = new QuoridorRenderer();


    private QuoridorState state;


    public QuoridorGame(ConsoleIO io, int n, int m) {
        this.io = io; this.state = new QuoridorState(n, m);
    }


    @Override public void run() {
        io.println(renderer.render(state));
        while (!rules.isTerminal(state)) {
            io.print((state.p1Turn ? "P1" : "P2") + " > ");
            String line = io.nextLine(); if (line == null) break; line = line.trim();
            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) break;
            if (line.equalsIgnoreCase("help")) { printHelp(); continue; }
            if (line.startsWith("size")) { // size n m
                String[] t = line.split("\\s");
                if (t.length != 3) { io.println("Usage: size n m"); continue; }
                try {
                    int n = Integer.parseInt(t[1]); int m = Integer.parseInt(t[2]);
                    this.state = new QuoridorState(n, m); io.println(renderer.render(state));
                } catch (Exception e) { io.println("Bad size"); }
                continue;
            }
            QuoridorAction act = parse(line);
            if (act == null) { io.println("Unknown command. Type 'help'."); continue; }
            if (!rules.isValid(state, act)) { io.println(((QuoridorRules)rules).validationError(state, act)); continue; }
            state = rules.apply(state, act);
            io.println(renderer.render(state));
        }
        io.println("Game over: " + (state.p1.r == 2*(state.n-1) ? "P1 wins!" : state.p2.r == 0 ? "P2 wins!" : "Quit"));
    }


    private void printHelp() {
        io.println("Commands: size n m restart with board n×m (≥3) move r c move to logical cell (r,c) if legal wall h r c place horizontal wall at top-left (r,c) wall v r c place vertical wall at top-left (r,c) help | quit");
    }


    private QuoridorAction parse(String line) {
        String[] t = line.split("\\s");
        if (t.length == 0) return null;
        switch (t[0].toLowerCase()) {
            case "move":
                if (t.length != 3) return null;
                try { return QuoridorAction.move(Integer.parseInt(t[1]), Integer.parseInt(t[2])); } catch (Exception e) { return null; }
            case "wall":
                if (t.length != 4) return null;
                String hv = t[1].toLowerCase();
                try {
                    int r = Integer.parseInt(t[2]); int c = Integer.parseInt(t[3]);
                    if ("h".equals(hv)) return QuoridorAction.wallH(r, c);
                    if ("v".equals(hv)) return QuoridorAction.wallV(r, c);
                } catch (Exception ignored) {}
                return null;
            default: return null;
        }
    }
}
