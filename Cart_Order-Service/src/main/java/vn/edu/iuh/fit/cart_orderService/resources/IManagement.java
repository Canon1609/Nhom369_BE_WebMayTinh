package vn.edu.iuh.fit.cart_orderService.resources;

import org.springframework.http.ResponseEntity;
import vn.edu.iuh.fit.cart_orderService.models.Response;

import java.util.List;

public interface IManagement <T,P>{
    ResponseEntity<Response> insert(T t);
    ResponseEntity<Response> insertAll(List<T> t);
    ResponseEntity<Response> update(P p,T t);
    ResponseEntity<Response> delete(P p);
    ResponseEntity<Response> findById(P p);
    ResponseEntity<Response> findAll();

}
