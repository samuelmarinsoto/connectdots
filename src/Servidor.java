import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.awt.Color;
import java.util.LinkedList;


public class Servidor {
    // Variables de estado del juego
    int dotSize = 5;
    int rows = 10; // Number of rows
    int cols = 10; // Number of columns
    private Dot firstDot;
    private Dot currentDot;
    private String errorMsg;
    LinkedListCustom<Dot> dots;
    LinkedListCustom<Line> lines;
    LinkedListCustom<Square> squares = new LinkedListCustom<Square>();
    DataInputStream out;
    private final ArrayList<Color> colors = new ArrayList<>(
            List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN));
    private int nextColorIndex = 0;
    private LinkedList<Color> coloresDisponibles  = new LinkedList<Color>();

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

        // Método para encolar un elemento.
        void enqueue(T value) {
            Node<T> newNode = new Node<>(value);
            if (rear == null) {
                front = rear = newNode;
                return;
            }
            rear.next = newNode;
            rear = newNode;
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
            return temp.value;
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







    //Clases internas
    private static class Dot {
        int row, col;

        Dot(int row, int col) {
            this.row = row;
            this.col = col;
        }

        String getIdentifier() {
            return Integer.toString(row) + Integer.toString(col);
        }
    }

    private static class Line {
        Dot dot1, dot2;
        int lineColor = 0;

        Line(Dot dot1, Dot dot2) {
            this.dot1 = dot1;
            this.dot2 = dot2;
            this.lineColor = 0;
        }

        String getUniqueRepresentation() {
            return dot1.getIdentifier() + "-" + dot2.getIdentifier();
        }
    }

    private static class Square {
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
    }




    //Metodos de inicializacion
    public void generateDots() {
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            dots.add(new Dot(i, j)); // Sólo pasamos la fila y la columna al constructor de Dot
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

    // Comunicaciones
    public void handleClientAction(String action) {
    // Parsear la acción recibida (posiblemente en formato JSON)

    // Realizar los cálculos y verificaciones de la lógica del juego
    if (firstDot == null) {
        firstDot = currentDot;
//        firstDot.isSelected = true;
    } else {
        if (areAdjacentDots(firstDot, currentDot) && !lineExists(firstDot, currentDot)) {
            Line newLine = new Line(firstDot, currentDot);
            lines.add(newLine);
            LinkedListCustom<Square> completedSquares = getCompletedSquares(newLine);

            // ... (Resto de la lógica del juego)

            // Enviar el estado actualizado del juego a todos los clientes conectados
            sendUpdatedGameStateToClients();
        } else {
            errorMsg = "Selecciona puntos adyacentes o puntos entre los que no exista una línea!";
//            firstDot.isSelected = false;
            firstDot = null;
        }
    }
}

public void sendUpdatedGameStateToClients() {
    // Enviar el estado actualizado del juego a todos los clientes conectados.
}








    public void startServer() {
        // Agregar colores disponibles
        coloresDisponibles.add(Color.RED);
        coloresDisponibles.add(Color.GREEN);
        coloresDisponibles.add(Color.BLUE);

        LinkedList<Integer> lista_puertos = new LinkedList<Integer>();

        try {
            ServerSocket server = new ServerSocket(5000);

            while (true) {
                Socket serverSocket = server.accept();
                System.out.println("Cliente conectado: " + serverSocket.getRemoteSocketAddress());

                if (nextColorIndex < coloresDisponibles.size()) {
                    Color assignedColor = coloresDisponibles.get(nextColorIndex++);
                    // Envía el color asignado al cliente.
                    // Puedes convertir el color a un string o a un formato que prefieras.
                    DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
                    out.writeUTF(assignedColor.toString());
                } else {
                    // Todos los colores han sido asignados, manejar según sea necesario
                }

                try (DataInputStream datos = new DataInputStream(serverSocket.getInputStream())) {
                    String mensajes = datos.readUTF();
                    System.out.println("Mensaje recibido: " + mensajes);

                    if (!mensajes.isEmpty() && Objects.equals(String.valueOf(mensajes.charAt(0)), "0")) {
                        mensajes = mensajes.substring(1, mensajes.length());
                        int puerto_final = Integer.parseInt(mensajes);
                        lista_puertos.add(puerto_final);
                        System.out.println("Conectado: " + puerto_final);
                    } else {
                        // Parsear el mensaje JSON y realizar las operaciones necesarias
                        System.out.println(mensajes);

                        for (Integer puerto : lista_puertos) {
                            Socket mensajepuertos = new Socket("127.0.0.1", puerto);
                            DataOutputStream puertoOut = new DataOutputStream(mensajepuertos.getOutputStream());
                            puertoOut.writeUTF(mensajes);
                            mensajepuertos.close();
                        }
                    }
                } catch (EOFException e) {
                    System.out.println("Se ha alcanzado el final del flujo de datos");
                } finally {
                    serverSocket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Servidor().startServer();
    }
}
