package NumberGuess;

import java.util.Random;
import java.util.Scanner;

public class GuessTheNumber {
    private static final int MAX_ATTEMPTS = 10; // Maximum attempts per round
    private static int totalScore = 0;           // Total score across rounds

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Prompt the user for the maximum number of rounds
        System.out.print("Enter the maximum number of rounds you want to play: ");
        int maxRounds = scanner.nextInt();
        
        for (int round = 1; round <= maxRounds; round++) {
            System.out.println("Round " + round + " of " + maxRounds);
            int numberToGuess = generateRandomNumber(1, 100);
            boolean hasGuessedCorrectly = false;
            int attempts = 0;

            while (attempts < MAX_ATTEMPTS) {
                System.out.print("Enter your guess (1-100): ");
                int userGuess = scanner.nextInt();
                attempts++;

                if (userGuess < 1 || userGuess > 100) {
                    System.out.println("Please enter a number between 1 and 100.");
                    attempts--; // Don't count this attempt
                    continue; // Skip to the next iteration
                }

                // Provide hints
                provideHints(userGuess, numberToGuess, attempts);

                if (userGuess == numberToGuess) {
                    hasGuessedCorrectly = true;
                    System.out.println("Congratulations! You guessed the number in " + attempts + " attempts.");
                    // Calculate score based on attempts
                    int score = calculateScore(attempts);
                    totalScore += score;
                    System.out.println("You earned " + score + " points this round.");
                    break;
                } else if (userGuess < numberToGuess) {
                    System.out.println("Higher! Try again.");
                } else {
                    System.out.println("Lower! Try again.");
                }
            }

            if (!hasGuessedCorrectly) {
                System.out.println("Sorry! You've used all attempts. The number was: " + numberToGuess);
            }
            System.out.println("Total Score: " + totalScore + "\n");
        }

        System.out.println("Game Over! Your final score is: " + totalScore);
        scanner.close();
    }

    private static int generateRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min; // Generate random number in range
    }

    private static int calculateScore(int attempts) {
        // Score is based on attempts; fewer attempts result in a higher score
        return Math.max(0, MAX_ATTEMPTS - attempts + 1); // Points for remaining attempts
    }

    private static void provideHints(int guess, int target, int attempts) {
        if (attempts > 1) {
            if (Math.abs(guess - target) <= 10) {
                System.out.println("Hint: You're very close!");
            } else if (Math.abs(guess - target) <= 20) {
                System.out.println("Hint: You're getting warmer!");
            } else {
                System.out.println("Hint: You're quite far from the target.");
            }
        }
    }
}
