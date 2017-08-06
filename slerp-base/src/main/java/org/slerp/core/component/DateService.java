package org.slerp.core.component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * The date converter work within {@link SimpleDateFormat} as default
 * 
 * @author kiditz
 */
@Component
public class DateService {
	private SimpleDateFormat dateFormat = new SimpleDateFormat();

	/**
	 * parse string to date within specific format
	 * 
	 * @param format
	 *            the pattern date format
	 * @param date
	 *            the {@link java.util.Date}
	 * @throws ParseException
	 *             if the format is wrong pattern
	 * 
	 */
	public Date parse(String format, String date) throws ParseException {
		dateFormat.applyLocalizedPattern(format);
		return dateFormat.parse(date);
	}

	/**
	 * convert date to string within specific format
	 */
	public String stringify(String format, Date date) {
		dateFormat.applyLocalizedPattern(format);
		return dateFormat.format(date);
	}

	/**
	 * convert date to string within default format {dd-MM-yyyy}
	 * 
	 * @param date
	 * @return the date string
	 */
	public String stringify(Date date) {
		return stringify("dd-MM-yyyy", date);
	}

	/**
	 * parse string to date within default format {dd-MM-yyyy}
	 * 
	 * @throws ParseException
	 *             if the date is wrong value
	 */
	public Date parse(String date) throws ParseException {
		return parse("dd-MM-yyyy", date);
	}

}
