package assignment01;
public class Grader {
	public static void main(String[] args) {
		int totalScore = 0;
		Assignment sub = new Assignment();
		if(sub.returnFour() == 4) {
			totalScore++;
		} else {
			System.out.println("You failed return four");
		}
		if(sub.returnThree() == 3) {
			totalScore++;
		} else {
			System.out.println("You failed return three");
		}
		if(sub.returnTwo() == 2) {
			totalScore++;
		} else {
			System.out.println("You failed return two");
		}
		if(sub.returnOne() == 1) {
			totalScore++;
		} else {
			System.out.println("You failed return one");
		}
		System.out.println("Your final score: \t\t\t" + totalScore + "/4");
	}
}
