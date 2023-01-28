package example.pratice.oauth2api.repo;

import example.pratice.oauth2api.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
	Optional<Member> findByOauthId(String oauthId);

}
