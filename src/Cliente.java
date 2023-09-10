import org.json.*;
import processing.core.PApplet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class Cliente extends PApplet implements Runnable {



    Socket socket;
    public static int color = 0;

    public static String samuel ="{\"color\":0}";

    public static  JSONObject data  = new JSONObject(samuel);
    public static int juego_iniciado = 0;





    public Cliente() {

        Thread hilo = new Thread(this);
        hilo.start();

    }



    public void settings(){
        size(500,500);



    }


    public void setup() {



    }



    public void draw() {

        background((Integer) data.get("color"));
        ellipse(100,100,100,100);


    }

    public void send(String hola){
        try {
            Socket socket = new Socket("127.0.0.1",5000);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(hola);
            socket.close();
        }
        catch(Exception ex){
            System.out.println(ex);
        }
    }

    public void mouseClicked(){
        Random rand = new Random();
        int rand_int1 = rand.nextInt(300);
        String mensaje =data.put("color",rand_int1).toString();

        send(mensaje);


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

                String mensajes = datos.readUTF();
                System.out.println(mensajes);
                data =new JSONObject(mensajes);






            }

        } catch (Exception e) {
            System.out.println(e);}
    }

    public static void main(String args[]) {
        Inicio  inicio =new Inicio();

        while(inicio.numero ==0){
            System.out.println("1");
        }
        java.awt.EventQueue.invokeLater(new Runnable() {


            public void run() {

                PApplet.main("Cliente");

            }

        });



    }



}




