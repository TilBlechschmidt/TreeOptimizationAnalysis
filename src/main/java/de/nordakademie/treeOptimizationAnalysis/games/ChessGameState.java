package de.nordakademie.treeOptimizationAnalysis.games;

import de.nordakademie.treeOptimizationAnalysis.GameState;
import de.nordakademie.treeOptimizationAnalysis.Player;
import de.nordakademie.treeOptimizationAnalysis.PlayerSituation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChessGameState implements GameState<ChessGameState> {
    public static class Field {
        private int x;
        private int y;

        public Field(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean isIn(ChessPiece[][] board) {
            return x >= 0 && y >= 0 && x < board.length && y < board[x].length;
        }
        public ChessPiece get(ChessPiece[][] board) {
            return board[x][y];
        }
    }
    private class Move  {
        Field start;
        Field goal;

        public Move(Field start, Field goal) {
            this.start = start;
            this.goal = goal;
        }
    }

    private ChessPiece[][] board;
    private Player nextPlayer;
    private Map<Player,Field> kingPositions;

    public ChessGameState(ChessPiece[][] board, Player nextPlayer) {
        this.board = board;
        this.nextPlayer = nextPlayer;
    }

    @Override
    public Map<Player, PlayerSituation> getGameSituation() {
        Map map = new HashMap();
        if(!calcNextStates().anyMatch(s -> true)) {
            if(isChess(nextPlayer)) {
                map.put(nextPlayer, PlayerSituation.HasLost);
                map.put(nextPlayer.getOther(), PlayerSituation.HasWon);
            } else {
                map.put(nextPlayer, PlayerSituation.Tie);
                map.put(nextPlayer, PlayerSituation.Tie);
            }
        } else {
            map.put(nextPlayer, PlayerSituation.Running);
            map.put(nextPlayer, PlayerSituation.Running);
        }
        return map;
    }

    @Override
    public Set<ChessGameState> getNextStates() {
        return calcNextStates()
            .collect(Collectors.toSet());
    }

    private Stream<ChessGameState> calcNextStates() {
        Player overNextPlayer = nextPlayer.getOther();
        return possibleMoves(nextPlayer)
                .flatMap(move -> move.start.get(board)
                    .onMove(move.goal, board)
                    .stream()
                    .map(newPiece -> {
                        ChessPiece[][] board = Arrays.stream(this.board)
                                .map(ChessPiece[]::clone)
                                .toArray(ChessPiece[][]::new);
                        board[move.start.getX()][move.start.getY()] = null;
                        board[move.goal.getX()][move.goal.getY()] = newPiece;
                        return new ChessGameState(board, overNextPlayer); }))
                .filter(state -> !state.isChess(nextPlayer));
    }


    private Stream<Move> possibleMoves(Player player) {
        return intStreamTo(board.length).flatMap(x -> intStreamTo(board[x].length).map(y -> new Field(x,y)))
            .filter(field -> field.get(board) != null && field.get(board).getPlayer().equals(player))
            .flatMap(field -> field.get(board).possibleGoals(field,board).stream().map(goal -> new Move(field, goal)));
    }

    private Stream<Integer> intStreamTo(int limit) {
        return Stream.iterate(0, i -> +1).limit(limit);
    }


    @Override
    public Player getNextChoice() {
        return nextPlayer;
    }

    public boolean isChess(Player player) {
        return possibleMoves(player.getOther())
                .map(m -> m.goal.get(board))
                .filter(Objects::nonNull)
                .anyMatch(ChessPiece::isKing);
    }
}