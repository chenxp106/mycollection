package cn.gdut.MyMap;

import javax.swing.tree.TreeNode;
import java.util.HashMap;
import java.util.Map;

public class MyHashMap<K,V> {

    // 成员变量
    // 默认初始容量，必须是2的次幂
    static final int  DEFAULT_INITIAL_CAPACITY = 1 << 4;

    // 最大的容量,为2^30
    static final int MAXIMUM_CAPACITY = 1 << 30;

    // 默认加载因子
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    // 链表长度到达8的时候将转化为红黑树
    static final int  TREEIFY_THRESHOLD = 8;

    //树的大小为6时，就转化为链表
    static final int UNTREEIFY_THRESHOLD = 6;

    // 至少容量达到64后，才可以转化为树
    static final int MIN_TREEIFY_CAPACITY = 64;

    // map中包含的个数
    int size;

    // 存储节点的数组
    Node<K,V> [] table;

    // 下一次扩容的值
    int threshold;

    // 装载因子
    float loadFactor ;


    // 普通节点
    static class Node<K,V> implements Map.Entry<K,V> {
        int hash;
        K key;
        V value;
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K, V> next){
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey(){
            return key;
        }

        public final V getValue(){
            return value;
        }


        public final String toString(){
            return key + "=" + value;
        }

        public final V setValue(V newValue){
            V oldValue = value;
            value = newValue;
            return oldValue;
        }
    }

    // 构造函数
    public MyHashMap(){
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }

    // 含有初始容量的构造函数
    public MyHashMap(int initialCapacity){
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    // 含有初始容量和装载因子的构造函数
    public MyHashMap(int initialCapacity, float loadFactor){
        if (initialCapacity < 0){
            throw new IllegalArgumentException("Illeage inintial capacity" + initialCapacity);
        }

        if (initialCapacity > MAXIMUM_CAPACITY){
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)){
            throw new IllegalArgumentException("Illeagel load factor" + loadFactor);
        }

        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }

