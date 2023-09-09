public class Tester {    public static void testNode() {
        System.out.println("Testing Node class:");

        // Create a new node
        Node node = new Node(5);
        System.out.println("Created node with data: " + node.getData());

        // Set and get data
        node.setData(10);
        System.out.println("Updated node data to: " + node.getData());

        // Set and get next node
        Node nextNode = new Node(15);
        node.setNext(nextNode);
        System.out.println("Set next node with data: " + node.getNext().getData());
    }

    public static void testLinkedList() {
        System.out.println("\nTesting LinkedList class:");

        LinkedList ll = new LinkedList();
        System.out.println("List initialized. Is empty? " + ll.isEmpty());

        // Insert data
        ll.insertFirst(20);
        ll.insertFirst(25);
        System.out.print("List after inserting 25 and 20: ");
        ll.displayList();

        // Delete first and display
        System.out.println("Deleted data: " + ll.deleteFirst().getData());
        System.out.print("List after deletion: ");
        ll.displayList();

        // Find and delete specific data
        System.out.println("Node with data 20 found: " + (ll.find(20) != null));
        System.out.println("Deleted node with data 20: " + ll.delete(20).getData());
        System.out.print("List after deletion: ");
        ll.displayList();
    }
    public static void testDoubleEndedList() {
        System.out.println("\nTesting DoubleEndedList class:");

        DoubleEndedList delist = new DoubleEndedList();
        System.out.println("Double ended list initialized. Is empty? " + delist.isEmpty());

        // Insert data
        delist.insertFirst(30);
        delist.insertLast(35);
        delist.insertLast(40);
        System.out.println("Size after inserting data: " + delist.size());
    }

    public static void testStack() {
        System.out.println("\nTesting Stack class:");

        Stack s = new Stack(5);

        // Push and peek
        s.push(45);
        s.push(50);
        System.out.println("Top of stack after pushing 45 and 50: " + s.peek());

        // Pop and peek
        System.out.println("Popped data: " + s.pop());
        System.out.println("Top of stack after pop: " + s.peek());
    }

    public static void testStackLinkedList() {
        System.out.println("\nTesting StackLinkedList class:");

        StackLinkedList sl = new StackLinkedList();

        // Push and peek
        sl.push(55);
        sl.push(60);
        System.out.println("Top of stack after pushing 55 and 60: " + sl.peek());

        // Pop and peek
        System.out.println("Popped data: " + sl.pop());
        System.out.println("Top of stack after pop: " + sl.peek());
    }

    public static void main(String[] args) {
        // ... other testing methods ...
        testDoubleEndedList();
        testStack();
        testStackLinkedList();
        testNode();
        testLinkedList();
        // ... We'll add more test methods later ...
    }
}

