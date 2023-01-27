package example.pratice.oauth2api.domain;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String oauthId;

	private String name;

	private String email;

	private String imageUrl;

	@Enumerated(EnumType.STRING)
	private Role role;

	protected Member() {
	}

	public Member(String oauthId, String name, String email, String imageUrl, Role role) {
		this(null, oauthId, name, email, imageUrl, role);
	}

	public Member(Long id, String oauthId, String name, String email, String imageUrl, Role role) {
		this.id = id;
		this.oauthId = oauthId;
		this.name = name;
		this.email = email;
		this.imageUrl = imageUrl;
		this.role = role;
	}

	public Member update(String name, String email, String imageUrl) {
		this.name = name;
		this.email = email;
		this.imageUrl = imageUrl;
		return this;
	}
}
