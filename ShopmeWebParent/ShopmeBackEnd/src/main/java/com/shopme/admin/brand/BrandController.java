package com.shopme.admin.brand;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;

@Controller
public class BrandController {
	@Autowired
	private BrandService service;
	
	@GetMapping("/brands")
	public String listAll(Model model) {
		 return listByPage(1, "name", "asc", null, model);
	}
	
	@GetMapping("/brands/page/{pagenum}")
	public String listByPage(@PathVariable("pagenum") Integer pagenum,@Param("sortField") String sortField,
			@Param("sortDir") String sortDir, @Param("keyword") String keyword, Model model) {
		Page<Brand> page = service.listByPage(pagenum, sortField, sortDir, keyword);
		List<Brand> listBrands = page.getContent();
		long startCount = (pagenum-1)*BrandService.BRAND_PER_PAGE-1;
		long endCount = startCount+BrandService.BRAND_PER_PAGE-1;
		if(endCount> page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		String reverseSortDir = sortDir.equals("asc")? "desc" : "asc";
		model.addAttribute("listBrands", listBrands);
		model.addAttribute("currentPage", pagenum);
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("listBrands", listBrands);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements()); 
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("sortField", sortField);
		model.addAttribute("keyword",keyword);
		model.addAttribute("moduleURL", "/brands");
		
		return "brands/brands";
	}
	
	@GetMapping("/brands/delete/{id}")
	public String deleteBrand(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		try {
			service.delete(id);
			redirectAttributes.addFlashAttribute("message", "The Brand ID: "+id+" deleted successfully");
			String uploadDir = "brand-logos/" + id;
			AmazonS3Util.removeFolder(uploadDir);
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
		}
		return "redirect:/brands";
	}
	
	@GetMapping("/brands/new")
	public String addNewBrand(Model model) {
		Brand brand = new Brand();
		List<Category> listCate = service.listAllCategory();
		model.addAttribute("brand", brand);
		model.addAttribute("listCategories", listCate);
		model.addAttribute("pageTitle", "Create New Brand");
		return "/brands/brand_form";
	}
	
	@PostMapping("/brands/save")
	public String saveBrand(Brand brand, @RequestParam("fileImage") MultipartFile multipartFile,
			RedirectAttributes redirectAttribute) throws IOException {

		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			brand.setLogo(fileName);
			Brand savedBrand = service.saveBrand(brand);
			String uploadDir = "brand-logos/" + savedBrand.getId();
			AmazonS3Util.removeFolder(uploadDir);
			AmazonS3Util.uploadFile(uploadDir, fileName, multipartFile.getInputStream());
		} else {
			service.saveBrand(brand);
		}
		redirectAttribute.addFlashAttribute("message", "The brand has been saved successfully.");
		return "redirect:/brands";
	}
	
	@GetMapping("/brands/edit/{id}")
	public String addNewBrand(Model model, @PathVariable("id") Integer id, RedirectAttributes redirectAttribute) {
		try {
			Brand brand = service.findById(id);
			List<Category> listCate = service.listAllCategory();
			model.addAttribute("brand", brand);
			model.addAttribute("listCategories", listCate);
			model.addAttribute("pageTitle", "Create New Brand");
			return "/brands/brand_form";
		} catch (BrandNotFoundException e) {
			redirectAttribute.addFlashAttribute("message", e.getMessage());
			return "redirect:/categories";
		}
	}
	
	
}
