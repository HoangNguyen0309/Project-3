package puzzles.quoridor;
import game.core.ConsoleIO;
import game.core.Game;
import game.core.GameFactory;


public final class QuoridorFactory implements GameFactory {
    @Override public String name() { return "Quoridor"; }


    @Override public Game create(ConsoleIO io) {
        io.println("Enter board size n m (≥3). Press Enter for 9 9:");
        String line = io.nextLine().trim();
        int n = 9, m = 9;
        if (!line.isEmpty()) {
            String[] t = line.split("[ \\t]+");
            if (t.length == 2) {
                try { n = Integer.parseInt(t[0]); m = Integer.parseInt(t[1]); } catch (Exception ignored) {}
            }
        }
        return new QuoridorGame(io, n, m);
    }
}