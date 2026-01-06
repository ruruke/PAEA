package su.rumishistem.paea;

import static su.rumishistem.rumi_java_lib.LOG_PRINT.Main.LOG;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import su.rumishistem.paea.Tool.SQL;
import su.rumishistem.paea.Tool.VerifyToken;
import su.rumishistem.paea.Type.Software;
import su.rumishistem.rumi_java_lib.LOG_PRINT.LOG_TYPE;

public class CheckAccount {
	public void check() throws SQLException, IOException {
		List<HashMap<String, Object>> account_list = SQL.run("SELECT * FROM `ACCOUNT`;", new Object[] {});
		//int max = account_list.size();
		int ok = 0;
		int error = 0;

		for (HashMap<String, Object> account:account_list) {
			String host = (String)account.get("HOST");
			LOG(LOG_TYPE.PROCESS, host + "に照会しています...");

			VerifyToken vt = new VerifyToken((String)account.get("TOKEN"), Software.value_of((String)account.get("SOFTWARE_NAME")), host);
			if (vt.verify()) {
				LOG(LOG_TYPE.PROCESS_END_OK, "");
				LOG(LOG_TYPE.OK, host + "の" + vt.user_name + "("+vt.user_id+")は有効なﾕｰｻﾞｰです。");
				ok += 1;
			} else {
				LOG(LOG_TYPE.PROCESS_END_FAILED, "");
				error += 1;
			}
		}

		System.out.println("\n\n");
		System.out.println("\u001B[42m成功：" + ok + "\u001B[0m");
		System.out.println("\u001B[41m失敗：" + error + "\u001B[0m");
	}
}