    int tableSizeFor(int cap){
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        int res = (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
        return res;
    }


    /**
     * 插入一个值，如果有这个key，则覆盖当前的value。并将之前的value返回。
     * @param key key
     * @param value v
     * @return 旧的值
     */
    public V put(K key, V value){
        return putVal(hash(key), key, value, false, true);
    }

    /**
     *
     * @param hash key的哈希值
     * @param key key值
     * @param value v
     * @param onlyIfAbsent 如果为真，不能改变现有的值
     * @param evict 如果为false，表位创建模式
     * @return 返回之前的值，如果为空则返回null
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict){
        // 桶数组
        Node<K,V>[] tab;
        // 节点
        Node<K,V> p;
        // n 是当前tab的长度
        int n, i;
        // 如果当前对象为null或是它的内部没有任何元素，那么需要重新resize()一下
        if ( (tab = table) == null || (n = tab.length) == 0){
            n = (tab = resize()).length;
        }
        /**
         * 传入的hash值在当前数组中是否已经有元素，如果没有就重新new一个
         */
        //
        i = (n - 1) & hash;
        p = tab[i];
        if (p == null){
            tab[i] = newNode(hash, key, value, null);
        }
        // 走到这里，说明发生了碰撞，需要处理碰撞
        else {
            Node<K,V> e;
            K k;
            // 判断该位置上的Node的hash值和key是否和传入的参数一样
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))){
                // 此时指的是table[i]中存储的那个Node，如果待插入的节点的hash值和key中已存在，则将p赋值给e
                e = p;
            }

            // 到这里说明碰撞的节点是以单链表的形式存在，需要for循环进行遍历
            for (int bitCount = 0; ;++bitCount){
                if ((e = p.next) == null){
                    // 查询到链表的最后一个也没有找到，那么新建一个Node，然后加到第一个元素后面
                    p.next = newNode(hash, key, value, null);
                    break;

                }

                // 判断下一元素的hash和key是否相等，如果相等，则说明找到，直接退出
                if (e.hash == hash && ((k = e.key) == key ||(key != null && key.equals(k)))){
                    break;
                }
                // 将p调整为下一个节点,p后移一位
                p = e;
            }

            // 若e不为null，表示已经存在和待插入节点hash，key相同的节点，hashmap后插入的key对应的value会覆盖以前相同的key对应的value值
            if (e != null){
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null){
                    e.value = value;
                }

                return oldValue;
            }
        }
        // 如果个数大于当前链表，则需要重新赋值数组。
        if (++size > threshold){
            resize();
        }
        return null;

    }

    static final int hash(Object key){
        int h ;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    // 新建一个节点,一个节点包含四个属性
    Node<K, V> newNode(int hash, K key, V value, Node<K, V> next){
        return new Node<K, V>(hash, key, value, next);
    }


    // 重新建一个桶
    final Node<K,V> [] resize(){
        Node<K,V>[] oldTab = table;
        // 如果没有元素，则将老的设置为0.将oldTab设为原来的长度
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0){
            // 如果大小超过了2^30
            if (oldCap >= MAXIMUM_CAPACITY){
                // 将容量设置为最大的值
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // 扩容，将元素扩大两倍
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY){
                newThr = oldThr << 1;
            }
        }
        // 如果一开始设置了，
        else if (oldThr > 0){
            newCap = oldThr;
        }
        // 设置为默认值
        else {
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);

        }
        if (newThr == 0){
            float ft = (float) newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ? (int) ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K, V>[] newTab = (Node<K,V>[]) new Node[newCap];
        table = newTab;
        // 扩容完成，需要将旧的数据拷贝
        if (oldTab != null){
            for (int j = 0; j < oldCap;j++){
                Node<K,V> e;
                if ((e = oldTab[j]) != null){
                    // 将当前位置置null
                    oldTab[j] = null;
                    // 当前的桶只有一个元素
                    if (e.next == null){
                        newTab[e.hash & (newCap - 1)] = e;
                    }
                    // 暂时没有考虑红黑树.链表的数据拷贝
                    else {
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            if ( (e.hash & oldCap) == 0){
                                if (loTail == null){
                                    loHead = e;
                                }
                                else {
                                    loHead.next = e;
                                }
                                loTail = e;

                            }
                            else {
                                if (hiTail == null){
                                    hiHead = e;
                                }
                                else {
                                    hiTail.next = e;
                                }
                                hiTail = e;
                            }
                        }while ((e = next) != null);
                        if (loTail != null){
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null){
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }

                }
            }
        }
        return newTab;
    }

    //

    /**
     * 根据key获取value.如果没有，返回null。有值返回value
     * @param key key
     * @return value
     */
    public V get(Object key){
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * 根据key返回节点
     * @param hash
     * @param key
     * @return
     */
    final Node<K,V> getNode(int hash, Object key){
        Node<K,V>[] tab;
        Node<K,V> first,e;
        int n;
        K k;
        // 判断表不为空，并且里面有元素,first表示当前桶的第一个元素
        if ((tab = table) != null && (n = tab.length) > 0 && (first = tab[(n - 1) & hash]) != null){
            // 第一个元素就为要找的元素
            if (first.hash == hash && ((k = first.key) == key || (key != null && key.equals(k)))){
                return first;
            }
            // 循环，找到与key相等的节点
            if ((e = first.next) != null){
                do {
                    if (e.hash == hash && ((k = e.key) == key || key.equals(k))){
                        return e;
                    }
                    // 继续循环，往下找节点
                }while ( ( e = e.next) != null);
            }
        }
        // 找不到，返回null
        return null;
    }

    /**
     * 返回节点的个数
     * @return 个数
     */
    public int size(){
        return size;
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public boolean containsKey(Object key){
        return getNode(hash(key), key) != null;
    }

    /**
     * 删除一个节点
     * @param key key
     * @return 删除元素的value
     */
    public V remove(Object key){
        Node<K,V> e;
        return (e = removeNode(hash(key) ,key, null, false,true)) == null ? null : e.value;
    }

    public Node<K,V> removeNode(int hash, Object key, Object value, boolean matchValue, boolean movable){
        Node<K,V>[] tab;
        Node<K,V> p;
        int n, index;
        // 桶不为空，有个数，并且key对应的有元素
        if ((tab = table) != null && (n = tab.length) > 0 && (p = tab[index = ( n - 1) & hash]) != null){
            Node<K,V> node = null;
            Node<K,V> e;
            K k;
            V v;
            if ( p.hash == hash && ( (k = p.key) == key  ||  (key != null && key.equals(k)) )  ){
                node = p;
            }
            else if ((e = p.next) != null){
                do {
                    if (e.hash == hash && ((k = e.key) == key ||(key != null && key.equals(k)))){
                        node = e;
                        break;
                    }
                    p = e;
                }while ((e = e.next) != null);
            }
            if (node != null && (!matchValue || (v = node.value) == value || (value != null && value.equals(v)))){
                if (node == p){
                    tab[index] = node.next;
                }
                else {
                    p.next = node.next;
                }
                --size;
                return node;
            }

        }
        return null;
    }

    public static void main(String[] args) {
        MyHashMap myHashMap = new MyHashMap(12);
        myHashMap.put(1,2);
        myHashMap.put(2,3);
        myHashMap.put(3,4);
        myHashMap.put(17,2);
        myHashMap.put(33,5);
        myHashMap.put('d','d');
        for (int i = 0 ;i<12;i++){
            myHashMap.put(i * 8,8);
        }
        System.out.println(myHashMap.get(64));
        System.out.println(myHashMap.size());
        myHashMap.remove(22);
        System.out.println(myHashMap.containsKey(22));
    }

}
