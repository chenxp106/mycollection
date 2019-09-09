package cn.gdut.ArrayDeque;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MyArrayDeque<E>  {

    // 存放元素，
    Object[] elements;

    // 标记队首元素所在的位置
    int head;

    // 标记队尾所在的位置
    int tail;

    // 队列的最小容量，must 2的倍数
    private static final int  MIN_INITAIL_CAPACITY = 8 ;

    /**
     * 默认构造函数，将长度设置为16，相当于最小容量2倍
     */
    public MyArrayDeque(){
        elements = new Object[16];
    }

    /**
     * 带初始容量的队列
     * @param numElements
     */
    public MyArrayDeque(int numElements){
        allocateElement(numElements);
    }

    private void allocateElement(int numElement){
        int initialCapacity = MIN_INITAIL_CAPACITY;
        if (numElement >= initialCapacity){
            initialCapacity = numElement;
            initialCapacity |= (initialCapacity >>> 1);
            initialCapacity |= (initialCapacity >>> 2);
            initialCapacity |= (initialCapacity >>> 4);
            initialCapacity |= (initialCapacity >>> 8);
            initialCapacity |= (initialCapacity >>> 16);
            initialCapacity++;
        }

        // 如果出现负数，还要向右移动一位
        if (initialCapacity < 0){
            initialCapacity >>>= 1;
        }

        elements = new Object[initialCapacity];
    }

    public void addFirst(E e) {
        if (e == null){
            throw new NullPointerException();
        }
//        int a =  head - 1;
//        int b = elements.length - 1;
//        head = a & b;
//        elements[head] = e;
        /**
         * 先确定位置，再赋值
         * 当为第一个数时，都指向0.head-1 = -1，二进制数为111111111,element.length - 1=15
         * & 的结果为15，所以element[15] = e;
         */
        elements[head = (head - 1) & (elements.length - 1)] = e;
        // 这时需要判断是否为队列满
        if (head == tail){
            doubleCapacity();
        }

    }

    private void doubleCapacity(){
        // 断言
        assert head == tail;
        int p = head;
        int n = elements.length;
        int r = n - p;
        int newCapacity = n << 1;
        // 容量太大，再移动就为负数
        if (newCapacity < 0){
            throw new IllegalStateException("Sorry ,duque too big");
        }
        Object[] a = new Object[newCapacity];
        // 左侧数据拷贝
        System.arraycopy(elements, p, a,0, r);
        // 右侧数据拷贝
        System.arraycopy(elements, 0, a, r, p);
        // 将a重新赋值为element
        elements = a;
        // 重新标记队头和队尾
        head = 0;
        tail = n;
    }

    /**
     * 在队尾添加元素
     * @param e 元素
     */
    public void addLast(E e) {
        if (e == null){
            throw new NullPointerException();
        }
        // 先赋值，在加1
        elements[tail] = e;
        // 将tail+1，并判断是否队满
        if((tail = (tail + 1) & (elements.length - 1)) == head){
            doubleCapacity();
        }

    }

    /**
     * 在队头插入元素，插入成功，返回true。addFirst没有返回值
     * @param e
     * @return
     */
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    public E removeFirst() {
        E x = pollFirst();
        if (x == null){
            throw new NoSuchElementException();
        }
        return x;
    }

    public E removeLast() {
        E x = pollLast();

        return x;
    }

    private E pollFirst() {
        // 获取第一个元素的值
        int h = head;
        E result = (E)elements[h];
        if (result == null){
            return null;
        }
        // 将队头的值赋值为null
        elements[h] = null;
        head = (h+1) & (elements.length - 1);
        return result;
    }

    private E pollLast() {
        int t = (tail - 1) & (elements.length - 1);;
        E result = (E) elements[t];
        if (result == null){
            return null;
        }
        // 将tail位置上的值赋值null
        elements[tail] = null;
        // 修改tail的值
        tail = t;
        return result;
    }

    /**
     * 获取第一个元素的值。如果为空，则抛出异常
     * @return 结果
     */
    public E getFirst() {
        int h = head;
        E result = (E)elements[h];
        if (result == null){
            throw new NullPointerException();
        }
        return result;
    }

    public E getLast() {
        int t = (tail - 1) & (elements.length - 1);
        E result = (E) elements[t];
        if (result == null){
            throw new NullPointerException();
        }
        return result;
    }

    public E peekFirst() {
        return (E) elements[head];
    }

    public E peekLast() {
        return (E)elements[(tail - 1) & (elements.length - 1)];
    }

    public boolean removeFirstOccurrence(Object o) {
        return false;
    }

    public boolean removeLastOccurrence(Object o) {
        return false;
    }

    public boolean add(E e) {
        addLast(e);
        return true;
    }

    public boolean offer(E e) {
        return offerLast(e);
    }

    public E remove() {

        return removeFirst();
    }

    public E poll() {
        return pollFirst();
    }

    /**
     * 获取队头的元素
     * @return
     */
    public E element() {
        return getFirst();
    }

    public E peek() {
        return peekFirst();
    }

    public void push(E e) {
        addFirst(e);
    }

    public E pop() {
        return removeFirst();
    }

    public boolean remove(Object o) {
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        return false;
    }

    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        return false;
    }

    // 将所有元素置null
    public void clear() {
        int h = head;
        int t = tail;
        if (h != t){
            //  将头尾指针都指向0
            head = tail = 0;
            int i = h;
            int mask = elements.length - 1;
            // 循环的讲所有元素进行赋null
            do {
                elements[i] = null;
                i = (i + 1) & mask;
            }while (i != t);
        }

    }

    public boolean contains(Object o) {
       if (o == null){
           return false;
       }
       Object x;
       int mask = elements.length - 1;
       int i = head;
       while ((x = elements[i]) != null){
           // 找到该元素值
           if (o.equals(x)){
               return true;
           }
           i = (i + 1) & mask;
       }
       return false;
    }

    public int size() {
        return (tail - head) & (elements.length - 1);
    }

    public boolean isEmpty() {
        return tail == head;
    }



    public static void main(String[] args) {
        MyArrayDeque myArrayDeque = new MyArrayDeque();
        myArrayDeque.addLast('l');
        myArrayDeque.addFirst('f');
        System.out.println(myArrayDeque.contains('l'));

    }
}
