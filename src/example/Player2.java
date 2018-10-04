package example;

import client.GameClient;

public class Player2 {
	
	public static void main(String[] args) throws InterruptedException {
		GameClient player2=new GameClient("Player2");
		player2.connect();
	}

}
