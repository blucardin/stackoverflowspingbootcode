package com.example.blackjack;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
// import SimpMessagingTemplate
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
    public String create(Model model) throws SQLException {
        // create a new room in the database
        //statement.execute("insert into room (locked, turn) values('false', 0)", Statement.RETURN_GENERATED_KEYS);
        // get the new id

        rooms.add(new Room(rooms.size()));
        model.addAttribute("id", rooms.size() - 1);//rs.getInt("id"));
        return "create";
    }

    // create a mapping for the join page
    @GetMapping("/join")
    public String join(Model model) throws SQLException {
        // return the join page
        return "join";
    }

    // create a mapping for the room page with the id as a parameter
    @GetMapping("/game")
    public String room(Model model, String id, OAuth2AuthenticationToken identifyer) throws SQLException {
        // get the room from the database
        //ResultSet rs = statement.executeQuery("select * from room where id = " + id + " AND locked = 'false'");
        System.out.println("id: " + id);

        for (int i = 0; i < rooms.size(); i++) {
            System.out.println("room id: " + rooms.get(i).id);

            if (rooms.get(i).locked == false && rooms.get(i).id == Integer.parseInt(id)) {

                String roomID = id;

                String sub = identifyer.getPrincipal().getAttributes().get("sub").toString();

                //rooms.get(i).engine.addPlayer(sub, 0);
                model.addAttribute("id", roomID);
                
                model.addAttribute("sub", sub);

                // insert the player into the database
                // statement.executeUpdate("insert into player (id, room_id) values('" + sub + "', " + roomID + ")");

                return "game";
            }
        }
        // if the room doesn't exist, return the home page
        return "index"; // TOTO: Create Error Page
    }

    @ResponseBody
    @GetMapping(value = "/cards", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter cards(String id) throws IOException{
        SseEmitter emitter = new SseEmitter();
        System.out.println("Direct Hit!!!");
        emitter.send("cards" + id);
        emitter.complete();
        return emitter;
    }

    @GetMapping("/output")
    public String output() {
        return "output";
    }

    // create a players mapping that uses server side events with Flux to send the players in the room to the client
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> handle() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "Hello, " + i + "!")
                .doOnSubscribe(s -> System.out.println("Subscribed"))
                .doOnComplete(() -> System.out.println("Completed"));
    }
}
