package example.pratice.oauth2api.domain;

import lombok.Getter;

@Getter
public enum Role {
	GUEST("ROLE_GUEST"),
	USER("ROLE_USER");

	private final String key;

	Role(String key) {
		this.key = key;
	}
}
