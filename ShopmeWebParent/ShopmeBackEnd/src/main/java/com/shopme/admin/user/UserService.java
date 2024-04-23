package com.shopme.admin.user;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

import jakarta.transaction.Transactional;
@Service
@Transactional
public class UserService {
	public static final int USER_PER_PAGE=4;
	@Autowired
	private UserRepository repo;
	
	@Autowired
	private RoleRepository roleRepo;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public List<User> listAll(){
		List<User> list = (List<User>) repo.findAll(Sort.by("firstName").ascending());
		return list;
	}
	
	public User getUserByEmail(String email) {
		return repo.getUserByEmail(email);
	}
	
	public List<Role> listRole(){
		List<Role> roles = (List<Role>) roleRepo.findAll();
		return roles;
	}
	
	public User save(User user) {
		boolean isUpdating = (user.getId()!=null);
		if(isUpdating) {
			User userIsExist = repo.findById(user.getId()).get();
			
			if(user.getPassword().isEmpty()) {
				user.setPassword(userIsExist.getPassword());
			}
			else {
				encodePassword(user);
			}
		} 
		else {
			encodePassword(user);
		}
		return repo.save(user);
	}
	
	private void encodePassword(User user) {
		String encodePassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassword);
	}
	
	public boolean isEmailUnique(String id, String email) {
		User oldUser = repo.getUserByEmail(email);
		
		if(oldUser == null) return true;
		boolean isCreatingNew = (id==null);
		if(isCreatingNew) {
			if(oldUser !=null) {
				return false;
			}
		}
		else {
			if(!id.equals(oldUser.getId()+"")) {
				return false;
			}
		}
		return true;
//		if(oldUser == null) {
//			if(id != null) {
//				repo.deleteById(Integer.parseInt(id));
//				return true;
//			}
//			else {
//				return true;
//			}
//		}
//		else {
//			if(id.equals(oldUser.getId()+"")) {
//				return true;
//			}
//			else {
//				return false;
//			}
//		}
	}
	
	

	public User get(Integer id) throws UserNotFoundException{
		// TODO Auto-generated method stub
		try {
			return repo.findById(id).get();
		} catch (NoSuchElementException e) {
			// TODO Auto-generated catch block
			throw new UserNotFoundException("Could not find user with id" +id);
		}
	}

	public void delete(Integer id) throws UserNotFoundException {
		Long count = repo.countById(id);
		if(count==0 || count ==  null) {
			throw new UserNotFoundException("Could not find user with ID: " + id);
		}
		repo.deleteById(id);
	}
	
	public void updateUserEnabledStatus(Integer id) throws UserNotFoundException {
		Long count = repo.countById(id);
		if(count==0 || count ==  null) {
			throw new UserNotFoundException("Could not find user with ID: " + id);
		}
		User user = repo.findById(id).get();
		if(user.getEnabled()==true) {
			repo.updateEnabledStatus(false, id);
		}
		else {
			repo.updateEnabledStatus(true, id);
		}
	}
	
	public Page<User> listByPage(int pageNum, String sortField, String sortDir, String keyword){
		Sort sort = Sort.by(sortField);
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		Pageable pageable = PageRequest.of(pageNum - 1, USER_PER_PAGE, sort);
		if(keyword!=null) {
			return repo.findAll(keyword, pageable);
		}
		return repo.findAll(pageable);
	}
	 public User updateAccount(User newUser) {
		 User oldUser = repo.findById(newUser.getId()).get();
		 
		 if(!newUser.getPassword().isEmpty()) {
			 oldUser.setPassword(newUser.getPassword());
			 encodePassword(oldUser);
		 }
		 if(newUser.getPhotos() !=null) {
			 oldUser.setPhotos(newUser.getPhotos());
		 }
		 oldUser.setFirstName(newUser.getFirstName());
		 oldUser.setLastName(newUser.getLastName());
		 return repo.save(oldUser);
	 }
	
}
