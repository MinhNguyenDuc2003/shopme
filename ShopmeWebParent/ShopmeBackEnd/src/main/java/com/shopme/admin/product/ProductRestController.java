package com.shopme.admin.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductRestController {
	@Autowired
	private ProductService service;
	
	@PostMapping("/products/check_unique")
	public String checkDuplicated(@Param("id") Integer id, @Param("name") String name) {
		return service.isUnique(id, name)==true ? "OK" : "Duplicated";
	}
}
