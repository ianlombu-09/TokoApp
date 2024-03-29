package com.woyo.toko.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.woyo.toko.response.HandlerResponse;
import com.woyo.toko.utils.SecurityUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(SecurityUtils.HEADER_STRING);

        if (header == null || !(header.startsWith(SecurityUtils.TOKEN_PREFIX))) {
            chain.doFilter(request, response);
            return;
        } else {
            UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(request, response);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            String token = request.getHeader(SecurityUtils.HEADER_STRING);

            if (token != null) {
                String user = JWT.require(Algorithm.HMAC512(SecurityUtils.SECRET.getBytes()))
                        .build()
                        .verify(token.replace(SecurityUtils.TOKEN_PREFIX, ""))
                        .getSubject();

                if (user != null) {
                    return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                }

                return null;
            }

            return null;
        } catch (JWTVerificationException e) {
            e.printStackTrace();
            HandlerResponse.responseUnauthorized(response, "");
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            HandlerResponse.responseInternalServerError(response, "");
            return null;
        }
    }
}
