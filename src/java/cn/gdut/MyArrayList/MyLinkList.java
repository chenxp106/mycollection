package cn.gdut.MyArrayList;


import java.util.Collection;
import java.util.NoSuchElementException;

public class MyLinkList<E> {

    /**
     * 节点的个数
     */
    int size = 0;

    /**
     * 头节点
     */
    Node<E> first;

    Node<E> last;

    @Override
    public String toString() {
        return "MyLinkList{" +
                "size=" + size +
                ", first=" + first +
                ", last=" + last +
                '}';
    }

    /**
     * 内部类
     * @param <E>
     */
    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next){
            this.item = element;
            this.prev = prev;
            this.next = next;
        }
    }

    /**
     * 无参构造
     */
    public MyLinkList(){

    }

    /**
     * 有参构造
     * @param c
     */
    public MyLinkList(Collection<? extends E> c){
        this();

    }

    /**
     * 添加一个元素，将元素添加到末尾
     * @param e
     * @return
     */
    public boolean add(E e){
        linkLast(e);
        return true;
    }

    /**
     * 连接e作为最后一个元素
     * @param e e
     */
    void linkLast(E e){
        final Node<E> l = last;
        // 新建一个节点
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        // 如果没有一个节点。如果没有，则newNode即是头节点也是尾节点
        if (l == null){
            first = newNode;
        }
        // 至少有一个节点
        else {
            l.next = newNode;
        }
        size++;
    }

    /**
     * 获取指定索引的元素的值
     * @param index 索引
     * @return 节点
     */
    Node<E> node(int index){
        // 要获取的节点在前半段
        if (index < (size >> 1)){
            Node<E> x = first;
            for (int i = 0;i < index;i++){
                x = x.next;
            }
            return x;
        }
        // 在后半段
        else {
            Node<E> x = last;
            for (int i = size - 1;i > index ;i--){
                x = x.prev;
            }
            return x;
        }
    }

    /**
     * 在指定位置插入元素
     * @param index 索引
     * @param element 元素
     */
    public void add(int index, E element){
        // 验证下标是否合法
        checkPositionIndex(index);
        if (index == size){
            linkLast(element);
        }
        else {
            linkBefore(element, node(index));
        }
    }

    /**
     * 插入一个元素到第一个节点
     * @param e
     */
    public void addFirst(E e){
        linkFirst(e);
    }

    public void addLast(E e){
        linkLast(e);
    }

    /**
     * 将e插入到头节点中
     * @param e
     */
    private void linkFirst(E e){
        final Node<E> f = first;
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        if (f == null){
            last = newNode;
        }
        else {
            f.prev = newNode;
        }
        size++;
    }

    private void checkPositionIndex(int index){
        if (!isPositionIndex(index)){
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    private boolean isPositionIndex(int index){
        return index >= 0 && index <= size;
    }

    private String outOfBoundsMsg(int index){
        return "Index:" + index +", size" + size;
    }

    /**
     * 将节点e插入到succ前面
     * @param e
     * @param succ
     */
    void linkBefore(E e, Node<E> succ){
        Node<E> pred = succ.prev;
        Node<E> newNode = new Node<>(pred, e, succ);
        succ.prev = newNode;
        if (pred == null){
            first = newNode;
        }
        else {
            pred.next = newNode;
        }
        size++;
    }

    /**
     * 移除第一个元素
     * @return
     */
    public E removeFirst(){
        final Node<E> f = first;
        // 判空，如果为空，则报错
        if (f == null){
            throw new NoSuchElementException();
        }
        return unlinkFirst(f);
    }

    /**
     * 删除第一个元素
     * @param f 第一个节点
     * @return 要删除的第一个元素 的值
     */
    private E unlinkFirst(Node<E> f){
        // 取出要移除的一个元素的值
        final E element = f.item;
        final Node<E> next = f.next;
        f.item = null;
        f.next = null;
        first = next;
        // 当前只有一个元素，
        if (next == null){
            last = null;
        }
        else {
            next.prev = null;
        }
        size--;
        return element;
    }

    public E removeLast(){
        final Node<E> l = last;
        if (l == null){
            throw new NoSuchElementException();
        }
        return unlinkLast(l);
    }

    private E unlinkLast(Node<E> l){
        final E element = l.item;
        Node<E> pre = l.prev;
        l.prev = null;
        l.item = null;
        last = pre;
        if (pre == null){
            first = null;
        }
        else {
            pre.next = null;
        }
        size--;
        return element;
    }

    /**
     * 获取指定位置的元素
     * @param index 索引
     * @return 元素的值
     */
    public E get(int index){
        // 索引判断
        checkElementIndex(index);
        return node(index).item;
    }

    /**
     * 删除指定位置的元素
     * @param index 索引
     * @return 删除元素的值
     */
    public E remove(int index){
        checkElementIndex(index);
        return unlink(node(index));
    }

    E unlink(Node<E> x){
        final E element = x.item;
        final Node<E> pre = x.prev;
        final Node<E> next = x.next;

        if (pre == null){
            first = next;
        }
        else {
            pre.next = next;
            x.prev = null;
        }

        if (next == null){
            last = pre;
        }
        else {
            next.prev = pre;
            x.next = null;
        }
        x.item = null;
        size--;
        return element;
    }

    /**
     * 获取第一个元素的值
     * @return
     */
    public E getFirst(){
        final Node<E> f = first;
        if (f == null){
            throw new NoSuchElementException();
        }
        return f.item;
    }

    private void checkElementIndex(int index){
        if (!isElementIndex(index)){
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    private boolean isElementIndex(int index){
        return index >= 0 && index < size;
    }

    /**
     * 将元素清空，将所有的元素置null
     */
    public void clear(){
        for (Node<E> x = first; x != null;){
            Node<E> next = x.next;
            x.item = null;
            x.prev = null;
            x.next = null;
            x = next;
        }

        // 最后将头尾指针置null
        first = last = null;
        size = 0;
    }

    public void printLinkedList(){
        Node<E> x = first;
        while (x != null){
            System.out.println(x.item);
            x = x.next;
        }
    }

    // 队列操作

    /**
     * 队列操作,仅仅获取第一个元素，并不会将第一个元素弹出
     * @return 返回第一个元素，如果为空，值返回null。不会抛出异常
     */
    public E peek(){
        Node<E> f = first;
        if (f == null){
            return null;
        }
        else {
            return f.item;
        }
    }

    /**
     * 队列操作，获取第一个元素，但是不会删除这个元素
     * @return 第一个元素的值，如果列表为空，则抛出异常
     */
    public E element(){
        return getFirst();
    }

    /**
     * 队列操作
     * @return 弹出第一个元素，如果链表为空，则返回null
     */
    public E poll(){
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    /**
     *  队列操作,移除第一个元素
     * @return 移除的元素值.并删除表头，表头为空，则抛出异常
     */
    public E remove(){
        return removeFirst();
    }

    /**
     *
     * @param e
     * @return
     */
    public boolean offer(E e){
        return add(e);
    }

    // 双端队列操作

    /**
     * 在首部插入新节点
     * @param e
     * @return
     */
    public boolean offerFirst(E e){
        addFirst(e);
        return true;
    }

    /**
     * 在末尾增加元素
     * @param e
     * @return
     */
    public boolean offerLast(E e){
        addLast(e);
        return true;
    }

    /**
     * 双端队列操作，获取链表的头结点
     * @return
     */
    public E peekFirst(){
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    /**
     * 双端队列操作，获取链表的尾节点
     * @return
     */
    public E peekLast(){
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

    /**
     *双端队列操作，弹出第一个元素
     * @return
     */
    public E pollFirst(){
        final Node<E> f = first;
        return unlinkFirst(f);
    }


    public E pollLast(){
        final Node<E> l = last;
        return unlinkLast(l);
    }
    public static void main(String[] args) {
        MyLinkList myLinkList = new MyLinkList();
        myLinkList.add("a");
        myLinkList.add("b");
        myLinkList.add("c");
        myLinkList.add("v");
//        System.out.println(myLinkList.first.item);
//        System.out.println(myLinkList.node(2).item);
//        myLinkList.add(1,"4");
//        myLinkList.removeFirst();
//        myLinkList.removeLast();
//        myLinkList.remove(3);
//        myLinkList.offer('b');
//        System.out.println(myLinkList.get(2));
//        myLinkList.clear();
//        myLinkList.offerFirst("first");
//        myLinkList.offerLast("last");
//        System.out.println(myLinkList.pollFirst());
        System.out.println(myLinkList.pollLast());
        myLinkList.printLinkedList();
//        System.out.println(myLinkList.printLinkedList());
//        System.out.println(myLinkList.peekFirst());
    }

}
