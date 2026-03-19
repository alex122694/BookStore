package bookstore.util;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	
	private final String secretString = "this_is_my_secret_key_and_do_not_share_with_anyone";
	private final SecretKey secretKey = Keys.hmacShaKeyFor(secretString.getBytes());
	private final long expireTime = 1000 * 60 * 60 * 24;
	
	public String generateToken(String memberId, String role) {
		return Jwts.builder()
				.subject(memberId)
				.claim("role", role)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expireTime))
				.signWith(secretKey)
				.compact();
	}
	
	public Claims getClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
	
	public String getMemberId(String token) {
		return getClaims(token).getSubject();
	}
	
	public String getRole(String token) {
		return (String) getClaims(token).get("role");
	}

}
