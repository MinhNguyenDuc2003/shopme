package com.shopme.admin.category;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.AmazonS3Util;
import com.shopme.common.entity.Category;
import com.shopme.common.exception.CategoryNotFoundException;

@Controller
public class CategoryController {
	@Autowired
	private CategoryService service;

	@GetMapping("/categories")
	public String listAll(Model model) {
		return listByPage(1, model, "name", "asc", null);
	}

	@GetMapping("/categories/page/{pageNum}")
	public String listByPage(@PathVariable("pageNum") int pageNum, Model model, @Param("sortField") String sortField,
			@Param("sortDir") String sortDir, @Param("keyword") String keyword) {
		long startCount = (pageNum - 1) * service.CATEGORY_PER_PAGE + 1;
		long endCount = startCount + service.CATEGORY_PER_PAGE - 1;

		CategoryPageInfo pageInfo = new CategoryPageInfo();
		List<Category> list = service.listByPage(pageInfo, pageNum, sortDir, keyword);

		if (endCount > pageInfo.getTotalElements()) {
			endCount = pageInfo.getTotalElements();
		}
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";

		model.addAttribute("totalPages", pageInfo.getTotalPages());
		model.addAttribute("totalItems", pageInfo.getTotalElements());
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("sortField", "name");
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("keyword", keyword);
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);

		model.addAttribute("listCategories", list);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("moduleURL", "/categories");
		return "categories/categories";
	}


	@GetMapping("/categories/{id}/enabled/{status}")
	public String updateCategoryEnabledStatus(@PathVariable("id") Integer id,
			@PathVariable("status") boolean enabled, RedirectAttributes redirectAttributes) {
		service.updateCategoryEnabledStatus(id, enabled);
		String status = enabled ? "enabled" : "disabled";
		String message = "The category ID " + id + " has been " + status;
		redirectAttributes.addFlashAttribute("message", message);
		
		return "redirect:/categories";
	}

	@GetMapping("/categories/new")
	public String newCategory(Model model) {
		Category cate = new Category();
		model.addAttribute("category", cate);
		List<Category> listParent = service.listAllCategory();
		model.addAttribute("pageTitle", "Shopme Category-form");
		model.addAttribute("listParent", listParent);
		return "categories/category_form";
	}

	@PostMapping("/categories/save")
	public String saveCategory(Category category, @RequestParam("fileImage") MultipartFile multipartFile,
			RedirectAttributes redirectAttribute) throws IOException {

		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			category.setImage(fileName);
			Category savedCat = service.save(category);
			String uploadDir = "category-images/" + savedCat.getId();
			AmazonS3Util.removeFolder(uploadDir);
			AmazonS3Util.uploadFile(uploadDir, fileName, multipartFile.getInputStream());
		} else {
			service.save(category);
		}
		redirectAttribute.addFlashAttribute("message", "The category have been saved successfully.");
		return "redirect:/categories";
	}

	@GetMapping("/categories/edit/{id}")
	public String editCategory(Model model, @PathVariable(name = "id") Integer id,
			RedirectAttributes redirectAttribute) {
		try {
			Category cate = service.findCategoryById(id);
			List<Category> list = service.listAllCategory();
			Category parent = cate.getParent();
			List<Category> listParent = null;
			if (parent == null) {
				listParent = null;
			} else {
				listParent = list.stream().filter(o -> o.getId() != parent.getId()).collect(Collectors.toList());
			}
			model.addAttribute("pageTitle", "Edit Category ID: " + cate.getId());
			model.addAttribute("category", cate);
			model.addAttribute("listParent", listParent);
			model.addAttribute("parent", parent);
			return "Categories/category_form";
		} catch (CategoryNotFoundException e) {
			redirectAttribute.addFlashAttribute("message", e.getMessage());
			return "redirect:/categoriess";
		}
	}
	
	@GetMapping("/categories/delete/{id}")
	public String deleteCategory(@PathVariable(name = "id") Integer id, 
			Model model,
			RedirectAttributes redirectAttributes) {
		try {
			service.delete(id);
			redirectAttributes.addFlashAttribute("message", 
					"The category ID " + id + " has been deleted successfully");
			String uploadDir = "category-images/" + id;
			AmazonS3Util.removeFolder(uploadDir);
		} catch (CategoryNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
		}
		
		return "redirect:/categories";
	}
}
