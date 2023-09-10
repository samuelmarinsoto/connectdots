import org.json.*;
import processing.core.PApplet;

import java.awt.*;
import java.util.ArrayList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.concurrent.TimeUnit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;


public class Cliente extends PApplet implements Runnable {



    Socket socket;
    public static int color = 0;

    public static String samuel ="{}";

    public static  JSONObject data  = new JSONObject(samuel);
    public static JSONArray datos = new JSONArray();
    public static int juego_iniciado = 0;
    ArrayList<Dot> dots;
    ArrayList<Line> lines;
    ArrayList<Square> squares = new ArrayList<Square>();
    int dotSize = 5;
    int rows = 10; // Number of rows
    int cols =10; // Number of columns
    int score = 0;
    int currentPlayer = 1;
    int player1Color = color(255, 0, 0);  // Red for player 1
    int  player2Color = color(0, 0, 255);  // Blue for player 2
    int selectedIndex = 0;
    int selectedRow = 0;
    int selectedCol = 0;
    String errorMsg = "";
    int player1Score = 0;
    int player2Score = 0;


    Dot firstDot = null;





    public Cliente() {

        Thread hilo = new Thread(this);
        hilo.start();

    }



    public void settings() {
        size(400, 400);
        dots = new ArrayList<Dot>();
        lines = new ArrayList<Line>();
        generateDots();
    }

    public void setup(){

    }





    public void draw() {
        background(255);
        for (Square sq : squares) {
            sq.display();

        }

        // Display the lines
        for (Line line : lines) {
            line.display();
            System.out.println();


        }

        // Display the
        // dots
        for (Dot dot : dots) {
            dot.display();
        }
        text("Score Player 1: " + player1Score, 20, 30);
        text("Score Player 2: " + player2Score, 20, 60);

        fill(255, 0, 0);
        text(errorMsg, 20, 90);
    };


