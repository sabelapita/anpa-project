package es.udc.anpaproject.backend.rest.common;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtFilter extends OncePerRequestFilter {
	
	private JwtGenerator jwtGenerator;


	public JwtFilter(JwtGenerator jwtGenerator) {

		this.jwtGenerator = jwtGenerator;
		
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		String path = request.getServletPath();

        // Ignorar endpoints públicos (no aplicar JWT aquí)
        if (path.equals("/users/signUp") || path.equals("/users/login")) {
            filterChain.doFilter(request, response);
            return;
        }
		
		String authHeaderValue = request.getHeader(HttpHeaders.AUTHORIZATION);
		// Si no hay token, dejar pasar (pero luego SecurityConfig exigirá autenticación)
		if (authHeaderValue == null || !authHeaderValue.startsWith("Bearer ")) {
			SecurityContextHolder.clearContext();
			filterChain.doFilter(request, response);
			return;
		}
		
		try {
			
			String serviceToken = authHeaderValue.replace("Bearer ", "");
			JwtInfo jwtInfo = jwtGenerator.getInfo(serviceToken);
			
			request.setAttribute("serviceToken", serviceToken);
			request.setAttribute("userId", jwtInfo.getUserId());
			
			// Crear Authentication con el userId como principal
            var authority = new SimpleGrantedAuthority("ROLE_" + jwtInfo.getRole());
            var authentication = new UsernamePasswordAuthenticationToken(
                    jwtInfo.getUserId(), null, List.of(authority));

            SecurityContextHolder.getContext().setAuthentication(authentication);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			SecurityContextHolder.clearContext();
			 return;
		}
		
		filterChain.doFilter(request, response);
		
	}

}
