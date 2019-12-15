package de.nordakademie.treeOptimizationAnalysis;

import java.util.Map;
import java.util.Set;

public interface GameState<T extends GameState<T>> {
    Map<Player, PlayerSituation> getGameSituation();

    Set<T> getNextStates();

    Player getNextChoice();

    // TODO Remove these deprecated methods
    boolean isFinal();

    Map<Player, Double> eval();
}
