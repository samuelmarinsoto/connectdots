import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class Servidor {

    public static class GameServerGUI {
        private JFrame frame;
        private JTextField matrixSizeField;
        private JTextField playersField;
        private JButton startButton;

        public GameServerGUI() {
            frame = new JFrame("Connect Dot Game Server");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 200);
            frame.setLayout(new GridLayout(3, 2));

            // Tamaño de Matriz:
            JLabel matrixSizeLabel = new JLabel("Tamaño de Matriz:");
            matrixSizeField = new JTextField();

            // Jugadores:
            JLabel playersLabel = new JLabel("Jugadores:");
            playersField = new JTextField();

            // Botón de inicio:
            startButton = new JButton("Iniciar Partida");
            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    startGame();
                }
            });

            frame.add(matrixSizeLabel);
            frame.add(matrixSizeField);
            frame.add(playersLabel);
            frame.add(playersField);
            frame.add(startButton);

            frame.setVisible(true);
        }

        private void startGame() {
            System.out.println("Iniciando juego...");
            int matrixSize = Integer.parseInt(matrixSizeField.getText());
            int numPlayers = Integer.parseInt(playersField.getText());

            String classpath = "C:\\Users\\Isaac\\Desktop\\CE\\Datos1\\ConnectDot\\out\\production\\prueba";
            String javaCmd = System.getProperty("java.home") + "\\bin\\java";
            System.out.println("Comando Java: " + javaCmd);

            for (int i = 0; i < numPlayers; i++) {
                try {
                    System.out.println("Intentando iniciar Cliente...");

                    ProcessBuilder pb = new ProcessBuilder(javaCmd, "-cp", classpath, "Cliente");
                    pb.start();
                    System.out.println("Cliente iniciado...");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startServer() {
        LinkedList<Integer> lista_puertos = new LinkedList<Integer>();

        try {
            ServerSocket server = new ServerSocket(5000);

            while (true) {
                Socket serversocker = server.accept();
                DataInputStream datos = new DataInputStream(serversocker.getInputStream());
                String mensajes = datos.readUTF();

                if (Objects.equals(String.valueOf(mensajes.charAt(0)), "0")) {
                    mensajes = mensajes.substring(1, mensajes.length());
                    int puerto_final = Integer.parseInt(mensajes);
                    lista_puertos.add(puerto_final);
                    System.out.println("Conectado: " + puerto_final);
                } else {
                    Socket mensajepuertos = null;
                    System.out.println(mensajes);

                    for (int i = 0; i < lista_puertos.size(); i++) {
                        mensajepuertos = new Socket("127.0.0.1", lista_puertos.get(i));
                        DataOutputStream out = new DataOutputStream(mensajepuertos.getOutputStream());
                        out.writeUTF(mensajes);
                        mensajepuertos.close();
                    }
                    System.out.println(mensajes);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GameServerGUI();
            }
        });
        new Servidor().startServer();
    }
}

