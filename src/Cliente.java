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
import java.io.EOFException;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;


public class Cliente extends PApplet implements Runnable {




    // Data Structures
    LinkedListCustom<Dot> dots;
    LinkedListCustom<Line> lines;
    LinkedListCustom<Dot> selectedDots = new LinkedListCustom<Dot>();
    LinkedListCustom<Square> squares = new LinkedListCustom<Square>();

    // Variables
    Socket socket;
    DataOutputStream out;
    public static String samuel = "{}";
    private boolean partidaComenzada = false;
    public static int juego_iniciado = 0;
    int rows; // Number of rows
    int cols; // Number of columns
    int currentPlayer = 1;
    int rand_int1 = (int) Math.random();
    int player1Color = color(rand_int1 + 100, 0, 0);  // Red for player 1
    int selectedIndex = 0;
    int selectedRow = 0;
    int selectedCol = 0;
    String errorMsg = "";
    long errorDisplayStartTime = 0; // Tiempo de inicio de la visualización del mensaje de error
    int errorDisplayDuration = 2000; // Duración del mensaje de error en milisegundos (2 segundos)
    Dot firstDot = null;
    DataInputStream in;
    private Color clientColor = Color.BLACK; // o cualquier otro color predeterminado

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
        void remove(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index out of bounds");
            }
            if (index == 0) {
                head = head.next;
            } else {
                Node<T> current = head;
                for (int i = 0; i < index - 1; i++) {
                    current = current.next;
                }
                current.next = current.next.next;
            }
            size--;
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

    public Cliente() {
            try {
                this.socket = new Socket("127.0.0.1", 5000);
                this.out = new DataOutputStream(socket.getOutputStream());
                this.in = new DataInputStream(socket.getInputStream());
                System.out.println("Cliente conectado con el socket: " + socket);

                // Recibir las dimensiones del servidor
                String dimensionsMessage = in.readUTF();
                JSONObject dimensions = new JSONObject(dimensionsMessage);
                this.rows = dimensions.getInt("rows");
                this.cols = dimensions.getInt("cols");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread hilo = new Thread(this);
            hilo.start();
//
//            // Programar el pull cada X segundos (ajusta el valor según tu necesidad)
//            Timer timer = new Timer();
//            int pullIntervalSeconds = 5; // Ejemplo: pull cada 5 segundos
//            timer.scheduleAtFixedRate(new TimerTask() {
//                @Override // Asegúrate de que estás usando @Override correctamente
//                public void run() {
//                    pullGameStateFromServer();
//                }
//            }, 0, pullIntervalSeconds * 1000); // Convertir segundos a milisegundos
        }

    public void settings() {
        int windowSizeX = convertRowToX(rows) - 10; // Ajustar según sea necesario
        int windowSizeY = convertColToY(cols) - 10; // Ajustar según sea necesario
        size(windowSizeX, windowSizeY);

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

        }

        // Display the dots
        for (Dot dot : dots) {
            dot.display();
        }
        // Display the error message
        if (millis() - errorDisplayStartTime < errorDisplayDuration) {
            fill(255, 0, 0);
            text(errorMsg, 20, height - 20); // Dibuja el mensaje de error
        }
    }

