package com.shopme.setting;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Currency;
import com.shopme.common.entity.Setting;
import com.shopme.common.entity.SettingCategory;
@Service
public class SettingService {
	@Autowired private SettingRepository repo;
	@Autowired private CurrencyRepository currencyRepo;
	
	public List<Setting> listAllBySetting(){
		return (List<Setting>)repo.findAll();
	}
	
	public List<Setting> getGeneralSettings() {
		return repo.findByTwoCategories(SettingCategory.GENERAL, SettingCategory.CURRENCY);
	}
	
	public void saveAll(Iterable<Setting> settings) {
		repo.saveAll(settings);
	}
	
	public EmailSettingBag getEmailSetting() {
		List<Setting> settings = repo.findByTwoCategories(SettingCategory.MAIL_SERVER, SettingCategory.MAIL_TEMPLATES);
		return new EmailSettingBag(settings);
	}
	
	public CurrencySettingBag getCurrencySettings() {
		List<Setting> settings = repo.findByCategory(SettingCategory.CURRENCY);
		return new CurrencySettingBag(settings);
	}
	
	public PaymentSettingBag getPaymentSettings() {
		List<Setting> settings = repo.findByCategory(SettingCategory.PAYMENT);
		return new PaymentSettingBag(settings);
	}
	
	public String getCurrencyCode() {
		Setting setting = repo.findByKey("CURRENCY_ID");
		Integer currencyId = Integer.parseInt(setting.getValue());
		Currency currency = currencyRepo.findById(currencyId).get();
		
		return currency.getCode();
	}
}
