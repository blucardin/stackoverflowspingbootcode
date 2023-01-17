package com.example.blackjack;

public class Room {
    // create an engine object, and id attirbute, and a locked boolean
    public BlackJackEngine engine = new BlackJackEngine();
    public int id;
    public boolean locked;

    /**
     * Constructor for room class
     * @param id id of the room
     */
    Room(int id) {
        this.id = id;
        this.locked = false;
    }
}
