package example;


import client.GameClient;

public class Player1 {
	
	public static void main(String[] args) throws InterruptedException{
		GameClient player1=new GameClient("Player1");
		player1.connect();
	}
}
