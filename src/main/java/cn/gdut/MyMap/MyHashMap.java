package cn.gdut.MyMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class MyHashMap<K,V> {

    // 成员变量
    // 默认初始容量，必须是2的次幂
    static final int  DEFAULT_INITIAL_CAPACITY = 1 << 4;

    // 最大的容量,为2^30
    static final int MAXIMUM_CAPACITY = 1 << 30;

    // 默认加载因子
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    // 链表长度到达8的时候将转化为红黑树
    static final int  TREEIFY_THRESHOLD = 4;

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
    static class Node<K,V> {
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
        // 数组
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
            else if (p instanceof TreeNode){
                e = ((TreeNode<K,V>) p).putTreeVal(this, tab, hash, key, value);
            }
            else {

                // 到这里说明碰撞的节点是以单链表的形式存在，需要for循环进行遍历
                for (int bitCount = 0; ;++bitCount){
                    if ((e = p.next) == null){
                        // 查询到链表的最后一个也没有找到，那么新建一个Node，然后加到第一个元素后面
                        p.next = newNode(hash, key, value, null);
                        // 冲突超过7，第一次又一次循环，将数组转化为二叉树
                        if (bitCount >= TREEIFY_THRESHOLD - 1){
                            treeifyBin(tab, hash);
                        }
                        break;

                    }

                    // 判断下一元素的hash和key是否相等，如果相等，则说明找到，直接退出
                    if (e.hash == hash && ((k = e.key) == key ||(key != null && key.equals(k)))){
                        break;
                    }
                    // 将p调整为下一个节点,p后移一位
                    p = e;
                }
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

    /**
     * 将链表转化为红黑树
     * @param tab 数组
     * @param hash hsah值
     */
    final void treeifyBin(Node<K,V>[] tab, int hash){
        int n, index;
        Node<K,V> e;

        /**
         * 如果数组为空，或是数组中的个数太少，少于64，没有达到树化的要求，使用扩容的方式进行。
         * 当一个数组位置上集中了多个键值，那是因为这么key的hash值和数组长度取磨之后的结果相同。（不是因为这些key相同）
         * 因为hash值相同的概率不高，所以可以采用扩容的方式，来使得这些key的hash值在和新的数组长度取磨之后拆分到数组中的其他位置。
         */
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY){
            resize();
        }
        /**
         * 如果数组的长度超过了64，那么就有必要进行树化操作
         * 根据hash值和数组长度进行取磨运算，得到链表的首节点。其中e为首节点
         */
        else if ((e = tab[index = ( n - 1) & hash]) != null){
            // 定义首尾节点
            TreeNode<K,V> hd = null, t1 = null;
            do {
                // 将普通节点转化为树的节点
                TreeNode<K,V> p = replacementTreeNode(e, null);
                // 如果尾节点为空，首节点指向当前节点
                if (t1 == null){
                    hd = p;
                }
                // 尾节点不为空，将当前节点p和前后节点连接起来
                else {
                    p.prev = t1;
                    t1.next = p;
                }
                // 并将尾节点后移一个
                t1 = p;
            }while ((e = e.next) != null);
            // 到目前为止，只是把Node节点换成了TreeNode节点。把单像链表变成了双向链表
            // 将转换后的双向链表，替换原来的单链表
            if ((tab[index] = hd) != null){
                hd.treeify(tab);
            }
        }
    }



    // 将普通节点换成红黑树的节点
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next){
        return new TreeNode<K, V>(p.hash,p.key, p.value, p.next);
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
            // 如果大小超过了2^30,没救了
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
                            // hash值落在旧的数组上。
                            if ( (e.hash & oldCap) == 0){
                                if (loTail == null){
                                    loHead = e;
                                }
                                else {
                                    loHead.next = e;
                                }
                                loTail = e;

                            }
                            // 否则，可能不在原来的位置上
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
                if (first instanceof TreeNode){
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                }
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
        // 定义一个节点变量，用于存储要被删除的节点
        Node<K,V> e;
        // 调用removeNode方法
        return (e = removeNode(hash(key) ,key, null, false,true)) == null ? null : e.value;
    }

    /**
     * 方法为final，不可以被覆盖。子类可以通过实现afterNodeRemoval方法来增加自己的处理逻辑，
     * @param hash key的hash值。 该值是通过hash(key)获得的
     * @param key 要删除的键值对key
     * @param value 要删除的键值对value。
     * @param matchValue 如果为true。则当key对应的键值对的值equals(value)为true时才删除，否则不关心value的值。
     * @param movable 删除后是否移动节点，如果为false，则不移动
     * @return 返回被删除的节点对象，如果没有删除任何节点，返回null
     */
    final Node<K,V> removeNode(int hash, Object key, Object value, boolean matchValue, boolean movable){
        Node<K,V>[] tab;
        Node<K,V> p;
        int n, index;
        // 桶不为空，有个数，并且key对应的有元素
        // 需要从该节点p向下遍历，找到那个和key匹配的节点对象
        if ((tab = table) != null && (n = tab.length) > 0 && (p = tab[index = ( n - 1) & hash]) != null){
            // 临时节点变量，键变量，值变量
            Node<K,V> node = null;
            Node<K,V> e;
            K k;
            V v;
            // 如果当前节点的键和key相等。那么当前节点就是要删除的节点，赋给node
            if ( p.hash == hash && ( (k = p.key) == key  ||  (key != null && key.equals(k)) )  ){
                node = p;
            }
            /**
             * 如果走到这里说明首节点没有匹配上。那么检查下是否有next节点
             * 如果没有next节点，说明该位置上没有发生冲突，最后返回null
             * 如果存在next节点，说明该数组上发生了hash碰撞，此时可能存在一个链表，也可能是一个红黑树
             */
            else if ((e = p.next) != null){
                if (p instanceof TreeNode){
                    // 如果当前节点是TreeNode类型，说明已经是一个红黑树，通过getTreeNode方法获取节点
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                }
                // 如果不是树节点 ，那么就是一个链表，只需要从头到尾逐个遍历
                else {
                    do {
                        // 如果节点的键和key相等，e节点就是要删除的节点，赋值给node节点。退出循环
                        if (e.hash == hash && ((k = e.key) == key ||(key != null && key.equals(k)))){
                            node = e;
                            break;
                        }
                        // 当前节点不是，指针下移一位。知道匹配到下一个节点
                        p = e;
                        // 如e存在下一个节点，那么继续去匹配下一个节点，知道退出到某个节点，或者 遍历完所有的节点
                    }while ((e = e.next) != null);
                }

            }
            /**
             * 如果node不为空，说明根据key匹配到了要删除的节点
             * 如果不需要对比value值 或者 对比value的值但而且value值也相等
             * 那么就可以删除该node节点了
             */
            if (node != null && (!matchValue || (v = node.value) == value || (value != null && value.equals(v)))){
                if (node instanceof TreeNode){
                    // 如果是TreeNod对象，说明此节点存在于红黑树中
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                }
                else if (node == p){
                    // 由于要删除的节点是首节点，只需要将节点数组对应的位置指向到第二个节点即可
                    tab[index] = node.next;
                }
                // 如果不是首节点，此时p是node节点的父节点，由于要删除node，只需要把p的下一个节点指向node的下一个节点
                else {
                    p.next = node.next;
                }
                --size;
                return node;
            }

        }
        return null;
    }


    /* -------------------------------------*/
    // Tree bins

    static final class TreeNode<K,V> extends MyHashMap.Node<K,V>{
        TreeNode<K,V> parent;
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;
        boolean red;
        TreeNode(int hash, K key, V val, Node<K,V> next){
            super(hash, key, val,next);
        }

        /**
         * 返回root节点
         * @return root
         */
        final TreeNode<K,V> root(){
            for (TreeNode<K,V> r = this,p;;){
                if ((p = r.parent) == null){
                    return r;
                }
                r = p;
            }
        }

        final TreeNode<K,V> putTreeVal(MyHashMap<K,V> map, Node<K,V> [] tab,
                                       int h, K k,V v){
            // key的类型
            Class<?> kc = null;
            boolean searched = false;
            // 找到父节点
            TreeNode<K,V> root = (parent != null) ? root() : this;
            // 从根节点开始遍历，没有终止条件，只能从内部跳出
            for (TreeNode<K,V> p = root; ;){
                // 声明方向， 当前节点的hash值
                int dir, ph;
                // 当前节点键的对象
                K pk;
                // 当前节点hash大于key的hash值
                if ((ph = p.hash) > h){
                    // 要添加的元素在当前节点的左边
                    dir = -1;
                }
                // 如果当前节点hash值小于，
                else if (ph < h){
                    // 要添加的元素在当前节点的右边
                    dir = 1;
                }
                // 如果当前节点对象和指定key对象相同
                else if ((pk = p.key) == k || (k != null && k.equals(pk))){
                    // 返回当前节点，在完成方法对V进行写入
                    return p;
                }
                // 走到这里说明，当前节点hash值和指定key的hash值是相同的，但是equals不等
                else if ((kc == null &&
                        (kc = comparableClassFor(k)) == null ||
                        (dir = compareComparables(kc, k, pk)) == 0)){
                    // 指定key没有实现comparable接口或是当前节点对象比较之后相等

                    /**
                     * searched 表示是否已经对比过当前节点的左右子节点了
                     * 如果没有遍历过，那么就递归遍历对比，看是否能够得到那个键对象equa相等的节点
                     * 如果得到了键equles相等的节点就返回
                     * 如果还是没有键equals相等的节点，那么说明应该创建一个新的节点
                     */
                    // 如果没有对比过当前节点的左右子节点
                    if (!searched){
                        // 要返回的节点和子节点
                        TreeNode<K,V> q, ch;
                        // 标记已经遍历过一次了
                        searched = true;

                        if (((ch = p.left) != null &&
                                (q = ch.find(h, k, kc)) != null) ||
                                ((ch = p.right) != null &&
                                        (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k,pk);
                }

                TreeNode<K,V> xp = p;
                /**
                 * 如果dir<=0.那么当前看当前节点的左节点是否为空。如果为空，就把要添加的元素当做当前节点的左节点，如果不为空，则进行下一步的比较
                 * dir>0,那么看当前节点的右节点是否为空。如果为空，就要当前节点左右右子树。
                 *
                 */
                if ((p = (dir <= 0) ? p.left : p.right) == null){
                    // 如果要添加的方向上子节点为空，此时节点p已经指向了这个空的子节点
                    Node<K,V> xpn = xp.next;
                    // 新建一个新的节点
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    // 左孩子指向这个新的节点
                    if (dir <= 0){
                        xp.left = x;
                    }
                    // 右孩子指向这个节点
                    else {
                        xp.right = x;
                    }
                    // 链表中的next节点指向这个新的节点
                    xp.next = x;
                    // 这个新的数的父节点，前节点都设置为当前节点
                    x.parent = x.prev = xp;
                    // 如果原来的next节点不为空
                    if (xpn != null){
                        // 那么原来的next节点的前节点指向新的节点
                        ((TreeNode<K,V>)xpn).prev = x;
                    }
                    return null;
                }
            }
        }

        /**
         * 从根节点p开始查找指定hash值和关键字k的节点
         * 当第一次使用比较器比较关键字时，参数kc存储了关键字key的比较器类别
         * @param h
         * @param k
         * @param kc
         * @return
         */
        final TreeNode<K,V> find(int h, Object k, Class<?> kc){
            TreeNode<K,V> p = this ;
            do {
                int ph, dir;
                K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                // 如果给定的hash值小于当前节点的哈希值，进入左节点
                if ((ph = p.hash) > h){
                    p = pl;
                }
                // 如果大于，进入右节点
                else if (ph < h){
                    p = pr;
                }
                // 如果哈希值相等，且关键字相等，则返回当前节点
                else if ((pk = p.key) == k || (k != null && k.equals(pk))){
                    return p;
                }
                // 如果左节点为空，则进入右节点
                else if (pl == null){
                    p = pr;
                }
                // 如果右节点为空，则进入左节点
                else if (pr == null){
                    p = pl;
                }
                // 如果再在右节点中找到，直接返回
                else if ((q = pr.find(h, k, kc)) != null){
                    return q;
                }
                // 否则进入左节点
                else {
                    p = pl;
                }
            }while (p != null);
            return null;

        }

        /**
         * 将链表的转化为红黑树
         * @param tab
         */
        final void treeify(Node<K,V> [] tab){
            // 定义根节点
            TreeNode<K,V> root = null;
            // 遍历链表，x指向当前节点。next指向下一个节点
            for (TreeNode<K,V> x = this, next; x != null; x = next){
                // 下一个节点
                next = (TreeNode<K,V> )x.next;
                // 设置左右子树为空
                x.left = x.right = null;
                // 如果没有根节点，将x设置为根节点，并将颜色设置为红色
                if (root == null){
                    x.parent = null;
                    x.red = false;
                    root = x;
                }
                // 如果存在了根节点了,
                else {
                    // 获取当前节点的key和value
                    K k = x.key;
                    int h = x.hash;
                    // 定义key所属的类型
                    Class<?> kc = null;
                    // 从根节点开始遍历，找到对应插入的节点。没有设置边界，只能从内部跳出
                    for (TreeNode<K,V> p = root;;){
                        // dir 为方向，ph当前key的hash值
                        int dir, ph;
                        // 当前节点的key值
                        K pk = p.key;
                        // 当前hash值>当前链表节点的hash值。标识当前链表节点会放到当前树的左侧
                        if ((ph = p.hash) > h){
                            dir = -1;
                        }
                        // 否则放到右侧
                        else{
                            dir = 1;
                        }
                        /**
                         * 如果die<0,当前节点节点一定放在当前节点的左侧，不一定是该节点的左孩子，也可能是左孩子的右孩子或是更深的节点
                         * >0,类似
                         * 如果当前节点不是叶子节点，那么最终会以当前节点的左孩子或是右孩子作为起始节点，从上一个节点开始重新遍历.寻找当前节点适的位置
                         * 如果当前节点是叶子节点，那么根据dir的值，将该节点挂载到当前节点的左字数或是右子树中。
                         */
                        TreeNode<K,V> xp = p;
                        // 在这里将p赋值。指针向下移动一位
                        if ((p = (dir <= 0) ? p.left : p.right) == null){
                            x.parent = xp;
                            if (dir < 0){
                                xp.left = x;
                            }
                            else {
                                xp.right = x;
                            }
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            /**
             * 把所有的链表都遍历完了之后，可能会构造出来的树会有多个平衡操作，根节点的目前到底是链表中的哪一个节点还是不确定的。
             * 因为我们要基于树来做查找，所以就应该把tab[N]得到的对象一定是根节点对象，而目前的链表的第一个节点对象，所以要做相应的处理
             * 把红黑树的跟节点设为其所在数组槽的第一个元素
             * TreeNode既是一个红黑树，也是一个双向链表
             * 这个方法，就是保证树的根节点一定要成为链表的首节点
             */
            moveRootToFront(tab, root);
        }

        /**
         * 红黑树节点插入后，重要重新平衡
         * @param root 当前的根节点
         * @param x 插入的节点
         * @param <K>
         * @param <V>
         * @return 返回平衡后的根节点
         */
        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root, TreeNode<K,V> x){
            x.red = true;
            /**
             * 这一步定义了变量，又开始了循环，没有控制条件，只能从内部跳出
             * xp当前节点的父节点，xpp爷爷节点，xppl,左叔叔节点，xppr右叔叔节点
             *
             */
            for (TreeNode<K,V> xp, xpp, xppl, xppr; ;){
                // 插入的节点是根节点，第一次插入。直接将此节点涂黑，并返回这个节点
                if ((xp = x.parent) == null){ // L1
                    x.red = false;
                    return x;
                }
                //被插入的节点父节点是黑色。什么都不要做，直接返回root，
                else if (!xp.red || (xpp = xp.parent) == null){ // L2
                    return root;
                }
                //如果父节点是爷爷的左孩子
                if (xp == (xppl = xpp.left)){  // L3
                    // 如果右叔叔不为空，且为红色。L3_1
                    // 当前情况是父亲和叔叔都为红色。需要变色操作，将父亲和叔叔都变成黑色，将爷爷变成红色，并将指针放到爷爷那里，进行下一轮的循环
                    if ((xppr = xpp.right) != null && xppr.red){
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                    }
                    /**
                     * 如果右叔叔为空，或是为黑色L3_2
                     */
                    else {
                        // 如果当前节点是父节点的右孩子 L3_2_1
                        // 当前情况是父节点为红色，叔叔为空或为黑色。需要进行将父节点进行左旋
                        if (x == xp.right){
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent ) == null ? null : xp.parent;
                        }
                        // 如果父节点不为空L3_2_2
                        if (xp != null){
                            xp.red = false;
                            if (xpp != null){
                                xpp.red = true;
                                root = rotateRight(root, x);
                            }
                        }
                    }
                }
                // 如果父节点是爷爷的右孩子
                else { //L4
                    // 如果左叔叔是红色 L4_1,变色操作
                    if (xppl != null && xppl.red){
                        xppl.red = false;
                        xp.red = true;
                        xpp.red = true;
                        x = xpp;
                    }
                    /**
                     * 如果左叔叔为空或是为给色 L4_2
                     */
                    else {
                        // 如果当前节点是左孩子
                        if (x == xp.left){
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null){
                            xp.red = false;
                            if (xpp != null){
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root, TreeNode<K,V> p){
            TreeNode<K,V> r, pp, rl;
            /**
             * 要左旋的节点不为空，并且要旋转的右孩子不为空
             */
            if (p != null && (r = p.right) != null){
                // 要旋转的右孩子的左节点赋值给 要旋转的节点的右盖子，节点为rl
                if ((rl = p.right = r.left) != null){
                    // 认爹。设置rl和要左旋的节点父子关系。之前只是爹认了孩子，孩子还不一定答应，这一步也认了爹
                    rl.parent = p;
                }
                /**
                 * 将要左旋的节点右孩子的父节点 指向 要左旋节点的父节点。 相当于右孩子提升了一层
                 * 如果父节点为空， 说明 已经是顶层点了，应该作为root，并标记为黑色。
                 */
                if((pp = r.parent = p.parent) == null){
                    (root = r).red = false;
                }
                // 如果父节点不为空， 要左旋的的节点是左孩子
                else if (pp.left == p){
                    // 设置r和父节点的父子关系
                    pp.left = r;
                }
                // 要左旋的节点是个右孩子
                else {
                    pp.right = r;
                }
                // 要旋转的节点作为 他右孩子的左节点
                r.left = p;
                // 要旋转的节点的右孩子 作为 他的父节点
                p.parent = r;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        /**
         * 把红黑树的根节点设置为 其所在的数组槽中 的第一个元素
         * 个方法里做的事情，就是保证树的根节点一定也要成为链表的首节点
         * @param tab
         * @param root
         * @param <K>
         * @param <V>
         */
        static <K,V> void moveRootToFront(Node<K,V> [] tab, TreeNode<K,V> root){
            int n;
            // 根节点不为空，并且hashmap的原数组不为空
            if (root != null && tab != null && ( n = tab.length) > 0){
                // 根据根节点的hash值  和 hashmap的元数组的长度相与， 得到根节点在数组中的位置
                int index = ( n- 1) & root.hash;
                // 获取该位置上的第一个节点对象
                TreeNode<K,V> first = ((TreeNode<K,V>) tab[index]);
                // 如果该节点对象  与  根节点对象不相等
                if (root != first){
                    // 定义根节点的后一个节点
                    Node<K,V> rn;
                    // 将元数组index位置的元素替换成根节点对象
                    tab[index] = root;
                    // 获取根节点对象的前一个节点
                    TreeNode<K,V> rp = root.prev;
                    if ((rn = root.next) != null){
                        ((TreeNode<K,V>)rn).prev = rp;
                    }
                    if (rp != null){
                        rp.next = rn;
                    }
                    if (first != null){
                        first.prev = root;
                    }
                    // 原来的第一个节点现在作为root的下一个节点，变成了第二个节点
                    root.next = first;
                    // 首节点没有根节点
                    root.prev = null;
                }
            }

        }

        final TreeNode<K,V> getTreeNode(int h, Object k){
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        final void removeTreeNode(MyHashMap<K,V> map, Node<K,V> [] tab,boolean movable){
            int n;
            //先判空
            if (tab == null || (n = tab.length) == 0){
                return;
            }
            int index = (n - 1) & hash;
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
            if (pred == null){
                tab[index] = first= succ;
            }
            else {
                pred.next = pred;
            }
            if (succ != null){
                succ.prev = pred;
            }
            if (first == null){
                return;
            }
           /* if (root == null || root.right == null ||
                    (rl = root.left) == null || rl.left == null) {
                tab[index] = first.untreeify(map);  // too small
                return;
            }*/
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<K,V> s = pr, sl;
                while ((sl = s.left) != null) // find successor
                    s = sl;
                boolean c = s.red; s.red = p.red; p.red = c; // swap colors
                TreeNode<K,V> sr = s.right;
                TreeNode<K,V> pp = p.parent;
                if (s == pr) { // p was s's direct parent
                    p.parent = s;
                    s.right = p;
                }
                else {
                    TreeNode<K,V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                if ((s.left = pl) != null)
                    pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            }
            else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<K,V> pp = replacement.parent = p.parent;
                if (pp == null)
                    root = replacement;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) {  // detach
                TreeNode<K,V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }

        /**
         * 平衡删除节点
         * @param root
         * @param x
         * @param <K>
         * @param <V>
         * @return
         */
        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root, TreeNode<K,V> x){
            for (TreeNode<K,V> xp, xpl, xpr;;)  {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                                (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                        null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                                (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                        null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

    }



    /**
     * 新建一个节点，并将这个节点连接到最后面
     * @param hash
     * @param key
     * @param value
     * @param next
     * @return
     */
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next){
        return new TreeNode<K, V>(hash, key, value, next);
    }


    /**
     *用这个方法来比较两个对象，返回值要么大于0，要么小于0.不会为0
     * 确定插入的节点要么是树的左节点，或是右节点。
     * 先比较两个对象的类名，类名是字符串，按字符串的比较规则
     * 如果两个对象是同一个类型，调用本地方法为对象生成hashcode值。再进行比较。hashcode相等的话就返回-1；
     * @param a
     * @param b
     * @return
     */
    static int tieBreakOrder(Object a, Object b) {
        int d;
        if (a == null || b == null ||
                (d = a.getClass().getName().
                        compareTo(b.getClass().getName())) == 0)
            d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                    -1 : 1);
        return d;
    }



    static int compareComparables(Class<?> kc, Object k,Object x){
        return (x == null || x.getClass() != kc ? 0 : ((Comparable)k).compareTo(x));
    }

    static Class<?> comparableClassFor(Object x){
        if (x instanceof Comparable){
            Class<?> c;
            Type[] ts, as;
            Type t;
            ParameterizedType p;
            if (( c = x.getClass()) == String.class){
                return c;
            }
            if ((ts = c.getGenericInterfaces()) != null){
                for (int i = 0; i < ts.length ;++i){
                    if (((t = ts[i]) instanceof ParameterizedType) && ((p = (ParameterizedType)t).getRawType() == Comparable.class) &&
                            (as = p.getActualTypeArguments()) != null && as.length == 1 && as[0] == c){
                        return c;
                    }
                }
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
        for (int i = 0 ;i<64;i++){
            myHashMap.put(i * 8,8);
        }
        System.out.println(myHashMap.get(392));
        System.out.println(myHashMap.size());
        myHashMap.remove(200);
        System.out.println(myHashMap.containsKey(200));
    }

}
