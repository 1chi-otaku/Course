package itstep.learning.filters;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.AuthDao;
import itstep.learning.dal.dto.User;
import itstep.learning.services.db.DbService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.LogRecord;

@Singleton
public class TokenAuthFilter implements Filter {

    private final AuthDao authDao;

    @Inject
    public TokenAuthFilter(DbService dbService, AuthDao authDao) {
        this.authDao = authDao;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null)
        {
            String authScheme = "Bearer ";
            if (authHeader.startsWith(authScheme)) {
                String token = authHeader.substring(authScheme.length());
                User user = authDao.getUserByToken(token);
                if (user != null) {
                    servletRequest.setAttribute("auth-token-user", user);
                }
            }
        }


    filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
