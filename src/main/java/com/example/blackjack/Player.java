/* 
    This is the class that holds the values for a players hand, their bets, 
    their name and their in-game status for a BlackJack game. This class also 
    offers multiple methods involved in the manipulation of a player's hand 
    and status in BlackJack such as hit or stand.

    Programmed by Aaron Avram
    Date Programmed: January 21 2023
*/ 

package com.example.blackjack;

import java.util.ArrayList;

public class Player {
    enum Status{STOOD, BUST, PLAYING};
    private ArrayList<Card> hand = new ArrayList<>();
    private int bet;
    private String name;
    private Status status = Status.PLAYING;
    

    /**
     * Constructor for player class intializing only name and bet
     * @param name name of the player
     * @param bet bet of the player
     */
    Player(String name, int bet) {
        this.name = name;
        this.bet = bet;
    }

    /**
     * Sets the player's hand
     * @param hand player's new hand
     */
    public void setHand(ArrayList<Card> hand) {
        this.hand = hand;
    }

    /**
     * Sets the player's bet for the game
     * @param bet player's new bet
     */
    public void setBet(int bet) {
        this.bet = bet;
    }

    /**
     * Sets the player's name for the game
     * @param name player's new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set's the player's status
     * @param status player's new status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Gets the player's current hand
     * @return player's hand
     */
    public ArrayList<Card> getHand() {
        return hand;
    }

    /**
     * Gets the player's bet
     * @return player's bet
     */
    public double getBet() {
        return bet;
    }

    /**
     * Gets the player's name
     * @return player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the player's current status
     * @return player's status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Adds a card to the players hand and if their new point total is above 21 sets their status to bust
     * @param card card to add to player's hand
     */
    public void hit(Card card) {
        hand.add(card);
        
        if (getPoints() > 21) {
            status = Status.BUST;
        }
    }

    /**
     * Card sum is calculated
     * @return sum of the cards in players hand.
     */
    public int getPoints() {
        int sum = 0;

        for (int i = 0; i < hand.size(); i++) {
            sum = sum + hand.get(i).getPoints();
        }

        return sum;
    }

    /**
     * Sets status to Stood
     */
    public void stand() {
        status = Status.STOOD;
    }

    /**
     * Checks the player's cards to see if they have a blackjack
     * @return true or false
     */
    public boolean isBlackJack() {
        if (hand.size() == 2 && getPoints() == 21) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks the player's cards to see if they have any aces
     * @return a list of the aces the player has
     */
    public ArrayList<Card> findAces() {
        ArrayList<Card> aces = new ArrayList<>();

        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getRank() == 1) {
                aces.add(hand.get(i));
            }
        }

        return aces;
    }

    /**
     * Checks if the player is still in the game
     * @return true or false
     */
    public boolean keepPlaying() {
        if (status == Status.PLAYING) {
            return true;
        } else {
            return false;
        }
    }

}

