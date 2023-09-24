import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * La clase Servidor se encarga de aceptar conexiones de clientes,
 * recibir mensajes y realizar acciones en función de los mensajes recibidos.
 */
public class Servidor {

    /**
     * Inicia el servidor en el puerto 5000 y espera conexiones de clientes.
     * Al recibir un mensaje, se verifica su contenido y se toman acciones correspondientes.
     * Los mensajes que comienzan con "0" indican un nuevo socket de cliente,
     * y otros mensajes se envían a todos los sockets de clientes conectados.
     */

     // Clase anidada Node para representar un nodo en una lista enlazada.
    static class Node<T> {
        T value; // Valor del nodo.
        Node<T> next; // Referencia al siguiente nodo en la lista.

        // Constructor que inicializa el valor del nodo.
        Node(T value) {
            this.value = value;
            this.next = null;
        }
        // Método para establecer el siguiente nodo.
        public void setNext(Node<T> next) {
            this.next = next;
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
    static class DoubleEndedList {
        private Node head;
        private Node last;
        private int size;

        public DoubleEndedList() {
            this.head = null;
            this.last = null;
            this.size = 0;
        }
        public Node getHead() {
            return head;
        }
        public boolean isEmpty() {
            return this.head == null && this.last == null;
        }

        public int size() {
            return this.size;
        }

        public void insertFirst(Object data) {
            Node newNode = new Node(data);
            if (this.isEmpty()) {
                this.head = this.last = newNode;
            } else {
                newNode.setNext(this.head);
                this.head = newNode;
            }
            this.size++;
        }

        public void insertLast(Object data) {
            Node newNode = new Node(data);
            if (this.isEmpty()) {
                this.head = this.last = newNode;
            } else {
                this.last.setNext(newNode);
                this.last = newNode;
            }
            this.size++;
        }
         public Object removeFirst() {
            if (isEmpty()) {
                throw new NoSuchElementException("List is empty");
            }
            Node removedNode = head;
            head = head.next;
            size--;
            if (isEmpty()) {
                last = null;
            }
            return removedNode.value;
        }
    }

    class Queue {
        private DoubleEndedList list;

        public Queue() {
            this.list = new DoubleEndedList();
        }

        public void enqueue(Object element) {
            this.list.insertLast(element);
        }

        public Object dequeue() {
            if (isEmpty()) {
                throw new NoSuchElementException("Queue is empty");
            }
            // Utiliza el método getHead() para acceder a la cabeza de la lista
            Node removedNode = this.list.getHead();
            this.list.removeFirst();
            return removedNode.value;
        }

        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        public int size() {
            return this.list.size();
        }
    }














    public void startServer() {
        // Cambio: Usar ConcurrentLinkedQueue para almacenar los sockets de los clientes conectados.
        // Esto permite manejar múltiples clientes de forma concurrente.
        ConcurrentLinkedQueue<Socket> socketsQueue = new ConcurrentLinkedQueue<>();

        try (ServerSocket server = new ServerSocket(5000)) {
            System.out.println("Servidor iniciado en el puerto 5000");

            // Bucle infinito para aceptar conexiones de clientes de forma continua.
            while (true) {
                // Aceptar la conexión del cliente y crear un nuevo hilo para manejarla.
                final Socket clientSocket = server.accept();
                System.out.println("Cliente conectado: " + clientSocket.getRemoteSocketAddress());

                // Crear un nuevo hilo para manejar cada conexión de cliente de forma independiente.
                new Thread(() -> {
                    try {
                        // Leer el mensaje del cliente.
                        DataInputStream datos = new DataInputStream(clientSocket.getInputStream());
                        String mensajes = datos.readUTF();
                        System.out.println("Mensaje recibido: " + mensajes);

                        // Verificar si el mensaje indica un nuevo socket de cliente.
                        if (mensajes.startsWith("0")) {
                            // Añadir el nuevo socket de cliente a la cola.
                            socketsQueue.add(clientSocket);
                            System.out.println("Socket de cliente añadido a la cola");
                        } else {
                            // Reenviar otros mensajes a todos los clientes conectados.
                            System.out.println(mensajes);
                            for (Socket socket : socketsQueue) {
                                try (DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                                    out.writeUTF(mensajes);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start(); // Iniciar el hilo para manejar la conexión del cliente.
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método principal que inicia el servidor.
     */
    public static void main(String[] args) {
        new Servidor().startServer();
    }
}
