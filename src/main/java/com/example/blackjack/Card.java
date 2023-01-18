/*
    This is a class that holds the values for a card along with their name.
    It allows developers to easily create and manipulate the values of a card. 
    This class is built around the rules of a BlackJack Game and it's card values. 
    But it can be easily modulated to accomodate other card games

    Programmed by Aaron Avram
    Date Programmed: January 21 2023
*/

package com.example.blackjack;


import java.util.Arrays;
import java.util.Map;

class Card {
    private String suit;
    private int rank;
    private boolean aceValueSwitch = false;

    /**
     * Constructor for card class
     * @param suit suit of the card
     * @param rank rank of the card
     */
    Card(String suit, int rank) {
        this.suit = suit;
        this.rank = rank;
    }

    /**
     * Sets the value of the card's suit
     * @param suit
     */
    public void setSuit(String suit) {
        this.suit = suit;
    }

    /**
     * Sets the value of the card's rank
     * @param rank
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Sets the value of the ace's value switch
     * @param aceValueSwitch
     */
    public void setAceValueSwitch(boolean aceValueSwitch) {
        this.aceValueSwitch = aceValueSwitch;
    }

    /**
     * Gets the value of the card's suit
     * @return the card's suit
     */
    public String getSuit() {
        return suit;
    }

    /**
     * Gets the value of the card's rank
     * @return the card's rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Gets the value of the ace value switch
     * @return whether or not the value of the ace should be switched
     */
    public boolean getAceValueSwitch() {
        return aceValueSwitch;
    }

    /**
     * Combines the suit and rank of the card to get it's name
     * @return the name of the card
     */
    public String getName() {
        Map<Integer, String> numbersInWords = Map.of(2, "Two", 3, "Three", 4, "Four", 5, "Five", 6, "Six", 7, "Seven", 8, "Eight", 9, "Nine", 10, "Ten");

        if (rank == 11) {
            return "Jack of " + suit;
        } else if (rank == 12) {
            return "Queen of " + suit; 
        } else if (rank == 13) {
            return "King of " + suit;
        } else if (rank == 1) {
            return "Ace of " + suit;
        } else {
            return numbersInWords.get(rank) + " of " + suit;
        }
    }

    /**
     * Gets the value of the card's points
     * @return card's points
     */
    public int getPoints() {
        if (Arrays.asList(11, 12, 13).contains(rank)) {
            return 10;
        } else if (rank == 1 && aceValueSwitch == true) {
            return 11;
        } else {
            return rank;
        }
    }
    
}
