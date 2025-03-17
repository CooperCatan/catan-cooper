package catan.util;

import java.util.List;

public interface DataAccessObject<T extends DataTransferObject> {
    void create(T dto);
    T findById(long id);
    List<T> findAll();
    void update(T dto);
    void delete(long id);
}