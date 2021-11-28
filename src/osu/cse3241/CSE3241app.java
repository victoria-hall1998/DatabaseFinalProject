package osu.cse3241;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.*;

public class CSE3241app {
    
	/**
	 *  The database file name.
	 *  
	 *  Make sure the database file is in the root folder of the project if you only provide the name and extension.
	 *  
	 *  Otherwise, you will need to provide an absolute path from your C: drive or a relative path from the folder this class is in.
	 */
	//private static String DATABASE = "FinalData";
	private static String DATABASE = "C:/OSU Files/Database/SQLiteStudio/FinalData";
	
	/**
	 *  The query statement to be executed.
	 *  
	 *  Remember to include the semicolon at the end of the statement string.
	 *  (Not all programming languages and/or packages require the semicolon (e.g., Python's SQLite3 library))
	 */
	// If there is an artist without any albums, it will still give the artist's info
	private static String sqlSearchArtist = "SELECT Artist.Stage_Name, Artist.Artist_ID, Album.Title, Album.Work_ID, Track.Track_Title, Album.Physical_Copies_Available, Album.Digital_Copies_Available"
			+ " FROM Artist"
			+ " LEFT JOIN Produces"
			+ " ON Artist.Artist_ID = Produces.Artist_ID"
			+ " LEFT JOIN Album"
			+ " ON Produces.Work_ID = Album.Work_ID"
			+ " LEFT JOIN Track"
			+ " ON Track.Work_ID = Album.Work_ID"
			+ " WHERE Artist.Stage_Name LIKE ?;";
	// If there is no track, then there is no info because track is a weak entity
	private static String sqlSearchTrack = "SELECT Track.Track_Title, Track.Rating, Track.Duration, Album.Work_ID, Album.Music_Genre, Artist.Artist_ID, Artist.Stage_Name"
			+ " FROM Track, Album, Produces, Artist"
			+ " WHERE Track_Title LIKE ? AND Track.Work_ID = Album.Work_ID AND Album.Work_ID = Produces.Work_ID AND Produces.Artist_ID = Artist.Artist_ID;";
	private static String sqlInsertIntoArtist = "INSERT INTO Artist(Artist_ID, No_Members, Stage_Name) VALUES (?, ?, ?);";
	private static String sqlInsertIntoAudiobook = "INSERT INTO Audiobook(Work_ID, Lit_Genre, No_Pages, No_Chapters, Title, Rating, Release_Date, Physical_Copies_Available, Physical_Copies_Out, Digital_Copies_Available, Digital_Copies_Out) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	private static String mostPopularActor = "SELECT max(total), Person.Name, Person.Actor_Flag\r\n"
            + "FROM PERSON, (SELECT count(Rents.Work_ID = Acts_In.Work_ID) as total\r\n"
            + "FROM PERSON, RENTS, ACTS_IN\r\n"
            + "WHERE Rents.Work_ID = ACTS_IN.Work_ID AND ACTS_IN.Personal_ID = Person.Personal_ID AND Rents.Type = \"Movie\")\r\n"
            + "WHERE Person.Actor_Flag = \"TRUE\";\r\n" + "";
    private static String mostPopularArtist = "SELECT max(time) , Artist.Stage_Name\r\n"
            + "FROM Artist, (SELECT count(Rents.Work_ID * Album.Play_Time) as time\r\n"
            + "FROM ARTIST, ALBUM, RENTS, PRODUCES\r\n"
            + "WHERE Artist.Artist_ID = Produces.Artist_ID AND Rents.Work_ID = Produces.Work_ID);\r\n";
    private static String tracksByArtistBeforeYear = "SELECT Track.Track_Title\r\n"
            + "FROM Album, Track, Artist, Produces\r\n"
            + "WHERE Artist.Stage_Name = ?\r\n"
            + "AND Album.Release_Date < ?\r\n"
            + "AND Produces.Work_ID = Album.Work_ID\r\n"
            + "AND Produces.Artist_ID = Artist.Artist_ID\r\n"
            + "AND Track.Work_ID = Album.Work_ID;";
    private static String numAlbumsRentedByPatron = " SELECT COUNT(Rents.Library_Card)\r\n"
            + "FROM Rents, Library_Member\r\n"
            + "WHERE Library_Member.Email = ?" + "AND Rents.Type = 'Album'\r\n"
            + "AND Rents.Library_Card = Library_Member.Library_Card";
    private static String patronMostVideos = "SELECT COUNT(Rents.Library_Card), Library_Member.*\r\n"
            + "FROM Rents, Library_Member\r\n"
            + "WHERE Rents.Library_Card = Library_Member.Library_Card\r\n"
            + "GROUP BY Rents.Library_Card\r\n"
            + "ORDER BY COUNT(Rents.Library_Card) DESC\r\n" + "LIMIT 1";
    private static String orderMovie = "INSERT INTO ORDERS(Employee_ID, Work_ID, Est_Delivery_Date, Quantity, Type) VALUES(?, ?, ?, ?,'Movie');";
    private static String orders = "SELECT * FROM ORDERS WHERE ORDERS.Work_ID = ?;";
    private static String moveOrderIntoInventory = "INSERT INTO MOVIE(Work_ID, Film_Genre, Duration, Title, Rating, Release_Date, "
			+ "Physical_Copies_Available, Physical_Copies_Out, Digital_Copies_Available, Digital_Copies_Out) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, '0', ?, '0');";
    private static String movieSearch = "SELECT * FROM MOVIE WHERE MOVIE.Work_ID = ?;";
    private static String updateMovie = "UPDATE MOVIE SET Physical_Copies_Available = ? WHERE Work_ID = ?;"; 
    
    
	/**
	 * Main Menu Options Print
	 */
	public static void printMenuOptions() {
		System.out.println("Main Menu");
		System.out.println("Please select from one of the following options by entering the letter next to the option and then pressing the enter key.");
		System.out.println("a. Search");
		System.out.println("b. Add new records");
		System.out.println("c. Order items");
		System.out.println("d. Edit records");
		System.out.println("e. Useful Reports (not implemented yet)");
		System.out.println("Any other letter will Exit the program");
	}
	
