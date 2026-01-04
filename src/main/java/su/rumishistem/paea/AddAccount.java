package su.rumishistem.paea;

import static su.rumishistem.rumi_java_lib.LOG_PRINT.Main.LOG;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import su.rumishistem.paea.Tool.InputChecker;
import su.rumishistem.paea.Tool.ListenUserInput;
import su.rumishistem.paea.Tool.SQL;
import su.rumishistem.paea.Tool.VerifyToken;
import su.rumishistem.paea.Type.Software;
import su.rumishistem.rumi_java_lib.Ajax.Ajax;
import su.rumishistem.rumi_java_lib.Ajax.AjaxResult;
import su.rumishistem.rumi_java_lib.LOG_PRINT.LOG_TYPE;

public class AddAccount {
	public void add(Software software) throws IOException, SQLException {
		//ホスト名
		System.out.print("ｲﾝｽﾀﾝｽのﾎｽﾄ名は？ > ");
		String host = ListenUserInput.listen();
		System.out.println("");

		if (InputChecker.is_host(host) == false) {
			System.out.println("不正な入力です。");
			add(software);
		}

		if (software == Software.Misskey) {
			//MiAuthかトークンか
			System.out.println("MiAuth経由での認証か、もしくはﾄｰｸﾝで認証を行います。");
			System.out.println("ﾄｰｸﾝの場合はﾄｰｸﾝを入力し、ｴﾝﾀｰｷｰを押してください。");
			System.out.println("(Shift + Insertでｺﾋﾟｰﾍﾟｰｽﾄを行えます。)");
			System.out.println("MiAuthの場合は何も記入せずにｴﾝﾀｰｷｰを押してください。");

			//MiAuthのURLを生成
			String miauth_id = UUID.randomUUID().toString();
			String miauth_url = "https://" + host + "/miauth/" + miauth_id + "?name=" + URLEncoder.encode("PAEA") + "&permission=" + URLEncoder.encode("read:account,read:blocks,write:blocks,read:mutes,write:mutes");
			System.out.println("MiAuth： " + miauth_url);

			String token = ListenUserInput.listen();

			//MiAuth
			if (token == "") {
				LOG(LOG_TYPE.PROCESS, host + "と通信しています...");

				Ajax ajax = new Ajax("https://" + host + "/api/miauth/" + miauth_id + "/check");
				ajax.set_header("Content-Type", "application/json");
				AjaxResult result = ajax.POST("{}".getBytes());
				if (result.get_code() != 200) {
					LOG(LOG_TYPE.PROCESS_END_FAILED, "");
					System.out.println("ｻｰﾊﾞｰがｴﾗｰを返しました。");
					return;
				}
				LOG(LOG_TYPE.PROCESS_END_OK, "");

				JsonNode body = new ObjectMapper().readTree(result.get_body_as_string());
				token = body.get("token").asText();
			}

			//チェック
			VerifyToken vt = new VerifyToken(token, software, host);
			LOG(LOG_TYPE.PROCESS, host + "に照会しています...///");
			if (vt.verify()) {
				LOG(LOG_TYPE.PROCESS_END_OK, "");
				LOG(LOG_TYPE.OK, host + "の" + vt.user_name + "("+vt.user_id+")としてログインしました。");

				SQL.up_run("INSERT INTO `ACCOUNT` (`ID`, `TOKEN`, `HOST`, `SOFTWARE_NAME`, `SOFTWARE_VERSION`) VALUES (?, ?, ?, ?, ?)", new Object[] {
					UUID.randomUUID().toString(),
					token,
					host,
					Software.Misskey.name().toUpperCase(),
					"13"
				});
			} else {
				LOG(LOG_TYPE.PROCESS_END_FAILED, "");
				System.out.println("失敗しました...");
			}
		} else {
			//TODO: Mastodon
		}
	}
}
