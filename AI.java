/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.Random;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/**
 * A Player that computes its own moves.
 *
 * @author Alex Lee
 */
class AI extends Player {

    /**
     * Maximum minimax search depth before going to static evaluation.
     */
    private static final int MAX_DEPTH = 3;
    /**
     * A position magnitude indicating a win (for red if positive, blue
     * if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     * a random-number generator for use in move computations.  Identical
     * seeds produce identical behaviour.
     */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to the findMove method
     * above.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _foundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _foundMove. If the game is over
     * on BOARD, does not set _foundMove.
     */
    private int minMax(Board board, int depth,
                       boolean saveMove, int sense, int alpha, int beta) {
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        int bestScore;
        bestScore = 0;

        java.util.ArrayList<Move> allMoves = findAllMove(board);
        bestScore =  -sense * INFTY;
        for (Move eachMove : allMoves) {
            if (sense == 1) {
                board.makeMove(eachMove);
                int response = minMax(board, depth - 1, false, -1, alpha, beta);
                if (response > bestScore) {
                    bestScore = response;
                    alpha = max(alpha, bestScore);
                    if (alpha >= beta) {
                        board.undo();
                        return bestScore;
                    }
                    if (saveMove) {
                        _lastFoundMove = eachMove;
                    }
                }
                board.undo();
            } else {
                board.makeMove(eachMove);
                int response = minMax(board, depth - 1, false, 1, alpha, beta);
                if (response < bestScore) {
                    bestScore = response;
                    beta = min(beta, bestScore);
                    if (alpha >= beta) {
                        board.undo();
                        return bestScore;
                    }
                    if (saveMove) {
                        _lastFoundMove = eachMove;
                    }
                }
                board.undo();
            }
        }
        return bestScore;
    }

    private java.util.ArrayList<Move> findAllMove(Board board) {
        java.util.ArrayList<Move> allMoves = new java.util.ArrayList<Move>();
        for (char colCounter = 'a';
             colCounter <= 'g'; colCounter++) {
            for (char rowCounter = '1';
                 rowCounter <= '7'; rowCounter++) {
                if (board.get(colCounter, rowCounter)
                        == board.whoseMove()) {
                    for (int boundColCounter = colCounter - 2;
                         boundColCounter <= colCounter + 2;
                         boundColCounter++) {
                        for (int boundRowCounter = rowCounter - 2;
                             boundRowCounter <= rowCounter + 2;
                             boundRowCounter++) {
                            Move potentialMove = Move.move(colCounter,
                                    rowCounter, (char) boundColCounter,
                                    (char) boundRowCounter);
                            if (board.legalMove(potentialMove)) {
                                allMoves.add(potentialMove);
                            }
                        }
                    }
                }
            }
        }
        if (!board.canMove(board.whoseMove())) {
            allMoves.add(Move.pass());
        }
        return allMoves;
    }

    /**
     * Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     * won positions, and 0 for ties.
     */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        }

        return board.redPieces() - board.bluePieces();
    }

    /**
     * Pseudo-random number generator for move computation.
     */
    private Random _random = new Random();
}