	/**
	 * Check if the input is allowing the user to keep picking options from the Main Menu
	 */
	public static boolean checkInputContinue(Connection conn, Scanner input) {
		char option = optionSelected(input);
		switch(option) {
			case 'a':
				searchOption(conn, input);
				break;
			case 'b':
				addNewRecordOption(conn, input);
				break;
			case 'c':
				orderItemsOption(conn, input);
				break;
			case 'd':
				editRecordsOption(conn, input);
				break;
			case 'e':
				usefulReportsOption(conn, input);
				break;
			default:
				System.out.println("Exiting the Program");
				System.out.println("Have a good day");
				break;
		}
		return !((option >= 97) && (option <= 101));
	}
	
	/**
	 * Get the Option of the User
	 */
	public static char optionSelected(Scanner input) {
		System.out.print("Option: ");
		String option = "t";
		if(input.hasNextLine()) {
			option = input.nextLine();
		}

		return option.charAt(0);
	}
	
	/**
	 * User selected a. Search as their option from Main Menu
	 */
	public static void searchOption(Connection conn, Scanner input) {
		/*
		 * User selects if they want an artist or a track?
		 * Then get all the values of selection
		 * search through VALUE attributes for the stagename or tracktitle
		 * display all the info
		 */
		ArrayList<Object> inputs = new ArrayList<Object>();
		System.out.println("Select what you would like to search for:");
		System.out.println("a. Artist");
		System.out.println("b. Track");
		char option = optionSelected(input);
		switch(option) {
			case 'a':
				System.out.print("What artist would you like to search for (30 characters or less)? ");
				String artist = input.nextLine();
				while (!CSE3241IOUtil.validStageName(artist)) {
		    		System.out.println("You entered an invalid artist name.");
		    		System.out.print("What artist would you like to search for (30 characters or less)? ");
		    		artist = input.nextLine();
		    	}
				
				inputs.add(artist);
				
				PreparedStatement artiststatement = CSE3241SQLUtil.setUpPS(conn, sqlSearchArtist, inputs);
				CSE3241SQLUtil.sqlQuerySearchAndPrint(artiststatement);
				break;
			case 'b':
				System.out.print("What track would you like to search for (30 characters or less)? ");
				String track = input.nextLine();
				while (!CSE3241IOUtil.validTrackTitle(track)) {
		    		System.out.println("You entered an invalid track name.");
		    		System.out.print("What track would you like to search for (30 characters or less)? ");
		    		track = input.nextLine();
		    	}

				inputs.add(track);
				PreparedStatement trackstatement = CSE3241SQLUtil.setUpPS(conn, sqlSearchTrack, inputs);
				CSE3241SQLUtil.sqlQuerySearchAndPrint(trackstatement);
				break;
			default:
				// IF the user does not select a or b, then give an error
				System.out.println("Invalid option selected. Please try again with a valid selection.");
				break;
		}
	}
	
