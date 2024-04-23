package com.shopme.admin.setting.country;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.shopme.common.entity.Country;

@Repository
public interface CountryRepository extends CrudRepository<Country, Integer>{
	
	public List<Country> findAllByOrderByNameAsc();
}
