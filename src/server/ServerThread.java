package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import game.GameRoom;

public class ServerThread extends Thread {

	private Map<String, Save> playerSaves = new HashMap<>();
	private Socket socket;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		String player = null;
		String currentGame = null;
		boolean inGame = true;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter pw = new PrintWriter(socket.getOutputStream());) {
			player = reader.readLine();
			final String threadPlayer = player;
			Thread loadSaves = new Thread() {
				public void run() {
					loadSaves(threadPlayer);
				}
			};
			loadSaves.start();
			synchronized (this) {
				if (GameServer.users.contains(player)) {
					pw.println("User already connected!");
					return;
				} else {
					GameServer.users.add(player);
				}
			}
			while (inGame) {
				// if (reader.ready()) {
				String clientMsg = reader.readLine();
				String[] words = clientMsg.split(" ");
				String command = words[0];
				String gameName = null;
				if (words.length > 1) {
					gameName = clientMsg.substring(words[0].length() + 1);
				}
				switch (command) {
				case "create-game": {
					if (createGame(gameName, currentGame, pw, reader, player)) {
						currentGame = gameName;
					}
					break;
				}
				case "list-games": {
					if (!GameServer.rooms.isEmpty()) {
						for (GameRoom room : GameServer.rooms.values()) {
							pw.println(room.getGameName() + " " + room.getCreator() + " " + room.getStatus() + " "
									+ room.getPlayers());
						}
					} else {
						pw.println("No games available.");
					}
					pw.flush();
					break;
				}
				case "join-game": {
					if (!checkIfInGame(currentGame, pw)) {
						if (gameName != null) {
							currentGame = gameName;
							if (GameServer.rooms.containsKey(currentGame)
									&& player.equals(GameServer.rooms.get(currentGame).getJoinedPlayer())) {
								GameServer.rooms.get(currentGame).joinLoadedGame(pw, reader, playerSaves);
							} else {
								joinGame(gameName, currentGame, pw, reader, player);
							}
							currentGame = null;
						} else {
							if (!getFreeRooms().isEmpty()) {
								int random;
								Random generator = new Random();
								GameRoom[] freeRooms = new GameRoom[getFreeRooms().size()];
								getFreeRooms().values().toArray(freeRooms);
								random = generator.nextInt(getFreeRooms().size());
								currentGame = freeRooms[random].getGameName();
								freeRooms[random].join(pw, reader, player, playerSaves);
								if (GameServer.rooms.containsKey(currentGame)) {
									GameServer.rooms.remove(currentGame);
								}
								currentGame = null;
							} else {
								pw.println("There are no free rooms!");
								pw.flush();
							}
						}
						loadSaves(player);
					}
					break;
				}
				case "saved-games": {
					if (loadSaves.isAlive()) {
						pw.println("Loading...\nTry again later.");
						pw.flush();
						break;
					} else {
						for (Save save : playerSaves.values()) {
							pw.println(save.getSaveName() + " " + save.getPlayer2() + " " + save.getDate());
						}
						pw.flush();
					}
					break;
				}
				case "load-game": {
					if (!checkIfInGame(currentGame, pw)) {
						loadGame(gameName, pw, reader);
					}
					break;
				}
				case "exit": {
					inGame = false;
					pw.println("Goodbye.");
					pw.flush();
					break;
				}
				case "---": {
					GameServer.rooms.get(currentGame).waiting();
					loadSaves(player);
					break;
				}
				default: {
					pw.println("Invalid command!\n");
					pw.flush();
				}
				}
				// }
			}
		} catch (Exception e) {
			System.out.println(player + " disconnected");
			deletePlayer(player);
			deleteRoom(currentGame);
			//e.printStackTrace();
		}
		synchronized (this) {
			System.out.println(player + " disconnected");
			deletePlayer(player);
			deleteRoom(currentGame);
		}
	}

	public Map<String, GameRoom> getFreeRooms() {
		Map<String, GameRoom> freeRooms = new HashMap<>();
		for (GameRoom room : GameServer.rooms.values()) {
			if (room.isFree()) {
				freeRooms.put(room.getGameName(), room);
			}
		}
		return freeRooms;
	}

	public boolean checkIfInGame(String currentGame, PrintWriter pw) {
		if (currentGame != null) {
			pw.println("You are already in game!");
			pw.flush();
			return true;
		} else {
			return false;
		}
	}

	public boolean createGame(String gameName, String currentGame, PrintWriter pw, BufferedReader reader,
			String player) {
		if (!checkIfInGame(currentGame, pw)) {
			if (gameName != null && !GameServer.rooms.containsKey(gameName)) {
				GameServer.rooms.put(gameName, new GameRoom(gameName, player, pw, reader, playerSaves));
				pw.println("Created game " + '"' + gameName + '"' + ",players 1/2");
				pw.flush();
				return true;
			} else {
				pw.println("There is a game with this name!");
				pw.flush();
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean loadGame(String saveName, PrintWriter pw, BufferedReader reader) {
		if (playerSaves.containsKey(saveName)) {
			if (GameServer.rooms.containsKey(saveName)) {
				pw.println("There is a game with this name!");
				pw.flush();
				return false;
			}
			GameServer.rooms.put(saveName,
					new GameRoom(saveName + " (save) with " + playerSaves.get(saveName).getPlayer2(),
							playerSaves.get(saveName).getPlayer1(), playerSaves.get(saveName).getPlayer2(), pw, reader,
							playerSaves, playerSaves.get(saveName).getGame1(), playerSaves.get(saveName).getGame2()));
			pw.println("Save loaded.");
			pw.flush();
			return true;
		} else {
			pw.println("No such save.");
			pw.flush();
			return false;
		}
	}

	public void loadSaves(String player) {
		try {
			BufferedReader fromNumberOfSaves = new BufferedReader(new FileReader("numberOfSaves.txt"));
			int savesCount = Integer.parseInt(fromNumberOfSaves.readLine());
			fromNumberOfSaves.close();
			if (savesCount == 0) {
				return;
			}
			ObjectInputStream fromFile = new ObjectInputStream(new FileInputStream(new File("saves.txt")));
			Save save;
			for (int i = 0; i < savesCount; i++) {
				save = (Save) fromFile.readObject();
				if (save.getPlayer1().equals(player)) {
					playerSaves.put(save.getSaveName(), save);
				}
			}
			fromFile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void joinGame(String gameName, String currentGame, PrintWriter pw, BufferedReader reader, String player) {
		if (getFreeRooms().containsKey(gameName)) {
			try {
				GameServer.rooms.get(gameName).join(pw, reader, player, playerSaves);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (GameServer.rooms.containsKey(currentGame)) {
				GameServer.rooms.remove(currentGame);
			}
		} else {
			if (GameServer.rooms.containsKey(gameName)) {
				pw.println("This game is already started!");
			} else {
				pw.println("This game doesn't exist!");
			}
			pw.flush();
		}
	}

	public void deletePlayer(String player) {
		if (GameServer.users.contains(player)) {
			GameServer.users.remove(player);
		}
	}

	public void deleteRoom(String currentGame) {
		if (!currentGame.equals(null)) {
			if (GameServer.rooms.containsKey(currentGame)) {
				GameServer.rooms.remove(currentGame);
			}
		}
	}
}
