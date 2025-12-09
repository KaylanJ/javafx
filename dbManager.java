import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import java.text.NumberFormat;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.sql.Date;
import java.time.format.DateTimeParseException;


public class dbManager extends Application{
	
	static final String DB_URL = "jdbc:mariadb://localhost:3306/lego"; //  URL
    static final String USER = "########"; //  MariaDB username
    static final String PASS = "#####"; //  MariaDB password
	
	private Connection conn;

    private TextField set_id_field;
    private TextField year_got_field;
    private TextField qty_field;
    private TextField have_box_field;
    private TextField complete_field;
	
	private TextField set_name_field;
	private TextField date_released_field;
	private TextField piece_count_field;
	private TextField genre_ID_field;
	private TextField group_num_field;
	
	private TextField genre_name_field;
	private TextField date_introduced_field; 
	
	private Button addDbButton;
	private Button addSetButton;
	private Button addGroupButton;
	private Button addFigureButton;
	private Button addThemeButton;
	
	
	public void start (Stage primaryStage){
			
			primaryStage.setTitle("Manage Database:");
			
			try {
            
				Class.forName("org.mariadb.jdbc.Driver");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				System.out.println("Database connection established successfully!");
			}
			catch (ClassNotFoundException e) {
				showAlert("Error", "MariaDB JDBC Driver not found. Please ensure it's in your classpath.");
				e.printStackTrace();
				return; // Exit if connection fails
			} 
			catch (SQLException e) {
				showAlert("Database Connection Error", "Failed to connect to the database: " + e.getMessage());
				System.err.println("SQLState: " + e.getSQLState());
				System.err.println("Error Code: " + e.getErrorCode());
				e.printStackTrace();
					return; // Exit if connection fails
			}
			
			addDbButton = new Button("Add to DB");
			addDbButton.setOnAction(this::addDbRequest);
			
			addSetButton = new Button("Add new set");
			addSetButton.setOnAction(this::addSetRequest);
			
			addGroupButton = new Button("Add figure group");
			addGroupButton.setOnAction(this::addGroupRequest);
			
			addFigureButton = new Button("Add new figures");
			addFigureButton.setOnAction(this::addFigureRequest);
			
			addThemeButton = new Button("Add new theme");
			addThemeButton.setOnAction(this::addThemeRequest);
			
			FlowPane mainPane = new FlowPane(addDbButton, addSetButton, addGroupButton, addFigureButton, addThemeButton);
			
			
			mainPane.setAlignment(Pos.CENTER);
			mainPane.setHgap(10);
		
			mainPane.setVgap(20);
			Scene mainScene = new Scene(mainPane, 320, 220);
		
			primaryStage.setScene(mainScene);
			primaryStage.show();
			
	}
	
