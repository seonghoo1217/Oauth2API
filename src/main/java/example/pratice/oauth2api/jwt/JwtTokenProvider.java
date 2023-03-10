package example.pratice.oauth2api.jwt;

import example.pratice.oauth2api.repo.MemberRepository;
import example.pratice.oauth2api.service.CustomUserDetails;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final String SECRET_KEY;
	private final String COOKIE_REFRESH_TOKEN_KEY;
	private final Long ACCESS_TOKEN_EXPIRE_LENGTH = 1000L * 60 * 60;		// 1hour
	private final Long REFRESH_TOKEN_EXPIRE_LENGTH = 1000L * 60 * 60 * 24 * 7;	// 1week
	private final String AUTHORITIES_KEY = "role";

	private final MemberRepository memberRepository;

	public JwtTokenProvider(@Value("${app.auth.token.secret-key}")String secretKey, @Value("${app.auth.token.refresh-cookie-key}")String cookieKey) {
		this.SECRET_KEY = Base64.getEncoder().encodeToString(secretKey.getBytes());
		this.COOKIE_REFRESH_TOKEN_KEY = cookieKey;
	}

	public String createAccessToken(Authentication authentication) {
		Date now = new Date();
		Date validity = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_LENGTH);

		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

		String userId = user.getName();
		String role = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		return Jwts.builder()
				.signWith(SignatureAlgorithm.HS512, SECRET_KEY)
				.setSubject(userId)
				.claim(AUTHORITIES_KEY, role)
				.setIssuer("debrains")
				.setIssuedAt(now)
				.setExpiration(validity)
				.compact();
	}

	public void createRefreshToken(Authentication authentication, HttpServletResponse response) {
		Date now = new Date();
		Date validity = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_LENGTH);

		String refreshToken = Jwts.builder()
				.signWith(SignatureAlgorithm.HS512, SECRET_KEY)
				.setIssuer("debrains")
				.setIssuedAt(now)
				.setExpiration(validity)
				.compact();

		saveRefreshToken(authentication, refreshToken);

		ResponseCookie cookie = ResponseCookie.from(COOKIE_REFRESH_TOKEN_KEY, refreshToken)
				.httpOnly(true)
				.secure(true)
				.sameSite("Lax")
				.maxAge(REFRESH_TOKEN_EXPIRE_LENGTH/1000)
				.path("/")
				.build();

		response.addHeader("Set-Cookie", cookie.toString());
	}

	private void saveRefreshToken(Authentication authentication, String refreshToken) {
		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
		Long id = Long.valueOf(user.getName());

		memberRepository.updateRefreshToken(id, refreshToken);
	}

	// Access Token??? ???????????? ?????? ????????? Authentication ?????? ??????
	public Authentication getAuthentication(String accessToken) {
		Claims claims = parseClaims(accessToken);

		Collection<? extends GrantedAuthority> authorities =
				Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
						.map(SimpleGrantedAuthority::new).collect(Collectors.toList());

		CustomUserDetails principal = new CustomUserDetails(Long.valueOf(claims.getSubject()), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
	}

	public Boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
			return true;
		} catch (ExpiredJwtException e) {
			log.info("????????? JWT ???????????????.");
		} catch (UnsupportedJwtException e) {
			log.info("???????????? ?????? JWT ???????????????.");
		} catch (IllegalStateException e) {
			log.info("JWT ????????? ?????????????????????");
		}
		return false;
	}

	// Access Token ????????? ????????? ????????? ????????? ?????? ?????? Claim ??????
	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(accessToken).getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}
}
