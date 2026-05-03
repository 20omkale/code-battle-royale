package com.codebattle;

import java.io.Serializable;

/**
 * Shared message protocol between Server and Client.
 * Demonstrates: Message Passing in Distributed Systems.
 */
public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        // Client -> Server
        JOIN, START, ANSWER,
        // Server -> Client
        WELCOME, PLAYER_LIST, QUESTION, TIMER, RESULT, GAMEOVER, ERROR, EVENT
    }

    public Type type;
    public String text;
    public String roomCode;
    public String[] options;
    public int number;
    public String[] names;
    public int[] scores;

    public GameMessage(Type t) { this.type = t; }
}
