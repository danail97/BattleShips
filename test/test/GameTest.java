package test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

import org.junit.Test;

import game.Game;

public class GameTest {
	
	private Game game;
	
	public GameTest() {
		game=new Game();
	}

	@Test
	public void shootingEmptyFieldShoudNotIncreaseShipHits(){
		BufferedReader br;
		PrintWriter pw;
		try {
			br = new BufferedReader(new FileReader("test1.txt"));
			pw =new PrintWriter("test.txt");
			Game enemyGame=new Game();
			assertEquals(5,game.shoot(pw, br,"A1", enemyGame, 5));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
