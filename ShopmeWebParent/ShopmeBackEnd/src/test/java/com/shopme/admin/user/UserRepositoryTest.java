package com.shopme.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class UserRepositoryTest {
	@Autowired
	private UserRepository repo;
	
	@Autowired
	private RoleRepository roleRepo;
	
	@Test
	public void testCreateUser() {
		User user1 = new User("ducm40877@gmail.com", "123456", "Minh", "Nguyen");
		User user2 = new User("ducm123456@gmail.com", "123456", "Minh", "Nguyen");
		
		Role role1 = new Role(1);
		Role role2 = new Role(2);
		
		user1.addRole(role2);
		user1.addRole(role1);
		user2.addRole(role2);
		
		repo.saveAll(List.of(user1, user2));
	}
	
	@Test
	public void testFindAllUser() {
		Iterable<User> list = repo.findAll();
		list.forEach(o -> System.out.println(o.toString()));
	}
}
