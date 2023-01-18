var personalCards = new EventSource("/personalcards?id=" + id);
var personalCardData = "";
personalCards.onmessage = function(event) {
    if (event.data != "" && event.data != personalCardData) {
        personalCardData = JSON.parse(event.data);
        console.log(personalCardData);

        var cards = personalCardData.cards;
        var parent = document.getElementById("cards");
        // clear the cards
        parent.innerHTML = "";

        for (var i = 0; i < cards.length; i++) {
            var div = document.createElement("div");
            div.innerHTML = cards[i];
            parent.appendChild(div);
        }

        var parent = document.getElementById("started");
        if (parent.innerHTML == "Game Not Started"){
            parent.innerHTML = "Game Started";
        }
    }
    else {
        console.log("No cards yet");
    }
};

var tableData = "";
var source = new EventSource("/getTable?id=" + id);
        source.onmessage = function(event) {

            if (event.data != "" && JSON.parse(event.data) != tableData) {
                tableData = JSON.parse(event.data);
                console.log(tableData);
                var parent = document.getElementById("table");
                // clear the table
                parent.innerHTML = "";

                var players = tableData["Players"];
                for (var key in players) {
                    var div = document.createElement("div");
                    div.innerHTML = players[key]["Username"] + ": ";
                    for (var i = 0; i < players[key]["Cards"].length; i++) {
                        div.innerHTML += players[key]["Cards"][i];
                        if (i != players[key]["Cards"].length - 1) {
                            div.innerHTML += ", ";
                        }
                    }
                    parent.appendChild(div);
                }

                if (tableData["Winner"] != [""]) {
                    console.log("Winner: " + tableData["Winner"][0]);
                    var parent = document.getElementById("winner");
                    var winningText = "";
                    for (var i = 0; i < tableData["Winner"].length; i++) {
                        if (i != 0) {
                            winningText += " and ";
                        }
                        winningText += tableData["Players"][tableData["Winner"][i]]["Username"];
                    }
                    parent.innerHTML = `
                    <h1>Winner: ${winningText}</h1>
                    <button onclick="window.location.href='/'">Back to Home Screen</button>
                    `
                    document.body.appendChild(parent);
                }
            }
            else {
                console.log("No table yet");
            }
        }

// poll for the current turn and update the page with the username that is currently playing
var turnData = "";
var turn = new EventSource("/getTurn?id=" + id);
turn.onmessage = function(event) {    
    if (event.data != "" && event.data != turnData) {
        turnData = event.data;
        console.log(turnData);
        var parent = document.getElementById("turn");
        parent.innerHTML = tableData["Players"][turnData]["Username"] + "'s Turn";
        if (event.data == sub) {
            var parent = document.getElementById("play");
            var child = document.createElement("div");
            child.innerHTML = `
            <button onclick="hit()">Hit</button>
            <button onclick="stand()">Stand</button>
                `
                parent.appendChild(child);
        }
    }
    else {
        console.log("No turn yet");
    }
}

function hit() {
    var hit = new EventSource("/hit?id=" + id);
    hit.onmessage = function(event) {
        console.log(event.data);
        hit.close();
    }
    deletePlay()
}

function stand() {
    var stand = new EventSource("/stand?id=" + id);
    stand.onmessage = function(event) {
        console.log(event.data);
        stand.close();
    }
    deletePlay()
}

function deletePlay() {
    var parent = document.getElementById("play");
    parent.innerHTML = "";
    turnData = "";
}