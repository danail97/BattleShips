package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.Test;

import game.GameBoard;

public class GameBoardTest {

	private GameBoard gameBoard;

	public GameBoardTest() {
		gameBoard = new GameBoard();
	}

	@Test
	public void validateTurnXshouldReturn6WhenGivenE10() {
		String field="E10";
		assertEquals(6,gameBoard.validateTurnX(field));
	}
	
	@Test
	public void validateTurnYshouldReturn17WhenGivenJ8() {
		String field="J8";
		assertEquals(17,gameBoard.validateTurnY(field));
	}
	
	@Test
	public void isValidShouldReturnTrueIfGivenF7() {
		String field="F7";
		assertTrue(gameBoard.isValid(field));
	}
	
	@Test
	public void hitShoudReturnTrueIfGivenA3() {
		GameBoard enemyBoard=new GameBoard();
		File test=new File("test.txt");
		PrintWriter pw;
		try {
			pw = new PrintWriter(test);
			String field="A10";
			assertTrue(gameBoard.hit(field,'0',pw,enemyBoard));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void hasEnoughEmptySpacesShoudReturnTrueWhenIsPossibleToPlaceAShipWhithTheGivenData() {
		assertTrue(gameBoard.hasEnoughEmptyFields(5,5,10,1,true,5));
	}
	
	@Test
	public void placeShipShoudReturnTrueWhenTheDirectionIsValid() {
		assertTrue(gameBoard.placeShip("C1", 5, "south"));
		assertTrue(gameBoard.placeShip("D3", 5, "east"));
		assertTrue(gameBoard.placeShip("B7", 5, "west"));
		assertTrue(gameBoard.placeShip("J2", 5, "north"));
	}
	
	@Test
	public void checkForShipShoudReturnTrueIfFiledHasShip() {
		gameBoard.placeShip("I4", 5, "north");
		assertTrue(gameBoard.checkForShip("G4"));
	}
}