	/**
	 * User selected b. Add new records as their option from Main Menu
	 */
	public static void addNewRecordOption(Connection conn, Scanner input) {
		ArrayList<Object> inputs = new ArrayList<Object>();
		// Ask if the user wants to add an artist or an audiobook
		System.out.println("Select what you would like to new record for:");
		System.out.println("a. Artist");
		System.out.println("b. Audiobook");
		char option = optionSelected(input);
		switch(option) {
			case 'a':
				System.out.print("What is the Artist_ID (15 numbers)? ");
		    	String artistidart = input.nextLine();
		    	while (!CSE3241IOUtil.validWorkOrArtistID(artistidart)) {
		    		System.out.println("You did not enter a proper Artist_ID made of 15 numbers.");
		    		System.out.print("What is the Artist_ID (15 numbers)? ");
		    		artistidart = input.nextLine();
		    	}
		    	inputs.add(artistidart);
		    	
		    	System.out.print("How many Memebers are in the band? ");
		    	String membersart = input.nextLine();
		    	while (!CSE3241IOUtil.validNumber(membersart)) {
		    		System.out.println("You did not enter a proper number for members.");
		    		System.out.print("How many Memebers are in the band? ");
		    		membersart = input.nextLine();
		    	}
		    	if(CSE3241IOUtil.checkForNull(membersart)) {
		    		inputs.add(null);
		    	}
		    	else { 
		    		inputs.add(Integer.parseInt(membersart));
		    	}
		    	
		    	System.out.print("What is their Stage_Name (Please use abbreviation if longer than 30 characters)? ");
		    	String nameart = input.nextLine();
		    	while (!CSE3241IOUtil.validStageName(nameart)) {
		    		System.out.println("You entered an invalid Stage_Name.");
		    		System.out.print("What is their Stage_Name (Please use abbreviation if longer than 30 characters)? ");
		    		nameart = input.nextLine();
		    	}
		    	inputs.add(nameart);
		    	
		    	PreparedStatement artiststatement = CSE3241SQLUtil.setUpPS(conn, sqlInsertIntoArtist, inputs);
				CSE3241SQLUtil.sqlQuery(artiststatement, false);
				break;
			case 'b':
				System.out.print("What is the Work_ID (15 numbers)? ");
		    	String workidab = input.nextLine();
		    	while (!CSE3241IOUtil.validWorkOrArtistID(workidab)) {
		    		System.out.println("You did not enter a proper Work_ID made of 15 numbers.");
		    		System.out.print("What is the Work_ID (15 numbers)? ");
		    		workidab = input.nextLine();
		    	}
		    	inputs.add(workidab);
		    	
		    	System.out.print("What is the Lit_Genre of the Audiobook (Please use abbreviation if longer than 15 characters)? ");
		    	String genreab = input.nextLine();
		    	while (!CSE3241IOUtil.validGenre(genreab)) {
		    		System.out.println("You entered too long of a Lit_Genre.");
		    		System.out.print("What is the Lit_Genre of the Audiobook (Please use abbreviation if longer than 15 characters)? ");
		    		genreab = input.nextLine();
		    	}
		    	if(CSE3241IOUtil.checkForNull(genreab)) {
		    		inputs.add(null);
		    	}
		    	else {
		    		inputs.add(genreab);
		    	}
		    	
		    	System.out.print("How many Pages does the Audiobook have? ");
		    	String numpagesab = input.nextLine();
		    	while (!CSE3241IOUtil.validNumber(numpagesab)) {
		    		System.out.println("You did not enter a valid number of pages.");
		    		System.out.print("How many Pages does the Audiobook have? ");
		    		numpagesab = input.nextLine();
		    	}
		    	if(CSE3241IOUtil.checkForNull(numpagesab)) {
		    		inputs.add(null);
		    	}
		    	else {
		    		inputs.add(Integer.parseInt(numpagesab));
		    	}
		    	
		    	System.out.print("How many Chapters are in the Audiobook? ");
		    	String numchaptersab = input.nextLine();
		    	while (!CSE3241IOUtil.validNumber(numchaptersab)) {
		    		System.out.println("You did not enter a valid number of chapters.");
		    		System.out.print("How many Chapters are in the Audiobook? ");
		    		numchaptersab = input.nextLine();
		    	}
		    	if(CSE3241IOUtil.checkForNull(numchaptersab)) {
		    		inputs.add(null);
		    	}
		    	else {
		    		inputs.add(numchaptersab);
		    	}
		    	
		    	System.out.print("What is the Title of the Audiobook (Please use abbreviation if longer than 40 characters)? ");
		    	String titleab = input.nextLine();
		    	while (!CSE3241IOUtil.validMovieTitle(titleab)) {
		    		System.out.println("You entered a invalid title.");
		    		System.out.print("What is the Title of the Audiobook (Please use abbreviation if longer than 40 characters)? ");
		    		titleab = input.nextLine();
		    	}
		    	inputs.add(titleab);
		    	
		    	System.out.println("What is the Rating of the Audiobook (enter in #.# or ##.# format between 0.0 and 5.0)? ");
		    	System.out.print("Note: if value has more than one value after the . they will be dropped. ");
		    	String ratingab = input.nextLine();
		    	// Check needed for this
		    	while (!CSE3241IOUtil.checkDoubleFormatting(ratingab) && !ratingab.isBlank()) {
		    		System.out.println("You entered an invalid rating.");
		    		System.out.println("What is the Rating of the Audiobook (enter in #.# or ##.# format between 0.0 and 10.0)? ");
		    		System.out.print("Note: if value has more than one value after the . they will be dropped. ");
		    		ratingab = input.nextLine();
		    	}
		    	if(CSE3241IOUtil.checkForNull(ratingab)) {
		    		inputs.add(null);
		    	}
		    	else {
		    		String val = String.format("%.1f", Double.parseDouble(ratingab));
		    		inputs.add(Double.parseDouble(val));
		    	}
		    	
		    	System.out.print("When was/is the Audiobook's Release_Date in mm/dd/yyyy? ");
		    	String releasedateab = input.nextLine();
		    	// Check needed for this
		    	while (!CSE3241IOUtil.checkDateFormat(releasedateab) && !releasedateab.isBlank()) {
		    		System.out.println("You entered an invalid release date.");
		    		System.out.println("What is the Rating of the Audiobook (enter in mm/dd/year format)? ");
		    		releasedateab = input.nextLine();
		    	}
		    	if(CSE3241IOUtil.checkForNull(releasedateab)) {
		    		inputs.add(null);
		    	}
		    	else {
		    		inputs.add(releasedateab);
		    	}
		    	
		    	System.out.print("How many Physical_Copies are Available? ");
		    	String pcopies_availab = input.nextLine();
		    	while (!CSE3241IOUtil.validNumberNotNull(pcopies_availab)) {
		    		System.out.println("You did not enter a valid number for Physical_Copies_Available.");
		    		System.out.print("How many Physical_Copies are Available? ");
		    		pcopies_availab = input.nextLine();
		    	}
		    	inputs.add(Integer.parseInt(pcopies_availab));
		    	
		    	System.out.print("How many Physical_Copies are Checked Out? ");
		    	String pcopies_outab = input.nextLine();
		    	while (!CSE3241IOUtil.validNumberNotNull(pcopies_outab)) {
		    		System.out.println("You did not enter a valid number for Physical_Copies_Out.");
		    		System.out.print("How many Physical_Copies are Out? ");
		    		pcopies_outab = input.nextLine();
		    	}
		    	inputs.add(Integer.parseInt(pcopies_outab));

		     	System.out.print("How many Digital_Copies are Available? ");
		    	String dcopies_availab = input.nextLine();
		    	while (!CSE3241IOUtil.validNumberNotNull(dcopies_availab)) {
		    		System.out.println("You did not enter a valid number for Digital_Copies_Available.");
		    		System.out.print("How many Digital_Copies are Available? ");
		    		dcopies_availab = input.nextLine();
		    	}
		    	inputs.add(Integer.parseInt(dcopies_availab));
		    	
		    	System.out.print("How many Digital_Copies are Checked Out? ");
		    	String dcopies_outab = input.nextLine();
		    	while (!CSE3241IOUtil.validNumberNotNull(dcopies_outab)) {
		    		System.out.println("You did not enter a valid number for Digital_Copies_Out.");
		    		System.out.print("How many Digital_Copies are Out? ");
		    		dcopies_outab = input.nextLine();
		    	}
		    	inputs.add(Integer.parseInt(dcopies_outab));
		    	
		    	PreparedStatement audiostatement = CSE3241SQLUtil.setUpPS(conn, sqlInsertIntoAudiobook, inputs);
				CSE3241SQLUtil.sqlQuery(audiostatement, false);
				break;		
			default:
				// IF the user entered something other than 1 or 2, give an error
				System.out.println("Invalid option selected. Please try again with a valid selection.");
				break;
		}
	}
	
