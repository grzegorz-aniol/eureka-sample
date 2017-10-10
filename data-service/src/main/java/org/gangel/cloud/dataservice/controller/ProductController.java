package org.gangel.cloud.dataservice.controller;

import org.gangel.cloud.dataservice.dto.ProductTO;
import org.gangel.cloud.dataservice.entity.Product;
import org.gangel.cloud.dataservice.service.AbstractService;
import org.gangel.cloud.dataservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ProductController.ENDPOINT)
public class ProductController extends AbstractController<Product, ProductTO, Long>{

    public final static String ENDPOINT = "/api2/products"; 
    
    @Autowired
    private ProductService service;
    
    @Override
    protected AbstractService<Product, ProductTO, Long> getService() {
        return this.service;
    }

    @Override
    protected String getEndpointRoot() {
        return ENDPOINT;
    }
    
}