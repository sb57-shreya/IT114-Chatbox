package Module4.Part3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Server {
    int port = 3001;
    // connected clients
    private List<ServerThread> clients = new ArrayList<ServerThread>();

    //initializing variables for number guesser game implementation
    private boolean gameActive = false;          //Shreya Bose
    private int secretNumber;                    //sb57
                                                 //February 19,2024
    private void start(int port) {
        this.port = port;
        // server listening
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            Socket incoming_client = null;
            System.out.println("Server is listening on port " + port);
            do {
                System.out.println("waiting for next client");
                if (incoming_client != null) {
                    System.out.println("Client connected");
                    ServerThread sClient = new ServerThread(incoming_client, this);
                    
                    clients.add(sClient);
                    sClient.start();
                    incoming_client = null;
                    
                }
            } while ((incoming_client = serverSocket.accept()) != null);
        } catch (IOException e) {
            System.err.println("Error accepting connection");
            e.printStackTrace();
        } finally {
            System.out.println("closing server socket");
        }
    }
    protected synchronized void disconnect(ServerThread client) {
		long id = client.getId();
        client.disconnect();
		broadcast("Disconnected", id);
	}
    
    protected synchronized void broadcast(String message, long id) {
        if(processCommand(message, id)){

            return;
        }
        // let's temporarily use the thread id as the client identifier to
        // show in all client's chat. This isn't good practice since it's subject to
        // change as clients connect/disconnect
        message = String.format("User[%d]: %s", id, message);
        // end temp identifier
        
        // loop over clients and send out the message
        Iterator<ServerThread> it = clients.iterator();
        while (it.hasNext()) {
            ServerThread client = it.next();
            boolean wasSuccessful = client.send(message);
            if (!wasSuccessful) {
                System.out.println(String.format("Removing disconnected client[%s] from list", client.getId()));
                it.remove();
                broadcast("Disconnected", id);
            }
        }
    }

    private boolean processCommand(String message, long clientId){
        System.out.println("Checking command: " + message);
        if(message.equalsIgnoreCase("disconnect")){
            Iterator<ServerThread> it = clients.iterator();
            while (it.hasNext()) {
                ServerThread client = it.next();
                if(client.getId() == clientId){
                    it.remove();
                    disconnect(client);
                    
                    break;
                }
            }
            return true;
        }

        //Number Guessing Game
        if (gameActive) {
            if (message.startsWith("guess: ")) {
                try {                                                                     //Shreya Bose
                    int guess = Integer.parseInt(message.split(" ")[1]);            //sb57
                    boolean isCorrect = (guess == secretNumber);                          //February 19, 2024
                    String response = isCorrect ? "correct" : "incorrect";
                    broadcast(String.format("User[%d] guessed %d and it was %s", clientId, guess, response), clientId);
                    if (isCorrect) {
                        gameActive = false;
                    }
                    return true;
                } catch (NumberFormatException e) {
                    broadcast(String.format("User[%d] made an invalid guess", clientId), clientId);
                    return true;
                }
            }
        }

        if (message.equalsIgnoreCase("start")) {
            gameActive = true;
            secretNumber = new Random().nextInt(10) + 1;
            broadcast("Number Guessing Game! Guess a number. Make sure to type 'guess:' and then your guess." , clientId);
            return true;
        }

        if (message.equalsIgnoreCase("stop")) {
            gameActive = false;
            broadcast("Number Guessing Game Exited.", clientId);
            return true;
        }

        //Coin Toss Command
        if (message.equalsIgnoreCase("flip coin") || message.equalsIgnoreCase("toss coin")) {
            Random rand = new Random();
            String result = rand.nextBoolean() ? "heads" : "tails";                                                 //Shreya Bose
            broadcast(String.format("User[%d] flipped a coin and got %s", clientId, result), clientId);      //sb57
            return true;                                                                                            //February 19, 2024
        }

        return false;
    }
    public static void main(String[] args) {
        System.out.println("Starting Server");
        Server server = new Server();
        int port = 3000;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        server.start(port);
        System.out.println("Server Stopped");
    }
}