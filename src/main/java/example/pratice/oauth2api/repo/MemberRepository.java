package example.pratice.oauth2api.repo;

import example.pratice.oauth2api.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
	Optional<Member> findByOauthId(String oauthId);

	Optional<Member> findByEmail(String email);

	boolean existsByEmail(String email);

	@Query("SELECT m.refreshToken FROM Member m WHERE m.id=:id")
	String getRefreshTokenById(@Param("id") Long id);

	@Transactional
	@Modifying
	@Query("UPDATE Member m SET m.refreshToken=:token WHERE m.id=:id")
	void updateRefreshToken(@Param("id") Long id, @Param("token") String token);
}