	/**
	 * User selected c. Order items as their option from Main Menu
	 * @param conn 
	 */
	public static void orderItemsOption(Connection conn, Scanner input) {
		System.out.println("Select what you would like to do:");
		System.out.println("a. Order a Movie");
		System.out.println("b. Activate item record");
		char option = optionSelected(input);
		switch(option) {
			case 'a':
				System.out.println("You chose to order a movie. Please enter your Employee ID to contiue: ");
            	String empID = input.nextLine();
		    	while (!CSE3241IOUtil.checkID(empID)) {
		    		System.out.println("You did not enter a proper Employee ID made of 15 numbers.");
		    		System.out.print("What is the Employee ID (15 numbers)? ");
		    		empID = input.nextLine();
		    	}
            	
            	System.out.println("Please enter an ID for the movie being ordered: ");
            	String workID = input.nextLine();
            	while (!CSE3241IOUtil.validWorkOrArtistID(workID)) {
		    		System.out.println("You did not enter a proper Work_ID made of 15 numbers.");
		    		System.out.print("What is the Work_ID (15 numbers)? ");
		    		workID = input.nextLine();
		    	}
		    	
            	System.out.println("Please specificy the expected delivery date in MM/DD/YYYY form: ");
            	String d1 = input.nextLine();
            	while (!CSE3241IOUtil.checkDateFormat(d1) && !d1.isBlank()) {
                    System.out.println("You entered an invalid delivery date.");
                    d1 = input.nextLine();
                }
            	
            	System.out.println("How many copies of the movie did you order? ");
            	String quantityDesired = input.nextLine();
            	while (!CSE3241IOUtil.checkIfNums(quantityDesired)) {
            		System.out.println("Invalid value entered.");
            		System.out.println("How many copies of the movie did you order? ");
            		quantityDesired = input.nextLine();
            	}
            	
            	ArrayList<Object> choiceA = new ArrayList<>();
            	choiceA.add(empID);
            	choiceA.add(workID);
            	choiceA.add(d1);
            	choiceA.add(quantityDesired);
            	
            	PreparedStatement ps1 = CSE3241SQLUtil.setUpPS(conn, orderMovie, choiceA);
            	CSE3241SQLUtil.sqlQuery(ps1, false);
                break;
			case 'b':
				// IF the user selects to activate item record
				System.out.println("You selected to activate item record. Please provide the Work ID: ");
                workID = input.nextLine();
                while (!CSE3241IOUtil.validWorkOrArtistID(workID)) {
                    System.out.println("Error, artistID must be 15 numbers long.");
                    workID = input.nextLine();
                }
                ArrayList<Object> starter = new ArrayList<>();
                starter.add(workID);
                
                PreparedStatement activate = CSE3241SQLUtil.setUpPS(conn, orders, starter);
                System.out.println("Set Up PS worked");
                ResultSet results = CSE3241SQLUtil.sqlQuery(activate, true);
                
				String type ="";
				int quantity =0;
				try {
					if(!results.isClosed()) {
						type = results.getString("Type");
						quantity = results.getInt("Quantity");
						type = type.toLowerCase();
					}else {
						System.out.println("No orders for this ID to be activated");
						return;
					}
				} catch (SQLException e3) {
					e3.printStackTrace();
				}
                
                System.out.println("Type = " + type + " Quantity = " + quantity);
                switch(type) {
                case "movie":
                	System.out.println("Searching for movie");
                	ArrayList<Object> movieIn = new ArrayList<Object>();
                	movieIn.add(workID);
                	PreparedStatement ps2 = CSE3241SQLUtil.setUpPS(conn, movieSearch, movieIn);
                	ResultSet movie = CSE3241SQLUtil.sqlQuery(ps2, true);
                	String value2 = "";
					try {
						if(!movie.isClosed()) {
							value2 = movie.getString("Work_ID");
						}
					}
					catch (SQLException e2) {
						e2.printStackTrace();
					}
                	
                	try {
						if(value2.length()==0) {
							System.out.println("Enter a title for the movie: ");
							String title = input.nextLine();
							while (CSE3241IOUtil.checkLength(title, 40) || title.isBlank()) {
					    		System.out.println("You entered a invalid title.");
					    		System.out.print("What is the Title of the Movie (Please use abbreviation if longer than 40 characters)? ");
					    		title = input.nextLine();
					    	}
							
							System.out.println("Enter a genre for the movie: ");
							String genre = input.nextLine();
							while (CSE3241IOUtil.checkLength(genre, 15) || !genre.isBlank()) {
					    		System.out.println("You entered too long of a Genre.");
					    		System.out.print("What is the Genre of the Movie (Please use abbreviation if longer than 15 characters)? ");
					    		genre = input.nextLine();
					    	}
							
							System.out.println("Enter a duration in HH:MM:SS: ");
							String time = input.nextLine();
							
							System.out.println("Enter a rating for the movie: ");
							String rating = input.nextLine();
							while (!CSE3241IOUtil.checkDoubleFormatting(rating)
				                        && !rating.isBlank()) {
				                    System.out.println("You entered an invalid rating.");
				                    System.out.println(
				                            "What is the Rating of the movie (enter in #.# or ##.# format between 0.0 and 10.0)? ");
				                    System.out.print(
				                            "Note: if value has more than one value after the . they will be dropped. ");
				                    rating = input.nextLine();
				            }
							
							System.out.println("Enter a release date in MM/DD/YYYY");
							String release = input.nextLine();
							while (!CSE3241IOUtil.checkDateFormat(release)
			                        && !release.isBlank()) {
			                    System.out.println("You entered an invalid release date.");
			                    System.out.println(
			                            "What is the release date of the movie (enter in mm/dd/year format)? ");
			                    release = input.nextLine();
			                }
							
							
							if(CSE3241IOUtil.checkForNull(genre)) {
								movieIn.add(null);
					    	}
					    	else {
					    		movieIn.add(genre);
					    	}
							
							if(CSE3241IOUtil.checkForNull(time)) {
								movieIn.add(null);
					    	}
					    	else {
					    		movieIn.add(time);
					    	}
							
							movieIn.add(title);
							
							if(CSE3241IOUtil.checkForNull(rating)) {
								movieIn.add(null);
					    	}
					    	else {
					    		movieIn.add(rating);
					    	}
							
							if(CSE3241IOUtil.checkForNull(release)) {
								movieIn.add(null);
					    	}
					    	else {
					    		movieIn.add(release);
					    	}
							
							movieIn.add(quantity);
							movieIn.add(quantity);
							
							PreparedStatement ps3 = CSE3241SQLUtil.setUpPS(conn, moveOrderIntoInventory, movieIn);
							CSE3241SQLUtil.sqlQuery(ps3, false);
							System.out.println("Insert into MOVIE table successful");
						}
						else {
							int val = 0;
							val = movie.getInt("Digital_Copies_Available");
							int totalValue = val + quantity;
							ArrayList<Object> toUpdate3 = new ArrayList<Object>();
							toUpdate3.add(totalValue);
							toUpdate3.add(workID);

							PreparedStatement updated = CSE3241SQLUtil.setUpPS(conn, updateMovie, toUpdate3);
							CSE3241SQLUtil.sqlQuery(updated, false);
							System.out.println("Update successful");
						}
					}
                	catch (SQLException e2) {
						e2.printStackTrace();
					}
                	break;
			default:
				// IF the user does not select a or b, then give an error
				System.out.println("Invalid option selected. Please try again with a valid selection.");
				break;
            }
		}
	}
	
