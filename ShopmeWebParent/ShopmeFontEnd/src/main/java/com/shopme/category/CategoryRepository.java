package com.shopme.category;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.shopme.common.entity.Category;
@Repository
public interface CategoryRepository extends CrudRepository<Category, Integer> {
	
	@Query("Select c from Category c where c.enabled = true Order by c.name asc")
	public List<Category> findAllEnabled();
	
	@Query("Select c from Category c where c.enabled = true and c.alias = ?1")
	public Category findByAliasEnabled(String alias);
}
