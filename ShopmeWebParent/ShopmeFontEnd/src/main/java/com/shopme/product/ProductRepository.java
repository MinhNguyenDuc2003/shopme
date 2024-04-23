package com.shopme.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.shopme.common.entity.Product;

@Repository
public interface ProductRepository extends CrudRepository<Product, Integer>{
	@Query("Select p from Product p where p.enabled = true "
			+ "AND (p.category.id = ?1 or p.category.allParentIDs Like %?2%) "
			+ "Order By p.name ASC")
	public Page<Product> listByCategory(Integer idCategory, String categoryIdMatch, Pageable pageable);
	
	public Product findByAlias(String alias);
	
	@Query(value="SELECT * FROM products WHERE enabled = true AND"
			+ " MATCH(name, short_description, full_description) AGAINST (?1)",
			nativeQuery=true)
	public Page<Product> search(String keyword, Pageable pageable);
}
