package example.pratice.oauth2api.domain.oauth2;

import example.pratice.oauth2api.domain.AuthProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {
	public static OAuth2UserInfo getOAuth2UserInfo(AuthProvider authProvider, Map<String, Object> attributes) {
		switch (authProvider) {
			case GITHUB: return new GithubOAuth2UserInfo(attributes);
			default: throw new IllegalArgumentException("Invalid Provider Type.");
		}
	}
}