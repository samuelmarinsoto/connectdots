import org.json.*;
import processing.core.PApplet;
import java.io.IOException;
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
import javax.swing.JOptionPane;

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
        public int size() {
            return size;
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
    DataOutputStream out;
    public static int color = 0;

    public static String samuel = "{}";


    public static int juego_iniciado = 0;
    LinkedListCustom<Dot> dots;
    LinkedListCustom<Line> lines;
    LinkedListCustom<Dot> selectedDots = new LinkedListCustom<Dot>();
    LinkedListCustom<Square> squares = new LinkedListCustom<Square>();
    int dotSize = 5;
    int rows; // Number of rows
    int cols; // Number of columns
    int score = 0;
    int currentPlayer = 1;

    int rand_int1 = (int) Math.random();
    int player1Color = color(rand_int1 + 100, 0, 0);  // Red for player 1

    int selectedIndex = 0;
    int selectedRow = 0;
    int selectedCol = 0;
    String errorMsg = "";
    int player1Score = 0;
    int player2Score = 0;


    Dot firstDot = null;


    DataInputStream in;

    public Cliente() {
        this.rows = 10;
        this.cols = 10;
        try {
            this.socket = new Socket("127.0.0.1", 5000);
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream()); // Añadido aquí
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread hilo = new Thread(this);
        hilo.start();
    }


    public void settings() {
        size(400, 400);
        dots = new LinkedListCustom<Dot>();
        lines = new LinkedListCustom<Line>();
        generateDots();
    }

    public void setup() {
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
        fill(255, 0, 0);
        text(errorMsg, 20, 90);
    }

    ;


    public void generateDots() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                dots.add(new Dot(i, j)); // Sólo pasamos la fila y la columna al constructor de Dot
            }
        }
    }

    public void keyPressed() {
        // Manejar la entrada del usuario para mover el punto seleccionado
        if (key == 'A' || key == 'a') {
            selectedRow = max(0, selectedRow - 1);
        } else if (key == 'W' || key == 'w') {
            selectedCol = max(0, selectedCol - 1);
        } else if (key == 'D' || key == 'd') {
            selectedRow = min(rows - 1, selectedRow + 1);
        } else if (key == 'S' || key == 's') {
            selectedCol = min(cols - 1, selectedCol + 1);
        } else  if (key == 'L' || key == 'l') {
            Dot currentDot = getDotAtRowCol(selectedRow, selectedCol);
            currentDot.setSelected(true);
            selectedDots.add(currentDot);

            if (selectedDots.size() == 1) {
                // Imprime un mensaje cuando el primer punto es seleccionado
                System.out.println("Primer punto seleccionado");
            } else if (selectedDots.size() == 2) {
                // Dibuja una línea cuando el segundo punto es seleccionado
                Line newLine = new Line(selectedDots.get(0), selectedDots.get(1));
                lines.add(newLine);
                // Vacía la lista de puntos seleccionados para la próxima línea
                selectedDots = new LinkedListCustom<Dot>();
            }
        }
    }

    // Comunicacion al servidor

    public void sendActionToServer(Dot currentDot) {
        JSONObject actionMessage = new JSONObject();
        actionMessage.put("player", socket.getLocalPort());
        actionMessage.put("dot", dotToJson(currentDot));

        // Convierte el objeto JSON a una cadena y haz un print de ella
        String jsonMessage = actionMessage.toString();
        System.out.println("Enviando mensaje al servidor: " + jsonMessage);

        try {
            out.writeUTF(jsonMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private JSONObject dotToJson(Dot currentDot) {
        JSONObject json = new JSONObject();
        json.put("row", currentDot.row);
        json.put("col", currentDot.col);
        return json;
    }
    public Dot jsonDotToDot(JSONObject json) {
    int row = json.getInt("row");
    int col = json.getInt("col");
    return new Dot(row, col);
}

    public Line jsonLineToLine(JSONObject jsonLine) {
        // Aquí debes extraer la información necesaria del JSONObject
        // y utilizarla para crear y devolver un nuevo objeto Line.
        // Este es solo un ejemplo básico:
        JSONObject jsonDot1 = jsonLine.getJSONObject("dot1");
        JSONObject jsonDot2 = jsonLine.getJSONObject("dot2");

        Dot dot1 = jsonDotToDot(jsonDot1); // Asumiendo que tienes un método jsonDotToDot
        Dot dot2 = jsonDotToDot(jsonDot2);

        return new Line(dot1, dot2);
    }



    //Metodos de utilidad

//    public void send(String hola){
//            try {
//                out.writeUTF(hola);
//            }  catch(Exception ex){
//                System.out.println(ex);
//            }
//        }
//    public void sendLinesAndDots() {
//        JSONArray jsonLines = new JSONArray();
//        for (Line line : lines) {
//            jsonLines.put(lineToJson(line));
//        }
//        send(jsonLines.toString());
//    }
//
//    public JSONObject lineToJson(Line line) {
//        JSONObject json = new JSONObject();
//        json.put("dot1", dotToJson(line.dot1));
//        json.put("dot2", dotToJson(line.dot2));
//        return json;
//    }
//
//    public JSONObject dotToJson(Dot dot) {
//        JSONObject json = new JSONObject();
//        json.put("x", dot.x);
//        json.put("y", dot.y);
//        json.put("row", dot.row);
//        json.put("col", dot.col);
//        return json;
//    }
//    public Line jsonLineToLine(JSONObject json) {
//            JSONObject jsonDot1 = json.getJSONObject("dot1");
//            JSONObject jsonDot2 = json.getJSONObject("dot2");
//
//            Dot dot1 = jsonDotToDot(jsonDot1); // Asumiendo que tienes un método jsonDotToDot
//            Dot dot2 = jsonDotToDot(jsonDot2); // Asumiendo que tienes un método jsonDotToDot
//
//            return new Line(dot1, dot2);
//        }
//
//        public Dot jsonDotToDot(JSONObject json) {
//            float x = (float) json.getDouble("x");
//            float y = (float) json.getDouble("y");
//            int row = json.getInt("row");
//            int col = json.getInt("col");
//
//            return new Dot(x, y, dotSize, row, col); // Asumiendo que dotSize es el tamaño de tus puntos
//        }

    //Metodos auxiliares de logica del juego


    public boolean lineExists(Dot d1, Dot d2) {
        for (Line line : lines) {
            if ((line.dot1 == d1 && line.dot2 == d2) || (line.dot1 == d2 && line.dot2 == d1)) {
                return true;
            }
        }
        return false;
    }
    private boolean doesSquareExist(Dot topLeft, Dot bottomRight) {
        Dot topRight = getDotAtRowCol(topLeft.row, bottomRight.col);
        Dot bottomLeft = getDotAtRowCol(bottomRight.row, topLeft.col);

        return lineExists(topLeft, topRight) && lineExists(topLeft, bottomLeft) && lineExists(bottomRight, bottomLeft) && lineExists(bottomRight, topRight);
    }
    Dot getDotAtRowCol(int row, int col) {
        for (Dot dot : dots) {
            if (dot.row == row && dot.col == col) {
                return dot;
            }
        }
        return null;
    }
    Dot getDotAt(int x, int y) {
        for (Dot dot : dots) {
            if (Math.abs(dot.row - x) < 1 && Math.abs(dot.col - y) < 1) {
                return dot;
            }
        }
        return null;
    }
    static int convertRowToX(int row) {
            int spacingX = 40; // Distancia entre los puntos en el eje x
            int marginLeft = 20; // Margen inicial en el eje x
            return marginLeft + row * spacingX;
        }

        static int convertColToY(int col) {
            int spacingY = 40; // Distancia entre los puntos en el eje y
            int marginTop = 20; // Margen inicial en el eje y
            return marginTop + col * spacingY;
        }

    //CLASES INTERNAS
    class Dot {
    int row, col;
    boolean isSelected = false; // Nuevo campo para indicar si el Dot está seleccionado

    Dot(int row, int col) {
        this.row = row;
        this.col = col;
    }

    void display() {
        noStroke();
        int x = Cliente.convertRowToX(row);
        int y = Cliente.convertColToY(col);

        if (row == selectedRow && col == selectedCol) {
            fill(0, 0, 255);  // Blue for the currently highlighted dot
        } else if (isSelected) {
            fill(0, 255, 0);  // Green for the selected dot
        } else {
            fill(100, 100, 100);
        }

        int radius = 5; // Puedes ajustar el valor del radio aquí
        ellipse(x, y, radius * 2, radius * 2);
    }

        String getIdentifier() {
            return row + "," + col;
        }

        // Nuevo método para establecer el campo isSelected
        void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }
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
    public Square getSquareRight(Dot d1, Dot d2) {
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

    class Line {
        Dot dot1, dot2;
        int lineColor = 0;

        Line(Dot dot1, Dot dot2) {
            this.dot1 = dot1;
            this.dot2 = dot2;
            this.lineColor = 0;
        }

        void display() {
            stroke(player1Color);
            int x1 = Cliente.convertRowToX(dot1.row);
            int y1 = Cliente.convertColToY(dot1.col);
            int x2 = Cliente.convertRowToX(dot2.row);
            int y2 = Cliente.convertColToY(dot2.col);
            line(x1, y1, x2, y2);
        }

        String getUniqueRepresentation() {
            return dot1.getIdentifier() + "-" + dot2.getIdentifier();
        }
    }

    class Square {
        Dot topLeft;
        Dot bottomRight;
        int size;

        int color = -1;

        boolean isClosed(HashSet<String> lineSet) {
            int sizePixel = Cliente.convertRowToX(1) - Cliente.convertRowToX(0);
            Line top = new Line(topLeft, getDotAt(topLeft.row, topLeft.col + 1));
            Line left = new Line(topLeft, getDotAt(topLeft.row + 1, topLeft.col));
            Line right = new Line(getDotAt(topLeft.row + 1, topLeft.col), getDotAt(topLeft.row + 1, topLeft.col + 1));
            Line bottom = new Line(getDotAt(topLeft.row, topLeft.col + 1), getDotAt(topLeft.row + 1, topLeft.col + 1));
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
            this.size = Cliente.convertRowToX(bottomRight.row) - Cliente.convertRowToX(topLeft.row);
        }

        void setColor(int playerColor) {
            this.color = playerColor;
        }

        void display() {
            if (color != -1) {
                fill(color);
                int x = Cliente.convertRowToX(topLeft.row);
                int y = Cliente.convertColToY(topLeft.col);
                rect(x, y, size, size);
            }
        }
    }

    //Métodos de lógica del juego



    public void run() {
        try {
            ServerSocket server = new ServerSocket(0);
            Socket socket = new Socket("127.0.0.1", 5000);

            String puerto_codificado = String.valueOf(server.getLocalPort());

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(puerto_codificado);

            while (true) {
                Socket serverSocket = server.accept();
                DataInputStream dataInput = new DataInputStream(serverSocket.getInputStream());

                String message = dataInput.readUTF();
                System.out.println("Mensaje recibido del servidor: " + message);

                // Parsear el mensaje JSON recibido
                JSONArray jsonLines = new JSONArray(message);

                // Iterar sobre las líneas JSON y agregarlas a la lista local de líneas
                for (int i = 0; i < jsonLines.length(); i++) {
                    JSONObject jsonLine = jsonLines.getJSONObject(i);

                    // Convertir JSON a Line y agregar a la lista de líneas
                    Line newLine = jsonLineToLine(jsonLine);
                    lines.add(newLine);

                    // Verificar si la nueva línea cierra un cuadrado y actualizar la interfaz y la puntuación
                    LinkedListCustom<Square> completedSquares = getCompletedSquares(newLine);
                    for (Square sq : completedSquares) {
                        squares.add(sq);
                        sq.setColor((currentPlayer == 1) ? player1Color : player1Color);
                        if (currentPlayer == 1) {
                            player1Score++;
                        } else {
                            player2Score++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        Inicio  inicio = new Inicio();
            while(inicio.numero ==0){
                try {
                    TimeUnit.SECONDS.sleep(1);
                System.out.println("1");
                } catch (Exception e){
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    PApplet.main("Cliente");
                }
            });
        }
    }





