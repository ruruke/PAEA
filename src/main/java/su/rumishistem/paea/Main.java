package su.rumishistem.paea;

import java.io.IOException;
import java.sql.SQLException;
import su.rumishistem.paea.Tool.SQL;

public class Main {
	public static JobSystem js;

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		SQL.init();
		js = new JobSystem();

		if (args.length == 0) {
			print_help();
			return;
		}


		//引数を
		switch (args[0]) {
			case "check":
				new CheckAccount().check();
				break;

			case "add":
				new AddAccount().add();
				return;

			default:
				System.out.println("その実行ﾓｰﾄﾞはありません。");
				System.out.println("");
				print_help();
				return;
		}

		js.start();
	}

	private static void print_help() {
		System.out.println("---------< PAEA >---------");
		System.out.println("check      -> 登録されているアカウントをチェックします");
		System.out.println("add        -> アカウントを追加します");
		System.out.println("block      -> ブロックします");
		System.out.println("unblock    -> ブロック解除します");
	}
}