    public void generateDots() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                float x = map(i, 0, rows - 1, dotSize, width - dotSize);
                float y = map(j, 0, cols - 1, dotSize, height - dotSize);
                dots.add(new Dot(x, y, dotSize, i, j));
            }
        }
    }

    public void keyPressed() {
        if (key == 'A' || key == 'a') {
            selectedRow = max(0, selectedRow - 1);
        } else if (key == 'W' || key == 'w') {
            selectedCol = max(0, selectedCol - 1);
        } else if (key == 'D' || key == 'd') {
            selectedRow = min(rows - 1, selectedRow + 1);
        } else if (key == 'S' || key == 's') {
            selectedCol = min(cols - 1, selectedCol + 1);
        } else if (key == 'L' || key == 'l') {
            Dot currentDot = getDotAtRowCol(selectedRow, selectedCol);
            if (firstDot == null) {
                firstDot = currentDot;
                firstDot.isSelected = true;
            } else {
                if (areAdjacentDots(firstDot, currentDot) && !lineExists(firstDot, currentDot)) {
                    Line newLine = new Line(firstDot, currentDot);
                    lines.add(newLine);

                    // Si se completa un cuadro, incrementa el puntaje del jugador correspondiente
                    if (isSquareCompleted(firstDot, currentDot)) {
                        Square sq = new Square(firstDot, currentDot);
                        squares.add(sq);
                        if (currentPlayer == 1) {
                            score++;
                        }
                        // No cambiamos el turno si alguien completa un cuadrado.
                    } else {
                        // Cambio de turno
                        currentPlayer = (currentPlayer == 1) ? 2 : 1;
                    }


                    firstDot.isSelected = false;
                    firstDot = null;
                    errorMsg = ""; // limpiar el mensaje de error
                } else {
                    errorMsg = "Selecciona puntos adyacentes o puntos entre los que no exista una línea!";
                    firstDot.isSelected = false;
                    firstDot = null;
                }
            }


            send(lines.toString());
        }
    }

    public boolean lineExists(Dot d1, Dot d2) {
        for (Line line : lines) {
            if ((line.dot1 == d1 && line.dot2 == d2) || (line.dot1 == d2 && line.dot2 == d1)) {
                return true;
            }
        }
        return false;
    }

    Dot getDotAtRowCol(int row, int col) {
        for (Dot dot : dots) {
            if (dot.row == row && dot.col == col) {
                return dot;
            }
        }
        return null;
    }

    class Dot {
        float x, y;
        float radius;
        boolean isSelected = false;
        int row, col;

        Dot(float x, float y, float radius, int row, int col) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.row = row;
            this.col = col;
        }

        void display() {
            noStroke();
            if (row == selectedRow && col == selectedCol) {
                fill(0, 0, 255);  // Blue for the currently highlighted dot
            } else if (isSelected) {
                fill(0, 255, 0);  // Green for the selected dot
            } else {
                fill(100, 100, 100);
            }
            ellipse(x, y, radius * 2, radius * 2);
        }
    }

    class Line {
        Dot dot1, dot2;
        int lineColor = 0;

        Line(Dot dot1, Dot dot2) {
            this.dot1 = dot1;
            this.dot2 = dot2;
            this.lineColor = 0;
        }

        void display() {
            stroke(lineColor);
            line(dot1.x, dot1.y, dot2.x, dot2.y);

        }
    }

    public boolean isSquareCompleted(Dot d1, Dot d2) {
        Dot topRight, topLeft, bottomRight, bottomLeft;

        if(d1.row == d2.row) { // Horizontal line
            topLeft = d1.col < d2.col ? d1 : d2;
            topRight = d1.col > d2.col ? d1 : d2;
            bottomLeft = getDotAtRowCol(topLeft.row + 1, topLeft.col);
            bottomRight = getDotAtRowCol(topRight.row + 1, topRight.col);
        } else { // Vertical line
            topLeft = d1.row < d2.row ? d1 : d2;
            bottomLeft = d1.row > d2.row ? d1 : d2;
            topRight = getDotAtRowCol(topLeft.row, topLeft.col + 1);
            bottomRight = getDotAtRowCol(bottomLeft.row, bottomLeft.col + 1);
        }

        if(topLeft == null || topRight == null || bottomLeft == null || bottomRight == null) return false;

        return lineExists(topLeft, topRight) && lineExists(bottomLeft, bottomRight) && lineExists(topLeft, bottomLeft) && lineExists(topRight, bottomRight);
    }

    Dot getDotAt(float x, float y) {
        for (Dot dot : dots) {
            if (abs(dot.x - x) < 1 && abs(dot.y - y) < 1) {
                return dot;
            }
        }
        return null;
    }

    boolean isLineExists(Dot d1, Dot d2) {
        for (Line line : lines) {
            if ((line.dot1 == d1 && line.dot2 == d2) || (line.dot1 == d2 && line.dot2 == d1)) {
                return true;
            }
        }
        return false;
    }
    boolean areAdjacentDots(Dot dot1, Dot dot2) {
        int dRow = abs(dot1.row - dot2.row);
        int dCol = abs(dot1.col - dot2.col);

        return ((dRow == 1 && dCol == 0) || (dRow == 0 && dCol == 1));
    }

    void fillSquare(Dot d1, Dot d2) {
        float dx = d2.x - d1.x;
        float dy = d2.y - d1.y;

        beginShape();
        fill(255);
        noStroke();

        if (abs(dx) > abs(dy)) { // Horizontal line
            vertex(d1.x, d1.y);
            vertex(d2.x, d2.y);
            vertex(d2.x + dy, d2.y - dx);
            vertex(d1.x + dy, d1.y - dx);
        } else { // Vertical line
            vertex(d1.x, d1.y);
            vertex(d2.x, d2.y);
            vertex(d2.x + dx, d2.y + dy);
            vertex(d1.x + dx, d1.y + dy);
        }

        endShape(CLOSE);
    }

    class Square {
        Dot d1, d2;


        Square(Dot d1, Dot d2) {
            this.d1 = d1;
            this.d2 = d2;

        }

        void display() {
            float dx = d2.x - d1.x;
            float dy = d2.y - d1.y;

            beginShape();

            noStroke();

            if (abs(dx) > abs(dy)) { // Horizontal line
                vertex(d1.x, d1.y);
                vertex(d2.x, d2.y);
                vertex(d2.x + dy, d2.y - dx);
                vertex(d1.x + dy, d1.y - dx);
            } else { // Vertical line
                vertex(d1.x, d1.y);
                vertex(d2.x, d2.y);
                vertex(d2.x + dx, d2.y + dy);
                vertex(d1.x + dx, d1.y + dy);
            }

            endShape(CLOSE);
        }
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




