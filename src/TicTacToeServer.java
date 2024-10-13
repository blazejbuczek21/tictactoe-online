package tictactoe;

import java.io.*;
import java.net.*;
import java.util.*;

public class TicTacToeServer {

    private static final int PORT = 9003;
    private static char[] board = new char[9];
    private static List<Handler> players = new ArrayList<>();
    private static char currentPlayer = 'X';

    public static void main(String[] args) throws Exception {
        System.out.println("The Tic-Tac-Toe server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (players.size() < 2) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private char mark;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (players) {
                    if (players.size() < 2) {
                        mark = players.isEmpty() ? 'X' : 'O';
                        players.add(this);
                        out.println("WELCOME " + mark);
                        if (players.size() == 2) {
                            startGame();
                        }
                    } else {
                        out.println("FULL");
                        return;
                    }
                }

                while (true) {
                    String command = in.readLine();
                    if (command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.substring(5));
                        if (board[location] == '\0' && mark == currentPlayer) {
                            board[location] = mark;
                            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                            broadcast("MOVE " + mark + " " + location);
                            if (hasWinner()) {
                                broadcast("VICTORY " + mark);
                            } else if (isBoardFull()) {
                                broadcast("TIE");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        private void startGame() {
            for (Handler player : players) {
                player.out.println("START");
            }
        }

        private void broadcast(String message) {
            for (Handler player : players) {
                player.out.println(message);
            }
        }

        private boolean hasWinner() {
            int[][] lines = {
                    {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                    {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                    {0, 4, 8}, {2, 4, 6}
            };
            for (int[] line : lines) {
                if (board[line[0]] != '\0' && board[line[0]] == board[line[1]] && board[line[1]] == board[line[2]]) {
                    return true;
                }
            }
            return false;
        }

        private boolean isBoardFull() {
            for (char c : board) {
                if (c == '\0') {
                    return false;
                }
            }
            return true;
        }
    }
}