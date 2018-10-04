package game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import server.AppendingObjectOutputStream;
import server.Save;

public class GameRoom {
	private String gameName;
	private String creator;
	private String joinedPlayer;
	private PrintWriter pw1;
	private BufferedReader br1;
	private PrintWriter pw2;
	private BufferedReader br2;
	private boolean gameStarted;
	private Game game1;
	private Game game2;
	private Map<String, Save> playersSaves = new HashMap<>();

	public GameRoom(String gameName, String creator, PrintWriter pw, BufferedReader br, Map<String, Save> playerSaves) {
		this.gameName = gameName;
		this.creator = creator;
		pw1 = pw;
		br1 = br;
		playersSaves.putAll(playerSaves);
		gameStarted = false;
		game1 = new Game();
		game2 = new Game();
	}

	public GameRoom(String gameName, String creator, String joinedPlayer, PrintWriter pw, BufferedReader br,
			Map<String, Save> playerSaves, Game game1, Game game2) {
		this.gameName = gameName;
		this.creator = creator;
		this.joinedPlayer = joinedPlayer;
		pw1 = pw;
		br1 = br;
		playersSaves.putAll(playerSaves);
		gameStarted = false;
		this.game1 = game1;
		this.game2 = game2;
	}

	public String getGameName() {
		return gameName;
	}

	public String getCreator() {
		return creator;
	}

	public String getJoinedPlayer() {
		return joinedPlayer;
	}

	public void join(PrintWriter pw, BufferedReader br, String joinedPlayer, Map<String, Save> playerSaves)
			throws IOException, InterruptedException {
		playersSaves.putAll(playerSaves);
		this.joinedPlayer = joinedPlayer;
		pw2 = pw;
		br2 = br;
		gameStarted = true;
		pw1.println("***");
		pw1.flush();
		//Thread.sleep(500);
		start();
	}

	public void start() throws IOException, InterruptedException {
		startingMsg();
		Thread player1 = new Thread() {
			public void run() {
				try {
					game1.placeShips(pw1, br1);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		player1.start();
		game2.placeShips(pw2, br2);
		if (player1.isAlive()) {
			player1.join();
		}
		pw1.println("Type save-game to save.");
		pw2.println("Type save-game to save.");
		pw1.flush();
		pw2.flush();
		startPlaying();
	}

	public void startPlaying() {
		int player1ShipHits = 0, player2ShipHits = 0;
		while (player1ShipHits != 30 && player2ShipHits != 30) {
			player1ShipHits = play(br1, pw1, br2, pw2, game1, game2, creator, joinedPlayer, player1ShipHits);
			if (player1ShipHits != 30) {
				player2ShipHits = play(br2, pw2, br1, pw1, game2, game1, joinedPlayer, creator, player2ShipHits);
			}
			if (player1ShipHits == -1 || player2ShipHits == -1) {
				return;
			}
		}
		endingMsg(player1ShipHits);
	}

	public String getStatus() {
		if (gameStarted) {
			return "in progress";
		} else {
			return "pending";
		}
	}

	public String getPlayers() {
		if (gameStarted) {
			return "2/2";
		} else {
			return "1/2";
		}

	}

	public boolean isFree() {
		return !gameStarted && joinedPlayer == null;
	}

	public void saveGame(BufferedReader br, PrintWriter pw, String player1, String player2)
			throws NumberFormatException, IOException {
		pw.println("Name of save :");
		pw.flush();
		String saveName = br.readLine();
		while (hasSave(saveName, player1)) {
			pw.println("This name already exists!\nSave name :");
			pw.flush();
			saveName = br.readLine();
		}
		int savesCount = 0;
		File numberOfSaves = new File("numberOfSaves.txt");
		BufferedReader fromNumberOfSaves = new BufferedReader(new FileReader(numberOfSaves));
		savesCount = Integer.parseInt(fromNumberOfSaves.readLine());
		PrintWriter toNumberOfSaves = new PrintWriter(numberOfSaves);
		savesCount++;
		toNumberOfSaves.println(savesCount);
		toNumberOfSaves.flush();
		toNumberOfSaves.close();
		File saves = new File("saves.txt");
		FileOutputStream fos = new FileOutputStream(saves, true);
		if (savesCount == 1) {
			ObjectOutputStream firstWrite = new ObjectOutputStream(fos);
			save(firstWrite, player1, player2, saveName);
		} else {
			AppendingObjectOutputStream toSaved = new AppendingObjectOutputStream(fos);
			save(toSaved, player1, player2, saveName);
		}
		pw.println("Saved successfully!");
		pw.flush();
		fromNumberOfSaves.close();
	}

	public int play(BufferedReader br1, PrintWriter pw1, BufferedReader br2, PrintWriter pw2, Game game1, Game game2,
			String player1, String player2, int playerShipHits) {
		String field = null;
		try {
			game1.emptyBr(br1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		pw2.println("Waiting for opponent...");
		pw2.flush();
		game1.printBoards(pw1);
		pw1.flush();
		pw1.println("Enter your turn:");
		pw1.flush();
		try {
			field = br1.readLine();
		} catch (IOException e) {
			System.out.println("Unable to read!");
			e.printStackTrace();
			return -1;
		}
		if (field.equals("save-game")) {
			try {
				saveGame(br1, pw1, player1, player2);
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
			pw1.println("Enter your turn:");
			pw1.flush();
			try {
				field = br1.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		while (!game1.getYourBoard().isValid(field)) {
			pw1.println("Invalid cell!\nEnter your turn:");
			pw1.flush();
			try {
				field = br1.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		return game1.shoot(pw1, br1, field, game2, playerShipHits);
	}

	public void startingMsg() {
		pw1.println(joinedPlayer + " joined the game!\nType " + '"' + "start" + '"' + "to start the game!");
		pw2.println("Joined game " + '"' + gameName + '"' + "!\nWaiting for " + creator + " to start the game!");
		pw1.flush();
		pw2.flush();
		try {
			if (br1.readLine().equals("start")) {
				pw1.println("Game starting!");
				pw2.println("Game starting!");
				pw1.flush();
				pw2.flush();
			} else {
				pw1.println("Invalid command");
				pw1.flush();
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public void endingMsg(int player1ShipHits) {
		gameStarted = false;
		if (player1ShipHits == 30) {
			pw1.println("You win!");
			pw1.flush();
			pw2.println("You lose!");
			pw2.flush();
		} else {
			pw2.println("You win!");
			pw2.flush();
			pw1.println("You lose!");
			pw1.flush();
		}
	}

	public void save(ObjectOutputStream toSaved, String player1, String player2, String saveName) {
		try {
			toSaved.writeObject(new Save(saveName, player1, player2, game1, game2, LocalDateTime.now()));
			toSaved.flush();
			toSaved.close();
		} catch (IOException e) {
			System.out.println("There is a problem with save files!");
		}
	}

	public boolean hasSave(String saveName, String player) {
		return playersSaves.containsKey(saveName) && playersSaves.get(saveName).getPlayer1().equals(player);
	}

	public void waiting() throws InterruptedException {
		while (gameStarted) {
			Thread.sleep(500);
		}
	}

	public void joinLoadedGame(PrintWriter pw, BufferedReader reader, Map<String, Save> playerSaves) {
		gameStarted = true;
		pw2 = pw;
		br2 = reader;
		playersSaves.putAll(playerSaves);
		startingMsg();
		startPlaying();
	}
}
