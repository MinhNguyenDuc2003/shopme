package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Category;
import com.shopme.common.exception.CategoryNotFoundException;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CategoryService {
	public static final int CATEGORY_PER_PAGE = 4;
	@Autowired
	private CategoryRepository repo;

	public List<Category> listAll() {
		return (List<Category>) repo.findAll();
	}

	public List<Category> listByPage(CategoryPageInfo pageInfo, int pageNum, String sortDir, String keyword) {
		Sort sort = Sort.by("name");
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		Pageable pageable = PageRequest.of(pageNum - 1, CATEGORY_PER_PAGE, sort);
		Page<Category> pageCategories = null;
		if (keyword != null && !keyword.isEmpty()) {
			pageCategories = repo.findAll(keyword, pageable);
		}
		else{
			pageCategories = repo.findRootCategories(pageable);
		}
		
		List<Category> rootCategories = pageCategories.getContent();
		
		pageInfo.setTotalElements(pageCategories.getTotalElements());
		pageInfo.setTotalPages(pageCategories.getTotalPages());
		
		if (keyword != null && !keyword.isEmpty()) {
			List<Category> searchResult = pageCategories.getContent();
			for (Category category : searchResult) {
				category.setHasChildren(category.getChildren().size() > 0);
			}
			
			return searchResult;
			
		} else {
			return listHierarchicalCategories(rootCategories, sortDir);
		}
	}

	public Category findCategoryById(int id) throws CategoryNotFoundException{
		return repo.findById(id).get();
	}

	public void updateCategoryEnabledStatus(Integer id, boolean enabled) {
		repo.updateEnabledStatus(id, enabled);
	}

	public void delete(Integer id) throws CategoryNotFoundException {
		if (repo.countById(id) == 0) {
			throw new CategoryNotFoundException("Could not find Category with ID: " + id);
		} else {
			repo.deleteById(id);
		}

	}

	public List<Category> listAllCategory() {
		List<Category> listInForm = new ArrayList<>();
		Iterable<Category> listInDb = repo.findAll();

		for (Category cat : listInDb) {
			if (cat.getParent() == null) {
				Category parent = Category.copyIdAndName(cat.getId(), cat.getName());
				listInForm.add(parent);
				Set<Category> set = cat.getChildren();
				for (Category cate : set) {
					addChild(cate, 0, listInForm);
				}
			}
		}
		return listInForm;
	}

	public void addChild(Category cat, int level, List<Category> listInForm) {
		int subLevel = level + 1;
		String prefix="";
		for (int i = 0; i < subLevel; i++) {
			prefix=prefix+"--";
		}
		Category sub = Category.copyIdAndName(cat.getId(), cat.getName());
		sub.setName(prefix+sub.getName());
		listInForm.add(sub);
		Set<Category> set = cat.getChildren();
		for (Category cate : set) {
			addChild(cate, subLevel, listInForm);
		}
	}

	public Category save(Category category) {
		Category parent = category.getParent();
		if (parent != null) {
			String allParentIds = parent.getAllParentIDs() == null ? "-" : parent.getAllParentIDs();
			allParentIds += String.valueOf(parent.getId()) + "-";
			category.setAllParentIDs(allParentIds);
		}
		
		return repo.save(category);
	}
	
	public List<Category> listHierarchicalCategories(List<Category> rootCategories, String sortDir){
		List<Category> hierarchicalCategories = new ArrayList<>();
		for (Category rootCategory : rootCategories) {
			hierarchicalCategories.add(Category.copyFull(rootCategory));
			
			Set<Category> children = sortSubCategories(rootCategory.getChildren(), sortDir);
			
			for (Category subCategory : children) {
				String name = "--" + subCategory.getName();
				hierarchicalCategories.add(Category.copyFull(subCategory, name));
				
				listSubHierarchicalCategories(hierarchicalCategories, subCategory, 1, sortDir);
			}
		}
		return hierarchicalCategories;
	}
	
	private void listSubHierarchicalCategories(List<Category> hierarchicalCategories,
			Category parent, int subLevel, String sortDir) {
		Set<Category> children = sortSubCategories(parent.getChildren(), sortDir);
		subLevel = subLevel+1;
		String name = "";  
		for(Category cate: children) {
			for(int i=0; i<subLevel; i++) {
				name += "--";
			}
			name = name+cate.getName();
			hierarchicalCategories.add(Category.copyFull(cate, name));
			listSubHierarchicalCategories(hierarchicalCategories, cate, subLevel, sortDir);
		}
		
	}
	
	private SortedSet<Category> sortSubCategories(Set<Category> children) {
		return sortSubCategories(children, "asc");
	}
	
	private SortedSet<Category> sortSubCategories(Set<Category> children, String sortDir) {
		SortedSet<Category> sortedChildren = new TreeSet<>(new Comparator<Category>() {
			@Override
			public int compare(Category cat1, Category cat2) {
				if (sortDir.equals("asc")) {
					return cat1.getName().compareTo(cat2.getName());
				} else {
					return cat2.getName().compareTo(cat1.getName());
				}
			}
		});
		
		sortedChildren.addAll(children);
		
		return sortedChildren;
	}
	
	public String checkUnique(Integer id, String name, String alias) {
		boolean isCreatingNew = (id == null || id == 0);
		
		Category categoryByName = repo.findByName(name);
		
		if (isCreatingNew) {
			if (categoryByName != null) {
				return "DuplicateName";
			} else {
				Category categoryByAlias = repo.findByAlias(alias);
				if (categoryByAlias != null) {
					return "DuplicateAlias";	
				}
			}
		} else {
			if (categoryByName != null && categoryByName.getId() != id) {
				return "DuplicateName";
			}
			
			Category categoryByAlias = repo.findByAlias(alias);
			if (categoryByAlias != null && categoryByAlias.getId() != id) {
				return "DuplicateAlias";
			}
			
		}
		
		return "OK";
	}

}
