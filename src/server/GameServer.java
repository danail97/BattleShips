package server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import game.GameRoom;


public class GameServer {
	
	public static Set<String> users=new HashSet<>();
	public static Map<String,GameRoom> rooms=new ConcurrentHashMap<>();

	public static final int SERVER_PORT = 1234;
	
	public static void main(String[] args) throws InterruptedException{
		try (ServerSocket ss = new ServerSocket(SERVER_PORT);) {
			while (true) {
				Socket s = ss.accept();
				new ServerThread(s).start();
			}
		}catch(IOException e) {
			System.out.println("There is a problem with the server socket!");
			e.printStackTrace();
		}	
	}

}
