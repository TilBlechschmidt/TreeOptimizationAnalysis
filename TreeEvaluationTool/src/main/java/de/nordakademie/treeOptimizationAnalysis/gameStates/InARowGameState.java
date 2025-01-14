package de.nordakademie.treeOptimizationAnalysis.gameStates;

import de.nordakademie.treeOptimizationAnalysis.Player;
import de.nordakademie.treeOptimizationAnalysis.gamePoints.GameSituation;
import de.nordakademie.treeOptimizationAnalysis.heuristicEvaluations.InARowGameHeuristicEvaluation;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static de.nordakademie.treeOptimizationAnalysis.Player.PLAYER_1;
import static de.nordakademie.treeOptimizationAnalysis.Player.PLAYER_2;

public class InARowGameState implements GameState<InARowGameState> {
    private final int width, height;

    private final Player[][] field;
    private final Player currentPlayer;
    private final int winLength;
    private final boolean gravity;
    private final InARowGameHeuristicEvaluation winHeuristic;

    public InARowGameState(int width, int height, int winLength, boolean gravity, Player currentPlayer) {
        this(width, height, winLength, gravity, currentPlayer, new Player[width][height]);
    }

    private InARowGameState(int width, int height, int winLength, boolean gravity, Player currentPlayer, Player[][] field) {
        this.width = width;
        this.height = height;
        this.field = field;
        this.winLength = winLength;
        this.gravity = gravity;
        this.currentPlayer = currentPlayer;
        this.winHeuristic = new InARowGameHeuristicEvaluation(0, InARowGameHeuristicEvaluation.LENGTH_BY_CONSECUTIVE.accumulateBy(InARowGameHeuristicEvaluation.ACCUMULATE_BY_MAX), winLength);
    }

    public Player[][] getField() {
        return field;
    }

    public int getWinLength() {
        return winLength;
    }

    private InARowGameState createChildState(Consumer<Player[][]> fieldMutator) {
        // 1. Create a copy of the field
        Player[][] newField = new Player[width][height];

        for (int i = 0; i < width; i++) {
            System.arraycopy(field[i], 0, newField[i], 0, height);
        }

        // 2. Mutate the field
        fieldMutator.accept(newField);

        // 3. Create a cloned instance
        return new InARowGameState(width, height, winLength, gravity, currentPlayer == PLAYER_1 ? PLAYER_2 : PLAYER_1, newField);
    }

    @Override
    public Player getNextChoice() {
        return currentPlayer;
    }

    @Override
    public Set<InARowGameState> getNextStates() {
        Set<InARowGameState> newStates = new HashSet<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (field[x][y] == null) {
                    int fX = x, fY = y;
                    newStates.add(createChildState(field -> field[fX][fY] = currentPlayer));

                    if (gravity) {
                        break;
                    }
                }
            }
        }

        return newStates;
    }

    @Override
    public GameSituation getGameSituation() {
        GameSituation situation = GameSituation.RUNNING;

        // Detect Tie conditions
        boolean foundValidMove = false;
        outer: for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (field[x][y] == null) {
                    foundValidMove = true;
                    break outer;
                }
            }
        }

        if (!foundValidMove) situation = GameSituation.TIE;

        if (winHeuristic.evalFor(PLAYER_1, this) > 0.99) {
            situation = GameSituation.WON_PLAYER1;
        }

        if (winHeuristic.evalFor(PLAYER_2, this) > 0.99) {
            situation = GameSituation.WON_PLAYER2;
        }

        return situation;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                if (field[x][y] == null) {
                    stringBuilder.append('_');
                } else {
                    stringBuilder.append(field[x][y].getIndex());
                }
            }
            stringBuilder.append('|');
        }
        return stringBuilder.toString();
    }


    public boolean isGravity() {
        return gravity;
    }

    @Override
    public int getBoardWidth() {
        return field.length;
    }

    @Override
    public int getBoardHeight() {
        return field[0].length;
    }
}
