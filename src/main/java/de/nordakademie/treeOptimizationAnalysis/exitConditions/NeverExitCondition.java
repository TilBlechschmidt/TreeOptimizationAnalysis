package de.nordakademie.treeOptimizationAnalysis.exitConditions;

import de.nordakademie.treeOptimizationAnalysis.gameStates.GameState;
import de.nordakademie.treeOptimizationAnalysis.gameStates.GameStateTreeNode;
import de.nordakademie.treeOptimizationAnalysis.heuristicEvaluations.HeuristicEvaluation;
import de.nordakademie.treeOptimizationAnalysis.knownReactionPaths.KnownReactionsPath;

public class NeverExitCondition<T extends GameState<T>> implements ExitCondition<T> {
    public NeverExitCondition() {}
    public NeverExitCondition(HeuristicEvaluation<T> h, KnownReactionsPath<T> k) {this();}

    @Override
    public boolean shouldBreak(GameStateTreeNode<T> evaluationBase, GameStateTreeNode<T> gameState) {
        return gameState.getState().getGameSituation().isFinal();
    }
}