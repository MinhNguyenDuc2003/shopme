package com.shopme.admin.user.export;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.shopme.common.entity.User;
public class UserCsvExported extends AbstractExporter{
	
	public void export(List<User> listUsers, HttpServletResponse response ) throws IOException {
		super.setResponseHeader(response, "text/csv", ".csv");
		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
		String[] csvHeader = {"User ID", "E-mail","First Name", "Last Name", "Roles", "Enabled"};
		csvWriter.writeHeader(csvHeader);
		
		String[] fieldMapping = {"id", "email", "firstName", "lastName", "roles", "enabled"};
		for(User user : listUsers) {
			csvWriter.write(user, fieldMapping); 
		}
		csvWriter.close();
	}
}