	@Override
    public void stop() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
            System.out.println("Database connection closed.");
        }
        super.stop();
    }
	
	public void addDbRequest(ActionEvent event){
			
			Label set_id_LABEL = new Label("Set ID:");
		
			set_id_field = new TextField();
			set_id_field.setPrefWidth(180);
			
			
			Label year_got_LABEL = new Label("Year Acquired:");
		
			year_got_field = new TextField();
			year_got_field.setPrefWidth(180);
			
			
			Label qty_LABEL = new Label("Quantity:");
		
			qty_field = new TextField();
			qty_field.setPrefWidth(180);
			
			
			Label have_box_LABEL = new Label("Have box? (True/False):");
		
			have_box_field = new TextField();
			have_box_field.setPrefWidth(180);
			
			
			Label complete_LABEL = new Label("Complete? (True/False):");
		
			complete_field = new TextField();
			complete_field.setPrefWidth(180);
			
			Button dbEnterButton = new Button("Enter");
			dbEnterButton.setOnAction(this::dbEnterRequest);
			
			
			FlowPane dbPane = new FlowPane(set_id_LABEL, set_id_field, year_got_LABEL, year_got_field, qty_LABEL, qty_field, have_box_LABEL, have_box_field, complete_LABEL, complete_field, dbEnterButton);
			
			dbPane.setAlignment(Pos.CENTER);
			dbPane.setHgap(10);
		
			dbPane.setVgap(20);
			
			Scene dbScene = new Scene(dbPane, 300, 250);
			Stage dbStage = new Stage();
			dbStage.setScene(dbScene);
			dbStage.show();
			
		
	}
	
	public void dbEnterRequest(ActionEvent event){
					String setID = set_id_field.getText();
					String setYear = year_got_field.getText();
					String setQty = qty_field.getText();
					String setBox = have_box_field.getText();
					String setComplete = complete_field.getText();
					
					
					int qtyOfSet;
					
					try {
						qtyOfSet = Integer.parseInt(setQty);
					} 
					catch (NumberFormatException e) {
						showAlert("Input Error", "Quantity must be a valid number.");
						return;
					}
					
					boolean gotBox;
					if( setBox.equalsIgnoreCase("true")){
						gotBox = true;
					}
					else if(setBox.equalsIgnoreCase("false")){
						gotBox = false;
					}
					else{
						showAlert("Input Error", "Please enter 'true', 'false' for active status.");
						return;
					}
					
					boolean gotComplete;
					if(setComplete.equalsIgnoreCase("true")){
						gotComplete = true;
					}
					else if(setComplete.equalsIgnoreCase("false")){
						gotComplete = false;
					}
					else{
						showAlert("Input Error", "Please enter 'true', 'false' for active status.");
						return;
					}
					
					java.sql.Date sqlYearAcquired; 
					if (setYear.isEmpty()) {
						showAlert("Input Error", "Year Acquired cannot be empty.");
						return;
					}
					
					try {
						LocalDate localDate = LocalDate.parse(setYear); // Expects YYYY-MM-DD
						sqlYearAcquired = java.sql.Date.valueOf(localDate); // Convert LocalDate to java.sql.Date
					} 
					catch (DateTimeParseException e) {
						showAlert("Input Error", "Please enter 'Year Acquired' in YYYY-MM-DD format.");
						return;
					}
						
					String query = "insert into collection (set_id, year_acquired, set_qty, have_box, is_complete) values (?, ?, ?, ?, ?)";
					
					try (PreparedStatement pstmt = conn.prepareStatement(query)) {
						pstmt.setString(1, setID);          
						pstmt.setDate(2, sqlYearAcquired);        
						pstmt.setInt(3, qtyOfSet);      
						pstmt.setBoolean(4, gotBox); 
						pstmt.setBoolean(5, gotComplete);


						int rowsAffected = pstmt.executeUpdate();
						if (rowsAffected > 0) {
							showAlert("Success", "Data saved successfully!");
							// Clear fields after successful save
							set_id_field.clear();
							year_got_field.clear();
							qty_field.clear();
							have_box_field.clear();
							complete_field.clear();
						} 
						else {
							showAlert("Error", "Failed to save data. No rows affected.");
						}

					} 
					
					catch (SQLException e) {
						showAlert("Database Error", "An error occurred while saving data: " + e.getMessage());
						e.printStackTrace();
					}
					
					
	}
	
	private void addSetRequest(ActionEvent event){
			/*
			
			private TextField set_name_field;
	private TextField date_released_field;
	private TextField piece_count_field;
	private TextField genre_ID_field;
	private TextField group_num_field;
	
		*/
		
		Label set_id_LABEL2 = new Label("set ID:");
		
		set_id_field = new TextField();
		set_id_field.setPrefWidth(180);
		
		
		Label set_name_LABEL = new Label("Set name:");
		
		set_name_field = new TextField();
		set_name_field.setPrefWidth(180);
		
		Label date_released_LABEL = new Label("Date released:");
		
		date_released_field = new TextField();
		date_released_field.setPrefWidth(180);
		
		
		Label piece_count_LABEL = new Label("Piece count:");
		
		piece_count_field = new TextField();
		piece_count_field.setPrefWidth(180);
		
		
		Label genre_ID_LABEL = new Label("Genre ID:");
		
		genre_ID_field = new TextField();
		genre_ID_field.setPrefWidth(180);
		
		
		Label group_num_LABEL = new Label("Group num:");
		
		group_num_field = new TextField();
		group_num_field.setPrefWidth(180);
		
		
		Button setEnterButton = new Button("Enter");
		setEnterButton.setOnAction(this::setEnterRequest);
			
			
		FlowPane setPane = new FlowPane(set_id_LABEL2, set_id_field, set_name_LABEL, set_name_field, date_released_LABEL, date_released_field, piece_count_LABEL, piece_count_field, genre_ID_LABEL, genre_ID_field, group_num_LABEL, group_num_field, setEnterButton);
			
		setPane.setAlignment(Pos.CENTER);
		setPane.setHgap(10);
		
		setPane.setVgap(20);
			
		Scene setScene = new Scene(setPane, 300, 250);
		Stage setStage = new Stage();
		setStage.setScene(setScene);
		setStage.show();
		
	}
		
	public void setEnterRequest(ActionEvent event){
		
					String setID = set_id_field.getText();
					String setName = set_name_field.getText();
					String setReleased = date_released_field.getText();
					String setPieces = piece_count_field.getText();
					String setGenre = genre_ID_field.getText();
					String setGroup = group_num_field.getText();
					
					
					int pieceCount;
					int GenreOfSet;
					int GroupOfSet;
					
					try {
						pieceCount = Integer.parseInt(setPieces);
					} 
					catch (NumberFormatException e) {
						showAlert("Input Error", "Piece count must be a valid number.");
						return;
					}
					
					try{
						GenreOfSet = Integer.parseInt(setGenre);
					}
					catch(NumberFormatException x){
						showAlert("Input Error", "Set genre must be a valid number.");
						return;
					}
					
					try{
						GroupOfSet = Integer.parseInt(setGroup);
					}
					catch(NumberFormatException j){
						showAlert("Input Error", "Set group must be a valid number.");
						return;
					}
					
					
					
					java.sql.Date sqlYearReleased; 
					if (setReleased.isEmpty()) {
						showAlert("Input Error", "Year Released cannot be empty.");
						return;
					}
					
					try {
						LocalDate localDate2 = LocalDate.parse(setReleased); // Expects YYYY-MM-DD
						sqlYearReleased = java.sql.Date.valueOf(localDate2); // Convert LocalDate to java.sql.Date
					} 
					catch (DateTimeParseException e) {
						showAlert("Input Error", "Please enter 'Year Released' in YYYY-MM-DD format.");
						return;
					}
						
					String query2 = "insert into sets (set_id, set_name, date_released, piece_count, genre_ID, group_num) values (?, ?, ?, ?, ?, ?)";
					
					try (PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
						pstmt2.setString(1, setID);          
						pstmt2.setString(2, setName);        
						pstmt2.setDate(3, sqlYearReleased);      
						pstmt2.setInt(4, pieceCount); 
						pstmt2.setInt(5, GenreOfSet);
						pstmt2.setInt(6, GroupOfSet);

/*
String setID = set_id_field.getText();
					String setName = set_name_field.getText();
					String setReleased = date_released_field.getText();
					String setPieces = piece_count_field.getText();
					String setGenre = genre_ID_field.getText();
					String setGroup = group_num_field.getText();
					
					*/

						int rowsAffected = pstmt2.executeUpdate();
						if (rowsAffected > 0) {
							showAlert("Success", "Data saved successfully!");
							// Clear fields after successful save
							set_id_field.clear();
							set_name_field.clear();
							date_released_field.clear();
							piece_count_field.clear();
							genre_ID_field.clear();
							group_num_field.clear();
						} 
						else {
							showAlert("Error", "Failed to save data. No rows affected.");
						}

					} 
					
					catch (SQLException e) {
						showAlert("Database Error", "An error occurred while saving data: " + e.getMessage());
						e.printStackTrace();
					}
					
					
	}
	
	private void addThemeRequest(ActionEvent event){
			/*
			
			private TextField genre_name_field;
	private TextField date_introduced_field;
	private TextField genre_ID_field;
	
	
		*/
		
		Label genre_ID_LABEL2 = new Label("Genre ID:");
		
		genre_ID_field = new TextField();
		genre_ID_field.setPrefWidth(180);
		
		
		Label genre_name_LABEL = new Label("Genre name:");
		
		genre_name_field = new TextField();
		genre_name_field.setPrefWidth(180);
		
		
		Label date_introduced_LABEL = new Label("Date introduced:");
		
		date_introduced_field = new TextField();
		date_introduced_field.setPrefWidth(180);
		
		
		
		Button themeEnterButton = new Button("Enter");
		themeEnterButton.setOnAction(this::themeEnterRequest);
			
			
		FlowPane themePane = new FlowPane(genre_name_LABEL, genre_name_field, genre_ID_LABEL2, genre_ID_field, date_introduced_LABEL, date_introduced_field, themeEnterButton);
			
		themePane.setAlignment(Pos.CENTER);
		themePane.setHgap(10);
		
		themePane.setVgap(20);
			
		Scene themeScene = new Scene(themePane, 300, 250);
		Stage themeStage = new Stage();
		themeStage.setScene(themeScene);
		themeStage.show();
		
	}
	
	
	private void themeEnterRequest(ActionEvent event){
		
		String genreID = genre_ID_field.getText();
		String genreName = genre_name_field.getText();
		String dateIntro = date_introduced_field.getText();
		
		int idOfGenre;
		try{
			idOfGenre = Integer.parseInt(genreID);
		}
		catch (NumberFormatException e) {
			showAlert("Input Error", "Genre ID must be a valid number.");
			return;
		}
		
		
		java.sql.Date sqlYearIntroduced;
		
		if (dateIntro.isEmpty()) {
			showAlert("Input Error", "Year Introduced cannot be empty.");
			return;
		}
		
		try{
			LocalDate localDate = LocalDate.parse(dateIntro);
			sqlYearIntroduced = java.sql.Date.valueOf(localDate); // Convert LocalDate to java.sql.Date
		} 
		catch (DateTimeParseException e) {
			showAlert("Input Error", "Please enter 'Year Introduced' in YYYY-MM-DD format.");
			return;
		}
		
		String query = "insert into genre (genre_ID, genre_name, date_introduced) values (?, ?, ?)";
		
		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, idOfGenre);          
			pstmt.setString(2, genre_name);        
			pstmt.setDate(3, sqlYearIntroduced);


			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
			showAlert("Success", "Data saved successfully!");
			// Clear fields after successful save
			genre_ID_field.clear();
			genre_name_field.clear();
			date_introduced_field.clear();
			
			} 
			else {
				showAlert("Error", "Failed to save data. No rows affected.");
			}
		}
		catch (SQLException e) {
			showAlert("Database Error", "An error occurred while saving data: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	private void addFigureRequest(ActionEvent event) {
		Label mini_num_LABEL = new Label("Mini Number:");
		mini_num_field = new TextField();
		mini_num_field.setPrefWidth(180);

		Label mini_name_LABEL = new Label("Mini Name:");
		mini_name_field = new TextField();
		mini_name_field.setPrefWidth(180);

		Label category_LABEL = new Label("Category:");
		category_field = new TextField();
		category_field.setPrefWidth(180);

		Label release_date_LABEL = new Label("Release Date (YYYY-MM-DD):");
		release_date_field = new TextField();
		release_date_field.setPrefWidth(180);

		Button figureEnterButton = new Button("Enter");
		figureEnterButton.setOnAction(this::figureEnterRequest);

		FlowPane figurePane = new FlowPane(
			mini_num_LABEL, mini_num_field,
			mini_name_LABEL, mini_name_field,
			category_LABEL, category_field,
			release_date_LABEL, release_date_field,
			figureEnterButton
		);
		figurePane.setAlignment(Pos.CENTER);
		figurePane.setHgap(10);
		figurePane.setVgap(20);

		Scene figureScene = new Scene(figurePane, 300, 250);
		Stage figureStage = new Stage();
		figureStage.setScene(figureScene);
		figureStage.show();
	}

	private void figureEnterRequest(ActionEvent event) {
		String miniNum = mini_num_field.getText();
		String miniName = mini_name_field.getText();
		String category = category_field.getText();
		String releaseDate = release_date_field.getText();

		if (miniNum.isEmpty() || miniName.isEmpty() || category.isEmpty() || releaseDate.isEmpty()) {
			showAlert("Input Error", "All fields must be filled.");
			return;
		}

		java.sql.Date sqlReleaseDate;
		try {
			LocalDate localDate = LocalDate.parse(releaseDate); // Expects YYYY-MM-DD
			sqlReleaseDate = java.sql.Date.valueOf(localDate);
		} catch (DateTimeParseException e) {
			showAlert("Input Error", "Please enter 'Release Date' in YYYY-MM-DD format.");
			return;
		}

		String query = "insert into figures (mini_num, mini_name, category, release_date) values (?, ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, miniNum);
			pstmt.setString(2, miniName);
			pstmt.setString(3, category);
			pstmt.setDate(4, sqlReleaseDate);
	
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				showAlert("Success", "Figure added successfully!");
				mini_num_field.clear();
				mini_name_field.clear();
				category_field.clear();
				release_date_field.clear();
			} else {
				showAlert("Error", "Failed to add figure. No rows affected.");
			}
		} catch (SQLException e) {
			showAlert("Database Error", "An error occurred while saving figure: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void addGroupRequest(ActionEvent event) {
		Label group_num_LABEL = new Label("Group Number:");
		figgroup_group_num_field = new TextField();
		figgroup_group_num_field.setPrefWidth(180);

		Label mini_num_LABEL = new Label("Mini Number:");
		figgroup_mini_num_field = new TextField();
		figgroup_mini_num_field.setPrefWidth(180);

		Label qty_LABEL = new Label("Quantity:");
		figgroup_qty_field = new TextField();
		figgroup_qty_field.setPrefWidth(180);

		Button groupEnterButton = new Button("Enter");
		groupEnterButton.setOnAction(this::groupEnterRequest);

		FlowPane groupPane = new FlowPane(
			group_num_LABEL, figgroup_group_num_field,
			mini_num_LABEL, figgroup_mini_num_field,
			qty_LABEL, figgroup_qty_field,
			groupEnterButton
		);
		groupPane.setAlignment(Pos.CENTER);
		groupPane.setHgap(10);
		groupPane.setVgap(20);
	
		Scene groupScene = new Scene(groupPane, 300, 200);
		Stage groupStage = new Stage();
		groupStage.setScene(groupScene);
		groupStage.show();
	}

	private void groupEnterRequest(ActionEvent event) {
		String groupNumStr = figgroup_group_num_field.getText();
		String miniNum = figgroup_mini_num_field.getText();
		String qtyStr = figgroup_qty_field.getText();
	
		int groupNum;
		int qty;

		try {
			groupNum = Integer.parseInt(groupNumStr);
		} catch (NumberFormatException e) {
			showAlert("Input Error", "Group number must be a valid integer.");
			return;
		}
	
		if (miniNum.isEmpty()) {
			showAlert("Input Error", "Mini number must not be empty.");
			return;
		}
	
		try {
			qty = Integer.parseInt(qtyStr);
		} catch (NumberFormatException e) {
			showAlert("Input Error", "Quantity must be a valid integer.");
			return;
		}

		String query = "insert into figgroup (group_num, mini_num, qty) values (?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, groupNum);
			pstmt.setString(2, miniNum);
			pstmt.setInt(3, qty);
	
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				showAlert("Success", "Figure group added successfully!");
				figgroup_group_num_field.clear();
				figgroup_mini_num_field.clear();
				figgroup_qty_field.clear();
			} else {
				showAlert("Error", "Failed to add figure group. No rows affected.");
			}
		} catch (SQLException e) {
			showAlert("Database Error", "An error occurred while saving figure group: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Default to INFO, use ERROR for errors
        if (title.contains("Error")) {
            alert.setAlertType(Alert.AlertType.ERROR);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    
					
					

}			
