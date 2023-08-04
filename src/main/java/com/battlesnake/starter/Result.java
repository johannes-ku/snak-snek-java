package com.battlesnake.starter;

public class Result {
    public Move move;
    public int score;

    public Result(Move move, int score) {
        this.move = move;
        this.score = score;
    }

    public int compareTo(Result other) {
        if (this.score > other.score) {
            return 1;
        } else if (this.score < other.score) {
            return -1;
        } else {
            return 0;
        }
    }
}
