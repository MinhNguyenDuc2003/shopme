package com.shopme.admin.brand;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.shopme.common.entity.Brand;
@Repository
public interface BrandRepository extends PagingAndSortingRepository<Brand, Integer>, CrudRepository<Brand, Integer>{
	@Query("select b from Brand b where concat(b.id,' ', b.name) like %?1%")
	public Page<Brand> findAll(String keyword, Pageable pageable);
	
	public Long countById(Integer id);
	
	@Query("select b from Brand b where b.name = %?1%")
	public Brand findByName(String name);
	
	@Query("Select NEW Brand(b.id, b.name) from Brand b order by b.name ASC")
	public List<Brand> findAll();
	
}