	/**
	 * User selected d. Edit records as their option from Main Menu
	 */
	public static void editRecordsOption(Connection conn, Scanner input) {
		ArrayList<Object> inputs = new ArrayList<Object>();
	    System.out.println("Displaying all the artists in the database");
	    String simpleArtists = "SELECT Artist_ID, Stage_Name, No_Members FROM Artist;";
	    PreparedStatement artiststatement = CSE3241SQLUtil.setUpPS(conn, simpleArtists, inputs);
	    CSE3241SQLUtil.sqlQuerySearchAndPrint(artiststatement);

        System.out.println(
                "\n\nEnter the desired Artist ID you would like to edit. (15 numbers): ");
        String artistID = input.nextLine();
        while (!CSE3241IOUtil.validWorkOrArtistID(artistID)) {
            System.out.println("Error: entered Artist ID was not valid.");
            System.out.println(
                    "Enter the desired Artist ID you would like to edit. (15 numbers): ");
            artistID = input.nextLine();
        }
        
        System.out.println("Enter the new number of members for this artist: ");
        String numMem = input.nextLine();
        while (!CSE3241IOUtil.validNumber(numMem)) {
            System.out.println("Error, please enter a number in numerical form for the number of members: ");
            numMem = input.nextLine();
        }
        
        System.out.println("Enter the new stage name for the artist that is under 30 characters: ");
        String artistName = input.nextLine();
        while (!CSE3241IOUtil.validStageName(artistName)) {
            System.out.println("Error, the artist stage name must contain a character and be less than 30 characters.");
            System.out.println("Please enter a new artist stage name.");
            artistName = input.nextLine();
        }
        
        inputs.add(artistName);
        inputs.add(Integer.parseInt(numMem));
        inputs.add(artistID);
        String SQLUpdate = "UPDATE Artist SET Stage_Name = ?,No_Members = ? WHERE Artist_ID = ?;";
        PreparedStatement artistEdit = CSE3241SQLUtil.setUpPS(conn, SQLUpdate, inputs);
        CSE3241SQLUtil.sqlQuery(artistEdit, false);
        System.out.println("====================== New Artist Records =======================");
        CSE3241SQLUtil.sqlQuerySearchAndPrint(artiststatement);
	}
	
