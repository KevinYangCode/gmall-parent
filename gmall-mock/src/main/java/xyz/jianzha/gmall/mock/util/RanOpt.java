package xyz.jianzha.gmall.mock.util;

/**
 * @author Y_Kevin
 * @date 2020-06-13 12:42
 */
public class RanOpt<T> {
    T value;
    int weight;

    public RanOpt(T value, int weight) {
        this.value = value;
        this.weight = weight;
    }

    public T getValue() {
        return value;
    }

    public int getWeight() {
        return weight;
    }
}
