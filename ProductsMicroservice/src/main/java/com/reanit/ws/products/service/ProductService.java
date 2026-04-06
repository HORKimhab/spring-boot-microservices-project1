package com.reanit.ws.products.service;

import com.reanit.ws.products.rest.CreateProductRestModel;

public interface ProductService {
    
    String createProduct(CreateProductRestModel productRestModel) throws Exception;
}