	/**
	 * User selected e. Useful reports as their option from Main Menu
	 */
	public static void usefulReportsOption(Connection conn, Scanner input) {
		ArrayList<Object> inputs = new ArrayList<Object>();
		System.out.println("Which option would you like to choose?");
		System.out.println("a. Tracks by ARTIST released before YEAR");
		System.out.println("b. Number of albums checked out by a single patron");
		System.out.println("c. Most popular actor in the database");
		System.out.println("d. Most listened to artist in the database");
		System.out.println("e. Patron who has checked out the most videos");
		System.out.println("Any other entry will exit the program");
		char option = optionSelected(input);
		switch(option) {
			case 'a': 
				System.out.println("You chose to search for tracks by artist released before year");
                System.out.print("Enter the artist: ");
                String artist = input.nextLine();
                while (CSE3241IOUtil.checkLength(artist, 30) || artist.isBlank()) {
		    		System.out.println("You entered an invalid Stage_Name.");
		    		System.out.print("What is their Stage_Name (Please use abbreviation if longer than 30 characters)? ");
		    		artist = input.nextLine();
		    	}
                inputs.add(artist);
                
                System.out.print("Enter the year: ");
                String yearStr = input.nextLine();
                java.sql.Date sqlYear = CSE3241SQLUtil.strToDate("yyyy", yearStr);
                inputs.add(sqlYear);
                PreparedStatement tracksByArtBefYear = CSE3241SQLUtil.setUpPS(conn, tracksByArtistBeforeYear, inputs);
                CSE3241SQLUtil.sqlQuerySearchAndPrint(tracksByArtBefYear);
                break;
			case 'b': 
				System.out.println("You chose to search for number of albums checked out by a single patron");
                System.out.print("Enter the email address of the patron: ");
                String email = input.nextLine();
                while (CSE3241IOUtil.checkLength(email, 40) || email.isBlank()) {
		    		System.out.println("You entered an invalid Stage_Name.");
		    		System.out.print("What is their Stage_Name (Please use abbreviation if longer than 30 characters)? ");
		    		email = input.nextLine();
		    	}
                inputs.add(email);
                
                PreparedStatement albumRentalsByPatron = CSE3241SQLUtil.setUpPS(conn, numAlbumsRentedByPatron, inputs);
                CSE3241SQLUtil.sqlQuerySearchAndPrint(albumRentalsByPatron);
                break;
			case 'c': 
				System.out.println("You chose to search for most popular actor in the database");
				PreparedStatement mostPopAct = CSE3241SQLUtil.setUpPS(conn, mostPopularActor, inputs);
                CSE3241SQLUtil.sqlQuerySearchAndPrint(mostPopAct);
				break;
			case 'd': 
				System.out.println("You chose to search for most listened to artist in the database");
				PreparedStatement mostPopArt = CSE3241SQLUtil.setUpPS(conn, mostPopularArtist, inputs);
                CSE3241SQLUtil.sqlQuerySearchAndPrint(mostPopArt);
				break;
			case 'e': 
				System.out.println("You chose to search for the person who has checked out the most videos");
                PreparedStatement patMostVid = CSE3241SQLUtil.setUpPS(conn, patronMostVideos, inputs);
                CSE3241SQLUtil.sqlQuerySearchAndPrint(patMostVid);
                break;
			default:
				System.out.println("Invalid choice. Process restarting. ");
				break;
		}
	}

    public static void main(String[] args) {
    	// Set up the database
    	System.out.println("This is a new run");
    	Connection conn = CSE3241SQLUtil.initializeDB(DATABASE);
    	
    	// While the user still wants to do something, continue showing the main menu
    	boolean exitFlag = false;
    	Scanner input = new Scanner(System.in);
    	while(!exitFlag) {
    		printMenuOptions();
    		exitFlag = checkInputContinue(conn, input);
    		System.out.println();
    	}
    	
    	input.close();
    	//NEED TO CLOSE OUT THE DB CONNECTION - look in the slides for this
    }
}
