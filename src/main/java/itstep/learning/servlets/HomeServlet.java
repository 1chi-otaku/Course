package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.AuthDao;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.services.db.DbService;
import itstep.learning.services.filename.FileNameService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Singleton
public class HomeServlet extends HttpServlet {
    private final AuthDao authDao; // інжекцію класів (не інтерфейсів) реєструвати не треба
    private final FileNameService fileNameService;
    private final DbService dbService;
    private final ProductDao productDao;
    private final CartDao cartDao;



    @Inject
    public HomeServlet(AuthDao authDao, FileNameService fileNameService, DbService dbService, ProductDao productDao, CartDao cartDao) {

        this.authDao = authDao;
        this.fileNameService = fileNameService;
        this.dbService = dbService;
        this.productDao = productDao;
        this.cartDao = cartDao;
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        boolean isSigned = false;
        Object signature = req.getAttribute("signature");
        if (signature instanceof Boolean) {
            isSigned = (Boolean) signature;
        }

        if (isSigned) {

            String dbMessage;
            try {
                 dbMessage = authDao.install() && productDao.install() && cartDao.install() ? "Install OK" : "Install failed";

            }
            catch (Exception e) {
                dbMessage = e.getMessage();
            }


            try {
                Statement stmt = dbService. getConnection().createStatement();
                ResultSet rs = stmt.executeQuery("SELECT CURRENT_TIMESTAMP FROM dual");
                rs.next();
                dbMessage += " "+ rs.getString(1);
                rs.close();
                stmt.close();
            }
            catch (SQLException e) {
                dbMessage += " " + e.getMessage();
            }

            req.setAttribute("hash", dbMessage);
            req.setAttribute("body", "home.jsp");

            // Генерація випадкових імен файлів
            String randomFileNameDefault = fileNameService.generateRandomFileName();
            String randomFileNameWithLength = fileNameService.generateRandomFileName(12);
            req.setAttribute("randomFileNameDefault", randomFileNameDefault);
            req.setAttribute("randomFileNameWithLength", randomFileNameWithLength);

        } else {
            req.setAttribute("body", "not_found.jsp");  // Якщо підпис неправильний - insecure.jsp
        }

        // ~ return View();
        req.getRequestDispatcher( "WEB-INF/views/_layout.jsp" ).forward(req, resp);

        // resp.getWriter().println("<h1>Home</h1>");
    }
}

/*
Сервлети - спеціалізовані класи для мережних задач, зокрема,
HttpServlet - для веб-задач, є аналогом контролерів в ASP
 */