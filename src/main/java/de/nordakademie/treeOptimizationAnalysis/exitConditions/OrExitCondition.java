package de.nordakademie.treeOptimizationAnalysis.exitConditions;

import de.nordakademie.treeOptimizationAnalysis.*;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class OrExitCondition<T extends GameState<T>> implements ExitCondition<T> {
    Set<ExitCondition<T>> exitConditions;

    public ExitCondition.Factory factory(ExitCondition.Factory... args) {
        return new Factory() {
            @Override
            public <T extends GameState<T>> ExitCondition<T> create(HeuristicEvaluation<T> heuristicEvaluation, KnownReactionsPath<T> path, Player player) {
                Set<ExitCondition<T>> createdArgs = Arrays.stream(args)
                        .map(arg -> arg.create(heuristicEvaluation, path, player))
                        .collect(Collectors.toSet());
                return new OrExitCondition<>(createdArgs);
            }
        };
    }

    public OrExitCondition(Set<ExitCondition<T>> exitConditions) {
        this.exitConditions = exitConditions;
    }

    @Override
    public boolean shouldBreak(GameStateTreeNode<T> evaluationBase, GameStateTreeNode<T> gameState) {
        for(ExitCondition<T> exitCondition:exitConditions) {
            if(exitCondition.shouldBreak(evaluationBase, gameState)) {
                return true;
            }
        }
        return false;
    }
}