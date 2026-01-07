package tn.agricultureai.service;

@FunctionalInterface
public interface DataTransformer<T, R> {
    R transform(T input);

    default DataTransformer<T, R> andThen(DataTransformer<R, R> after) {
        return (T t) -> after.transform(transform(t));
    }
}