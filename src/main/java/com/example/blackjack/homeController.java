package com.example.blackjack;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
// import SimpMessagingTemplate
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;

import java.util.ArrayList;

@CrossOrigin(origins = "*")
@Controller
public class homeController {

    static Connection connection = null;
    static Statement statement = null;
    public static volatile ArrayList<Room> rooms = new ArrayList<Room>();    ; // number of engines running

    @GetMapping("/home")
    public String index() {
        return "index";
    }

    @GetMapping("/create")
    public String create(Model model, OAuth2AuthenticationToken identifyer){
        // create a new room in the database
        //statement.execute("insert into room (locked, turn) values('false', 0)", Statement.RETURN_GENERATED_KEYS);
        // get the new id
        String sub = identifyer.getPrincipal().getAttributes().get("sub").toString();

        rooms.add(new Room(rooms.size(), sub));
        model.addAttribute("id", rooms.size() - 1);//rs.getInt("id"));
        return "create";
    }

    // create a post-only mapping to start the game
    @GetMapping("/start")
    public String start(Model model, String id, OAuth2AuthenticationToken identifyer) {

        String roomID = id;
        // get the dealer id
        String sub = identifyer.getPrincipal().getAttributes().get("sub").toString();

        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).id == Integer.parseInt(id) && rooms.get(i).dealersub.equals(sub)) {
                rooms.get(i).locked = true;
                rooms.get(i).engine.initializeGame();
                model.addAttribute("id", roomID);
                model.addAttribute("sub", sub);
                return "start";
            }
        }
        // if the room doesn't exist, return the home page
        return "index"; // TODO: Create Error Page
    }


    // create a mapping for the join page
    @GetMapping("/join")
    public String join(Model model)  {
        // return the join page
        return "join";
    }

    // create a mapping for the room page with the id as a parameter
    @GetMapping("/game")
    public String room(Model model, String id, OAuth2AuthenticationToken identifyer)  {
        // get the room from the database
        //ResultSet rs = statement.executeQuery("select * from room where id = " + id + " AND locked = 'false'");
        System.out.println("id: " + id);

        for (int i = 0; i < rooms.size(); i++) {
            System.out.println("room id: " + rooms.get(i).id);

            if (rooms.get(i).locked == false && rooms.get(i).id == Integer.parseInt(id)) {

                String roomID = id;

                String sub = identifyer.getPrincipal().getAttributes().get("sub").toString();

                rooms.get(i).engine.addPlayer(sub, 0); // TODO: Betting System

                //rooms.get(i).engine.addPlayer(sub, 0);
                model.addAttribute("id", roomID);
                
                model.addAttribute("sub", sub);

                // insert the player into the database
                // statement.executeUpdate("insert into player (id, room_id) values('" + sub + "', " + roomID + ")");

                return "game";
            }
        }
        // if the room doesn't exist, return the home page
        return "index"; // TODO: Create Error Page
    }

    @ResponseBody
    @GetMapping(value = "/personalcards", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter personalcards(String id, OAuth2AuthenticationToken identifyer) throws IOException{
        SseEmitter emitter = new SseEmitter();
        String sub = identifyer.getPrincipal().getAttributes().get("sub").toString();

        for (int i = 0; i < rooms.size(); i++) {

            if (rooms.get(i).id == Integer.parseInt(id)) {

                ArrayList<Player> players = rooms.get(i).engine.getPlayers();

                for (int j = 0; j < players.size(); j++) {

                    if (players.get(j).getName().equals(sub)) {

                        ArrayList<Card> hand = players.get(j).getHand();
                        String payload = "";
                        
                        if (hand.size() > 0) {
                            payload = "{ \"cards\": [";
                            // format the payload as an json array
                            for (int k = 0; k < hand.size(); k++) {
                                payload += "\"" + hand.get(k).getName() + "\"" ;
                                if (k != hand.size() - 1) {
                                    payload += ",";
                                }
                            }
                            payload += "]}";
                        }

                        emitter.send(payload);
                        emitter.complete();
                        return emitter;
                    }
                }
                emitter.send(""); // TODO: Configure these empty strings as error messages to be handled by the client
                emitter.complete();
                return emitter;
            }
        }
        emitter.send("");
        emitter.complete();
        return emitter;
    }

    @ResponseBody
    @GetMapping(value = "/getTable", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getTable(String id, OAuth2AuthenticationToken identifyer) throws IOException{
        SseEmitter emitter = new SseEmitter();
        String sub = identifyer.getPrincipal().getAttributes().get("sub").toString();

        for (int i = 0; i < rooms.size(); i++) {

            if (rooms.get(i).id == Integer.parseInt(id)) {

                ArrayList<Player> table = rooms.get(i).engine.getPlayers();
                String payload = "";
                
                if (table.size() > 0) {
                    payload = "{ \"table\": {";
                    // format the payload as an json array
                    for (int j = 0; j < table.size(); j++) {

                        payload += "\"" + table.get(j).getName() + "\": [" ;
                        payload += "\"" + "unknown" + "\"," ;
                        for (int k = 1; k < table.get(j).getHand().size(); k++) {
                            payload += "\"" + table.get(j).getHand().get(k).getName() + "\"" ;
                            if (k != table.get(j).getHand().size() - 1) {
                                payload += ",";
                            }
                        }
                        payload += "]";

                        if (j != table.size() - 1) {
                            payload += ",";
                        }
                    }
                    payload += "}}";

                    System.out.println(payload);

                }

                emitter.send(payload);
                emitter.complete();
                return emitter;
            }
        }
        emitter.send("");
        emitter.complete();
        return emitter;
    }
    
}
