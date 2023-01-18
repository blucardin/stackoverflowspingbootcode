package com.example.blackjack;

public class Room {
    // create an engine object, and id attirbute, and a locked boolean
    public BlackJackEngine engine = new BlackJackEngine();
    public int id;
    public boolean locked;
    public String dealersub;
    public volatile int turn = 0;

    /**
     * Constructor for room class
     * @param id id of the room
     * @param dealersub dealer identification
     */
    Room(int id, String dealersub) {
        this.id = id;
        this.locked = false;
        this.dealersub = dealersub;
        this.turn = 0;
    }
}
