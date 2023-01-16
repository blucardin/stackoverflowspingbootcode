package com.example.blackjack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
// import SimpMessagingTemplate

import reactor.core.publisher.Flux;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.Executors;

@CrossOrigin(origins = "*")
@Controller
public class homeController {

    static Connection connection = null;
    static Statement statement = null;

    @Autowired
    private SimpMessagingTemplate template;

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

        // create a new player in the database with id, name, and room id
        statement.executeUpdate("drop table if exists player");
        statement.executeUpdate("create table player (id integer primary key autoincrement, name text not null, room_id integer not null, foreign key(room_id) references room(id))");
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

    // create a mapping for the join page, when GET request is made adds the player to the player database with the id and return join.html, 
    @GetMapping("/game")
    public String join(Model model) throws SQLException {
        // create a new player in the database
        statement.executeUpdate("insert into player (name, room_id) values('player', 1)", Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = statement.getGeneratedKeys();
        System.out.println(rs.getString(1));
        model.addAttribute("id", rs.getString(1));//rs.getInt("id"));
        return "join";
    }

    // create a mapping for the game 
    @GetMapping("/join")
    public String game() {
        return "join";
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
