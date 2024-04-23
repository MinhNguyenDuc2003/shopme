package com.shopme.admin.product;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.brand.BrandService;
import com.shopme.admin.category.CategoryService;
import com.shopme.admin.security.ShopmeUserDetails;
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;
import com.shopme.common.entity.Product;
import com.shopme.common.entity.ProductDetail;
import com.shopme.common.exception.ProductNotFoundException;

@Controller
public class ProductController {
	@Autowired
	private ProductService service;

	@Autowired
	private BrandService brandService;
	
	@Autowired
	private CategoryService catService;

	@GetMapping("/products")
	public String listAll(Model model) {
		return listByPage(1, model, "name", "asc", "", 0);
	}

	@GetMapping("/products/page/{pagenum}")
	public String listByPage(@PathVariable("pagenum") int pageNum, Model model, @Param("sortField") String sortField,
			@Param("sortDir") String sortDir, @Param("keyword") String keyword, @RequestParam("categoryId") Integer catId) {
		Page<Product> page = service.listByPage(pageNum, sortField, sortDir, keyword, catId);
		List<Product> list = page.getContent();
		List<Category> listCat = catService.listAllCategory();
		long startCount = (pageNum - 1) * service.PRODUCT_PER_PAGE + 1;
		long endCount = startCount + service.PRODUCT_PER_PAGE - 1;
		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		String reSortDir = sortDir.equals("asc") ? "desc" : "asc";
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("listProducts", list);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("sortDir", sortDir);   
		model.addAttribute("reverseSortDir", reSortDir);
		model.addAttribute("sortField", sortField);
		model.addAttribute("keyword", keyword);
		model.addAttribute("moduleURL", "/products");
		model.addAttribute("listCategories", listCat);
		if (catId != null) model.addAttribute("categoryId", catId);
		return "products/products";
	}

	@GetMapping("/products/new")
	public String newProduct(Model model) {
		List<Brand> brand = brandService.listAll();

		Product product = new Product();
		product.setEnabled(true);
		product.setInStock(true);

		model.addAttribute("product", product);
		model.addAttribute("listBrands", brand);
		model.addAttribute("pageTitle", "Create new Product");
		model.addAttribute("numberOfExistingExtraImages", 0);
		return "products/product_form";
	}

	@PostMapping("/products/save")
	public String saveProduct(Product product, RedirectAttributes ra,
			@RequestParam(name = "fileImage", required = false) MultipartFile mainImageMultipart,
			@RequestParam(name = "extraImage", required = false) MultipartFile[] extraImageMultiparts,
			@RequestParam(name = "detailIDs", required = false) String[] detailIDs,
			@RequestParam(name = "detailNames", required = false) String[] detailNames,
			@RequestParam(name = "detailValues", required = false) String[] detailValues,
			@RequestParam(name = "imageIDs", required = false) String[] imageIDs,
			@RequestParam(name = "imageNames", required = false) String[] imageNames,
			@AuthenticationPrincipal ShopmeUserDetails loggedUser) throws IOException {
		
		if (!loggedUser.hasRole("Admin") && !loggedUser.hasRole("Editor")) {
			if (loggedUser.hasRole("Salesperson")) {
				service.saveProductPrice(product);
				ra.addFlashAttribute("message", "The product has been saved successfully.");			
				return "redirect:/products";
			}
		}
		
		ProductSaveHelper.setMainImageName(mainImageMultipart, product);
		ProductSaveHelper.setExistingExtraImageNames(imageIDs, imageNames, product);
		ProductSaveHelper.setNewExtraImageNames(extraImageMultiparts, product);
		ProductSaveHelper.setProductDetails(detailIDs, detailNames, detailValues, product);
		
		Product savedProduct = service.save(product);
		
		ProductSaveHelper.savedUploadImage(mainImageMultipart, extraImageMultiparts, savedProduct);
		ProductSaveHelper.deleteExtraImagesWeredRemovedOnForm(savedProduct);
		ra.addFlashAttribute("message", "The product has been saved successfully.");
		return "redirect:/products";
	}


	@PostMapping("/products/{id}/enabled/{status}")
	public String updateEnabled(@PathVariable("id") Integer id, @PathVariable("status") boolean status,
			RedirectAttributes redirectAttribute) {
		try {
			service.updateStatus(id, status);
			redirectAttribute.addFlashAttribute("message", "The user ID: " + id + " updated status successfully!");
		} catch (ProductNotFoundException e) {
			redirectAttribute.addFlashAttribute("message", e.getMessage());
		}
		return "redirect:/products";
	}

	
	@GetMapping("/products/edit/{id}")
	public String editProduct(@PathVariable("id") Integer id,
			Model model,
			 RedirectAttributes ra,
			 @AuthenticationPrincipal ShopmeUserDetails loggedUser) {
		try {
			Product product = service.get(id);
			List<Brand> brand = brandService.listAll();
			int numberOfExistingExtraImages = product.getImages().size(); 
			
			boolean isReadOnlyForSalesperson = false;
			
			if (!loggedUser.hasRole("Admin") && !loggedUser.hasRole("Editor")) {
				if (loggedUser.hasRole("Salesperson")) {
					isReadOnlyForSalesperson = true;
				}
			}
			
			model.addAttribute("product", product);
			model.addAttribute("listBrands", brand);
			model.addAttribute("pageTitle", "Edit product (ID: "+id+")");
			model.addAttribute("numberOfExistingExtraImages", numberOfExistingExtraImages);
			return "products/product_form";
		} catch (ProductNotFoundException e) {
			ra.addFlashAttribute("message", e.getMessage());
			return "redirect:/products";
		}
	}
	
	@GetMapping("/products/detail/{id}")
	public String viewProductDetails(@PathVariable("id") Integer id,
			Model model,
			 RedirectAttributes ra) {
		try {
			Product product = service.get(id);
			model.addAttribute("product", product);
			return "products/product_detail_modal";
		} catch (ProductNotFoundException e) {
			ra.addFlashAttribute("message", e.getMessage());
			return "redirect:/products";
		}
	}
	
}
