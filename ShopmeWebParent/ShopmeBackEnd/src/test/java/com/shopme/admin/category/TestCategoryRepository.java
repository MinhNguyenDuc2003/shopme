package com.shopme.admin.category;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Category;


@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class TestCategoryRepository {
	@Autowired
	private CategoryRepository repo;
	
	@Test
	public void testPrintRootCategory() {
		Iterable<Category> list = repo.findAll();
		
		for(Category cat : list) {
			if(cat.getParent()==null) {
				System.out.println(cat.getName());
				Set<Category> set = cat.getChildren();
				for(Category cate : set) {
					printChild(cate, 0);
				}
			}
		}
	}
	public void printChild(Category cat, int level) {
		int subLevel = level +1;
		for(int i =0; i<subLevel; i++) {
			System.out.print("--");
		}
		System.out.println(cat.getName());
		Set<Category> set = cat.getChildren();
		for(Category cate : set) {
			printChild(cate, subLevel);
		}
	}
	@Test
	public void addCategory() {
		Category cat = new Category("Desktop");
		Category cat1 = new Category("PC");
		repo.saveAll(List.of(cat, cat1));
	}
}
