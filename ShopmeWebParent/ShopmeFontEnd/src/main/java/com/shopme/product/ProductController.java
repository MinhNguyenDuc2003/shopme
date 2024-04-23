package com.shopme.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.shopme.category.CategoryService;
import com.shopme.common.entity.Category;
import com.shopme.common.entity.Product;
import com.shopme.common.exception.CategoryNotFoundException;
import com.shopme.common.exception.ProductNotFoundException;

@Controller
public class ProductController {
	@Autowired
	private CategoryService catService;
	@Autowired
	private ProductService proService;

	@GetMapping("/c/{category_alias}")
	public String viewCategoryFirstPage(@PathVariable("category_alias") String alias, Model model) {
		return viewCategory(alias, model, 1);
	}

	@GetMapping("/c/{category_alias}/page/{pageNum}")
	public String viewCategory(@PathVariable("category_alias") String alias, Model model,
			@PathVariable("pageNum") int pageNum) {
		try {
			Category cat = catService.getCatefory(alias);
			List<Category> listCategoryParents = catService.getCategoryParents(cat);
			Page<Product> pageProduct = proService.listByCategory(pageNum, cat.getId());

			long startCount = (pageNum - 1) * proService.PRODUCT_PER_PAGE + 1;
			long endCount = startCount + proService.PRODUCT_PER_PAGE - 1;
			if (endCount > pageProduct.getTotalElements()) {
				endCount = pageProduct.getTotalElements();
			}
			model.addAttribute("currentPage", pageNum);
			model.addAttribute("startCount", startCount);
			model.addAttribute("endCount", endCount);
			model.addAttribute("totalPages", pageProduct.getTotalPages());
			model.addAttribute("totalItems", pageProduct.getTotalElements());
			model.addAttribute("listCategoryParents", listCategoryParents);
			model.addAttribute("pageTitle", cat.getName());
			model.addAttribute("listProducts", pageProduct.getContent());
			model.addAttribute("category", cat);

			return "product/product_by_category";
		} catch (CategoryNotFoundException e) {
			return "error/404";
		}
	}

	@GetMapping("/p/{product_alias}")
	public String viewProductDetail(@PathVariable("product_alias") String alias,
			Model model) {
	try {
		Product product = proService.getProduct(alias);
		List<Category> listCategoryParents = catService.getCategoryParents(product.getCategory());
		
		model.addAttribute("listCategoryParents", listCategoryParents);
		model.addAttribute("product", product);
		model.addAttribute("pageTitle", product.getShortName());
		return "product/product_detail";
	} catch(ProductNotFoundException e) {
		return "error/404";
	}
	}
	
	@GetMapping("/search")
	public String searchFirstPage(@Param("keyword") String keyword, Model model) {
		return searchByPage(keyword, 1, model);
	}
	
	@GetMapping("/search/page/{pageNum}")
	public String searchByPage(@Param("keyword") String keyword, 
			@PathVariable("pageNum") int pageNum,
			Model model){
		Page<Product> pageProduct = proService.search(keyword, pageNum);
		List<Product> listProducts = pageProduct.getContent();
		
		long startCount = (pageNum - 1) * proService.SEARCH_RESULTS_PER_PAGE + 1;
		long endCount = startCount + proService.SEARCH_RESULTS_PER_PAGE - 1;
		if (endCount > pageProduct.getTotalElements()) {
			endCount = pageProduct.getTotalElements();
		}
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalPages", pageProduct.getTotalPages());
		model.addAttribute("totalItems", pageProduct.getTotalElements());
		model.addAttribute("pageTitle", keyword + " - Search Result");
		model.addAttribute("keyword", keyword);
		model.addAttribute("listResult", listProducts);
		return "product/search_result";
		
		
	}
}
