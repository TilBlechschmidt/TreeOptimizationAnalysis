package de.nordakademie.treeOptimizationAnalysis;

import de.nordakademie.treeOptimizationAnalysis.exitConditions.*;
import de.nordakademie.treeOptimizationAnalysis.gameStates.GameState;
import de.nordakademie.treeOptimizationAnalysis.gameStates.GameStateTreeNode;
import de.nordakademie.treeOptimizationAnalysis.gameStates.InARowGameState;
import de.nordakademie.treeOptimizationAnalysis.games.ChessGameState;
import de.nordakademie.treeOptimizationAnalysis.games.ChessPiece;
import de.nordakademie.treeOptimizationAnalysis.heuristicEvaluations.ChessHeuristicEvaluation;
import de.nordakademie.treeOptimizationAnalysis.heuristicEvaluations.HeuristicEvaluation;
import de.nordakademie.treeOptimizationAnalysis.heuristicEvaluations.InARowGameHeuristicEvaluation;
import de.nordakademie.treeOptimizationAnalysis.heuristicEvaluations.InARowGameHeuristicEvaluation.PointsCounter.Builder;
import de.nordakademie.treeOptimizationAnalysis.knownReactionPaths.CompressedKnownReactionPath;
import de.nordakademie.treeOptimizationAnalysis.knownReactionPaths.KnownReactionsPath;
import de.nordakademie.treeOptimizationAnalysis.traversalIterator.BreadthFirstTreeTraversalIterator;
import de.nordakademie.treeOptimizationAnalysis.traversalIterator.DepthFirstTreeTraversalIterator;
import de.nordakademie.treeOptimizationAnalysis.traversalIterator.TreeTraversalIterator;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static de.nordakademie.treeOptimizationAnalysis.Player.*;
import static de.nordakademie.treeOptimizationAnalysis.games.ChessPiece.*;
import static de.nordakademie.treeOptimizationAnalysis.heuristicEvaluations.InARowGameHeuristicEvaluation.*;

public class App implements Runnable {
    // define Output
    private static final String ENTRY_SEPARATOR = "\n";
    private static final String FIELD_SEPARATOR = "; ";

    // settings
    private static final Consumer<String> output = System.out::print;
    private static final long TIMEOUT_IN_NANOS = 60L * 1000000000L;
    private static final int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();

    private final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private final List<ExitCondition.Factory> exitConditions;
    private final List<Supplier<TreeTraversalIterator<?>>> treeTraversalIterators;
    private final List<Game<?>> games;
    private final List<KnownReactionsPath.Factory> caches;



    public static void main(String[] args) {
        List<ExitCondition.Factory> exits = new ArrayList<>();
        List<Supplier<TreeTraversalIterator<?>>> iterators = Arrays.asList(
                BreadthFirstTreeTraversalIterator::new,
                DepthFirstTreeTraversalIterator::new
        );
        List<KnownReactionsPath.Factory> caches = Collections.singletonList(
                CompressedKnownReactionPath.FACTORY
        );


        for (int i = 1; i < 3; i++) {
            exits.add(TurnCountingCondition.factory(i));
            for (double j = 0.0; j < 0.3; j += 0.05) {
                exits.add(OrExitCondition.factory(
                        TurnCountingCondition.factory(i),
                        CompareToOtherOptionsByHeuristicExitCondition.factory(j)
                ));
            }
        }

        List<Game<?>> games = Collections.singletonList(getChess());

        System.out.println(exits.size());
        int whatever = exits.size() * iterators.size() * caches.size();
        System.out.println(whatever);
        System.out.println(whatever * whatever * games.size() * 16);

        new App(exits, iterators, games, caches).run();
    }

    private static Game<?> getChess() {
        ChessPiece[][] board = {
                {rook(PLAYER_1),pawn(PLAYER_1),null,null,null,null,pawn(PLAYER_2),rook(PLAYER_2)},
                {knight(PLAYER_1),pawn(PLAYER_1),null,null,null,null,pawn(PLAYER_2),knight(PLAYER_2)},
                {bishop(PLAYER_1),pawn(PLAYER_1),null,null,null,null,pawn(PLAYER_2),bishop(PLAYER_2)},
                {queen(PLAYER_1),pawn(PLAYER_1),null,null,null,null,pawn(PLAYER_2),queen(PLAYER_2)},
                {king(PLAYER_1),pawn(PLAYER_1),null,null,null,null,pawn(PLAYER_2),king(PLAYER_2)},
                {bishop(PLAYER_1),pawn(PLAYER_1),null,null,null,null,pawn(PLAYER_2),bishop(PLAYER_2)},
                {knight(PLAYER_1),pawn(PLAYER_1),null,null,null,null,pawn(PLAYER_2),knight(PLAYER_2)},
                {rook(PLAYER_1),pawn(PLAYER_1),null,null,null,null,pawn(PLAYER_2),rook(PLAYER_2)},
        };
        ChessGameState initialBoard = new ChessGameState(board,PLAYER_1);

        return new Game<>(Collections.<Factory<ChessGameState>>singletonList(ChessHeuristicEvaluation::new), Collections.singletonList(initialBoard));
    }

