package org.gangel.cloud.dataservice.repository;

import org.gangel.cloud.dataservice.entity.Product;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends PagingAndSortingRepository<Product, Long> {

}
