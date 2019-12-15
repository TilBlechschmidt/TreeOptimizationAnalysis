package de.nordakademie.treeOptimizationAnalysis.heuristicEvaluations;

import de.nordakademie.treeOptimizationAnalysis.GameState;
import de.nordakademie.treeOptimizationAnalysis.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class NullSumHeuristicEvaluation<T extends GameState> extends RuleCompliantHeuristicEvaluation<T> {

    @Override
    public Map<Player, Double> evalHeuristic(T state) {
        Map<Player, Double> result = new HashMap<>();
        double val = evalFor(Player.PLAYER_1, state) - evalFor(Player.PLAYER_2, state);
        result.put(Player.PLAYER_1, val);
        result.put(Player.PLAYER_2, 0 - val);
        return result;
    }

    protected abstract double evalFor(Player p, T state);
}
