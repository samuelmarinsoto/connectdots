import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.awt.Color;
import java.util.LinkedList;
import processing.core.PApplet;
import java.io.IOException;
import java.net.SocketException;
import org.json.JSONObject;
import javax.swing.JOptionPane;
import org.json.JSONException;
import java.net.ConnectException;
import org.json.JSONArray;

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

    String errorMsg = "";
    private Queue<Player> playerQueue = new Queue<>();
    long elapsedTime;
    boolean gameStarted = false;
    DataInputStream out;
    private  ArrayList<Color> colors = new ArrayList<>(
            List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN));
    private LinkedList<Color> coloresDisponibles = new LinkedList<>(List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN));
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
    public void settings() {
    // Solicitar al usuario que ingrese el número de filas y columnas, separados por coma (,):
    String input;
    int minSize = 5; // Tamaño mínimo permitido

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


    public void setup() {
        System.out.println("Configurando setup y arrancando servidor");
        new Thread(this::startServer).start();
    }

    public void draw() {
        background(255);
        for (Square sq : squares) {
            sq.display();
        }

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

    //Clases internas
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
    }

    private class Square {
        Dot topLeft;
        Dot bottomRight;
        int size;
        int color = -1;

        Square(Dot topLeft, Dot bottomRight) {
            this.topLeft = topLeft;
            this.bottomRight = bottomRight;
            this.size = (int) (bottomRight.col - topLeft.col);
        }

        boolean isClosed(HashSet<String> lineSet) {
            // Implementación del método isClosed aquí...
            return false;
        }

        void setColor(int playerColor) {
            this.color = playerColor;
        }

        void display() {
            if (color != -1) {
                fill(color);
                int x = Servidor.convertRowToX(topLeft.row);
                int y = Servidor.convertColToY(topLeft.col);
                rect(x, y, size, size);
            }
        }
    }

    public class Player {
        private Color color;
        private Socket socket;

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
    }


    //Metodos de inicializacion
    public void generateDots() {
        System.out.println("Generando puntos...");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                dots.add(new Dot(i, j));
            }
        }
    }


    //Metodos de logica del juego
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

    //    // Comunicaciones
