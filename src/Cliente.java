//import org.json.*;
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
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.HashSet;


public class Cliente extends PApplet implements Runnable {

    static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
            this.next = null;
        }
    }

    static class LinkedListCustom<T> implements Iterable<T> {
        Node<T> head;
        int size;

        LinkedListCustom() {
            head = null;
            size = 0;
        }

        void add(T value) {
            if (head == null) {
                head = new Node<>(value);
            } else {
                Node<T> current = head;
                while (current.next != null) {
                    current = current.next;
                }
                current.next = new Node<>(value);
            }
            size++;
        }

        T get(int index) {
            if (index >= size || index < 0) {
                throw new IndexOutOfBoundsException("Index out of bounds");
            }
            Node<T> current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            return current.value;
        }
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                private Node<T> current = head;

                @Override
                public boolean hasNext() {
                    return current != null;
                }

                @Override
                public T next() {
                    if (current == null) {
                        throw new NoSuchElementException();
                    }
                    T value = current.value;
                    current = current.next;
                    return value;
                }
            };
        }
    }


    Socket socket;
    public static int color = 0;

    public static String samuel ="{}";

    //public static  JSONObject data  = new JSONObject(samuel);
//    public static JSONArray datos = new JSONArray();
    public static int juego_iniciado = 0;
    LinkedListCustom<Dot> dots;
    LinkedListCustom<Line> lines;
    LinkedListCustom<Square> squares = new LinkedListCustom<Square>();
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
        dots = new LinkedListCustom<Dot>();
        lines = new LinkedListCustom<Line>();
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
                    LinkedListCustom<Square> completedSquares = getCompletedSquares(newLine);
                    if (completedSquares.size == 0) {
                        currentPlayer = (currentPlayer == 1) ? 2 : 1;
                    }

                    for (Square sq : completedSquares) {
                        squares.add(sq);
                        sq.setColor((currentPlayer == 1) ? player1Color : player2Color); // Set color
                        if (currentPlayer == 1) {
                            player1Score++;
                        } else {
                            player2Score++;
                        }
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
        HashSet<String> lineSet = new HashSet<>();
        for (Line line : lines) {
            lineSet.add(line.getUniqueRepresentation());
        }

        for (Square sq : squares) {
            if (sq.isClosed(lineSet)) {
                if (currentPlayer == 1) {
                    sq.setColor(player1Color);
                    player1Score++;  // Incrementar el puntaje del jugador 1
                } else {
                    sq.setColor(player2Color);
                    player2Score++;  // Incrementar el puntaje del jugador 2
                }
                // No cambiamos el turno si alguien completa un cuadrado.
            } else {
                // Cambio de turno
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
            }
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
        String getIdentifier() {
            return row + "," + col;
        }
    }

    class Line {
        Dot dot1, dot2;
        int lineColor = 0;
        Dot start;
        Dot end;

        Line(Dot dot1, Dot dot2) {
            this.dot1 = dot1;
            this.dot2 = dot2;
            this.lineColor = 0;
        }

        void display() {
            stroke(lineColor);
            line(dot1.x, dot1.y, dot2.x, dot2.y);
        }
        String getUniqueRepresentation() {
            return dot1.getIdentifier() + "-" + dot2.getIdentifier();
        }
    }
    private boolean doesSquareExist(Dot topLeft, Dot bottomRight) {
        Dot topRight = getDotAtRowCol(topLeft.row, bottomRight.col);
        Dot bottomLeft = getDotAtRowCol(bottomRight.row, topLeft.col);

        return lineExists(topLeft, topRight) && lineExists(topLeft, bottomLeft) && lineExists(bottomRight, bottomLeft) && lineExists(bottomRight, topRight);
    }
    private Square getSquareAbove(Dot d1, Dot d2) {
        if (d1.row != d2.row) {
            return null; // Not a horizontal line
        }

        if (d1.row == 0) {
            return null; // No square above the first row
        }

        Dot topRight = d1.col > d2.col ? d1 : d2;
        Dot topLeft = d1.col < d2.col ? d1 : d2;
        Dot upperLeft = getDotAtRowCol(topLeft.row - 1, topLeft.col);
        Dot upperRight = getDotAtRowCol(topRight.row - 1, topRight.col);

        if (upperLeft != null && upperRight != null && doesSquareExist(upperLeft, topRight)) {
            return new Square(upperLeft, topRight);
        }
        return null;
    }
    private Square getSquareBelow(Dot d1, Dot d2) {
        if (d1.row != d2.row) {
            return null; // Not a horizontal line
        }

        if (d1.row == rows - 1) {
            return null; // No square below the last row
        }

        Dot bottomRight = d1.col > d2.col ? d1 : d2;
        Dot bottomLeft = d1.col < d2.col ? d1 : d2;
        Dot lowerLeft = getDotAtRowCol(bottomLeft.row + 1, bottomLeft.col);
        Dot lowerRight = getDotAtRowCol(bottomRight.row + 1, bottomRight.col);

        if (lowerLeft != null && lowerRight != null && doesSquareExist(bottomLeft, lowerRight)) {
            return new Square(bottomLeft, lowerRight);
        }
        return null;
    }
    private Square getSquareLeft(Dot d1, Dot d2) {
        if (d1.col != d2.col) {
            return null; // Not a vertical line
        }

        if (d1.col == 0) {
            return null; // No square to the left of the first column
        }

        Dot top = d1.row < d2.row ? d1 : d2;
        Dot bottom = d1.row > d2.row ? d1 : d2;
        Dot leftTop = getDotAtRowCol(top.row, top.col - 1);
        Dot leftBottom = getDotAtRowCol(bottom.row, bottom.col - 1);

        if (leftTop != null && leftBottom != null && doesSquareExist(leftTop, bottom)) {
            return new Square(leftTop, bottom);
        }
        return null;
    }
    private Square getSquareRight(Dot d1, Dot d2) {
        if (d1.col != d2.col) {
            return null; // Not a vertical line
        }

        if (d1.col == cols - 1) {
            return null; // No square to the right of the last column
        }

        Dot top = d1.row < d2.row ? d1 : d2;
        Dot bottom = d1.row > d2.row ? d1 : d2;
        Dot rightTop = getDotAtRowCol(top.row, top.col + 1);
        Dot rightBottom = getDotAtRowCol(bottom.row, bottom.col + 1);

        if (rightTop != null && rightBottom != null && doesSquareExist(rightTop, bottom)) {
            return new Square(top, rightBottom);
        }
        return null;
    }



    public LinkedListCustom<Square> getCompletedSquares(Line line) {
        LinkedListCustom<Square> completedSquares = new LinkedListCustom<>();

        if (line.dot1.row == line.dot2.row) { // línea horizontal
            Square above = getSquareAbove(line.dot1, line.dot2);
            Square below = getSquareBelow(line.dot1, line.dot2);

            if (above != null) {
                completedSquares.add(above);
            }
            if (below != null) {
                completedSquares.add(below);
            }

        } else { // línea vertical
            Square left = getSquareLeft(line.dot1, line.dot2);
            Square right = getSquareRight(line.dot1, line.dot2);

            if (left != null) {
                completedSquares.add(left);
            }
            if (right != null) {
                completedSquares.add(right);
            }
        }

        return completedSquares;
    }



    private boolean checkSquareAbove(Dot left, Dot right) {
        Dot topLeft = getDotAtRowCol(left.row - 1, left.col);
        Dot topRight = getDotAtRowCol(right.row - 1, right.col);

        if(topLeft == null || topRight == null) return false;

        return lineExists(topLeft, left) && lineExists(topRight, right) && lineExists(topLeft, topRight);
    }

    private boolean checkSquareBelow(Dot left, Dot right) {
        Dot bottomLeft = getDotAtRowCol(left.row + 1, left.col);
        Dot bottomRight = getDotAtRowCol(right.row + 1, right.col);

        if(bottomLeft == null || bottomRight == null) return false;

        return lineExists(bottomLeft, left) && lineExists(bottomRight, right) && lineExists(bottomLeft, bottomRight);
    }

    private boolean checkSquareLeft(Dot top, Dot bottom) {
        Dot topLeft = getDotAtRowCol(top.row, top.col - 1);
        Dot bottomLeft = getDotAtRowCol(bottom.row, bottom.col - 1);

        if(topLeft == null || bottomLeft == null) return false;

        return lineExists(top, topLeft) && lineExists(bottom, bottomLeft) && lineExists(topLeft, bottomLeft);
    }

    private boolean checkSquareRight(Dot top, Dot bottom) {
        Dot topRight = getDotAtRowCol(top.row, top.col + 1);
        Dot bottomRight = getDotAtRowCol(bottom.row, bottom.col + 1);

        if(topRight == null || bottomRight == null) return false;

        return lineExists(top, topRight) && lineExists(bottom, bottomRight) && lineExists(topRight, bottomRight);
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


    class Square {
        Dot topLeft;  // Punto superior izquierdo del cuadrado
        Dot bottomRight; // Punto inferior derecho del cuadrado
        int size;     // Tamaño del cuadrado, asumiendo que todos los cuadrados son del mismo tamaño

        int color = -1; // Color del cuadrado, -1 si no se ha completado

        boolean isClosed(HashSet<String> lineSet) {
            Line top = new Line(topLeft, getDotAt(topLeft.x, topLeft.y + size));
            Line left = new Line(topLeft, getDotAt(topLeft.x + size, topLeft.y));
            Line right = new Line(getDotAt(topLeft.x + size, topLeft.y), getDotAt(topLeft.x + size, topLeft.y + size));
            Line bottom = new Line(getDotAt(topLeft.x, topLeft.y + size), getDotAt(topLeft.x + size, topLeft.y + size));

            if (lineSet.contains(top.getUniqueRepresentation()) &&
                    lineSet.contains(left.getUniqueRepresentation()) &&
                    lineSet.contains(right.getUniqueRepresentation()) &&
                    lineSet.contains(bottom.getUniqueRepresentation())) {
                this.color = (currentPlayer == 1) ? player1Color : player2Color; // Set color
                return true;
            }

            return false;
        }
        Square(Dot topLeft, Dot bottomRight) {
            this.topLeft = topLeft;
            this.bottomRight = bottomRight;
            this.size = (int) (bottomRight.x - topLeft.x);
        }
        void setColor(int playerColor) {
            this.color = playerColor;
        }

        void display() {
            if (color != -1) {
                fill(color);
                rect(topLeft.x, topLeft.y, size, size);
            }
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

/* public void mouseClicked(){
        Random rand = new Random();
        int rand_int1 = rand.nextInt(300);
        String mensaje =data.put("color",rand_int1).toString();

        send(mensaje);


    }
*/
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




