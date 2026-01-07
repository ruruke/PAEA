package su.rumishistem.paea.Tool;

import java.util.regex.Pattern;

public class InputChecker {
	/**
	 * ホスト名の正規表現 (RFC 1123準拠)
	 * - 各ラベルは1〜63文字
	 * - 全体で253文字以内
	 * - 英数字で始まり英数字で終わる（途中にハイフン可）
	 */
	private static final Pattern HOST_PATTERN = Pattern.compile(
		"^" +
		"(?=.{1,253}$)" +                                  // 全体の長さ制限
		"(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)" + // 先頭ラベル
		"(?:\\.(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?))*" + // 続くラベル
		"$"
	);

	public static boolean is_host(String input) {
		if (input == null) return false;

		return HOST_PATTERN.matcher(input).matches();
	}
}
