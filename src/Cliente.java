
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import processing.core.PApplet;
import java.util.Random;

public class Cliente extends PApplet implements Runnable {



    Socket socket;
    int color = 0;

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
        background(color);
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
        int rand_int1 = rand.nextInt(200);
        String numero = String.valueOf(rand_int1);
        send(numero);


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
                int color_numero = Integer.parseInt(mensajes);
                color = color_numero;

            }

        } catch (Exception e) {
            System.out.println(e);}
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PApplet.main("Cliente");
            }
        });




    }



}




