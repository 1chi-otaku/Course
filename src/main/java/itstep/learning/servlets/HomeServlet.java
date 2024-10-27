package itstep.learning.servlets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.AccessLogDao;
import itstep.learning.dal.dao.AuthDao;
import itstep.learning.services.db.DbService;
import itstep.learning.services.filegenerator.FileGeneratorService;
import itstep.learning.services.hash.HashService;
import itstep.learning.services.kdf.KdfService;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@Singleton
public class HomeServlet extends HttpServlet {

    private final HashService hashService;
    private final KdfService kdfService;
    private final DbService dbService;
    private final FileGeneratorService fileGeneratorService;
    private final AuthDao authDao; //інжекцію класі (не інтерфейсів) реєструвати не треба.
    private final AccessLogDao accessLogDao;

    @Inject
    public HomeServlet(HashService hashService, KdfService kdfService, DbService dbService, FileGeneratorService fileGeneratorService, AuthDao authDao, AccessLogDao accessLogDao) {
        this.hashService = hashService;
        this.kdfService = kdfService;
        this.dbService = dbService;
        this.fileGeneratorService = fileGeneratorService;
        this.authDao = authDao;
        this.accessLogDao = accessLogDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // ~ return View(); аналог
        String dbMessage =
                authDao.install() ? "Connection OK" : "Connection failed";

        req.setAttribute("body", "main.jsp");


        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}

/*
* Сервелети - спеціализовані класи для мережних
* задач, зокремма HTTPServlet - для веб-задач, є аналогом
* контролерів в ASP
* */