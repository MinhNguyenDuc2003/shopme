package com.shopme.admin.paging;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SearchRepository<T, ID> extends CrudRepository<T, ID> {
	public Page<T> findAll(String keyword, Pageable pageable);
	
	public Page<T> findAll(Pageable pageable);
}