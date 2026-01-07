package su.rumishistem.paea.Tool;

import java.util.Scanner;

public class ListenUserInput {
	public static String listen() {
		//↓閉じてはいけない
		Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
	}
}
