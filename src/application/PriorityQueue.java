package application;

public class PriorityQueue {
    
    private Node[] heap;
    private int size;
    private int capacity;
    
    
    public PriorityQueue() {
        this.capacity = 1000;  
        this.heap = new Node[capacity];
        this.size = 0;
    }
    
    public PriorityQueue(int initialCapacity) {
        this.capacity = initialCapacity;
        this.heap = new Node[capacity];
        this.size = 0;
    }
    
    public void add(Node element) {
        if (element == null) {
            throw new NullPointerException("Cannot add null element");
        }
        
        if (size == capacity) {
            resize();
        }
        
        heap[size] = element;
        heapifyUp(size);
        size++;
    }
    
    // إزالة أصغر node 
    public Node poll() {
        if (size == 0) {
            return null;
        }
        
        Node min = heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        
        if (size > 0) {
            heapifyDown(0);
        }
        
        return min;
    }
    
    // شوف أصغر node بدون ما تشيله
    public Node peek() {
        if (size == 0) {
            return null;
        }
        return heap[0];
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    
    private void heapifyUp(int index) {
        int current = index;
        
        while (current > 0) {
            int parent = (current - 1) / 2;
            
            if (heap[current].getCost() >= heap[parent].getCost()) {
                break;
            }
            
            Node temp = heap[current];
            heap[current] = heap[parent];
            heap[parent] = temp;
            
            current = parent;
        }
    }
    private void heapifyDown(int index) {
        int current = index;
        
        while (true) {
            int left = 2 * current + 1;
            int right = 2 * current + 2;
            int smallest = current;
            
            // قارن بالـ cost
            if (left < size && heap[left].getCost() < heap[smallest].getCost()) {
                smallest = left;
            }
            
            if (right < size && heap[right].getCost() < heap[smallest].getCost()) {
                smallest = right;
            }
            
            if (smallest == current) {
                break;
            }
            
            // Swap
            Node temp = heap[current];
            heap[current] = heap[smallest];
            heap[smallest] = temp;
            
            current = smallest;
        }
    }
    
    private void resize() {
        capacity *= 2;
        Node[] newHeap = new Node[capacity];
        
        for (int i = 0; i < size; i++) {
            newHeap[i] = heap[i];
        }
        
        heap = newHeap;
    }
    
    public void clear() {
        size = 0;
        for (int i = 0; i < heap.length; i++) {
            heap[i] = null;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(heap[i]);
            if (i < size - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
