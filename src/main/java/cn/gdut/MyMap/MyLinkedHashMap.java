package cn.gdut.MyMap;

import java.util.*;

public class MyLinkedHashMap<K,V> extends MyHashMap<K,V> {

    /**
     * 双向链表，有连个指针
     * @param <K>
     * @param <V>
     */
    static class Entry<K,V> extends MyHashMap.Node<K,V> {
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next){
            super(hash, key, value, next);
        }
    }

    // 成员变量,双向链表的表头
    MyLinkedHashMap.Entry<K,V> head;

    // 表尾
    MyLinkedHashMap.Entry<K,V> tail;

    // 为访问顺序，false为顺序访问
    boolean accessOrder;

    /**
     *三个构造函数，有参构造，无参构造
     * inittialCapacity没有很重要，因为链表不需要想数组那样先声明足够的空间。
     * 下面的狗仔函数都是支持顺序访问的
     */
    public MyLinkedHashMap(int initalCapacity, float loadFactor){
        super(initalCapacity, loadFactor);
        accessOrder = false;
    }

    public MyLinkedHashMap(int initalCapacity){
        super(initalCapacity);
        accessOrder = false;
    }

    public MyLinkedHashMap(){
        super();
        accessOrder = false;
    }

    /**
     * 是否包含某个value
     * @param value value
     * @return 有 true。没有false
     */
    public boolean containsValue(Object value){
        for (MyLinkedHashMap.Entry<K,V> e = head; e != null; e = e.after){
            V v = e.value;
            if (v == value || (value != null && value.equals(v))){
                return true;
            }
        }
        return false;
    }

    public V get(Object key){
        Node<K,V> e;
        if ((e = getNode(hash(key) ,key)) == null){
            return null;
        }
        if (accessOrder){
            afterNodeAccess(e);
        }
        return e.value;
    }

    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next){
        MyLinkedHashMap.Entry<K,V> p = new MyLinkedHashMap.Entry<K,V>(hash, key, value, next);
        linkNodeLast(p);
        return p;
    }

    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next){
        MyLinkedHashMap.Entry<K,V> q = (MyLinkedHashMap.Entry<K,V>) p;
        MyLinkedHashMap.Entry<K,V> t = new MyLinkedHashMap.Entry<K,V>(q.hash, q.key, q.value, next);
        transferLinks(q, t);
        return t;
    }

    private void transferLinks(MyLinkedHashMap.Entry<K,V> src, MyLinkedHashMap.Entry<K,V> dst ){
        MyLinkedHashMap.Entry<K,V> b = dst.before = src.before;
        MyLinkedHashMap.Entry<K,V> a = dst.after = src.after;
        if (b == null){
            head = dst;
        }
        else {
            b.after = dst;
        }
        if (a == null){
            tail = dst;
        }
        else {
            a.before = dst;
        }
    }

    private void linkNodeLast(MyLinkedHashMap.Entry<K,V> p){
        MyLinkedHashMap.Entry<K,V> last = tail;
        tail = p;
        if (last == null){
            head = p;
        }
        else {
            p.before = last;
            last.after = p;
        }
    }

    // 将节点移到最后
    void afterNodeAccess(Node<K,V> e){
        MyLinkedHashMap.Entry last;
        // 判断最后一个元素不为空，并将tail赋值为last
        if ( accessOrder && (last = tail) != e){
            MyLinkedHashMap.Entry<K,V> p = (MyLinkedHashMap.Entry<K,V>) e, b = p.before , a = p.after;
            p.after = null;
            // 如果头节点为空，将a赋给head
            if (b == null){
                head = a;
            }
            else {
                b.after = a;
            }
            if ( a != null){
                a.before = b;
            }
            else {
                last = b;
            }
            if (last == null){
                head = p;
            }
            else {
                p.before = last;
                last.after = p;
            }
            tail = p;
        }
    }






    public static void main(String[] args) {
        MyLinkedHashMap myLinkedHashMap = new MyLinkedHashMap();
        for (int i = 0;i<3;i++){
            myLinkedHashMap.put(i,i+1);
        }
        System.out.println(myLinkedHashMap.get(2));
    }


}
