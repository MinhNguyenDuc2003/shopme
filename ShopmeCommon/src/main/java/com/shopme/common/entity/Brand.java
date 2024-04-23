package com.shopme.common.entity;

import java.util.HashSet;
import java.util.Set;

import com.shopme.common.Constants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "brands")
public class Brand {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 45, unique = true)

	private String name;
	@Column(nullable = false, length = 128)

	private String logo;
	@ManyToMany(fetch = FetchType.EAGER)  
	@JoinTable(name = "brands_categories", joinColumns = @JoinColumn(name = "brand_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
	private Set<Category> categories = new HashSet<>();

	public Brand() {

	}
	
	public Brand(Integer id) {
		this.id = id;
	}



	public Brand(String name, String logo) {
		this.name = name;
		this.logo = logo;
	}

	


	public Brand(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public void setCategories(Set<Category> categories) {
		this.categories = categories;
	}
	
	@Transient
	public String getLogoPath() {
		if(id==null || logo==null) {
			return "/images/default-category.png";
		}
		return Constants.S3_BASE_URI +"/brand-logos/"+this.id+"/"+this.logo;
	}



	@Override
	public String toString() {
		return "Brand [id=" + id + ", name=" + name + ", logo=" + logo + ", categories=" + categories + "]";
	}
	
}
