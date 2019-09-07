package cn.gdut.MyArrayList;


import java.util.Arrays;

public class MyArrayList<E> {
    // 默认初始长度为10
    private static final int DEFAULT_CAPACITY = 10;
    /**
     * MyArrayList 的底层数据结构是数组，数组的数据类型为Object类型，即可以存放所有的数据类型
     *
     */
    Object[] elementDate;

    /**
     * 空对象数组
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    /**
     * 数据实际长度的大小
     */
    private int size;

    /**
     * 最大的容量
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    @Override
    public String toString() {
        return "MyArrayList{" +
                "elementDate=" + Arrays.toString(elementDate) +
                ", size=" + size +
                '}';
    }

    // 无参构造方法
    public MyArrayList(){
        this.elementDate = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    /**
     * 有参构造函数
     * @param initialCapacity 定义的的初始容量大小
     */
    public MyArrayList(int initialCapacity){
        /**
         * 先判断参数是否合法,不合法则报异常
          */
        if (initialCapacity > 0){
            this.elementDate = new Object[initialCapacity];
        }
        else if (initialCapacity == 0){
            this.elementDate = EMPTY_ELEMENTDATA;
        }
        else{
            throw new IllegalArgumentException("Illeagal Capacity:" +
                    initialCapacity);
        }
    }

    /**
     * 添加一个元素到列表的末尾中
     *
     * @param e 需要添加的元素
     * @return boolean
     */
    public boolean add(E e){
        // 确保数组是否越界，size是数组中的数据个数，因为要添加一个元素。所有size+1，先判断size+1，这个数组是否放的下
        ensureCapacityInternal(size + 1);
        // 如果放得下，则将数据放进去，并将size++
        elementDate[size++] = e;
        return true;
    }


    /**
     * 判断是否合法
     * @param minCapacity 容量
     */
    private void ensureCapacityInternal(int minCapacity){
        if (elementDate == DEFAULTCAPACITY_EMPTY_ELEMENTDATA){
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        // 如果长度不能满足需求，则需要扩充了。
        if (minCapacity - elementDate.length > 0){
            grow(minCapacity);
        }

    }

    /**
     * 数组扩容
     * @param minCapacity 数组
     */
    private void grow(int minCapacity){
        //  先保存旧元素
        int oldCapacity = elementDate.length;
        // 新元素以1.5倍速度增长
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        // 如果这个判断成立，这相当于是初始化过程
        if (newCapacity - minCapacity < 0){
            newCapacity = minCapacity;
        }
        // 将旧的数组拷贝到新的数组中
        elementDate = Arrays.copyOf(elementDate, newCapacity);
    }

    /**
     *
     * @return 返回list的数组大小
     */
    public int size(){
        return size;
    }

    /**
     *
     * @return 判断数组是否为空
     */
    public boolean isEmpty(){
        return size == 0;
    }

    /**
     * 在特定位置插入元素
     * 先判断参数是否合法
     * 然后确保elementData数组长度的合法性
     * 然后将后面的元素的往后移动一位，留出一个空间供e插入
     * @param index index
     * @param element 元素
     */
    public void add(int index, E element){
        rangeCheckForAdd(index);
        ensureCapacityInternal(size + 1);
        System.arraycopy(elementDate, index, elementDate, index + 1, size - index);
        elementDate[index] = element;
        size++;
    }

    /**
     * 插入的索引确认
     * @param index 索引
     */
    private void rangeCheckForAdd(int index){
        if (index > size || index < 0){
            throw new IndexOutOfBoundsException(outOfBoundMsg(index));
        }
    }

    /**
     * 删除指定位置的元素,
     * 返回删除的元素值。先通过索引得到要删除的值，再将要删除的部分拷贝。
     * @param index 索引
     * @return 删除的元素
     */
    public E remove(int index){
        rangeCheck(index);
        E oldValue = elementDate(index);
        int numMoved = size - index - 1;
        if (numMoved > 0){
            System.arraycopy(elementDate, index + 1, elementDate, index, numMoved);
        }
        elementDate[--size] = null;
        return oldValue;
    }

    /**
     * 通过索引直接获取值。需要向下转型Object-> E转型
     * @param index 索引
     * @return 当前索引的值
     */
    E elementDate(int index){
        return (E) elementDate[index];
    }

    private void rangeCheck(int index){
        if (index > size){
            throw new IndexOutOfBoundsException(outOfBoundMsg(index));
        }
    }

    /**
     * 将全部元素赋值为null,等待GC进行回收
     * 并将size设置为0
     */
    public void clear(){
        for (int i = 0; i < size; i++){
            elementDate[i] = null;
        }
        size = 0;
    }

    /**
     * 抛出异常的字符串
     * @param index 索引
     * @return 异常的字符串
     */
    private String outOfBoundMsg(int index){
        return "Index" + index + ", size: " + size;
    }

    /**
     * 更新元素，将指定位置的元素更新为新的值,并且返回旧的值。
     * @param index 索引
     * @param element 更新的值
     * @return 被替换的值
     */
    public E set(int index, E element){
        rangeCheck(index);
        E oldValue = elementDate(index);
        elementDate[index] = element;
        return oldValue;
    }

    /**
     * 查找特定元素出现的第一个位置
     * 算法实现是遍历数组。
     * 其中o可以为null，当为null是，判断方式是==。否则是elementData[i].equal。
     * 否则是elementData[i].equal。
     * @param o 元素
     * @return 索引
     */
    public int indexOf(Object o){
        if (o == null){
            for (int i = 0;i < size;i++){
                if (elementDate[i] == null){
                    return i;
                }
            }
        }
        else {
            for (int i = 0;i<size;i++){
                if (elementDate[i].equals(o)){
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 返回元素为o的最后的索引
     * @param o 元素
     * @return 索引
     */
    public int lastIndexOf(Object o){
        if (o == null){
            for (int i = size - 1;i >= 0; i++){
                if (elementDate[i] == null){
                    return i;
                }
            }
        }
        else {
            for (int i = size - 1;i >= 0;i++){
                if (elementDate[i].equals(o)){
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 先判断索引的合法性，再通过elementDate方法获取元素
     * @param index 索引
     * @return 查找的值
     */
    public E get(int index){
        rangeCheck(index);
        return elementDate(index);
    }

    /**
     * 判断元素是否存在ArrayList中
     * @param o 元素
     * @return 结果
     */
    public boolean contain(Object o){
        return indexOf(o) >= 0;
    }

    public static void main(String[] args) {
        MyArrayList myArrayList = new MyArrayList(11);
        for (int i = 0;i<10;i++){
            myArrayList.add("a");
        }
        myArrayList.add(10,"d");
        myArrayList.add(null);
        System.out.println(myArrayList.set(10,'1'));
        System.out.println(myArrayList.lastIndexOf(null));
        System.out.println(myArrayList.get(10));
//        myArrayList.remove(10);
        System.out.println(myArrayList);
    }
}
