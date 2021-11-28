package osu.cse3241;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class CSE3241IOUtil {
	/**
	 * This class is used as a helper to check the input of the user before 
	 */
	
	private CSE3241IOUtil() {
    }
	
	/**
	 * 
	 */
	static String[] ALLSYMBOLS = {"<", ">", ",", ".", "?", "!", "@", "#", "$", "^", "&", "*", "(", ")", "_", "-", "+", "=", ":", ";", "\\", "|", "/", "%", "{", "}", "[", "]"};
	static String[] EXTRASYMBOLS = {"--"};
	static String[] SPACES = {"\t", "\n", "\r", " "};
	static String[] DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
	
	
	// Helper Checks
	
	/**
	 * Checks if all the values are numbers
	 */
	public static boolean checkIfNums(String workid) {
		boolean allNums = true;
		for (int i = 0; i < workid.length(); i++) {
			if (!Character.isDigit(workid.charAt(i))) {
				allNums = false;
			}
		}
		return allNums;
	}
	
	/**
	 * Checks if the value should be set to null
	 */
	public static boolean checkForNull(String value) {
		return value.isBlank();
	}
	
	/**
	 * Checks if the length is not over max
	 */
	public static boolean checkLength(String value, int maxlength) {
		return value.length() > maxlength;
	}
	
	/**
	 * Checks if there are no symbols in the input
	 */
	public static boolean checkForAllSymbols(String value) {
		boolean contain = false;
		for (int i = 0; i < ALLSYMBOLS.length; i++) {
			contain = contain || value.contains(ALLSYMBOLS[i]);
		}
		return contain;
	}
	
	/**
	 * Checks if there are no symbols in the input, excluding -
	 */
	public static boolean checkForSymbols(String value) {
		boolean contain = false;
		for (int i = 0; i < ALLSYMBOLS.length; i++) {
			if (!ALLSYMBOLS[i].equals("-")) {
				contain = contain || value.contains(ALLSYMBOLS[i]);
			}
		}
		for (int i = 0; i < EXTRASYMBOLS.length; i++) {
			contain = contain || value.contains(EXTRASYMBOLS[i]);
		}
		return contain;
	}
	
	/**
	 * Checks if there are no symbols in the input
	 */
	public static boolean checkForDigits(String value) {
		boolean contain = false;
		for (int i = 0; i < DIGITS.length; i++) {
			contain = contain || value.contains(DIGITS[i]);
		}
		return contain;
	}
	
	/**
	 * Checks the formatting of a double. Must be "0.0".
	 */
	public static boolean checkDoubleFormatting(String value) {
		boolean makedoub = true;
		boolean betweenvals = false;
		Double doubval = null;
		try {
			doubval = Double.parseDouble(value);
		}
		catch (NumberFormatException e) {
			makedoub = false;
		}
		
		if (makedoub) {
			betweenvals = 0.0 <= doubval && doubval <= 5.0;			// or 0.0-10.0
		}
		
		return betweenvals && makedoub;
	}
	
	/**
	 * Check if the date is formatted properly
	 */
	public static boolean checkDateFormat(String date) {
		boolean formattedProperly = true;
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date.trim());
        } catch (ParseException pe) {
            formattedProperly = false;
        }
        return formattedProperly;
	}

	
	
	// Attribute input checks
	/**
	 * Checks if the Library ID input is 9 numbers
	 */
	public static boolean checkID(String id) {
		return (id.length() == 9) && checkIfNums(id);
	}
	
	/**
	 * Checks for a valid artist stage_name
	 */
	public static boolean validStageName(String name) {
		boolean lengthCheck = checkLength(name, 30);
		boolean nullCheck = checkForNull(name);
		boolean allSymbolsCheck = checkForAllSymbols(name);
		boolean noDigitsCheck = checkForDigits(name);
		return !lengthCheck && !nullCheck && !allSymbolsCheck && !noDigitsCheck;
	}
	
	/**
	 * Checks for a valid track_title
	 */
	public static boolean validTrackTitle(String name) {
		boolean lengthCheck = checkLength(name, 30);
		boolean nullCheck = checkForNull(name);
		boolean allSymbolsCheck = checkForAllSymbols(name);
		boolean noDigitsCheck = checkForDigits(name);
		return !lengthCheck && !nullCheck && !allSymbolsCheck && !noDigitsCheck;
	}

	/**
	 * Checks if the Work_ID or Artist_ID input is 15 numbers
	 */
	public static boolean validWorkOrArtistID(String id) {
		return (id.length() == 15) && checkIfNums(id);
	}

	/**
	 * Check for a valid number input
	 */
	public static boolean validNumber(String number) {
		int value = 0;
		boolean noParse = false;
		boolean notNegative = false;
		boolean isNull = checkForNull(number);
		if (!isNull) {
			try {
				value = Integer.parseInt(number);
				notNegative = value >= 0;
			}
			catch(NumberFormatException e) {
				noParse = true;
			}
		}
		return (!noParse && notNegative) || isNull;
	}

	/**
	 * Check for a valid genre
	 */
	public static boolean validGenre(String genre) {
		boolean lengthCheck = checkLength(genre, 15);
		boolean symbolsCheck = checkForSymbols(genre);
		boolean noDigitsCheck = checkForDigits(genre);
		boolean isNull = checkForNull(genre);
		return (!lengthCheck && !symbolsCheck && !noDigitsCheck) || isNull;
	}

	/**
	 * Check for a valid number input not null
	 */
	public static boolean validNumberNotNull(String number) { // Not used yet, not sure if correct
		int value = 0;
		boolean noParse = false;
		boolean notNegative = false;
		boolean isNull = checkForNull(number);
		if (!isNull) {
			try {
				value = Integer.parseInt(number);
				notNegative = value >= 0;
			}
			catch(NumberFormatException e) {
				noParse = true;
			}
		}
		return !noParse && notNegative && !isNull;
	}
	
	/**
	 * Check for a valid genre
	 */
	public static boolean validMovieTitle(String name) {
		boolean lengthCheck = checkLength(name, 40);
		boolean symbolsCheck = checkForSymbols(name);
		boolean noDigitsCheck = checkForDigits(name);
		boolean isNull = checkForNull(name);
		return !lengthCheck && !symbolsCheck && !noDigitsCheck || !isNull;
	}
}
