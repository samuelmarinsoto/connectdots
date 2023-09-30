/**
 * Librerias usadas
 */


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.awt.Color;
import java.util.LinkedList;
import processing.core.PApplet;
import java.io.IOException;

import org.json.JSONObject;
import javax.swing.JOptionPane;
import org.json.JSONException;

import org.json.JSONArray;


/**
 * Variables gobales
 * listas de los objetos
 */

public class Servidor extends PApplet {
    // Variables de estado del juego
    int dotSize = 5;
    int rows; // Number of rows
    int cols; // Number of columns
    private Dot firstDot;
    private Dot currentDot;
    LinkedListCustom<Dot> dots;
    LinkedListCustom<Line> lines;
    LinkedListCustom<Square> squares = new LinkedListCustom<Square>();
    LinkedListCustom<Dot> selectedDots = new LinkedListCustom<Dot>();
    private List<Player> players = new ArrayList<>();
    // Asumiendo que estás dentro de un método de la clase 'Servidor'
    List<Socket> allClientSockets = new ArrayList<>();

    String errorMsg = "";
    private Queue<Player> playerQueue = new Queue<>();
    long elapsedTime;
    boolean gameStarted = false;
    DataInputStream out;
    private  ArrayList<Color> colors = new ArrayList<>(
            List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN));
    private LinkedList<Color> coloresDisponibles = new LinkedList<>(List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN));
    LinkedListCustom<Square> completedSquares = new LinkedListCustom<>();
    Map<Integer, Player> colorToPlayerMap = new HashMap<>();

    // Definición de la clase Nodo para la cola
    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
            this.next = null;
        }
    }
    // Definición de la clase Cola
    private static class Queue<T> {
        Node<T> front, rear;
        int size;
        public Queue(){
            front = rear = null;
            size = 0;
        }

        // Método para encolar un elemento.
        // Método para encolar un elemento.
        void enqueue(T value) {
            Node<T> newNode = new Node<>(value);
            if (rear == null) {
                front = rear = newNode;
            } else {
                rear.next = newNode;
                rear = newNode;
            }
            size++; // Incrementar el tamaño de la cola
        }

        // Método para desencolar un elemento.
        T dequeue() {
            if (front == null) {
                return null;
            }
            Node<T> temp = front;
            front = front.next;
            if (front == null) {
                rear = null;
            }
            size--; // Decrementar el tamaño de la cola
            return temp.value;
        }
        // Método para verificar si la cola está vacía.
        boolean isEmpty() {
            return front == null;
        }
        int size(){
            return size;
        }
    }
    static class LinkedListCustom<T> implements Iterable<T> {
        Node<T> head;
        int size;

        LinkedListCustom() {
            head = null;
            size = 0;
        }

        public boolean isEmpty() {
            return size() == 0;
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
    /*
    Aqui se ingresan el numero de filas y columnas
     */

    /**
     * Se fija si ya terminaron los puntos, por lo tanto, es el final del juego
     *
     */
    public boolean isMaxLinesReached(int rows, int cols) {
        int maxLines = (rows - 1) * cols + rows * (cols - 1);
        boolean maxReached = lines.size() == maxLines;

        if (maxReached) {
            int max = 0;
             Object nombre = null;

            for (Player player : players) {

                try {
                    System.out.println(player.score);
                    if(player.score>=max){
                        max = player.score;
                        nombre = "("+player.color.getRed()+","+player.color.getGreen()+","+player.color.getBlue()+")";

                    }

                    DataOutputStream out = new DataOutputStream(player.getSocket().getOutputStream());
                    out.writeUTF("CLOSE_CLIENT");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            JOptionPane.showMessageDialog(null, "Se han creado todas las líneas posibles,"+"ganador: "+nombre+" con puntaje de: "+max, "Información", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }

        return maxReached;
    }


    /**
     * todos principales de processing
     */
    public void settings() {
    // Solicitar al usuario que ingrese el número de filas y columnas, separados por coma (,):
    String input;
    int minSize = 2; // Tamaño mínimo permitido
    do {
        input = JOptionPane.showInputDialog(null, "Ingrese el número de filas y columnas, separados por coma (,):");

        // Verificar si el usuario presionó cancelar
        if (input == null) {
            System.out.println("El usuario canceló la operación.");
            exit(); // Finalizar la ejecución de la aplicación
            return;
        }

        // Dividir la cadena de entrada para obtener rows y cols
        String[] parts = input.split(",");
        if (parts.length != 2) {
            JOptionPane.showMessageDialog(null, "Entrada no válida. Debe ingresar dos números separados por coma.");
        } else {
            // Convertir las cadenas a números
            try {
                rows = Integer.parseInt(parts[0].trim());
                cols = Integer.parseInt(parts[1].trim());

                // Verificar si rows y cols son iguales o mayores que minSize
                if (rows < minSize || cols < minSize) {
                    JOptionPane.showMessageDialog(null, "El número de filas y columnas debe ser igual o mayor que " + minSize + ".");
                } else {
                    // Tamaño válido, salir del bucle
                    break;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Entrada no válida. Debe ingresar números enteros.");
            }
        }
    } while (true);

        int windowSizeX = convertRowToX(rows) - 10; // Ajustar según sea necesario
        int windowSizeY = convertColToY(cols) - 10; // Ajustar según sea necesario

        size(windowSizeX, windowSizeY);

        dots = new LinkedListCustom<>();
        lines = new LinkedListCustom<>();
        generateDots();
    }
    /*
    Métodos para convertir entre coordenadas de la ventana y coordenadas de la matriz
     */
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
    /*
    Métodos para inicializar y ejecutar el servidor
     */
    public void setup() {
        System.out.println("Configurando setup y arrancando servidor");
        new Thread(this::startServer).start();
    }
    // BUCLE PARA PINTAR LAS COSAS
    public void draw() {
        background(255);
        for (Square sq : completedSquares) {
            sq.display();
        }
        isMaxLinesReached(cols,rows);

        // Display the lines
        for (Line line : lines) {
            line.display();
        }

        // Display the dots
        for (Dot dot : dots) {
            dot.display();
        }
        fill(150);
        // Verificar si errorMsg es diferente de null y mostrarlo en la ventana
        if (errorMsg == null) {
            fill(150);
//            text(errorMsg, 20, 90);
        }


    }


    /**
    AQUÍ ESTÁN LOS OBJETOS QUE SE USAN EN EL JUEGO
     */
    private class Dot {
        int row, col;

        Dot(int row, int col) {
            this.row = row;
            this.col = col;
        }

        String getIdentifier() {
            return Integer.toString(row) + Integer.toString(col);
        }

        void display() {
            noStroke();
            int x = Servidor.convertRowToX(row);
            int y = Servidor.convertColToY(col);

            fill(100, 100, 100);


            int radius = 5; // Puedes ajustar el valor del radio aquí
            ellipse(x, y, radius * 2, radius * 2);
        }
    }

    private class Line {
        Dot dot1, dot2;
        int lineColor;
        int port;

        Line(Dot dot1, Dot dot2, int port) {
            this.dot1 = dot1;
            this.dot2 = dot2;
            this.lineColor = 0;
            this.port = port;
        }


        void display() {
            stroke(lineColor);
            int x1 = Servidor.convertRowToX(dot1.row);
            int y1 = Servidor.convertColToY(dot1.col);
            int x2 = Servidor.convertRowToX(dot2.row);
            int y2 = Servidor.convertColToY(dot2.col);
            line(x1, y1, x2, y2);
        }
        public JSONObject toJsonObject() {
            JSONObject jsonLine = new JSONObject();

            JSONObject jsonDot1 = new JSONObject();
            jsonDot1.put("row", dot1.row);
            jsonDot1.put("col", dot1.col);

            JSONObject jsonDot2 = new JSONObject();
            jsonDot2.put("row", dot2.row);
            jsonDot2.put("col", dot2.col);

            jsonLine.put("dot1", jsonDot1);
            jsonLine.put("dot2", jsonDot2);
            jsonLine.put("lineColor", lineColor);
            jsonLine.put("port", port);

            return jsonLine;
        }
        String getUniqueRepresentation() {
            return dot1.getIdentifier() + "-" + dot2.getIdentifier();
        }
        @Override
        public String toString() {
            return "Line from " + dot1 + " to " + dot2 + " with color: " + lineColor;
        }
    }

    private class Square {
        Dot topLeft;
        Dot bottomRight;
        int size;
        int color; // Color en formato RGB
        int port;  // Puerto del cliente que envió el cuadrado

        public Square(Dot topLeft, Dot bottomRight, int port) {
            this.topLeft = topLeft;
            this.bottomRight = bottomRight;
            this.port = port;
            this.size = Math.abs(bottomRight.col - topLeft.col);
        }

        /**
         * Constructor using a JSONObject
          */

        public Square(JSONObject jsonSquare) {
            JSONObject jsonTopLeft = jsonSquare.getJSONObject("topLeft");
            JSONObject jsonBottomRight = jsonSquare.getJSONObject("bottomRight");

            this.topLeft = new Dot(jsonTopLeft.getInt("row"), jsonTopLeft.getInt("col"));
            this.bottomRight = new Dot(jsonBottomRight.getInt("row"), jsonBottomRight.getInt("col"));
            this.port = jsonSquare.getInt("port");
            this.color = jsonSquare.getInt("color");
            this.size = Math.abs(bottomRight.col - topLeft.col);
        }
        boolean isClosed(HashSet<String> lineSet) {
        Dot topRight = getDotAtRowCol(topLeft.row, bottomRight.col);
        Dot bottomLeft = getDotAtRowCol(bottomRight.row, topLeft.col);


            /**
             * Crear una representación única de cada línea que debería estar presente para cerrar el cuadrado.
              */

        String line1 = topLeft.toString() + topRight.toString();
        String line2 = topLeft.toString() + bottomLeft.toString();
        String line3 = bottomLeft.toString() + bottomRight.toString();
        String line4 = bottomRight.toString() + topRight.toString();

            /**Verificar si todas las líneas están presentes en el conjunto.
             *
             */
        return lineSet.contains(line1) && lineSet.contains(line2) && lineSet.contains(line3) && lineSet.contains(line4);
    }
    void display() {
        if (color != -1) {
            System.out.println("Drawing square with color: " + color);
            fill(color);
            int x = Servidor.convertRowToX(topLeft.row);
            int y = Servidor.convertColToY(topLeft.col);
            rect(x, y, size, size);
        } else {
            System.out.println("Color is -1, square is not drawn");
        }
    }


        /** Método para obtener el color en función del puerto.
         * Puedes modificar esta función según tu lógica para asociar puertos y colores.

         */

        private int getColorFromPort(int port) {
            // Ejemplo: supongamos que el puerto 5000 corresponde al color rojo
            if(port == 5000) {
                return color(255, 0, 0); // Rojo en formato RGB
            }
            // Añadir más reglas aquí según sea necesario
            return -1; // Color por defecto (o puedes lanzar una excepción si un puerto no válido es inaceptable)
        }

        // Método para convertir el cuadrado a un objeto JSON
        public JSONObject toJsonObject() {
            JSONObject jsonSquare = new JSONObject();

            JSONObject jsonTopLeft = new JSONObject();
            jsonTopLeft.put("row", topLeft.row);
            jsonTopLeft.put("col", topLeft.col);

            JSONObject jsonBottomRight = new JSONObject();
            jsonBottomRight.put("row", bottomRight.row);
            jsonBottomRight.put("col", bottomRight.col);

            jsonSquare.put("topLeft", jsonTopLeft);
            jsonSquare.put("bottomRight", jsonBottomRight);
            jsonSquare.put("color", color); // Color RGB como un entero
            jsonSquare.put("port", port);   // Puerto del cliente que envió el cuadrado

            return jsonSquare;
        }
    }

    public class Player {
        private Color color;
        private Socket socket;
        private int score;
        public Player(Color color, Socket socket) {
            this.color = color;
            this.socket = socket;
        }

        public Color getColor() {
            return color;
        }

        public Socket getSocket() {
            return socket;
        }
        public int getScore() {
            return score;
        }

        public void incrementScore() {
            this.score++;
        }
    }


    /**Metodos de inicializacion
     *
     */
    public void generateDots() {
        System.out.println("Generando puntos...");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                dots.add(new Dot(i, j));
            }
        }
    }


    ////////////////////////////////////////////

    /**Metodos de logica del juego

     */


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
    private boolean doesSquareExist(Dot topLeft, Dot bottomRight) {
        Dot topRight = getDotAtRowCol(topLeft.row, bottomRight.col);
        Dot bottomLeft = getDotAtRowCol(bottomRight.row, topLeft.col);

        return lineExists(topLeft, topRight) && lineExists(topLeft, bottomLeft) && lineExists(bottomRight, bottomLeft) && lineExists(bottomRight, topRight);
    }
    public LinkedListCustom<Square> getCompletedSquares(Line line, int port) {
        LinkedListCustom<Square> completedSquares = new LinkedListCustom<>();

        if (line.dot1.row == line.dot2.row) { // línea horizontal
            Square above = getSquareAbove(line.dot1, line.dot2, port);
            Square below = getSquareBelow(line.dot1, line.dot2, port);

            if (above != null) {
                completedSquares.add(above);
            }
            if (below != null) {
                completedSquares.add(below);
            }

        } else { // línea vertical
            Square left = getSquareLeft(line.dot1, line.dot2, port);
            Square right = getSquareRight(line.dot1, line.dot2, port);

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

        if (topLeft == null || topRight == null) return false;

        return lineExists(topLeft, left) && lineExists(topRight, right) && lineExists(topLeft, topRight);
    }
    private boolean checkSquareBelow(Dot left, Dot right) {
        Dot bottomLeft = getDotAtRowCol(left.row + 1, left.col);
        Dot bottomRight = getDotAtRowCol(right.row + 1, right.col);

        if (bottomLeft == null || bottomRight == null) return false;

        return lineExists(bottomLeft, left) && lineExists(bottomRight, right) && lineExists(bottomLeft, bottomRight);
    }
    private boolean checkSquareLeft(Dot top, Dot bottom) {
        Dot topLeft = getDotAtRowCol(top.row, top.col - 1);
        Dot bottomLeft = getDotAtRowCol(bottom.row, bottom.col - 1);

        if (topLeft == null || bottomLeft == null) return false;

        return lineExists(top, topLeft) && lineExists(bottom, bottomLeft) && lineExists(topLeft, bottomLeft);
    }
    private boolean checkSquareRight(Dot top, Dot bottom) {
        Dot topRight = getDotAtRowCol(top.row, top.col + 1);
        Dot bottomRight = getDotAtRowCol(bottom.row, bottom.col + 1);

        if (topRight == null || bottomRight == null) return false;

        return lineExists(top, topRight) && lineExists(bottom, bottomRight) && lineExists(topRight, bottomRight);
    }
    boolean areAdjacentDots(Dot dot1, Dot dot2) {
        int dRow = Math.abs(dot1.row - dot2.row);
        int dCol = Math.abs(dot1.col - dot2.col);

        return ((dRow == 1 && dCol == 0) || (dRow == 0 && dCol == 1));
    }
    boolean isLineExists(Dot d1, Dot d2) {
        for (Line line : lines) {
            if ((line.dot1 == d1 && line.dot2 == d2) || (line.dot1 == d2 && line.dot2 == d1)) {
                return true;
            }
        }
        return false;
    }
    Dot getDotAt(int x, int y) {
        for (Dot dot : dots) {
            if (Math.abs(dot.row - x) < 1 && Math.abs(dot.col - y) < 1) {
                return dot;
            }
        }
        return null;
    }
    private Square getSquareAbove(Dot d1, Dot d2, int port) {
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
            return new Square(upperLeft, topRight, port);
        }
        return null;
    }
    private Square getSquareBelow(Dot d1, Dot d2, int port) {
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
            return new Square(bottomLeft, lowerRight, port);
        }
        return null;
    }
    private Square getSquareLeft(Dot d1, Dot d2, int port) {
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
            return new Square(leftTop, bottom, port);
        }
        return null;
    }
    private Square getSquareRight(Dot d1, Dot d2, int port) {
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
            return new Square(top, rightBottom, port);
        }
        return null;
    }


/**
Aqui empieza la logica de los jsons y colores etc.
 */



    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        // Añade los campos de la línea al objeto json
        // Por ejemplo:
        // json.put("start", this.start);
        // json.put("end", this.end);
        return json;
    }
    public Dot jsonDotToDot(JSONObject json) {
        // Extraer las propiedades fila y columna del objeto JSON
        int row = json.getInt("row");
        int col = json.getInt("col");

        // Devolver una nueva instancia de Dot utilizando las propiedades
        return new Dot(row, col);
    }

    private Line jsonLineToLine(JSONObject jsonLine, int port, int colorRGB) {
        // Extraer los objetos JSON para dot1 y dot2
        JSONObject jsonDot1 = jsonLine.getJSONObject("dot1");
        JSONObject jsonDot2 = jsonLine.getJSONObject("dot2");

        // Extraer las coordenadas row y col de dot1 y dot2
        int row1 = jsonDot1.getInt("row");
        int col1 = jsonDot1.getInt("col");
        int row2 = jsonDot2.getInt("row");
        int col2 = jsonDot2.getInt("col");

        // Obtener los objetos Dot correspondientes a las coordenadas
        Dot dot1 = getDotAtRowCol(row1, col1);
        Dot dot2 = getDotAtRowCol(row2, col2);

        // Crear y retornar un nuevo objeto Line con los puntos dot1 y dot2
        Line line = new Line(dot1, dot2, port);
        line.lineColor = colorRGB;
        return line;
    }
    public JSONObject lineToJson(Line line) {
        JSONObject jsonLine = new JSONObject();

        JSONObject jsonDot1 = new JSONObject();

        jsonDot1.put("row", line.dot1.row);
        jsonDot1.put("col", line.dot1.col);

        JSONObject jsonDot2 = new JSONObject();
        jsonDot2.put("row", line.dot2.row);
        jsonDot2.put("col", line.dot2.col);

        jsonLine.put("tipo", "LineaEnviadaPorCliente");
        jsonLine.put("dot1", jsonDot1);
        jsonLine.put("dot2", jsonDot2);
        jsonLine.put("color", line.lineColor);

        return jsonLine;
    }
    public void sendLineToAllClients(Line line) {
        JSONObject jsonLine = lineToJson(line);
        // Cambiar "LineaEnviadaPorCliente" a "LineaRecibida" para que los clientes sepan que deben dibujar la línea.
        jsonLine.put("tipo", "LineaRecibida");
        String lineString = jsonLine.toString();

        for (Player player : players) {
            try {
                DataOutputStream out = new DataOutputStream(player.getSocket().getOutputStream());
                out.writeUTF(lineString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Square jsonToSquare(JSONObject jsonSquare, int port) {
        // Obtener los objetos JSON para los puntos superior izquierdo e inferior derecho
        JSONObject jsonTopLeft = jsonSquare.getJSONObject("topLeft");
        JSONObject jsonBottomRight = jsonSquare.getJSONObject("bottomRight");

        // Convertir los objetos JSON de los puntos a objetos Dot
        Dot topLeft = jsonDotToDot(jsonTopLeft);
        Dot bottomRight = jsonDotToDot(jsonBottomRight);

        // Crear y devolver una nueva instancia de Square utilizando los puntos y el puerto
        return new Square(topLeft, bottomRight, port);
    }
    private JSONObject dotToJson(Dot dot) {
        JSONObject json = new JSONObject();
        json.put("row", dot.row);
        json.put("col", dot.col);
        return json;
    }

    private JSONObject squareToJson(Square square, int color) {
        JSONObject json = new JSONObject();
        json.put("tipo", "CuadradoCompletado");
        json.put("topLeft", dotToJson(square.topLeft));
        json.put("bottomRight", dotToJson(square.bottomRight));
        json.put("color", color);
        return json;
}

    private void sendSquareToAllClients(Square square, int color) {
        // Convierte el cuadrado a JSON
        JSONObject jsonSquare = squareToJson(square, color);

        // Envia a todos los clientes
        for (Socket clientSocket : allClientSockets) {
            try {
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                out.writeUTF(jsonSquare.toString());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                // Manejar la excepción, posiblemente eliminando el socket del cliente de la lista si la conexión se ha perdido
            }
        }
    }

    public void handleSquareReceived(JSONObject jsonSquare) {
        try {
            Square square = new Square(jsonSquare);

            /** Obtener el jugador basado en el color del cuadrado y actualizar la puntuación
             *
             */
            Player player = colorToPlayerMap.get(square.color);
            if (player != null) {
                player.incrementScore();
                /** Puedes también notificar a todos los clientes sobre la puntuación actualizada
                 si es necesario.*/
            } else {
                // Manejar el caso cuando no hay jugador para el color dado.
                System.err.println("No player found for color: " + square.color);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void startGameCountdown() {
        elapsedTime = System.currentTimeMillis(); // Inicializar elapsedTime aquí
        Thread countdownThread = new Thread(() -> {
            try {
                while (true) {
                    long currentTime = System.currentTimeMillis();
                    long timeLeft = 15000 - (currentTime - elapsedTime);

                    if (!gameStarted && timeLeft <= 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        gameStarted = true;
                        System.out.println("La partida ha comenzado.");

                        // Informar a todos los clientes conectados que la partida ha comenzado
                        for (Player player : players) {
                            try {
                                System.out.println("Enviando inicioPartida al jugador con socket: " + player.getSocket()); // Mensaje de registro
                                DataOutputStream out = new DataOutputStream(player.getSocket().getOutputStream());
                                JSONObject jsonResponse = new JSONObject();
                                jsonResponse.put("tipo", "inicioPartida");
                                out.writeUTF(jsonResponse.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // Dormir durante un segundo antes de verificar nuevamente
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        countdownThread.start();
    }
    private Color getNextPlayerColor() {
            if (coloresDisponibles.isEmpty()) {
                /** Si se agotaron los colores disponibles, reiniciar la lista de colores
                 *
                 */
                coloresDisponibles.addAll(colors);
            }
            Color playerColor = coloresDisponibles.poll(); // Obtener el siguiente color disponible
            return playerColor;

        }
    class ClientHandler implements Runnable {
        private Socket clientSocket;
        private Color playerColor;
        private List<Player> players;
        private DataOutputStream out;

        public ClientHandler(Socket socket, List<Player> players, Color playerColor) {
            this.clientSocket = socket;
            this.playerColor = playerColor;
            this.players = players;
            try {
                this.out = new DataOutputStream(clientSocket.getOutputStream());
                // Aquí es donde enviaríamos el color al cliente después de asignarlo:
                JSONObject colorMessage = new JSONObject();
                colorMessage.put("tipo", "asignacionColor");
                colorMessage.put("color", playerColor.getRGB());
                out.writeUTF(colorMessage.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Color buscarColorPorPuerto(int puerto) {
            for (Player player : players) {
                if (player.getSocket().getPort() == puerto) {
                    return player.getColor();
                }
            }
            return null;
        }


        private List<Line> buscarLineasPorPuerto(int puerto) {
            // Implementa la lógica para buscar las líneas por puerto
            return new ArrayList<>();
        }


        @Override
        public void run() {
            try {
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                while (!clientSocket.isClosed()) { // Mientras el socket no esté cerrado
                    // Leer el JSON enviado por el cliente
                    String jsonStr = in.readUTF();

                    // Imprimir cada mensaje recibido
                    System.out.println("Mensaje recibido: " + jsonStr);

                    JSONObject json = new JSONObject(jsonStr);
                    // Procesar el objeto JSON según su tipo
                    String tipo = json.getString("tipo");
                    switch (tipo) {
                        case "puerto":
                            // Manejar mensaje de puerto
                            int puerto = json.getInt("puerto");
                            // Aquí se puede procesar el puerto recibido
                            System.out.println("Puerto recibido: " + puerto);
                            break;
                        case "LineaEnviadaPorCliente":
                            // Obtener el puerto y el objeto JSON de la línea
                            int PuertoDeRecepcionDeLinea = json.getInt("puerto");
                            JSONObject jsonLine = json.getJSONObject("linea");
                            int lineColorRGB = json.getInt("color");  // Asumiendo que el color está como un entero RGB

                            /** Convertir el objeto JSON a un objeto Line
                             *
                             */
                            Line LineaEnviadaPorCliente = jsonLineToLine(jsonLine, PuertoDeRecepcionDeLinea, lineColorRGB);

                            /** Imprimir información de la línea recibida
                             *
                             */
                            System.out.println("Línea recibida: " + LineaEnviadaPorCliente.toString());

                            /** Añadir la línea a una lista (si es necesario) y repintar la ventana
                             *
                             */
                            lines.add(LineaEnviadaPorCliente);
                            redraw();


                            /** Enviar la línea a todos los clientes
                             *
                             */
                            sendLineToAllClients(LineaEnviadaPorCliente);



                            break;
                        case "CuadradoEnviadoPorCliente":
                           System.out.println("Mensaje de tipo CuadradoEnviadoPorCliente recibido");

                            /** Obtener el puerto y el objeto JSON del cuadrado
                             *
                             */
                            int port = json.getInt("puerto");
                            JSONObject jsonCuadrado = json.getJSONObject("cuadrado");

                            /** Convertir el objeto JSON a un objeto Square
                             *
                             */
                            Square square = jsonToSquare(jsonCuadrado, port);

                            // Incrementar el puntaje del jugador
                            for (Player player : players) {
                                if (player.getSocket().equals(clientSocket)) {
                                    player.incrementScore();
                                    break;
                                }
                            }
                            /** Obtener el color del cuadrado del JSON
                             *
                             */
                            int color = jsonCuadrado.getInt("color");

                            // Reenviar el cuadrado a todos los demás clientes
                            // Reenviar el cuadrado a todos los demás clientes
                            sendSquareToAllClients(square, color);

                            break;

                        case "solicitarEstado":
                            /** Manejar mensaje de solicitarEstado, Aquí puedes enviar de vuelta al cliente el estado actual del juego.
                             *
                             */

                            // Crear un JSONObject que contenga toda la información del estado del juego.
                            JSONObject estadoJuego = new JSONObject();
                            estadoJuego.put("tipo", "estadoJuego");

                            /** Añadir el puerto al JSONObject estadoJuego
                             *
                             */
                            estadoJuego.put("puerto", clientSocket.getPort());

                            // Buscar el color asociado a este puerto y añadirlo al JSONObject estadoJuego
                            // Asumiendo que tienes una manera de buscar el color por puerto.
                            Color color1 = buscarColorPorPuerto(clientSocket.getPort());
                            estadoJuego.put("color", color1.getRGB());

                            /** Buscar las líneas asociadas a este puerto y añadirlas al JSONObject estadoJuego, Asumiendo que tienes una manera de buscar las líneas por puerto.
                             *
                             */
                            List<Line> lineList = buscarLineasPorPuerto(clientSocket.getPort());
                            JSONArray lineas = new JSONArray();
                            for (Line currentLine : lineList) {
                                // Suponiendo que tienes un método para convertir un objeto Line a JSONObject
                                JSONObject currentJsonLine = currentLine.toJsonObject();
                                lineas.put(currentJsonLine);
                            }
                            estadoJuego.put("lineas", lineas);


                            /** Enviar el JSONObject estadoJuego de vuelta al cliente.
                             *
                             */
                            out.writeUTF(estadoJuego.toString());
                            break;
                        default:
                            System.err.println("Tipo de mensaje desconocido: " + tipo);
                    }
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    public void startServer() {
        System.out.println("Iniciando servidor...");
        generateDots();

        LinkedList<String> lista_puertos = new LinkedList<String>();
        try (ServerSocket server = new ServerSocket(5000)) {
            while (true) {
                try {
                    Socket clientSocket = server.accept();
                    lista_puertos.add(Integer.toString(clientSocket.getPort()));
                    allClientSockets.add(clientSocket);

                    System.out.println("Cliente conectado: " + clientSocket.getRemoteSocketAddress());
                    System.out.println(coloresDisponibles);
                    Color playerColor = getNextPlayerColor();
                    Player player = new Player(playerColor, clientSocket);

                    colorToPlayerMap.put(playerColor.getRGB(), player);

                    players.add(player);
                    playerQueue.enqueue(player);
                    if (playerQueue.size() == 1) {
                        // Llama al método para iniciar la cuenta regresiva del juego
                        startGameCountdown();
                    }
                    // Crear DataOutputStream para enviar mensajes al cliente
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                    // Crear un JSONObject para enviar la información de la malla al cliente
                    JSONObject mallaInfo = new JSONObject();
                    mallaInfo.put("tipo", "mallaInfo");
                    mallaInfo.put("rows", rows);
                    mallaInfo.put("cols", cols);

                    // Enviar la información de la malla al cliente
                    out.writeUTF(mallaInfo.toString());

                    // Enviar mensaje informativo "Conexión exitosa" al cliente
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("tipo", "info");
                    jsonResponse.put("message", "Conexión exitosa");
                    out.writeUTF(jsonResponse.toString());

                    // Iniciar hilo para manejar al cliente
                    ClientHandler clientHandler = new ClientHandler(clientSocket, players, playerColor);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        PApplet.main("Servidor");
    }
}