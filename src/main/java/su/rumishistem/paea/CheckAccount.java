package su.rumishistem.paea;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import su.rumishistem.paea.Tool.SQL;
import su.rumishistem.paea.Tool.VerifyToken;
import su.rumishistem.paea.Type.Job;
import su.rumishistem.paea.Type.Software;
import su.rumishistem.paea.Type.ThrowRunnable;

public class CheckAccount {
	public void check() throws SQLException, IOException {
		List<HashMap<String, Object>> account_list = SQL.run("SELECT * FROM `ACCOUNT`;", new Object[] {});
		JobSystem js = new JobSystem();

		for (HashMap<String, Object> account:account_list) {
			Job job = new Job();
			String host = (String)account.get("HOST");

			job.set_task(new ThrowRunnable() {
				@Override
				public void run() throws Exception {
					job.set_message(host + "に照会しています...");

					try {
						VerifyToken vt = new VerifyToken((String)account.get("TOKEN"), Software.value_of((String)account.get("SOFTWARE_NAME")), host);
						if (vt.verify()) {
							job.set_message(host + "の" + vt.user_name + "("+vt.user_id+")は有効なﾕｰｻﾞｰです。");
						} else {
							throw new RuntimeException(host + "は無効なﾕｰｻﾞｰです。");
						}
					} catch (ConnectException ex) {
						throw new RuntimeException(host + "への接続に失敗しました");
					}
				}
			});

			js.submit(job);
		}

		js.start();
	}
}