    private static Game<?> getInARowGame() {
        InARowGameState ticTacToe = new InARowGameState(3, 3, 3, false, Player.PLAYER_1);
        InARowGameState fourWins = new InARowGameState(6, 4, 4, true, Player.PLAYER_1);
        List<InARowGameState> games = Collections.singletonList(fourWins);

        HeuristicEvaluation.Factory<InARowGameState> objectsInFrameSquareSum = InARowGameHeuristicEvaluation.factory(LENGTH_BY_OBJECTS_IN_POSSIBLE_BOX.accumulateBy(ACCUMULATE_BY_SQUARE_SUM), 0);

        List<HeuristicEvaluation.Factory<InARowGameState>> heuristics = new ArrayList<>();
        for (Builder pcb : new Builder[] { LENGTH_BY_OBJECTS_IN_POSSIBLE_BOX, LENGTH_BY_CONSECUTIVE }) {
            for (BinaryOperator<Integer> acc : new BinaryOperator[] { ACCUMULATE_BY_MAX, ACCUMULATE_BY_SQUARE_SUM }) {
                heuristics.add(factory(pcb.accumulateBy(acc),1000));
            }
        }

        return new Game<>(heuristics, games);
    }

    public App(List<ExitCondition.Factory> exitConditions, List<Supplier<TreeTraversalIterator<?>>> treeTraversalIterators, List<Game<?>> games, List<KnownReactionsPath.Factory> caches) {
        this.exitConditions = exitConditions;
        this.treeTraversalIterators = treeTraversalIterators;
        this.games = games;
        this.caches = caches;
    }

    @Override
    public void run() {
        printHeaders();
        games.forEach(this::analyze);
        executor.shutdown();
    }

    private <T extends GameState<T>> void analyze(Game<T> game) {
        for (T initialState : game.initialGameStates) {
            forEachController(game, Player.PLAYER_1, controller1Fact ->
                    forEachController(game, PLAYER_2, controller2Fact -> executor.execute(() -> {
                        Controller<T> controller1 = controller1Fact.get();
                        Controller<T> controller2 = controller2Fact.get();
                        long start = time();
                        GameStateTreeNode<T> node = new GameStateTreeNode<>(initialState, null, 0);

                        while (!node.getState().getGameSituation().isFinal()) {
                            try {
                                Controller<T> controller = node.getState().getNextChoice() == Player.PLAYER_1 ? controller1 : controller2;
                                node = controller.nextMove(node);
                            } catch (Exception e) {
                                printMetrics(controller1, controller2, node, initialState, time() - start, e.getClass().getSimpleName() + " during execution: " + e.getLocalizedMessage() + " at " + Arrays.toString(e.getStackTrace()));
                                return;
                            }
                            if (time() - start > TIMEOUT_IN_NANOS) {
                                printMetrics(controller1, controller2, node, initialState, time() - start, " timed out ");
                                return;
                            }
                        }
                        printMetrics(controller1, controller2, node, initialState, time() - start, null);
                    })));
        }
    }

    private long time() {
        return ManagementFactory.getThreadMXBean().getCurrentThreadUserTime();
    }

    private <T extends GameState<T>> void forEachController(Game<T> game, Player player, Consumer<Supplier<Controller<T>>> callback) {
        for (ExitCondition.Factory exitConditionFactory : exitConditions) {
            for (Supplier<TreeTraversalIterator<?>> iteratorFactory : treeTraversalIterators) {
                for (KnownReactionsPath.Factory cacheFactory : caches) {
                    for (HeuristicEvaluation.Factory<T> heuristicEvaluationFactory : game.heuristicEvaluations) {
                        callback.accept(() -> this.createController(exitConditionFactory, iteratorFactory, cacheFactory, heuristicEvaluationFactory, player));
                    }
                }
            }
        }
    }

    private <T extends GameState<T>> Controller<T> createController(
            ExitCondition.Factory exitConditionFactory,
            Supplier<TreeTraversalIterator<?>> treeTraversalIteratorFactory,
            KnownReactionsPath.Factory cacheFactory,
            HeuristicEvaluation.Factory<T> heuristicEvaluationFactory,
            Player player) {
        TreeTraversalIterator<T> treeTraversalIterator = (TreeTraversalIterator<T>) treeTraversalIteratorFactory.get();
        KnownReactionsPath<T> cache = cacheFactory.create();
        HeuristicEvaluation<T> heuristicEvaluation = heuristicEvaluationFactory.create();

        ExitCondition<T> exitCondition = exitConditionFactory.create(heuristicEvaluation, cache, player);
        return new Controller<>(exitCondition, treeTraversalIterator, heuristicEvaluation, cache);
    }

    private void printHeaders() {
        for (NamedMetric m : NamedMetric.allMetrics) {
            output.accept(m.toString());
            output.accept(FIELD_SEPARATOR);
        }
        output.accept(ENTRY_SEPARATOR);
    }

    private void printMetrics(Controller<?> controller1, Controller<?> controller2, GameStateTreeNode<?> finalNode, GameState<?> initialNode, long time, String error) {
        synchronized (output) {
            for (NamedMetric m : NamedMetric.allMetrics) {
                Object val = m.measure(controller1, controller2, finalNode, initialNode, time, error);
                output.accept(val == null ? " - " : val.toString());
                output.accept(FIELD_SEPARATOR);
            }
            output.accept(ENTRY_SEPARATOR);
        }
    }

    private static class Game<T extends GameState<T>> {
        private final List<HeuristicEvaluation.Factory<T>> heuristicEvaluations;
        private final List<T> initialGameStates;

        public Game(List<HeuristicEvaluation.Factory<T>> heuristicEvaluations, List<T> initialGameStates) {
            this.heuristicEvaluations = heuristicEvaluations;
            this.initialGameStates = initialGameStates;
        }
    }
}
