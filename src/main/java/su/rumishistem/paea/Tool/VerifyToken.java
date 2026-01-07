package su.rumishistem.paea.Tool;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import su.rumishistem.paea.Type.Software;
import su.rumishistem.rumi_java_lib.Ajax.Ajax;
import su.rumishistem.rumi_java_lib.Ajax.AjaxResult;

public class VerifyToken {
	private final String token;
	private final Software software;
	private final String host;

	public boolean status = false;
	public String user_id;
	public String user_name;

	public VerifyToken(String token, Software software, String host) {
		this.token = token;
		this.software = software;
		this.host = host;
	}

	public boolean verify() throws IOException {
		if (software == Software.Misskey) {
			Ajax ajax = new Ajax("https://"+host+"/api/i");
			ajax.set_header("Content-Type", "application/json");
			AjaxResult result = ajax.POST(("{\"i\": \""+token+"\"}").getBytes());
			if (result.get_code() != 200) {
				return false;
			}

			JsonNode user = new ObjectMapper().readTree(result.get_body_as_string());
			user_id = user.get("id").asText();
			user_name = user.get("username").asText();

			status = true;
			return true;
		} else if (software == Software.Mastodon) {
			Ajax ajax = new Ajax("https://"+host+"/api/v1/accounts/verify_credentials");
			ajax.set_header("Authorization", "Bearer "+token);
			AjaxResult result = ajax.GET();
			if (result.get_code() != 200) {
				return false;
			}

			JsonNode user = new ObjectMapper().readTree(result.get_body_as_string());
			user_id = user.get("id").asText();
			user_name = user.get("username").asText();

			status = true;
			return true;
		} else {
			throw new UnsupportedOperationException("");
		}
	}
}