//    public void handleClientAction(String action) {
//    // Parsear la acción recibida (posiblemente en formato JSON)
//
//    // Realizar los cálculos y verificaciones de la lógica del juego
//    if (firstDot == null) {
//        firstDot = currentDot;
////        firstDot.isSelected = true;
//    } else {
//        if (areAdjacentDots(firstDot, currentDot) && !lineExists(firstDot, currentDot)) {
//            Line newLine = new Line(firstDot, currentDot);
//            lines.add(newLine);
//            LinkedListCustom<Square> completedSquares = getCompletedSquares(newLine);
//
//            // ... (Resto de la lógica del juego)
//
//            // Enviar el estado actualizado del juego a todos los clientes conectados
//            sendUpdatedGameStateToClients();
//        } else {
//            errorMsg = "Selecciona puntos adyacentes o puntos entre los que no exista una línea!";
////            firstDot.isSelected = false;
//            firstDot = null;
//        }
//    }
//}
//
//public void sendUpdatedGameStateToClients() {
//    // Enviar el estado actualizado del juego a todos los clientes conectados.
//}

     // Función para asignar colores a los clientes en orden



    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        // Añade los campos de la línea al objeto json
        // Por ejemplo:
        // json.put("start", this.start);
        // json.put("end", this.end);
        return json;
    }
    private Line jsonLineToLine(JSONObject jsonLine) {
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
        int port = jsonLine.getInt("port");
        // Crear y retornar un nuevo objeto Line con los puntos dot1 y dot2
        return new Line(dot1, dot2, port);
    }

    private Color getNextPlayerColor() {
        if (coloresDisponibles.isEmpty()) {
            // Si se agotaron los colores disponibles, reiniciar la lista de colores
            coloresDisponibles.addAll(colors);
        }
        Color playerColor = coloresDisponibles.poll(); // Obtener el siguiente color disponible
        return playerColor;

    }

    public void startGameCountdown() {
        elapsedTime = System.currentTimeMillis(); // Inicializar elapsedTime aquí
        Thread countdownThread = new Thread(() -> {
            try {
                while (true) {
                    long currentTime = System.currentTimeMillis();
                    long timeLeft = 15000 - (currentTime - elapsedTime);

                    if (!gameStarted && timeLeft <= 0) {
                        gameStarted = true;
                        System.out.println("La partida ha comenzado.");

                        // Informar a todos los clientes conectados que la partida ha comenzado
                        while (!playerQueue.isEmpty()) {
                            Player player = playerQueue.dequeue();
                            DataOutputStream out = new DataOutputStream(player.getSocket().getOutputStream());
                            out.writeUTF("Partida Comenzada");
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
            }catch (IOException e){
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


        private List<Line> buscarLineasPorPuerto ( int puerto){
            // Implementa la lógica para buscar las líneas por puerto
            return new ArrayList<>();
        }


        @Override
        public void run() {
            try {
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                // Leer el JSON enviado por el cliente
                String jsonStr = in.readUTF();
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
                    case "linea":
                        // Obtener la línea
                        JSONObject jsonLine = json.getJSONObject("Line");
                        Line line = jsonLineToLine(jsonLine); // Convertir el JSONObject a un objeto Line
                        // Añadir la línea a la lista de líneas y asignarle el color del jugador
                        Player playerInstance = null;
                        for (Player player : players) {
                            if (player.getSocket().equals(clientSocket)) {
                                playerInstance = player;
                                break;
                            }
                        }
                        if (playerInstance != null) {
                            // ...
                        } else {
                            // Si playerInstance es nulo, se debería añadir el jugador a la lista players
                            Color playerColor = getNextPlayerColor();
                            Player player = new Player(playerColor, clientSocket);
                            players.add(player);
                            playerQueue.enqueue(player);
                            System.out.println("Cliente conectado. Color asignado: " + playerColor);
                        }

                        if (playerInstance != null) {
                            Color playerColor = playerInstance.getColor();
                            if (playerColor != null) {
                                line.lineColor = playerColor.getRGB();
                            } else {
                                // Si playerColor es nulo, asigna un color predeterminado al primer jugador.
                                if (players.isEmpty()) {
                                    Color defaultColor = Color.RED; // Puedes elegir el color predeterminado que desees.
                                    line.lineColor = defaultColor.getRGB();
                                } else {
                                    // Manejo de caso donde no es el primer jugador.
                                    // Asigna colores de la lista "colors" a los jugadores subsiguientes.
                                    // Asegúrate de tener una lista de colores disponibles en "coloresDisponibles".

                                    if (!coloresDisponibles.isEmpty()) {
                                        // Obtén el próximo color disponible desde la lista de colores disponibles.
                                        Color nextPlayerColor = coloresDisponibles.poll();

                                        // Asigna el color al jugador.
                                        line.lineColor = nextPlayerColor.getRGB();
                                    } else {
                                        // Manejo del caso donde te has quedado sin colores disponibles.
                                        // Puedes generar un mensaje de error o manejarlo de la manera que prefieras.
                                    }
                                }
                            }
                        } else {
                            // Manejo de caso donde playerInstance es nulo
                            // Aquí debes manejar el caso donde no se encuentra una instancia de jugador válida.
                        }



                        lines.add(line);
                        // Asignar colores a los clientes en el orden en que se conectan
                        if (!gameStarted) {
                            Color playerColor = getNextPlayerColor();
                            Player player = new Player(playerColor, clientSocket);
                            players.add(player);
                            playerQueue.enqueue(player);
                            System.out.println("Cliente conectado. Color asignado: " + playerColor);
                        } else {
                            System.out.println("La partida ha comenzado. Cliente en espera.");
                        }
                        break;
                    case "solicitarEstado":
                    // Manejar mensaje de solicitarEstado
                    // Aquí puedes enviar de vuelta al cliente el estado actual del juego.

                    // Crear un JSONObject que contenga toda la información del estado del juego.
                    JSONObject estadoJuego = new JSONObject();
                    estadoJuego.put("tipo", "estadoJuego");

                    // Añadir el puerto al JSONObject estadoJuego
                    estadoJuego.put("puerto", clientSocket.getPort());

                    // Buscar el color asociado a este puerto y añadirlo al JSONObject estadoJuego
                    // Asumiendo que tienes una manera de buscar el color por puerto.
                    Color color = buscarColorPorPuerto(clientSocket.getPort());
                    estadoJuego.put("color", color.getRGB());

                    // Buscar las líneas asociadas a este puerto y añadirlas al JSONObject estadoJuego
                    // Asumiendo que tienes una manera de buscar las líneas por puerto.
                    List<Line> lineList = buscarLineasPorPuerto(clientSocket.getPort());
                    JSONArray lineas = new JSONArray();
                    for (Line currentLine : lineList) {
                        // Suponiendo que tienes un método para convertir un objeto Line a JSONObject
                        JSONObject currentJsonLine = currentLine.toJsonObject();
                        lineas.put(currentJsonLine);
                    }
                    estadoJuego.put("lineas", lineas);


                        // Enviar el JSONObject estadoJuego de vuelta al cliente.
                    out.writeUTF(estadoJuego.toString());
                    break;
                    default:
                        System.err.println("Tipo de mensaje desconocido: " + tipo);
                }
            } catch (IOException e) {
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
                    System.out.println("Cliente conectado: " + clientSocket.getRemoteSocketAddress());
                    System.out.println(coloresDisponibles);
                    Color playerColor = getNextPlayerColor();
                    Player player = new Player(playerColor, clientSocket);
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