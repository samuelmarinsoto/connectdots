import processing.core.PApplet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Main extends PApplet implements Runnable {

    Socket socket;
    String mensajes = "10";

    public void Main(){
        Thread hilo = new Thread(this);
        hilo.start();
    }




    int posicion_y =10;
    int posicion_x = 10;

    public void settings(){
        size(500,500);





    }


    public void setup() {



    }

    public void mouseClicked(){
        posicion_y +=10;
        String posicion = String.valueOf(posicion_y);


    }


    public void enviar(String mensaje){
        try {
            Socket socket = new Socket("127.0.0.1",5000);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(mensaje);
            socket.close();
        }
        catch(Exception ex){
            System.out.println(ex);
        }

    }

    public void run() {

        try {
            ServerSocket server = new ServerSocket(0);
            Socket socket = new Socket("127.0.0.1",5000);


            String puerto_codificado = String.valueOf("0" + server.getLocalPort());

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(puerto_codificado);
            socket.close();


            while(true){
                Socket serversocker =  server.accept();
                DataInputStream datos = new DataInputStream(serversocker.getInputStream());

                mensajes = datos.readUTF();


            }

        } catch (Exception e) {
            System.out.println(e);}
    }


    public void draw() {
        background(0);
        text("posicion: "+posicion_y, 40, 220);
        textSize(60);





    }



    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
//
//        PApplet.main("Main");





    }
}