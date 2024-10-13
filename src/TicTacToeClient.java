package tictactoe;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class TicTacToeClient {

    private JFrame frame = new JFrame("Tic-Tac-Toe");
    private JLabel messageLabel = new JLabel("...");
    private JButton[] board = new JButton[9];
    private BufferedReader in;
    private PrintWriter out;
    private char mark;

    public TicTacToeClient(String serverAddress) throws Exception {

        Socket socket = new Socket(serverAddress, 9003);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new java.awt.GridLayout(3, 3));
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new JButton();
            board[i].setEnabled(false);
            board[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    out.println("MOVE " + j);
                }
            });
            boardPanel.add(board[i]);
        }

        frame.getContentPane().add(messageLabel, "South");
        frame.getContentPane().add(boardPanel, "Center");
        frame.pack();

        new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        String line = in.readLine();
                        if (line.startsWith("WELCOME")) {
                            mark = line.charAt(8);
                            messageLabel.setText("Welcome, player " + mark);
                        } else if (line.startsWith("START")) {
                            for (JButton button : board) {
                                button.setEnabled(true);
                            }
                            messageLabel.setText("Game started! Player X goes first.");
                        } else if (line.startsWith("MOVE")) {
                            String[] parts = line.split(" ");
                            char moveMark = parts[1].charAt(0);
                            int loc = Integer.parseInt(parts[2]);
                            board[loc].setText(moveMark + "");
                            board[loc].setEnabled(false);
                            messageLabel.setText("Player " + (mark == 'X' ? 'O' : 'X') + "'s turn");
                        } else if (line.startsWith("VICTORY")) {
                            messageLabel.setText("Player " + line.charAt(8) + " wins!");
                            for (JButton button : board) {
                                button.setEnabled(false);
                            }
                        } else if (line.startsWith("TIE")) {
                            messageLabel.setText("It's a tie!");
                            for (JButton button : board) {
                                button.setEnabled(false);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        String serverAddress = JOptionPane.showInputDialog(
                null,
                "Enter IP Address of the Server:",
                "Welcome to Tic-Tac-Toe",
                JOptionPane.QUESTION_MESSAGE);
        TicTacToeClient client = new TicTacToeClient(serverAddress);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
    }
}