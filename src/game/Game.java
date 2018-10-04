package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

public class Game implements Serializable{
	private GameBoard yourBoard;
	private GameBoard enemyBoard;

	public Game() {
		yourBoard = new GameBoard();
		enemyBoard = new GameBoard();
	}

	public void placeShips(PrintWriter pw, BufferedReader br) throws IOException {
		yourBoard.printBoard(pw);
		placeShip(pw, br, 5, 1);
		placeShip(pw, br, 4, 2);
		placeShip(pw, br, 3, 3);
		placeShip(pw, br, 2, 4);
	}

	public void placeShip(PrintWriter pw, BufferedReader br, int shipLength, int shipCount) {
		int counter = 0;
		String field, direction;
		try {
		emptyBr(br);
		while (counter != shipCount) {
			pw.println("Type starting point of " + shipLength + "-celled ship :");
			pw.flush();
			field = br.readLine();
			pw.println("Type direction(east,west,north,south) :");
			pw.flush();
			direction = br.readLine();
			if (yourBoard.placeShip(field, shipLength, direction)) {
				yourBoard.printBoard(pw);
				counter++;
			} else {
				pw.println("Invalid cell!");
			}
			pw.flush();
		}
		}catch(IOException e){
			System.out.println("Player disconnected!");
			return;
		}
	}

	public GameBoard getYourBoard() {
		return yourBoard;
	}

	public GameBoard getEnemyBoard() {
		return enemyBoard;
	}

	public boolean hitEnemy(String field, char a,PrintWriter pw,GameBoard enemyRealBoard) {
		return enemyBoard.hit(field, a,pw,enemyRealBoard);
	}

	public int shoot(PrintWriter pw, BufferedReader br, String field, Game enemyGame,int shipHits) {
		if (enemyGame.getYourBoard().checkForShip(field)) {
			shipHits++;
			if (hitEnemy(field, 'X',pw,enemyGame.getYourBoard())) {
			} else {
				pw.println("Invalid cell!");
			}
		} else {
			if (hitEnemy(field, '0',pw,enemyGame.getYourBoard())) {
				pw.println("Empty field!");
			} else {
				pw.println("Invalid cell!");
			}
		}
		pw.flush();
		return shipHits;
	}

	public void emptyBr(BufferedReader br) throws IOException {
			while (br.ready() && br.readLine()!=null) {
			}
	}
	
	public void printBoards(PrintWriter pw) {
		pw.println("\tYOUR BOARD");
		yourBoard.printBoardWithLegend(pw);
		pw.println();
		pw.println("\tENEMY BOARD");
		enemyBoard.printBoard(pw);
	}
}
