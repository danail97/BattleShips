package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import server.GameServer;

public class GameClient {
	private String name;

	public GameClient(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void printMenu() {
		System.out.println("Available commands :");
		System.out.println("create-game <game-name>");
		System.out.println("list-games");
		System.out.println("join-game [<game-name>]");
		System.out.println("saved-games");
		System.out.println("load-game <game-name>");
		//System.out.println("delete-game <game-name>");
		System.out.println("exit");
	}

	public void connect(){
		try (Socket s = new Socket("localhost", GameServer.SERVER_PORT);
				PrintWriter pw = new PrintWriter(s.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));) {
			printMenu();
			pw.println(name);
			pw.flush();
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(System.in);
			String command;
			Thread reciever = new Thread() {

				public void run() {
					String line;
					boolean go = true;
					while (go) {
						try {
							if (br.ready()) {
								while ((line = br.readLine())!=null) {
									if(line.equals("***")) {
										pw.println("---");
										pw.flush();
									}else {
									System.out.println(line);
									}
									if(line.equals("User already connected!")) {
									return;
									}
									if(line.equals("Goodbye.")) {
										return;
									}
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			};
			reciever.start();
			Thread.sleep(100);
			while (reciever.isAlive()) {
				command = sc.nextLine();
				pw.println(command);
				pw.flush();
			}
		} catch (UnknownHostException e) {
			System.out.println("Unknow host!");
		} catch (IOException e) {
			System.out.println("There is a problem with the socket!");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Interupted!");
			e.printStackTrace();
		}
	}
}
