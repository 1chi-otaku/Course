package itstep.learning.servlets;

import com.google.inject.Inject;
import itstep.learning.kdf.KdfService;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class WebXmlServlet extends HttpServlet {
    private final KdfService kdfService;

    @Inject
    public WebXmlServlet(KdfService kdfService) {
        this.kdfService = kdfService;
    }

    @Override
    protected  void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        req.setAttribute("hash", kdfService.hashCode());

        req.setAttribute("body", "web_xml.jsp"); // ViewData["body"] = "home.jsp   "

        // ~ return View();
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}
