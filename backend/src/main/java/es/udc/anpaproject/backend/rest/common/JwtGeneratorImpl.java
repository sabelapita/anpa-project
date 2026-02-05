package es.udc.anpaproject.backend.rest.common;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtGeneratorImpl implements JwtGenerator {
	
	@Value("${project.jwt.signKey}")
	private String signKey;
	
	@Value("${project.jwt.expirationMinutes}")
	private long expirationMinutes;

	@Override
	public String generate(JwtInfo info) {

		return Jwts.builder()
			.claim("userId", info.getUserId())
			.claim("role", info.getRole())
			.setExpiration(new Date(System.currentTimeMillis() + expirationMinutes*60*1000))
			.signWith(Keys.hmacShaKeyFor(signKey.getBytes()))
			.compact();

	}

	@Override
	public JwtInfo getInfo(String token) {
		
		Claims claims = Jwts.parserBuilder()
		.setSigningKey(Keys.hmacShaKeyFor(signKey.getBytes()))
		.build()
		.parseClaimsJws(token)
		.getBody();

		Number idNumber = (Number) claims.get("userId");
    	Long userId = idNumber.longValue();
		
		String role = claims.get("role").toString();

		return new JwtInfo(userId, role);
		
	}

}
