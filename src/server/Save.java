package server;

import java.io.Serializable;
import java.time.LocalDateTime;

import game.Game;

public class Save implements Serializable{
	private String saveName;
private String player1;
private String player2;
private Game game1;
private Game game2;
private LocalDateTime date;

public Save(String saveName,String player1,String player2,Game game1,Game game2,LocalDateTime date) {
this.saveName=saveName;
this.player1=player1;
this.player2=player2;
this.game1=game1;
this.game2=game2;
this.date=date;
}


public String getSaveName() {
	return saveName;
}

public String getPlayer1() {
	return player1;
}

public String getPlayer2() {
	return player2;
}

public Game getGame1() {
	return game1;
}

public Game getGame2() {
	return game2;
}

public LocalDateTime getDate() {
	return date;
}

}
