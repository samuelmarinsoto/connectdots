import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;

public class Servidor {

    public void startServer() {
        LinkedList<Integer> lista_puertos = new LinkedList<Integer>();

        try {
            ServerSocket server = new ServerSocket(5000);

            while (true) {
                Socket serversocker = server.accept();
                System.out.println("Cliente conectado: " + serversocker.getRemoteSocketAddress());

                try (DataInputStream datos = new DataInputStream(serversocker.getInputStream())) {
                    // if (datos.available() > 0) {
                    String mensajes = datos.readUTF();
                    System.out.println("Mensaje recibido: " + mensajes);

                    if (Objects.equals(String.valueOf(mensajes.charAt(0)), "0")) {
                        mensajes = mensajes.substring(1, mensajes.length());
                        int puerto_final = Integer.parseInt(mensajes);
                        lista_puertos.add(puerto_final);
                        System.out.println("Conectado: " + puerto_final);
                    } else {
                        Socket mensajepuertos;
                        // Parsear el mensaje JSON y imprimirlo de forma legible
                        // JSONTokener tokener = new JSONTokener(mensajes);
                        // Object json = tokener.nextValue();
                        // if (json instanceof JSONObject) {
                        //     System.out.println(((JSONObject) json).toString(4)); // 4 es el número de espacios para la indentación
                        // } else if (json instanceof JSONArray) {
                        //     System.out.println(((JSONArray) json).toString(4)); // 4 es el número de espacios para la indentación
                        // } else {
                        //     // No es un JSON válido
                        System.out.println(mensajes);
                        // }

                        for (Integer puerto : lista_puertos) {
                            mensajepuertos = new Socket("127.0.0.1", puerto);
                            try (DataOutputStream out = new DataOutputStream(mensajepuertos.getOutputStream())) {
                                out.writeUTF(mensajes);
                            }
                            mensajepuertos.close();
                        }
                    }

                } catch (EOFException e) {
                    System.out.println("Se ha alcanzado el final del flujo de datos");
                } finally {
                    serversocker.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // } catch (EOFException e) {
            //     System.out.println("Se ha alcanzado el final del flujo de datos");
            // } catch (IOException e) {
            //     e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Servidor().startServer();
    }
}
