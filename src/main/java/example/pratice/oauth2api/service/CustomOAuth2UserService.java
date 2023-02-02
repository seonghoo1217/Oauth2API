package example.pratice.oauth2api.service;

import example.pratice.oauth2api.domain.AuthProvider;
import example.pratice.oauth2api.domain.Member;
import example.pratice.oauth2api.domain.Role;
import example.pratice.oauth2api.domain.oauth2.OAuth2UserInfo;
import example.pratice.oauth2api.domain.oauth2.OAuth2UserInfoFactory;
import example.pratice.oauth2api.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final MemberRepository memberRepository;

	// OAuth2UserRequest에 있는 Access Token으로 유저정보 get
	@Override
	public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

		return process(oAuth2UserRequest, oAuth2User);
	}

	// 획득한 유저정보를 Java Model과 맵핑하고 프로세스 진행
	private OAuth2User process(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
		AuthProvider authProvider = AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase());
		OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(authProvider, oAuth2User.getAttributes());

		if (userInfo.getEmail().isEmpty()) {
			throw new OAuthProcessingException("Email not found from OAuth2 provider");
		}
		Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
		User user;

		if (userOptional.isPresent()) {		// 이미 가입된 경우
			user = userOptional.get();
			if (authProvider != user.getAuthProvider()) {
				throw new OAuthProcessingException("Wrong Match Auth Provider");
			}

		} else {			// 가입되지 않은 경우
			user = createUser(userInfo, authProvider);
		}
		return CustomUserDetails.create(user, oAuth2User.getAttributes());
	}

	private Member createUser(OAuth2UserInfo userInfo, AuthProvider authProvider) {
		Member member = Member.builder()
				.email(userInfo.getEmail())
				.img(userInfo.getImageUrl())
				.role(Role.USER)
				.authProvider(authProvider)
				.build();
		return memberRepository.save(member);
	}
}
