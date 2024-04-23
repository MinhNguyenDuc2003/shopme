package com.shopme.admin.brand;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shopme.admin.category.CategoryService;
import com.shopme.admin.user.UserNotFoundException;
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;
@Service
public class BrandService {
	public static final int BRAND_PER_PAGE = 4;
	@Autowired
	private BrandRepository repo;
	
	@Autowired
	private CategoryService catService;
	
	public Page<Brand> listByPage(int pagenum, String sortField, String sortDir, String keyword){
		Page<Brand> page = null;
		Sort sort = Sort.by(sortField);
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		Pageable pageable = PageRequest.of(pagenum-1, BRAND_PER_PAGE, sort);
		if(keyword != null && !keyword.isEmpty()) {
			page = repo.findAll(keyword, pageable);
		}
		else {
			page = repo.findAll(pageable);
		}
		return page;
	}
	
	public void delete(Integer id) throws BrandNotFoundException {
		Long count = repo.countById(id);
		if(count==0 || count == null) {
			throw new BrandNotFoundException("Could not find brand with ID: " + id);
		}
		repo.deleteById(id);
	}
	
	public List<Category> listAllCategory(){
		return catService.listAllCategory();
	}
	
	public Brand saveBrand(Brand brand) {
		return repo.save(brand);
	}

	public Brand findById(Integer id) throws BrandNotFoundException{
		return repo.findById(id).get();
	}
	
	public boolean isUnique(Integer id, String name) {
		boolean result = true;
		Brand brandForm = repo.findByName(name);
		boolean isCreateNew = (id==null || id == 0);
		if(isCreateNew) {
			if(brandForm!=null) {
				result = false;
			}
		}
		else {
			if(brandForm != null &&!brandForm.getId().equals(id)) {
				result = false;
			}
		}
		return result;
	}
	
	public List<Brand> listAll(){
		return repo.findAll();
	}

	public Brand get(Integer brandId) throws BrandNotFoundException{
		return repo.findById(brandId).get();
	}
}
