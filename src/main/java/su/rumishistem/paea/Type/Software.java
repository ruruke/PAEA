package su.rumishistem.paea.Type;

public enum Software {
	Misskey,
	Mastodon;

	public static Software value_of(String input) {
		for (Software s:Software.values()) {
			if (s.name().equalsIgnoreCase(input)) {
				return s;
			}
		}

		throw new UnsupportedOperationException("");
	}
}
