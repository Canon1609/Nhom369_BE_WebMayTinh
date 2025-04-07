package vn.edu.iuh.fit.cart_orderService.services;

import java.util.List;
import java.util.Optional;

public interface IService<T,P> {
    T add(T t);
    List<T> addMany(List<T> list);
    T update(T t);
    void delete(P p) ;
    Optional<T> getById(P p) ;
    List<T> getAll();
}
