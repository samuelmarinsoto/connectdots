import processing.core.PApplet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashSet;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
/**
 * La clase Cliente representa un cliente del juego de puntos y líneas.
 * Se conecta a un servidor y se comunica con él para gestionar el estado del juego.
 */
public class Cliente extends PApplet implements Runnable {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // Clase anidada Node para representar un nodo en una lista enlazada.
    static class Node<T> {
        T value; // Valor del nodo.
        Node<T> next; // Referencia al siguiente nodo en la lista.

        // Constructor que inicializa el valor del nodo.
        Node(T value) {
            this.value = value;
            this.next = null;
        }
    }

    // Clase anidada LinkedListCustom para representar una lista enlazada personalizada.
    static class LinkedListCustom<T> implements Iterable<T> {
        Node<T> head; // Referencia a la cabeza de la lista.
        int size; // Tamaño de la lista.

        // Constructor que inicializa la lista enlazada vacía.
        LinkedListCustom() {
            head = null;
            size = 0;
        }

        // Método para agregar un elemento al final de la lista.
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

        // Método para obtener el elemento en un índice específico.
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

        // Método para obtener un iterador sobre los elementos de la lista.
        @Override
        public Iterator<T> iterator() {
            return new Iterator<>() {
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

    // Variables de instancia para la conexión y comunicación con el servidor.
    Socket socket;
    DataOutputStream out;

    // Variables de instancia para el estado del juego.
    LinkedListCustom<LinkedListCustom<Dot>> dots; // Lista de filas, cada fila es una lista de puntos (Dot).
    LinkedListCustom<Line> lines; // Lista de líneas en el juego.
    LinkedListCustom<Square> squares = new LinkedListCustom<>(); // Lista de cuadrados en el juego.
    int dotSize = 5; // Tamaño de los puntos.
    int rows = 4; // Número de filas.
    int cols = 4; // Número de columnas.
    int score = 0; // Puntuación.
    int currentPlayer = 1; // Jugador actual.
    int selectedIndex = 0; // Índice seleccionado.
    int selectedRow = 0; // Fila seleccionada.
    int selectedCol = 0; // Columna seleccionada.
    String errorMsg = ""; // Mensaje de error.
    int player1Score = 0; // Puntuación del jugador 1.
    int player2Score = 0; // Puntuación del jugador 2.
    Dot firstDot = null; // Primer punto seleccionado para crear una línea.
    int player1Color; // Color del jugador 1.


    // Constructor de la clase Cliente.
    public Cliente() {
        try {
            this.socket = new Socket("127.0.0.1", 5000);
            System.out.println("Conectado al servidor desde el puerto local: " + socket.getLocalPort());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Iniciar un nuevo hilo para manejar la comunicación con el servidor.
        Thread hilo = new Thread(this);
        hilo.start();
    }

    // Método para configurar el tamaño del lienzo y generar los puntos iniciales.
    public void settings() {
        size(200, 200);
        dots = new LinkedListCustom<>();
        lines = new LinkedListCustom<>();
        generateDots();
    }

    // Método para dibujar los elementos del juego en el lienzo.
    public void draw() {
        background(255);
        for (Square sq : squares) {
            sq.display();
        }

        // Mostrar las líneas.
        for (Line line : lines) {
            line.display();
        }

        // Mostrar los puntos.
        for (LinkedListCustom<Dot> row : dots) {
            for (Dot dot : row) {
                dot.display();
            }
        }


        // Mostrar la puntuación de los jugadores.
        text("Score Player 1: " + player1Score, 20, 30);
        text("Score Player 2: " + player2Score, 20, 60);

        // Mostrar mensajes de error.
        fill(255, 0, 0);
        text(errorMsg, 20, 90);
    }

    // Método para generar los puntos iniciales.
    public void generateDots() {
    dots = new LinkedListCustom<>();
    for (int i = 0; i < rows; i++) {
        LinkedListCustom<Dot> row = new LinkedListCustom<>();
        for (int j = 0; j < cols; j++) {
            float x = map(i, 0, rows - 1, dotSize, width - dotSize);
            float y = map(j, 0, cols - 1, dotSize, height - dotSize);
            row.add(new Dot(x, y, dotSize, i, j));
        }
        dots.add(row);
    }
}


    // Método para enviar mensajes al servidor.
    public void send(String message) {
        System.out.println("Intentando enviar: " + message);
        try {
            out.writeUTF(message);
            System.out.println("Mensaje enviado exitosamente: " + message);
        } catch (Exception ex) {
            System.out.println("Error al enviar mensaje: " + ex);
        }
    }


    // Método que se ejecuta cuando se presiona una tecla.
    public void keyPressed() {
        // Navegación entre los puntos utilizando las teclas W, A, S, D.
        if (key == 'A' || key == 'a') {
            selectedRow = max(0, selectedRow - 1); // Mover a la izquierda
        } else if (key == 'W' || key == 'w') {
            selectedCol = max(0, selectedCol - 1); // Mover arriba
        } else if (key == 'D' || key == 'd') {
            selectedRow = min(rows - 1, selectedRow + 1); // Mover a la derecha
        } else if (key == 'S' || key == 's') {
            selectedCol = min(cols - 1, selectedCol + 1); // Mover abajo
        } else if (key == 'L' || key == 'l') {
            // Acción para intentar crear una línea entre dos puntos.
            Dot currentDot = getDotAtRowCol(selectedRow, selectedCol);

            if (firstDot == null) {
                firstDot = currentDot;
                firstDot.isSelected = true;
            } else {
                // Verificar si los puntos son adyacentes y si la línea ya existe.
                if (areAdjacentDots(firstDot, currentDot) && !lineExists(firstDot, currentDot)) {
                    Line newLine = new Line(firstDot, currentDot);
                    lines.add(newLine);

                    // Verificar si se ha completado un cuadrado y asignar puntaje.
                    LinkedListCustom<Square> completedSquares = getCompletedSquares(newLine);
                    if (completedSquares.size == 0) {
                        currentPlayer = (currentPlayer == 1) ? 2 : 1; // Cambio de turno
                    }

                    for (Square sq : completedSquares) {
                        squares.add(sq);
                        sq.setColor((currentPlayer == 1) ? player1Color : player1Color); // Asignar color
                        if (currentPlayer == 1) {
                            player1Score++; // Incrementar puntaje del jugador 1
                        } else {
                            player2Score++; // Incrementar puntaje del jugador 2
                        }
                    }

                    firstDot.isSelected = false;
                    firstDot = null;
                    errorMsg = ""; // Limpiar mensaje de error
                } else {
                    errorMsg = "Selecciona puntos adyacentes o puntos entre los que no exista una línea!";
                    firstDot.isSelected = false;
                    firstDot = null;
                }
            }

            // Enviar el estado actual de las líneas al servidor.
            JSONArray jsonLines = new JSONArray();
            for (Line line : lines) {
                jsonLines.put(lineToJson(line));
            }
            if(jsonLines.length() > 0) {
                System.out.println("Enviando líneas desde keyPressed: " + jsonLines.toString());
                send(jsonLines.toString());
            } else {
                System.out.println("Intento de enviar un JSONArray vacío en keyPressed");
            }
        }

        // Verificar si se ha cerrado algún cuadrado.
        HashSet<String> lineSet = new HashSet<>();
        for (Line line : lines) {
            lineSet.add(line.getUniqueRepresentation());
        }

        for (Square sq : squares) {
            if (sq.isClosed(lineSet)) {
                if (currentPlayer == 1) {
                    sq.setColor(player1Color);
                    player1Score++;
                } else {
                    sq.setColor(player1Color);
                    player2Score++;
                }
                // No cambiamos el turno si alguien completa un cuadrado.
            } else {
                // Cambio de turno
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
            }
        }
    }

    // Método que verifica si una línea entre dos puntos ya existe.
    public boolean lineExists(Dot d1, Dot d2) {
        for (Line line : lines) {
            if ((line.dot1 == d1 && line.dot2 == d2) || (line.dot1 == d2 && line.dot2 == d1)) {
                return true;
            }
        }
        return false;
    }

    // Método que devuelve el punto ubicado en una fila y columna específica.
    Dot getDotAtRowCol(int row, int col) {
        LinkedListCustom<Dot> rowList = dots.get(row);
        if (rowList != null) {
            return rowList.get(col);
        }
        return null;
    }


    // Clase que representa un punto en el juego.
    class Dot {
        float x, y; // Coordenadas del punto.
        float radius; // Radio del punto.
        boolean isSelected = false; // Indica si el punto está seleccionado.
        int row, col; // Posición en la fila y columna del punto.

        // Constructor de la clase Dot.
        Dot(float x, float y, float radius, int row, int col) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.row = row;
            this.col = col;
        }

        // Método para dibujar el punto en el lienzo.
        void display() {
            noStroke();
            if (row == selectedRow && col == selectedCol) {
                fill(0, 0, 255);  // Azul para el punto resaltado actualmente.
            } else if (isSelected) {
                fill(0, 255, 0);  // Verde para el punto seleccionado.
            } else {
                fill(100, 100, 100); // Gris para los otros puntos.
            }
            ellipse(x, y, radius * 2, radius * 2);
        }

        // Método para obtener un identificador único para el punto.
        String getIdentifier() {
            return row + "," + col;
        }
    }

    // Clase que representa una línea en el juego.
    class Line {
        Dot dot1, dot2;
        int lineColor = 0;

        Line(Dot dot1, Dot dot2) {
            if (dot1 == null || dot2 == null) {
                throw new IllegalArgumentException("dot1 y dot2 no pueden ser null");
            }
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

    // Método que verifica si un cuadrado existe entre dos puntos diagonales.
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

    // Método que devuelve una lista de cuadrados completados por una línea.
    public LinkedListCustom<Square> getCompletedSquares(Line line) {
        LinkedListCustom<Square> completedSquares = new LinkedListCustom<>();

        // Comprueba si la línea es horizontal o vertical y busca cuadrados completados.
        if (line.dot1.row == line.dot2.row) { // Línea horizontal
            Square above = getSquareAbove(line.dot1, line.dot2);
            Square below = getSquareBelow(line.dot1, line.dot2);

            if (above != null) {
                completedSquares.add(above);
            }
            if (below != null) {
                completedSquares.add(below);
            }

        } else { // Línea vertical
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

    // Método que obtiene el punto en las coordenadas dadas, si existe.
    Dot getDotAt(float x, float y) {
        for (LinkedListCustom<Dot> row : dots) {
            for (Dot dot : row) {
                if (dot.x == x && dot.y == y) {
                    return dot;
                }
            }
        }
        return null;
    }


    // Método que verifica si dos puntos son adyacentes en la cuadrícula.
    boolean areAdjacentDots(Dot dot1, Dot dot2) {
        int dRow = abs(dot1.row - dot2.row);
        int dCol = abs(dot1.col - dot2.col);

        // Los puntos son adyacentes si están en la misma fila o columna y son consecutivos.
        return ((dRow == 1 && dCol == 0) || (dRow == 0 && dCol == 1));
    }

    // Clase que representa un cuadrado en el juego.
    class Square {
        Dot topLeft;      // Punto superior izquierdo del cuadrado.
        Dot bottomRight;  // Punto inferior derecho del cuadrado.
        int size;         // Tamaño del cuadrado, asumiendo que todos los cuadrados son del mismo tamaño.
        int color = -1;   // Color del cuadrado, -1 si no se ha completado.

        // Método que verifica si el cuadrado está cerrado por líneas.
        boolean isClosed(HashSet<String> lineSet) {
            Dot topRight = getDotAtRowCol(topLeft.row, bottomRight.col);
            Dot bottomLeft = getDotAtRowCol(bottomRight.row, topLeft.col);

            String topLine = topLeft.getIdentifier() + "-" + topRight.getIdentifier();
            String leftLine = topLeft.getIdentifier() + "-" + bottomLeft.getIdentifier();
            String rightLine = topRight.getIdentifier() + "-" + bottomRight.getIdentifier();
            String bottomLine = bottomLeft.getIdentifier() + "-" + bottomRight.getIdentifier();

            if (lineSet.contains(topLine) && lineSet.contains(leftLine) &&
                lineSet.contains(rightLine) && lineSet.contains(bottomLine)) {
                this.color = (currentPlayer == 1) ? player1Color : player1Color;
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
        // Método para dibujar el cuadrado en la pantalla.
        void display() {
            if (color != -1) {
                fill(color);
                rect(topLeft.x, topLeft.y, size, size);
            }
        }
    }

    // Método para enviar líneas al servidor en formato JSON.
    public void sendLinesAndDots() {
        JSONArray jsonLines = new JSONArray();
        for (Line line : lines) {
            jsonLines.put(lineToJson(line)); // Convertir cada línea a formato JSON y añadir al JSONArray.
        }

        // Enviar la representación en cadena del JSONArray al servidor.
        if(jsonLines.length() > 0) {
            System.out.println("Enviando líneas desde sendLinesAndDots: " + jsonLines.toString());
            send(jsonLines.toString());
        } else {
            System.out.println("Intento de enviar un JSONArray vacío en sendLinesAndDots");
        }
    }

    // Si también deseas enviar puntos, crea otro JSONArray y agrégalo al mensaje.
    //     JSONArray jsonDots = new JSONArray();
    //     for (Dot dot : dots){
    //         jsonDots.put(dotToJson(dot));
    //     }
    //
    // Crear un objeto JSON principal que contiene tanto las líneas como los puntos
    // JSONObject mainJson = new JSONObject();
    // mainJson.put("lines", jsonLines);
    // mainJson.put("dots", jsonDots);

    // Enviar la representación en cadena del objeto JSON principal al servidor
    // Método para convertir un objeto Line a un objeto JSONObject.
    public JSONObject lineToJson(Line line) {
        JSONObject json = new JSONObject();
        json.put("dot1", dotToJson(line.dot1)); // Convertir el primer punto de la línea a formato JSON.
        json.put("dot2", dotToJson(line.dot2)); // Convertir el segundo punto de la línea a formato JSON.
        return json;
    }

    // Método para convertir un objeto Dot a un objeto JSONObject.
    public JSONObject dotToJson(Dot dot) {
        JSONObject json = new JSONObject();
        json.put("x", dot.x);  // Coordenada x del punto.
        json.put("y", dot.y);  // Coordenada y del punto.
        json.put("row", dot.row); // Fila en la que se encuentra el punto.
        json.put("col", dot.col); // Columna en la que se encuentra el punto.
        return json;
    }

    // Método para convertir un objeto JSONObject a un objeto Line.
    public Line jsonLineToLine(JSONObject json) {
        JSONObject jsonDot1 = json.getJSONObject("dot1"); // Obtener el primer punto en formato JSON.
        JSONObject jsonDot2 = json.getJSONObject("dot2"); // Obtener el segundo punto en formato JSON.

        Dot dot1 = jsonDotToDot(jsonDot1); // Convertir el primer punto a formato Dot.
        Dot dot2 = jsonDotToDot(jsonDot2); // Convertir el segundo punto a formato Dot.

        return new Line(dot1, dot2); // Crear un nuevo objeto Line y devolverlo.
    }

    // Método para convertir un objeto JSONObject a un objeto Dot.
    public Dot jsonDotToDot(JSONObject json) {
        float x = (float) json.getDouble("x"); // Obtener la coordenada x del punto.
        float y = (float) json.getDouble("y"); // Obtener la coordenada y del punto.
        int row = json.getInt("row"); // Obtener la fila del punto.
        int col = json.getInt("col"); // Obtener la columna del punto.

        return new Dot(x, y, dotSize, row, col); // Crear un nuevo objeto Dot y devolverlo.
    }

    // Método run del hilo, encargado de recibir mensajes del servidor y actualizar el estado del juego.
    public void run() {
        try {
            ServerSocket server = new ServerSocket(0);
            Socket socket = new Socket("127.0.0.1", 5000);

            String puerto_codificado = String.valueOf("0" + server.getLocalPort());

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(puerto_codificado); // Enviar el puerto al servidor.

            while (true) {
                Socket serverSocket = server.accept();
                DataInputStream dataInput = new DataInputStream(serverSocket.getInputStream());

                String message = dataInput.readUTF(); // Leer el mensaje del servidor.
                System.out.println(message);

                // Parsear el mensaje JSON recibido.
                JSONArray jsonLines = new JSONArray(message);

                // Iterar sobre las líneas JSON y agregarlas a la lista local de líneas.
                for (int i = 0; i < jsonLines.length(); i++) {
                    JSONObject jsonLine = jsonLines.getJSONObject(i);
                    Line newLine = jsonLineToLine(jsonLine); // Convertir cada línea JSON a formato Line.
                    lines.add(newLine);

                    // Verificar si la nueva línea cierra un cuadrado y actualizar la interfaz y la puntuación.
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
            System.out.println("Error: " + e.getMessage());
            try {
                // Espera 5 segundos antes de intentar reconectar
                Thread.sleep(5000);
                System.out.println("Intentando reconectar...");
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
    // Método principal que inicia la aplicación.
    public static void main(String args[]) {
        Inicio inicio = new Inicio();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        // Solicitar el estado del juego cada n segundos
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                // Aquí debes colocar el código para hacer la solicitud al servidor
                // y obtener el estado actualizado del juego.
                // Puedes mover parte del código del método run() aquí.
            }
        }, 0, 1, TimeUnit.SECONDS); // reemplaza n por el número de segundos que desees

        while (inicio.numero == 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
                System.out.println("1");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PApplet.main("Cliente"); // Iniciar la aplicación Cliente.
            }
        });
    }
}


