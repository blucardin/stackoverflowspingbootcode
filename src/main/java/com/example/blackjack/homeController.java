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

import reactor.core.publisher.Flux;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

import java.util.Map;

@CrossOrigin(origins = "*")
@Controller
public class homeController {

    static Connection connection = null;
    static Statement statement = null;
    public static volatile int engines = 0; // number of engines running

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() throws ClassNotFoundException, SQLException {  
        // create a database connection
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        statement = connection.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec.

        statement.executeUpdate("drop table if exists room");
        // create room table with text locked, id int autoincrementing, and turn int
        statement.executeUpdate("create table room (id integer primary key autoincrement, locked text not null, turn integer)");

        statement.executeUpdate("drop table if exists player");
        // create player table with text name, id int, and room_id int
        statement.executeUpdate("create table player (id integer primary key, room_id integer)");
      }

    @GetMapping("/home")
    public String index() {
        return "index";
    }

    @GetMapping("/create")
    public String create(Model model) throws SQLException {
        // create a new room in the database
        //statement.execute("insert into room (locked, turn) values('false', 0)", Statement.RETURN_GENERATED_KEYS);
        // get the new id

        statement.executeUpdate("insert into room (locked, turn) values('false', 0)", Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = statement.getGeneratedKeys();
        // }
        System.out.println(rs.getString(1));
        model.addAttribute("id", rs.getString(1));//rs.getInt("id"));
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
        ResultSet rs = statement.executeQuery("select * from room where id = " + id + " AND locked = 'false'");
        // if the room exists
        if (rs.next()) {

            model.addAttribute("id", rs.getString("id"));
            
            model.addAttribute("sub", identifyer.getPrincipal().getAttributes().get("sub").toString());

            return "game";
        }
        // if the room doesn't exist, return the home page
        return "index";
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
