package com.acorn.gymmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException{

        authenticateFromSession(request);

        filterChain.doFilter(request, response);
    }

    private void authenticateFromSession(HttpServletRequest request){
        HttpSession session = request.getSession(false);

        if(session == null){
            return;
        }

        Object attribute = session.getAttribute(SessionUser.SESSION_KEY);

        if(!(attribute instanceof SessionUser sessionUser)){
            return;
        }
        if(!sessionUser.hasValidRole()){
            session.removeAttribute(SessionUser.SESSION_KEY);
            return;
        }

        Authentication currentAuthentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        Object currentPrincipal = currentAuthentication == null
                ? null
                : currentAuthentication.getPrincipal();

        if(sessionUser.equals(currentPrincipal)){
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        sessionUser,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        sessionUser.authority()
                                )
                        )
                );

        authentication.setDetails(
                request.getRemoteAddr()
        );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