    public void generateDots() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                dots.add(new Dot(i, j)); // Sólo pasamos la fila y la columna al constructor de Dot
            }
        }
    }
    //Metodos de utilidad
    public String lineToJson(Line line) {
        // Crear objetos JSONObject para dot1 y dot2
        JSONObject jsonDot1 = new JSONObject();
        jsonDot1.put("row", line.dot1.row);
        jsonDot1.put("col", line.dot1.col);

        JSONObject jsonDot2 = new JSONObject();
        jsonDot2.put("row", line.dot2.row);
        jsonDot2.put("col", line.dot2.col);

        // Crear el objeto JSONObject para la línea y agregar dot1 y dot2
        JSONObject jsonLine = new JSONObject();
        jsonLine.put("dot1", jsonDot1);
        jsonLine.put("dot2", jsonDot2);

        // Retornar la representación en cadena del objeto JSONObject de la línea
        return jsonLine.toString();
    }
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
//    private void pullGameStateFromServer() {
//        try {
//            // Enviar solicitud al servidor para obtener el estado del juego
//            JSONObject request = new JSONObject();
//            request.put("tipo", "solicitarEstado");
//            request.put("action", "pull");
//            out.writeUTF(request.toString());
//
//            // Leer la respuesta del servidor
//            String response = in.readUTF();
//            // Procesar la respuesta (puede ser un JSON que contiene el estado del juego)
//            // Aquí debes actualizar la representación local del juego con la información recibida
//            // y luego volver a dibujar la pantalla.
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

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
            fill(0, 0, 0);
        } else if (isSelected) {
            fill(93, 192, 285);
        } else {
            fill(clientColor.getRGB());
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
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("row", this.row);
            json.put("col", this.col);
            return json;
        }
    }
    class Line {
        Dot dot1, dot2;
        int lineColor ;

        Line(Dot dot1, Dot dot2) {
            this.dot1 = dot1;
            this.dot2 = dot2;
            this.lineColor = clientColor.getRGB(); // Toma el color clientColor directamente
        }

        void display() {
            stroke(lineColor);
            int x1 = Cliente.convertRowToX(dot1.row);
            int y1 = Cliente.convertColToY(dot1.col);
            int x2 = Cliente.convertRowToX(dot2.row);
            int y2 = Cliente.convertColToY(dot2.col);
            line(x1, y1, x2, y2);
        }

        String getUniqueRepresentation() {
            return dot1.getIdentifier() + "-" + dot2.getIdentifier();
        }
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("dot1", this.dot1.toJson());
            json.put("dot2", this.dot2.toJson());
            return json;
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


    public void keyPressed() {
        if (!partidaComenzada) {
            return;
        }

        // Manejar la entrada del usuario para mover el punto seleccionado
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
            if (currentDot != null) {
                if (selectedDots.size() == 2) {
                    // Desmarcar el primer punto seleccionado
                    Dot firstSelectedDot = selectedDots.get(0);
                    firstSelectedDot.setSelected(false);
                }
                // Marcar el punto actual y añadirlo a la lista de puntos seleccionados
                currentDot.setSelected(true);
                selectedDots.add(currentDot);
            }

            if (selectedDots.size() == 2) {
                Dot dot1 = selectedDots.get(0);
                Dot dot2 = selectedDots.get(1);
                boolean areAdjacent = (Math.abs(dot1.row - dot2.row) == 1 && dot1.col == dot2.col) ||
                        (Math.abs(dot1.col - dot2.col) == 1 && dot1.row == dot2.row);

                if (areAdjacent) {
                    // Verificar si la línea ya existe
                    if (!lineExists(dot1, dot2)) {
                        // Dibuja una línea y vacía la lista de puntos seleccionados
                        Line newLine = new Line(dot1, dot2);
                        lines.add(newLine);
                        selectedDots.get(0).setSelected(false); // Desmarcar el primer punto
                        selectedDots.get(1).setSelected(false); // Desmarcar el segundo punto

                        // Envía la nueva línea al servidor
                        try {
                            // Envía la nueva línea al servidor
                            sendLineToServer(newLine);
                            selectedDots = new LinkedListCustom<Dot>();
                        } catch (IOException e) {
                            // Manejar la excepción aquí. Por ejemplo:
                            e.printStackTrace();
                            errorMsg = "Error al enviar la línea al servidor.";
                            errorDisplayStartTime = millis(); // Establece el tiempo de inicio actual
                        }
                    } else {
                        errorMsg = "La línea ya existe!";
                        errorDisplayStartTime = millis(); // Establece el tiempo de inicio actual
                        dot1.setSelected(false);
                        dot2.setSelected(false);
                        selectedDots = new LinkedListCustom<Dot>();
                    }
                } else {
                    // Manejar el caso cuando los puntos no son adyacentes
                    errorMsg = "Los puntos seleccionados no son adyacentes";
                    errorDisplayStartTime = millis(); // Establece el tiempo de inicio actual

                    // Desmarcar ambos puntos seleccionados
                    dot1.setSelected(false);
                    dot2.setSelected(false);
                    selectedDots = new LinkedListCustom<Dot>();
                }
            }
        }
    }


    // Comunicacion al servidor


    public void sendLineToServer(Line line) throws IOException {
        // Crear el objeto JSON principal y agregar la línea, tipo y puerto
        DataOutputStream out2 = new DataOutputStream(socket.getOutputStream());
        JSONObject mainJson = new JSONObject();
        mainJson.put("tipo", "LineaEnviadaPorCliente");
        mainJson.put("puerto", socket.getLocalPort());
        mainJson.put("linea", line.toJson());

        System.out.println("Enviando mensaje: " + mainJson.toString());
        try {
            out2.writeUTF(mainJson.toString());
            out2.flush();
            System.out.println("Mensaje enviado.");
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










    //Métodos de lógica del juego
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Enviar el número de puerto en un objeto JSON
            JSONObject puertoJson = new JSONObject();
            puertoJson.put("tipo", "puerto");
            puertoJson.put("puerto", socket.getLocalPort());
            out.writeUTF(puertoJson.toString());

            // Solicitar la información de la partida al servidor
//            JSONObject solicitudJson = new JSONObject();
//            solicitudJson.put("tipo", "solicitud");
//            solicitudJson.put("accion", "SOLICITAR_INFORMACION_PARTIDA");
//            out.writeUTF(solicitudJson.toString());

            // Leer la respuesta del servidor
            String respuesta = in.readUTF();
            System.out.println("Información de la partida recibida: " + respuesta);
            handleServerMessage(respuesta); // Usar una función para manejar mensajes

            while (true) {
                DataInputStream dataInput = new DataInputStream(socket.getInputStream());

                try {
                    String message = dataInput.readUTF();
                    System.out.println("Mensaje recibido del servidor: " + message);

                    handleServerMessage(message); // Usar la misma función para manejar mensajes dentro del bucle
                } catch (EOFException e) {
                    System.err.println("Se ha alcanzado el final del stream mientras se leía. ¿El servidor cerró la conexión?");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleServerMessage(String message) throws JSONException {
        JSONObject mensajeJson = new JSONObject(message);
        String tipo = mensajeJson.getString("tipo");
        switch (tipo) {
            case "info":
                System.out.println("¡Conexión con el servidor establecida con éxito!");
                break;
            case "inicioPartida":
                partidaComenzada = true;
                System.out.println("¡La partida ha comenzado!");
                break;
            case "asignacionColor":
                int assignedColorRGB = mensajeJson.getInt("color");
                clientColor = new Color(assignedColorRGB);
                System.out.println("Color asignado: " + clientColor.toString());
                redraw();
                break;
            case "estadoJuego":
                // Aquí debes agregar la lógica para manejar el estado del juego que se recibe.
                // Por ejemplo, actualizar las líneas y el color del cliente en función de la información recibida.
                break;
            default:
                System.err.println("Tipo de mensaje desconocido: " + tipo);
        }
    }


    public static void main(String args[]) {
        System.out.println("Iniciando una nueva instancia de Cliente");

        Inicio inicio = new Inicio();
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
                PApplet.main("Cliente");
            }
        });
    }
    }






