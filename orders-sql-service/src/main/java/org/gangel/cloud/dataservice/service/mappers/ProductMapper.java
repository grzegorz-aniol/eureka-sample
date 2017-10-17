package org.gangel.cloud.dataservice.service.mappers;

import org.gangel.cloud.dataservice.dto.ProductTO;
import org.gangel.orders.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Component
@Mapper(unmappedTargetPolicy=ReportingPolicy.WARN, nullValueCheckStrategy=NullValueCheckStrategy.ALWAYS)
public abstract class ProductMapper extends AbstractMapper<Product, ProductTO, Long>{

    public abstract ProductTO toDTO(Product source);

    public abstract Product toEntity(ProductTO source);
        
} 