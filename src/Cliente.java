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
import java.lang.reflect.Type;
import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.HashSet;
import org.json.JSONObject;
import org.json.JSONArray;

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

    int rand_int1 = (int) Math.random();
    int player1Color = color(rand_int1+100, 0, 0);  // Red for player 1

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

                    String dot1x = String.valueOf(firstDot.x);
                    String dot1y = String.valueOf(firstDot.y);
                    String dot2x = String.valueOf(currentDot.x);
                    String dot2y = String.valueOf(currentDot.y);
                    String dot1c = String.valueOf(firstDot.col);
                    String dot1r = String.valueOf(firstDot.row);
                    String dot2c = String.valueOf(currentDot.col);
                    String dot2r = String.valueOf(currentDot.row);




                    lines.add(newLine);

                    send(dot1x+","+dot1y+","+dot2x+","+dot2y+","+dot1c+","+dot1r+","+dot2c+","+dot2r);



                    // Si se completa un cuadro, incrementa el puntaje del jugador correspondiente
                    LinkedListCustom<Square> completedSquares = getCompletedSquares(newLine);
                    if (completedSquares.size == 0) {
                        currentPlayer = (currentPlayer == 1) ? 2 : 1;
                    }

                    for (Square sq : completedSquares) {
                        squares.add(sq);
                        sq.setColor((currentPlayer == 1) ? player1Color : player1Color); // Set color
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

			JSONArray jsonLines = new JSONArray();
			for (Line line : lines) {
			    jsonLines.put(lineToJson(line));
			}
//			send(jsonLines.toString());
			
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
                    sq.setColor(player1Color);
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
        int radius;
        boolean isSelected = false;
        int row, col;

        Dot(float x, float y, int radius, int row, int col) {
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
            int radius = 5;
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
            stroke(player1Color);
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
                this.color = (currentPlayer == 1) ? player1Color : player1Color; // Set color
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
    public void sendLinesAndDots() {
        JSONArray jsonLines = new JSONArray();
        for (Line line : lines) {
            jsonLines.put(lineToJson(line));
        }
    
        // Si también deseas enviar puntos, crea otro JSONArray y agrégalo al mensaje.
    //     JSONArray jsonDots = new JSONArray();
    //     for (Dot dot : dots) {
    //         jsonDots.put(dotToJson(dot));
    //     }
    // 
        // Crear un objeto JSON principal que contiene tanto las líneas como los puntos
        // JSONObject mainJson = new JSONObject();
        // mainJson.put("lines", jsonLines);
        // mainJson.put("dots", jsonDots);
    
        // Enviar la representación en cadena del objeto JSON principal al servidor
//        send(jsonLines.toString());
    }
    
    public JSONObject lineToJson(Line line) {
        JSONObject json = new JSONObject();
        json.put("dot1", dotToJson(line.dot1));
        json.put("dot2", dotToJson(line.dot2));
        return json;
    }
    
    public JSONObject dotToJson(Dot dot) {
        JSONObject json = new JSONObject();
        json.put("x", dot.x);
        json.put("y", dot.y);
        json.put("row", dot.row);
        json.put("col", dot.col);
        return json;
    }
//    public Line jsonLineToLine(JSONObject json) {
//            JSONObject jsonDot1 = json.getJSONObject("dot1");
//            JSONObject jsonDot2 = json.getJSONObject("dot2");
//
//            Dot dot1 = jsonDotToDot(jsonDot1); // Asumiendo que tienes un método jsonDotToDot
//            Dot dot2 = jsonDotToDot(jsonDot2); // Asumiendo que tienes un método jsonDotToDot
//
//            return new Line(dot1, dot2);
//        }
    
//        public Dot jsonDotToDot(JSONObject json) {
//            float x = (float) json.getDouble("x");
//            float y = (float) json.getDouble("y");
//            int row = json.getInt("row");
//            int col = json.getInt("col");
//
//            return new Dot(x, y, dotSize, row, col); // Asumiendo que dotSize es el tamaño de tus puntos
//        }

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
            Socket serverSocket = server.accept();
            DataInputStream dataInput = new DataInputStream(serverSocket.getInputStream());
            
            String message = dataInput.readUTF();

            String[] vamos_aver = message.split(",");

            System.out.println(Float.valueOf(vamos_aver[0]));

            Dot dot1 = new Dot(Float.valueOf(vamos_aver[0]),Float.valueOf(vamos_aver[1]),5,Integer.parseInt(vamos_aver[5]),Integer.parseInt(vamos_aver[4]));
            Dot dot2 = new Dot(Float.valueOf(vamos_aver[2]),Float.valueOf(vamos_aver[3]),5,Integer.parseInt(vamos_aver[7]),Integer.parseInt(vamos_aver[6]));


            Line newLine = new Line(dot1, dot2);
            lines.add(newLine);
            getSquareAbove(dot1,dot2);
            getSquareBelow(dot1,dot2);
            getSquareLeft(dot1,dot2);
            getSquareRight(dot1,dot2);










            // Parsear el mensaje JSON recibido
//            JSONArray jsonLines = new JSONArray(message);
            
            // Iterar sobre las líneas JSON y agregarlas a la lista local de líneas
//            for (int i = 0; i < jsonLines.length(); i++) {
//                JSONObject jsonLine = jsonLines.getJSONObject(i);
//                Line newLine = jsonLineToLine(jsonLine); // Método para convertir JSON a Line
//                lines.add(newLine);
//
//                // Verificar si la nueva línea cierra un cuadrado y actualizar la interfaz y la puntuación
//                LinkedListCustom<Square> completedSquares = getCompletedSquares(newLine);
//                for (Square sq : completedSquares) {
//                    squares.add(sq);
//                    sq.setColor((currentPlayer == 1) ? player1Color : player1Color);
//                    if (currentPlayer == 1) {
//                        player1Score++;
//                    } else {
//                        player2Score++;
//                    }
//                }
//            }
        }
    } catch (Exception e) {
        System.out.println(e);
    }
}

    public static void main(String args[]) {
        Inicio  inicio = new Inicio();

        // while(inicio.numero ==0){
        //     System.out.println("1");
        // }
        java.awt.EventQueue.invokeLater(new Runnable() {


            public void run() {

                PApplet.main("Cliente");

            }

        });



    }



}




