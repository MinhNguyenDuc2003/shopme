package com.shopme.customer;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.AuthenticationType;
import com.shopme.common.entity.Country;
import com.shopme.common.entity.Customer;
import com.shopme.common.exception.CustomerNotFoundException;
import com.shopme.setting.CountryRepository;

import jakarta.transaction.Transactional;
import net.bytebuddy.utility.RandomString;

@Service
@Transactional
public class CustomerService {
	@Autowired
	private CustomerRepository cusRepo;
	@Autowired
	private CountryRepository countryRepo;
	@Autowired
	private PasswordEncoder passwordEncoder;

	public List<Country> listAllCountry() {
		return countryRepo.findAllByOrderByNameAsc();
	}

	public boolean isEmailUnique(String email) {
		Customer customer = cusRepo.findByEmail(email);
		return customer == null;
	}

	public void register(Customer customer) {
		encodePassword(customer);
		customer.setEnabled(false);
		customer.setCreatedTime(new Date());
		customer.setAuthenticationType(AuthenticationType.DATABASE);
		String randomCode = RandomString.make(64);
		customer.setVerificationCode(randomCode);

		cusRepo.save(customer);
	}

	private void encodePassword(Customer customer) {
		String passwordEncoded = passwordEncoder.encode(customer.getPassword());
		customer.setPassword(passwordEncoded);
	}

	public boolean verify(String verificationCode) {
		Customer customer = cusRepo.findByVerificationCode(verificationCode);

		if (customer == null || customer.isEnabled()) {
			return false;
		}
		cusRepo.enable(customer.getId());
		return true;
	}

	public void updateAuthenticationType(Customer customer, AuthenticationType type) {
		if (!customer.getAuthenticationType().equals(type)) {
			cusRepo.updateAuthenticationType(customer.getId(), type);
		}
	}

	public Customer getCustomerByEmail(String email) {
		return cusRepo.findByEmail(email);
	}

	private void setName(String name, Customer customer) {
		String[] nameArray = name.split(" ");
		if (nameArray.length < 2) {
			customer.setFirstName(name);
			customer.setLastName("");
		} else {
			String firstName = nameArray[0];
			customer.setFirstName(firstName);

			String lastName = name.replaceFirst(firstName + " ", "");
			customer.setLastName(lastName);
		}
	}

	public void addNewCustomerUponOAuthLogin(String name, String email, String countryCode,
			AuthenticationType authenticationType) {
		Customer customer = new Customer();
		customer.setEmail(email);
		setName(name, customer);

		customer.setEnabled(true);
		customer.setCreatedTime(new Date());
		customer.setAuthenticationType(authenticationType);
		customer.setPassword("");
		customer.setAddressLine1("");
		customer.setCity("");
		customer.setState("");
		customer.setPhoneNumber("");
		customer.setPostalCode("");
		customer.setCountry(countryRepo.findByCode(countryCode));

		cusRepo.save(customer);
	}
	
	public void update(Customer customerInForm) {
		Customer customerInDB = cusRepo.findById(customerInForm.getId()).get();
		
		if (customerInDB.getAuthenticationType().equals(AuthenticationType.DATABASE)) {
			if (!customerInForm.getPassword().isEmpty()) {
				String encodedPassword = passwordEncoder.encode(customerInForm.getPassword());
				customerInForm.setPassword(encodedPassword);			
			} else {
				customerInForm.setPassword(customerInDB.getPassword());
			}		
		} else {
			customerInForm.setPassword(customerInDB.getPassword());
		}
		
		customerInForm.setEnabled(customerInDB.isEnabled());
		customerInForm.setCreatedTime(customerInDB.getCreatedTime());
		customerInForm.setVerificationCode(customerInDB.getVerificationCode());
		customerInForm.setAuthenticationType(customerInDB.getAuthenticationType());
		customerInForm.setResetPasswordToken(customerInDB.getResetPasswordToken());
		
		cusRepo.save(customerInForm);
	}

	public String updateResetPasswordToken(String email) throws CustomerNotFoundException {
		Customer customer = cusRepo.findByEmail(email);
		if(customer != null) {
			String token = RandomString.make(30);
			customer.setResetPasswordToken(token);
			cusRepo.save(customer);
			return token;
		} else {
			throw new CustomerNotFoundException("Could not find any customer with the email " + email);
		}
		
	}
	
	public Customer getByResetPasswordToken(String token) {
		return cusRepo.findByResetPasswordToken(token);
	}
	
	public void updatePassword(String token, String newPassword) throws CustomerNotFoundException {
		Customer customer = cusRepo.findByResetPasswordToken(token);
		if(customer == null) {
			throw new CustomerNotFoundException("No customer found: invalid token");
		}
		
		customer.setPassword(newPassword);
		encodePassword(customer);
		cusRepo.save(customer);
	}

}
