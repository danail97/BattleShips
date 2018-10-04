package game;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GameBoard implements Serializable{
	private char[][] board;
	private Map<ShipPair, Ship> ships;

	public GameBoard(){
		board = new char[13][24];
		ships = new HashMap<>();
		buildBoard();
	}

	public Map<ShipPair, Ship> getShips() {
		return ships;
	}

	private void buildBoard() {
		for (int j = 3; j < 23; j += 2) {
			board[0][j] = (char) (j / 2 + '0');
			board[j / 2 + 1][0] = (char) (j / 2 - 1 + 'A');
		}
		board[0][21] = '1';
		board[0][22] = '0';
		for (int i = 1; i < 12; i++) {
			for (int j = 2; j < 23; j += 2) {
				board[i + 1][j] = '|';
				board[i][j + 1] = '_';
			}
		}
	}

	public void printBoard(PrintWriter pw) {
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 23; j++) {
				pw.print(board[i][j]);
			}
			pw.println();
		}
	}
	
	public void printRow(int numberOfRow,PrintWriter pw) {
		for(int j=0;j<23;j++) {
			pw.print(board[numberOfRow][j]);
			}
	}
	
	public void printBoardWithLegend(PrintWriter pw) {
		printRow(0,pw);
		pw.println();
		printRow(1,pw);
		pw.println();
		printRow(2,pw);
		pw.print("\t\t\tLegend:");
		pw.println();
		printRow(3,pw);
		pw.print("\t\t\t\t#-ship field");
		pw.println();
		printRow(4,pw);
		pw.print("\t\t\t\tX-hit ship field");
		pw.println();
		printRow(5,pw);
		pw.print("\t\t\t\t0-hit empty field");
		pw.println();
		for(int i=6;i<12;i++) {
		printRow(i,pw);
		pw.println();
		}
	}

	public char[][] getBoard() {
		return board;
	}

	public int validateTurnX(String field) {
		if (field.length() > 1) {
			switch (field.charAt(0)) {
			case 'A': {
				return 2;
			}
			case 'B': {
				return 3;
			}
			case 'C': {
				return 4;
			}
			case 'D': {
				return 5;
			}
			case 'E': {
				return 6;
			}
			case 'F': {
				return 7;
			}
			case 'G': {
				return 8;
			}
			case 'H': {
				return 9;
			}
			case 'I': {
				return 10;
			}
			case 'J': {
				return 11;
			}
			default: {
				return -1;
			}
			}
		}else {
			return -1;
		}
	}

	public int validateTurnY(String field) {
		if (field.length() == 2 && field.charAt(1) > '0' && field.charAt(1) <= '9') {
			return (field.charAt(1) - '0') * 2 + 1;
		} else if (field.length() == 3 && field.charAt(1) == '1' && field.charAt(2) == '0') {
			return 21;
		} else {
			return -1;
		}
	}
	
	public boolean isValid(String field) {
		int x=validateTurnX(field);
		int y=validateTurnY(field);
		return (x!=-1 && y!=-1);
	}

	public boolean hit(String field, char a, PrintWriter pw, GameBoard enemyRealBoard) {
		int x = validateTurnX(field);
		int y = validateTurnY(field);
		if (x != -1 && y != -1 && board[x][y]=='_') {
			board[x][y] = a;
			enemyRealBoard.getBoard()[x][y] = a;
			if (a == 'X') {
				pw.println("Ship hit!");
				ShipPair shipPair = new ShipPair(x, y);
				enemyRealBoard.getShips().get(shipPair).decreaseCells();
				destroyedMsg(pw, shipPair, enemyRealBoard);
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean placeShip(String field, int numberOfCells, String direction) {
		int x = validateTurnX(field);
		int y = validateTurnY(field);
		if (x == -1 || y == -1) {
			return false;
		}
		switch (direction) {
		case "south": {
			if (x + numberOfCells < 13) {
				return hasEnoughEmptyFields(x, y, x + numberOfCells, 1, true, numberOfCells);
			} else {
				return false;
			}
		}
		case "north": {
			if (x - numberOfCells > 0) {
				return hasEnoughEmptyFields(x - numberOfCells + 1, y, x + 1, 1, true, numberOfCells);
			} else {
				return false;
			}
		}
		case "east": {
			if (y + numberOfCells * 2 < 24) {

				return hasEnoughEmptyFields(y, x, y + numberOfCells * 2, 2, false, numberOfCells);
			} else {
				return false;
			}
		}
		case "west": {
			if (y + numberOfCells * 2 > 19) {
				return hasEnoughEmptyFields(y - numberOfCells * 2 + 2, x, y + 2, 2, false, numberOfCells);
			} else {
				return false;
			}
		}
		default: {
			return false;
		}
		}
	}

	public boolean checkForShip(String field) {
		int x = validateTurnX(field);
		int y = validateTurnY(field);
		if (x == -1 || y == -1) {
			return false;
		}
		if (board[x][y] == '#') {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasEnoughEmptyFields(int x, int y, int limit, int addition, boolean isVertical, int shipLength) {
		int counter = 0;
		for (int i = x; i < limit; i += addition) {
			if (isVertical) {
				if (board[i][y] == '_') {
					counter++;
				}
			} else {
				if (board[y][i] == '_') {
					counter++;
				}
			}
		}
		if (counter == shipLength) {
			Ship ship = new Ship(shipLength);
			for (int i = x; i < limit; i += addition) {
				if (isVertical) {
					board[i][y] = '#';
					ships.put(new ShipPair(i, y), ship);
				} else {
					board[y][i] = '#';
					ships.put(new ShipPair(y, i), ship);
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public void destroyedMsg(PrintWriter pw, ShipPair shipPair, GameBoard enemyRealBoard) {
		if (enemyRealBoard.getShips().get(shipPair).isDestroyed()) {
			pw.println("Ship Destroyed!");
			pw.flush();
		}
	}

}
