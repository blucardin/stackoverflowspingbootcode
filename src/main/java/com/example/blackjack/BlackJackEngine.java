/* 
    This is a BlackJack Engine that offers a programmer all of the tools 
    to create a BlackJack game. An object of this class stores all of the 
    information in the game and allows developers to dynamically access
    and modify them.

    Programmed by Aaron Avram
    Date Programmed: January 21 2023
 */ 

 // TO DO: ask rob about the documentation for methods

package com.example.blackjack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class BlackJackEngine {
    private ArrayList<Player> players = new ArrayList<>(Arrays.asList(new Player("Dealer", "Dealer", 0)));
    private ArrayList<Card> deck = new ArrayList<>();

    /**
     * Sets the value of the list of players
     * @param players the list of players in the game
     */
    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    /**
     * Sets the value of the deck
     * @param deck list of cards in the game
     */
    public void setDeck(ArrayList<Card> deck) {
        this.deck = deck;
    }

    /**
     * Gets the value of the list of players
     * @return list of players
     */
    public ArrayList<Player> getPlayers() {
        return players;
    }

    /**
     * Gets the value of the deck
     * @return list of cards in the deck
     */
    public ArrayList<Card> getDeck() {
        return deck;
    }

    /**
     * Generates the cards in the deck
     */
    private void generateDeck() {
        String[] suits = {"Spades", "Clubs", "Hearts", "Diamonds"};
        for (String i: suits) {
            for (int j = 1; j < 14; j++) {
                deck.add(new Card(i, j));
            }
        }
    }

    /**
     * Generates a BlackJack deck with any given amount of regular card decks in it
     * @param numberOfdecks number of standard decks in the BlackJack deck
     */
    private void generateDeck(int numberOfdecks) {
        String[] suits = {"Spades", "Clubs", "Hearts", "Diamonds"};
        for (int k = 0; k < numberOfdecks; k++) {
            for (String i: suits) {
                for (int j = 1; j < 14; j++) {
                    deck.add(new Card(i, j));
                }
            }
        }
    }

    /**
     * Adds a player to the current list of players
     * @param name name of the player
     * @param username username of the player
     * @param bet bet of the player
     */
    public void addPlayer(String name, String username, int bet) {
        Player player = new Player(name, username, bet);
        players.add(player);
    }

    /**
     * Returns the value of the element of a list of cards at a given index while also erasing it from the list
     * @param list list to get card from
     * @param index index of element
     * @return popped element from list
     */
    private Card pop(ArrayList<Card> deck, int index) {
        Card value = deck.get(index);
        deck.remove(index);

        return value;
    }

    /**
     * Gets a card out of the deck and removes it
     * @return the card popped from the deck
     */
    public Card dealCard() {
        Random numGenerator = new Random();
        return pop(deck, numGenerator.nextInt(0, deck.size()));

    }

    /**
     * Gets a specifed number of cards from the deck and removes them from the deck
     * @param numCards number of cards to remove
     * @return the cards popped from the deck
     */
    public Card[] dealCard(int numCards) {
        Random numGenerator = new Random();
        Card[] cards = new Card[numCards];

        for (int i = 0; i < numCards; i++) {
            cards[i] = pop(deck, numGenerator.nextInt(0, deck.size()));
        }
        return cards;

    }

    /**
     * Sets the first two hands of each player in the game and returns all the cards dealt.
     * Uses only one deck
     */
    public Card[][] initializeGame() {
        generateDeck();

        Card[][] listOfCards = new Card[2][players.size()];
        Card[] cards = dealCard(players.size()*2);

        // Gives all players their first two hands
        for (int j = 0; j < players.size(); j++) {
            for (int i = 0; i < 2; i++) {    
                
                int k = 0;
                if (i == 0) {
                    k = j;
                } else {
                    k = j + players.size();
                }
        
                listOfCards[i][j] = cards[k];
                players.get(j).getHand().add(cards[k]);
            }
        }

        return listOfCards;
    }

    /**
     * Sets the first two hands of each player in the game and returns all the cards dealt. 
     * Uses a non standard card deck with at least 2 regular card decks in it
     */
    public Card[][] initializeGame(int numDecks) {
        generateDeck(numDecks);

        Card[][] listOfCards = new Card[2][players.size()];
        Card[] cards = dealCard(players.size()*2);

        // Gives all players their first two hands
        for (int j = 0; j < players.size(); j++) {
            for (int i = 0; i < 2; i++) {    
                
                int k = 0;
                if (i == 0) {
                    k = j;
                } else {
                    k = j + players.size();
                }
        
                listOfCards[i][j] = cards[k];
                players.get(j).getHand().add(cards[k]);
            }
        }

        return listOfCards;
    }


    /**
     * Plays the dealers turn
     */
    public void dealerTurn() {
        Player dealer = players.get(0);
        ArrayList<Card> aces = dealer.findAces();

        // If dealer has ace
        if (aces.size() == 1) {
            if (dealer.getPoints() > 6 && dealer.getPoints() < 11) {
                aces.get(0).setAceValueSwitch(true);
            } 
        } else if (aces.size() > 1) {
            aces.get(0).setAceValueSwitch(true);
        }

        // If dealer has hand higher than 16 he stands
        if (dealer.getPoints() > 16) {
            dealer.stand();
        } 
        
        // If dealer has hand lower than 16 he takes a card
        else {
            dealer.hit(dealCard());
        }
    }

    /**
     * Checks the state of the game currently and if dealer has busted all players who are still playing are automatically stood
     * @return true or false
     */
    public boolean keepPlaying() {

        // If dealer busts game is over
        if (players.get(0).getStatus() == Player.Status.BUST) {
            for (int i = 1; i < players.size(); i++) {

                // change the status of players whose current status is playing to stood
                if (players.get(i).getStatus() == Player.Status.PLAYING) {
                    players.get(i).setStatus(Player.Status.STOOD);
                }
            }
            return false;

        } else {

            int numPlayersPlaying = players.size();

            // Check how many players are still playing
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
    
                if (player.getStatus() != Player.Status.PLAYING) {
                    numPlayersPlaying--;
                }
            }

            // If no players are playing game is over
            if (numPlayersPlaying == 0) {
                return false;
            } else {
                return true;
            }
        }
    }
}
