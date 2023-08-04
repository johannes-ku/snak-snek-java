package com.battlesnake.starter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.battlesnake.starter.Move.*;

public class Service {

    private static final int DEPTH = 0;
    private static final int DEATH_SCORE = Integer.MIN_VALUE;
    private static final int FOOD_SCORE = 100;
    private static final int FOOD_HOTNESS_DEPTH = 6;

    public Move move(MoveRequest moveRequest) {
        Result result = minimax(moveRequest, 0).stream().max(Result::compareTo).get();
        System.out.println("returning " + result.move + " with score " + result.score);
        return result.move;
    }

    private List<Result> minimax(MoveRequest moveRequest, int currentDepth) {
        if (currentDepth == DEPTH) {
            List<Result> results = new ArrayList<>();
            results.add(new Result(UP, getScore(moveRequest, UP)));
            results.add(new Result(DOWN, getScore(moveRequest, DOWN)));
            results.add(new Result(LEFT, getScore(moveRequest, LEFT)));
            results.add(new Result(RIGHT, getScore(moveRequest, RIGHT)));
            return results;
        } else {
            return new ArrayList<>();
        }
    }

    private int getScore(MoveRequest moveRequest, Move move) {
        // check for borders
        if (moveRequest.you.head.y == 0 && move == DOWN) {
            return DEATH_SCORE;
        } else if (moveRequest.you.head.y + 1 == moveRequest.board.width && move == UP) {
            return DEATH_SCORE;
        } else if (moveRequest.you.head.x == 0 && move == LEFT) {
            return DEATH_SCORE;
        } else if (moveRequest.you.head.x + 1 == moveRequest.board.height && move == RIGHT) {
            return DEATH_SCORE;
        }

        byte[][] boardScores = getBoardScores(moveRequest);
        Coordinate newCoordinate = getNewCoordinates(moveRequest.you.head, move);

        return boardScores[newCoordinate.x][newCoordinate.y];


    }

    private Coordinate getNewCoordinates(Coordinate current, Move move) {
        Coordinate newCoordinate = new Coordinate(current.x, current.y);
        if (move == UP) {
            newCoordinate.y += 1;
        } else if (move == DOWN) {
            newCoordinate.y -= 1;
        } else if (move == LEFT) {
            newCoordinate.x -= 1;
        } else if (move == RIGHT) {
            newCoordinate.x += 1;
        }
        return newCoordinate;
    }

    private byte[][] getBoardScores(MoveRequest moveRequest) {
        byte[][] board = new byte[moveRequest.board.height][moveRequest.board.width];
        for (BattleSnake snek : moveRequest.board.snakes) {
            board[snek.head.x][snek.head.y] = Byte.MIN_VALUE;
            for (int i = 0; i < snek.body.size(); i++) {
                byte score = (byte) (Byte.MIN_VALUE + i + 1);
                Coordinate bodypart = snek.body.get(i);
                board[bodypart.x][bodypart.y] = score;
            }
            if (!snek.id.equals(moveRequest.you.id) && snek.body.size() + 1 >= moveRequest.you.body.size()) {
                List<Hotness> neighbours = getNeighbours(snek.head, moveRequest.board, (byte) 1, (byte) 2);
                for (Hotness neighbour : neighbours) {
                    if (board[neighbour.coordinate.x][neighbour.coordinate.y] == 0) {
                        board[neighbour.coordinate.x][neighbour.coordinate.y] = (byte) ((Byte.MIN_VALUE / 2) / neighbour.depth);
                    }
                }
            }

        }
        for (Coordinate snak : moveRequest.board.food) {
            board[snak.x][snak.y] = Byte.MAX_VALUE;
            List<Hotness> neighbours = getNeighbours(snak, moveRequest.board, (byte) 1, (byte) FOOD_HOTNESS_DEPTH);
            for (Hotness neighbour : neighbours) {
                byte myHotness = (byte) (127 / neighbour.depth);
                if (board[neighbour.coordinate.x][neighbour.coordinate.y] >= 0 && board[neighbour.coordinate.x][neighbour.coordinate.y] < myHotness) {
                    board[neighbour.coordinate.x][neighbour.coordinate.y] = myHotness;
                }
            }
        }
        board = getBoardWithBorderColdness(board, (byte) 4);
        printHotness(board);
        return board;
    }

    private void printHotness(byte[][] board) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            byte[] row = board[i];
            for (int j = 0; j < row.length; j++) {
                stringBuilder.append(String.format("%1$4s", row[j]));
            }
            stringBuilder.append("\n");
        }
        System.out.println(stringBuilder.toString());
    }

    private byte[][] getBoardWithBorderColdness(byte[][] board, byte startingColdness) {
        for (int i = 0; i < board.length; i++) {
            byte[] row = board[i];
            for (int j = 0; j < row.length; j++) {
                if (board[i][j] == 0) {
                    Integer distanceFromBorder = getDistanceFromBorder(board.length, board[0].length, i, j);
                    if (distanceFromBorder <= startingColdness) {
                        board[i][j] = (byte) ((byte) (startingColdness - distanceFromBorder) * -1);
                    }
                }
            }
        }
        return board;
    }

    private Integer getDistanceFromBorder(int boardHeight, int boardWidth, int x, int y) {
        List<Integer> ints = new ArrayList<>();
        ints.add(x);
        ints.add(y);
        ints.add(boardWidth - y);
        ints.add(boardHeight - x);
        return Collections.min(ints);
    }

    private List<Hotness> getNeighbours(Coordinate coordinate, Board board, byte depth, byte maxDepth) {
        if (depth == FOOD_HOTNESS_DEPTH) {
            return new ArrayList<>();
        }
        List<Hotness> neighbours = new ArrayList<>();
        if (coordinate.y > 0) {
            Coordinate coordinate1 = new Coordinate(coordinate.x, coordinate.y - 1);
            neighbours.add(new Hotness(coordinate1, depth));
            neighbours.addAll(getNeighbours(coordinate1, board, (byte) (depth + 1), maxDepth));
        }
        if (coordinate.y < board.height - 1) {
            Coordinate coordinate1 = new Coordinate(coordinate.x, coordinate.y + 1);
            neighbours.add(new Hotness(coordinate1, depth));
            neighbours.addAll(getNeighbours(coordinate1, board, (byte) (depth + 1), maxDepth));
        }
        if (coordinate.x > 0) {
            Coordinate coordinate1 = new Coordinate(coordinate.x - 1, coordinate.y);
            neighbours.add(new Hotness(coordinate1, depth));
            neighbours.addAll(getNeighbours(coordinate1, board, (byte) (depth + 1), maxDepth));
        }
        if (coordinate.x < board.height - 1) {
            Coordinate coordinate1 = new Coordinate(coordinate.x + 1, coordinate.y);
            neighbours.add(new Hotness(coordinate1, depth));
            neighbours.addAll(getNeighbours(coordinate1, board, (byte) (depth + 1), maxDepth));
        }
        return neighbours;
    }
}