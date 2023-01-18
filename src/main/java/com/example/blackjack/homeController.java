package com.example.blackjack;

import org.json.JSONArray;
import org.json.JSONObject;
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

import com.example.blackjack.Player.Status;

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
                String name = identifyer.getPrincipal().getAttributes().get("name").toString();

                rooms.get(i).engine.addPlayer(sub, name, 0); // TODO: Betting System

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
                JSONObject jo = new JSONObject();
                
                if (table.size() > 0) {
                    for (int j = 0; j < table.size(); j++) {
                        JSONObject player = new JSONObject();
                        JSONArray cards = new JSONArray();
                        cards.put("unknown");
                        for (int k = 1; k < table.get(j).getHand().size(); k++) {
                            cards.put(table.get(j).getHand().get(k).getName());
                        }
                        player.put("Cards", cards);
                        player.put("Username", table.get(j).getUsername());
                        player.put("Status", table.get(j).getStatus());
                        jo.put(table.get(j).getName(), player);
                    }
                    payload = jo.toString();
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

    @ResponseBody
    @GetMapping(value = "/getTurn", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getTurn(String id) throws IOException{
        SseEmitter emitter = new SseEmitter();

        for (int i = 0; i < rooms.size(); i++) {

            if (rooms.get(i).id == Integer.parseInt(id)) {

                if (rooms.get(i).locked == false) {
                    emitter.send("");
                    emitter.complete();
                    return emitter;
                }

                int turn = rooms.get(i).turn;
                ArrayList<Player> players = rooms.get(i).engine.getPlayers();
                String payload = players.get(players.size() - turn - 1).getName();

                System.out.println("turn: " + payload);
                if (payload == "Dealer"){
                    rooms.get(i).engine.dealerTurn();
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

    @ResponseBody
    @GetMapping(value = "/hit", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter hit(String id, OAuth2AuthenticationToken identifyer) throws IOException{
        SseEmitter emitter = new SseEmitter();
        String sub = identifyer.getPrincipal().getAttributes().get("sub").toString();

        for (int i = 0; i < rooms.size(); i++) {

            if (rooms.get(i).id == Integer.parseInt(id)) {

                int turn = rooms.get(i).turn;
                ArrayList<Player> players = rooms.get(i).engine.getPlayers();
                Player hittingplayer = players.get(players.size() - turn - 1);

                if (hittingplayer.getName() == sub){
                    if (hittingplayer.getStatus() == Status.PLAYING) {

                        hittingplayer.hit(rooms.get(i).engine.dealCard());

                        if (hittingplayer.getStatus() == Status.BUST) {
                            rooms.get(i).turn++;
                        }

                        emitter.send("Hit Succsessful");
                        emitter.complete();
                        return emitter;
                    }
                }
            }
        }
        emitter.send("Hit FAILUE");
        emitter.complete();
        return emitter;
    }

    @ResponseBody
    @GetMapping(value = "/stand", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stand(String id, OAuth2AuthenticationToken identifyer) throws IOException{
        SseEmitter emitter = new SseEmitter();
        String sub = identifyer.getPrincipal().getAttributes().get("sub").toString();

        for (int i = 0; i < rooms.size(); i++) {

            if (rooms.get(i).id == Integer.parseInt(id)) {

                int turn = rooms.get(i).turn;
                ArrayList<Player> players = rooms.get(i).engine.getPlayers();
                Player standingplayer = rooms.get(i).engine.getPlayers().get(players.size() - turn - 1);
                if (standingplayer.getName() == sub){
                    if (standingplayer.getStatus() == Status.PLAYING) {
                        standingplayer.stand();
                        rooms.get(i).turn++;
                    }
                }
                break;
            }
        }

        emitter.send("Stand Succsessful");
        emitter.complete();
        return emitter;
    }

    
}
