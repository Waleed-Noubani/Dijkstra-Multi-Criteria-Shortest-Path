package application;

public class LinkedList<T> {
    
    private Node<T> head;
    private Node<T> tail;
    private int size;
    
    private static class Node<T> {
        T data;
        Node<T> next;
        
        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }
    
    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }
    
 
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            // أضف في النهاية
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }
    

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }
    
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        T data;
        
        if (index == 0) {
            data = head.data;
            head = head.next;
            if (head == null) {
                tail = null;
            }
        } else {
            Node<T> current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            
            data = current.next.data;
            current.next = current.next.next;
            
            if (current.next == null) {
                tail = current;
            }
        }
        
        size--;
        return data;
    }
    
 
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
    
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
    
    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        Node<T> current = head;
        
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        
        sb.append("]");
        return sb.toString();
    }
    

    public Iterator<T> iterator() {
        return new Iterator<T>();
    }
    
    public class Iterator<E> {
        private Node<T> current = head;
        
        public boolean hasNext() {
            return current != null;
        }
        
        public T next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            T data = current.data;
            current = current.next;
            return data;
        }
    }
}