package com.shope.category;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.shopme.category.CategoryRepository;
import com.shopme.common.entity.Category;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class CategoryRepositoryTest {
	@Autowired private CategoryRepository repo;
	
	@Test
	public void testEnabledCategory() {
		List<Category> list = repo.findAllEnabled();
		list.forEach(o -> System.out.println(o.getName()+ " " + o.isEnabled()));
	}
}	