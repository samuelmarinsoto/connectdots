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
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import java.io.EOFException; // Importar EOFException
import java.io.IOException; // Importar IOException

public class Servidor {








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
                    System.out.println(mensajes);
                    Socket mensajepuertos = null;

                    // Parsear el mensaje JSON y imprimirlo de forma legible



                    for (int i = 0; i < lista_puertos.size(); i++) {
                        mensajepuertos = new Socket("127.0.0.1", lista_puertos.get(i));
                        DataOutputStream out = new DataOutputStream(mensajepuertos.getOutputStream());
                        out.writeUTF(mensajes);
                        mensajepuertos.close();
                    }
                }
            }
		} catch (EOFException e) {	
            System.out.println("Se ha alcanzado el final del flujo de datos");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
    }

    public static void main(String[] args) {
        new Servidor().startServer();
    }
}
