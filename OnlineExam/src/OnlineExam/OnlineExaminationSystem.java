package OnlineExam;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Question {
    String questionText;
    List<String> options;
    int correctOption;

    public Question(String questionText, List<String> options, int correctOption) {
        this.questionText = questionText;
        this.options = options;
        this.correctOption = correctOption;
    }
}

class User {
    String username;
    String password;
    String email;
    String name;

    public User(String username, String password, String email, String name) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
    }
}

public class OnlineExaminationSystem {

    static User loggedInUser;
    static boolean sessionOpen = false;

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/OnlineExamination";
    private static final String USER = "root";
    private static final String PASSWORD = "root"; 
    

    public static void main(String[] args) {
    	initializeQuestions();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("--------ONLINE EXAMINATION--------");
            System.out.println("\n1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("\nEnter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    login(scanner);
                    break;
                case 2:
                    register(scanner);
                    break;
                case 3:
                    System.out.println("Exiting the system.");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please select a valid option!");
            }
        }
    }
    
    public static void initializeQuestions() {
        // Clear existing questions
        clearQuestions();

        // Define the questions and their options
        List<Question> predefinedQuestions = List.of(
            createQuestion("Who invented Java Programming?", List.of("Guido van Rossum", "James Gosling", "Dennis Ritchie", "Bjarne Stroustrup"), 1),
            createQuestion("Which component is used to compile, debug and execute Java programs?", List.of("JRE", "JIT", "JDK", "JVM"), 2),
            createQuestion("Which one of the following is not a Java feature?", List.of("Object-oriented", "Use of pointers", "Portable", "Dynamic and Extensible"), 1),
            createQuestion("What is the extension of Java code files?", List.of(".js", ".txt", ".class", ".java"), 3),
            createQuestion("Which of the following is not an OOPS concept in Java?", List.of("Polymorphism", "Inheritance", "Compilation", "Encapsulation"), 2)
        );

        // Connect to the database and insert the questions
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES (?, ?, ?, ?, ?, ?)")) {

            for (Question question : predefinedQuestions) {
                pstmt.setString(1, question.questionText);
                pstmt.setString(2, question.options.get(0)); // option A
                pstmt.setString(3, question.options.get(1)); // option B
                pstmt.setString(4, question.options.get(2)); // option C
                pstmt.setString(5, question.options.get(3)); // option D
                pstmt.setInt(6, question.correctOption);      // correct answer index
                pstmt.executeUpdate(); // Execute the insert statement
            }

           
        } catch (SQLException e) {
            System.out.println("Error inserting questions: " + e.getMessage());
        }
    }

    private static void clearQuestions() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM questions")) {
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println("Error clearing questions: " + e.getMessage());
        }
    }

    private static Question createQuestion(String questionText, List<String> options, int correctOption) {
        return new Question(questionText, options, correctOption);
    }

    public static void register(Scanner scanner) {
        System.out.print("Enter a Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter a Password: ");
        String password = scanner.nextLine();
        System.out.print("Enter your Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter your Name: ");
        String name = scanner.nextLine();

        // Connect to the database and insert the new user
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username, password, email, name) VALUES (?, ?, ?, ?)")) {

            pstmt.setString(1, username);
            pstmt.setString(2, password); // Note: Consider hashing passwords for security
            pstmt.setString(3, email);
            pstmt.setString(4, name);
            pstmt.executeUpdate();

            System.out.println("Registration successful! You can now log in.");
        } catch (SQLException e) {
            System.out.println("Error during registration: " + e.getMessage());
        }
    }

    public static void login(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // Validate credentials from the database
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                loggedInUser = new User(rs.getString("username"), rs.getString("password"), rs.getString("email"), rs.getString("name"));
                sessionOpen = true;
                startExam(scanner);
            } else {
                System.out.println("Invalid credentials. Please try again!");
            }
        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
        }
    }

    public static void startExam(Scanner scanner) {
        System.out.println("\nWelcome, " + loggedInUser.username + "!");
        long startTime = System.currentTimeMillis();
        List<Question> questions = fetchQuestions();
        int totalQuestions = questions.size();
        int score = 0;

        for (int i = 0; i < totalQuestions; i++) {
            Question question = questions.get(i);
            System.out.println("Question " + (i + 1) + ": " + question.questionText);
            for (int j = 0; j < question.options.size(); j++) {
                System.out.println((j + 1) + ". " + question.options.get(j));
            }

            System.out.print("Select your answer (1-" + question.options.size() + "): ");
            int userChoice = scanner.nextInt();

            if (userChoice == question.correctOption + 1) {
                System.out.println("Correct!");
                score++;
            } else {
                System.out.println("Incorrect! Correct answer: " + question.options.get(question.correctOption));
            }
            // Store the response in the database
            storeResponse(loggedInUser, question, userChoice);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = (endTime - startTime) / 1000; // Time taken in seconds
        System.out.println("Exam completed! Your score: " + score + "/" + totalQuestions + ". Time taken: " + totalTime + " seconds.");
        clearExamResponses();
        sessionOpen = false;
    }

    private static List<Question> fetchQuestions() {
        List<Question> questions = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM questions");
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String questionText = rs.getString("question_text");
                List<String> options = List.of(rs.getString("option_a"), rs.getString("option_b"), rs.getString("option_c"), rs.getString("option_d"));
                int correctOption = rs.getInt("correct_answer");
                questions.add(new Question(questionText, options, correctOption));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching questions: " + e.getMessage());
        }
        return questions;
    }

    private static void storeResponse(User user, Question question, int selectedAnswer) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO exam_responses (user_id, question_id, selected_answer) VALUES (?, ?, ?)")) {

            pstmt.setInt(1, getUserId(user.username)); // Get the user ID from username
            pstmt.setInt(2, getQuestionId(question.questionText)); // Get the question ID from question text
            pstmt.setInt(3, selectedAnswer);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error storing response: " + e.getMessage());
        }
    }

    private static int getUserId(String username) {
        int userId = -1; // Default to -1 for not found
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("SELECT user_id FROM users WHERE username = ?")) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                userId = rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user ID: " + e.getMessage());
        }
        return userId;
    }

    private static int getQuestionId(String questionText) {
        int questionId = -1; // Default to -1 for not found
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("SELECT question_id FROM questions WHERE question_text = ?")) {

            pstmt.setString(1, questionText);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                questionId = rs.getInt("question_id");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching question ID: " + e.getMessage());
        }
        return questionId;
    }
    
    private static void clearExamResponses() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM exam_responses")) {
            pstmt.executeUpdate();
           
        } catch (SQLException e) {
            System.out.println("Error clearing exam responses: " + e.getMessage());
        }
    }
}
