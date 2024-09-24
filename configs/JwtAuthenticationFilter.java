package com.pilot.project.configs;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtTokenHelper jwtTokenHelper;

    @Autowired
    public JwtAuthenticationFilter(@Lazy UserDetailsService userDetailsService, JwtTokenHelper jwtTokenHelper) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenHelper = jwtTokenHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {
                username=this.jwtTokenHelper.getUsernameFromToken(jwtToken);
            }
            catch(IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
                throw new IllegalArgumentException("Unable to get JWT Token");
            }
            catch(ExpiredJwtException e) {
                System.out.println("Expired JWT Token");
                throw new ExpiredJwtException(null,null,"Expired JWT Token");
            }
            catch(MalformedJwtException e) {
                System.out.println("Malformed JWT Token");
                throw new MalformedJwtException("Malformed JWT Token");
            }
        }
        else{
        }
        if(username!= null && SecurityContextHolder.getContext().getAuthentication()==null) {
            UserDetails user = this.userDetailsService.loadUserByUsername(username);
            if(this.jwtTokenHelper.validateToken(jwtToken,user)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, user.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
            else {
                throw new UsernameNotFoundException("Invalid JWT Token");
            }

        }
        else {
            System.out.println("Username is null or context is not null");
        }
        filterChain.doFilter(request, response);
    }
}
