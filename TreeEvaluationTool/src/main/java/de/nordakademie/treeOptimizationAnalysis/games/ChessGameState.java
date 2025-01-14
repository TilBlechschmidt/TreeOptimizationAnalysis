package de.nordakademie.treeOptimizationAnalysis.games;

import de.nordakademie.treeOptimizationAnalysis.Player;
import de.nordakademie.treeOptimizationAnalysis.gamePoints.GameSituation;
import de.nordakademie.treeOptimizationAnalysis.gameStates.GameState;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChessGameState implements GameState<ChessGameState> {
    @Override
    public GameSituation getGameSituation() {
        if (calcNextStates().noneMatch(s -> true)) {
            if (isChess(nextPlayer)) {
                return GameSituation.hasWon(nextPlayer.getOther());
            } else {
                return GameSituation.TIE;
            }
        }
        if(Arrays.stream(board)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .allMatch(ChessPiece::isKing)) {
            return GameSituation.TIE;
        }

        return GameSituation.RUNNING;
    }

    private static class Move {
        final Field start;
        final Field goal;

        public Move(Field start, Field goal) {
            this.start = start;
            this.goal = goal;
        }
    }

    private final ChessPiece[][] board;
    private final Player nextPlayer;

    public ChessGameState(ChessPiece[][] board, Player nextPlayer) {
        this.board = board;
        this.nextPlayer = nextPlayer;
    }

    public static class Field {
        private final int x;
        private final int y;

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
            .flatMap(field -> {
                ChessPiece piece = field.get(board);
                //System.out.println(piece);
                List<Field> options = piece.possibleGoals(field,board);
                return  options.stream().map(goal -> new Move(field, goal));
            });
    }

    private Stream<Integer> intStreamTo(int limit) {
        return Stream.iterate(0, i -> i+1).limit(limit);
    }

    @Override
    public Player getNextChoice() {
        return nextPlayer;
    }
    public ChessPiece[][] getBoard() {
        return board;
    }

    public boolean isChess(Player player) {
        return possibleMoves(player.getOther())
                .map(m -> m.goal.get(board))
                .filter(Objects::nonNull)
                .anyMatch(ChessPiece::isKing);
    }

    @Override
    public int getBoardWidth() {
        return board.length;
    }

    @Override
    public int getBoardHeight() {
        return board[0].length;
    }

    public  String toString() {
        StringBuilder b = new StringBuilder();
        for(ChessPiece[] row: board) {
            for(ChessPiece field: row) {
                b.append(field == null ? "_": field.toString());
            }
            b.append("|");
        }
        return b.toString();
    }
}
