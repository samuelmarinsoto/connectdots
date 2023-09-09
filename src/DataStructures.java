class Node {
    private Object data;
    private Node next;

    public Node(Object data) {
        this.next = null;
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Node getNext() {
        return this.next;
    }

    public void setNext(Node node) {
        this.next = node;
    }
}

class LinkedList {
    private Node head;
    private int size;

    public LinkedList() {
        this.head = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return this.head == null;
    }

    public int size() {
        return this.size;
    }

    public void insertFirst(Object data) {
        Node newNode = new Node(data);
        newNode.setNext(this.head);
        this.head = newNode;
        this.size++;
    }

    public Node deleteFirst() {
        if (this.head != null) {
            Node temp = this.head;
            this.head = this.head.getNext();
            this.size--;
            return temp;
        } else {
            return null;
        }
    }

    public void displayList() {
        Node current = this.head;
        while (current != null) {
            System.out.println(current.getData());
            current = current.getNext();
        }
    }

    public Node find(Object searchValue) {
        Node current = this.head;
        while (current != null) {
            if (current.getData().equals(searchValue)) {
                return current;
            } else {
                current = current.getNext();
            }
        }
        return null;
    }

    public Node delete(Object searchValue) {
        Node current = this.head;
        Node previous = this.head;
        while (current != null) {
            if (current.getData().equals(searchValue)) {
                if (current == this.head) {
                    this.head = this.head.getNext();
                } else {
                    previous.setNext(current.getNext());
                }
                this.size--;
                return current;
            } else {
                previous = current;
                current = current.getNext();
            }
        }
        return null;
    }
}

class DoubleEndedList {
    private Node head;
    private Node last;
    private int size;

    public DoubleEndedList() {
        this.head = null;
        this.last = null;
        this.size = 0;
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
}

class Stack {
    private int maxSize;
    private Object[] stackArray;
    private int top;

    public Stack(int maxSize) {
        this.maxSize = maxSize;
        this.stackArray = new Object[maxSize];
        this.top = -1;
    }

    public void push(Object newObject) {
        if (top < maxSize - 1) {
            this.stackArray[++top] = newObject;
        } else {
            throw new RuntimeException("Stack is full");
        }
    }

    public Object pop() {
        if (top >= 0) {
            return this.stackArray[top--];
        } else {
            throw new RuntimeException("Stack is empty");
        }
    }

    public Object peek() {
        if (top >= 0) {
            return this.stackArray[top];
        } else {
            throw new RuntimeException("Stack is empty");
        }
    }
}

class StackLinkedList {
    private LinkedList stackList;

    public Stack1() {
        this.stackList = new LinkedList();
    }

    public void push(Object newElement) {
        this.stackList.insertFirst(newElement);
    }

    public Object pop() {
        Node removedNode = this.stackList.deleteFirst();
        return (removedNode != null) ? removedNode.getData() : null;
    }

    public Object peek() {
        return (this.stackList.head != null) ? this.stackList.head.getData() : null;
    }
}
