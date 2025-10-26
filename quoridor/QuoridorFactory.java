package puzzles.quoridor;

import game.core.ConsoleIO;
import game.core.Game;
import game.core.GameFactory;

public final class QuoridorFactory implements GameFactory {
    @Override public String name() { return "Quoridor"; }
    @Override public Game create(ConsoleIO io) { return new QuoridorGame(io); }
}
