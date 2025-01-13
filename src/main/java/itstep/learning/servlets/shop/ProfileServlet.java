package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.dal.dto.User;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Singleton
public class ProfileServlet extends RestServlet {
    private final CartDao cartDao;
    private final ProductDao productDao;


    @Inject
    public ProfileServlet(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.restResponse = new RestResponse().setMeta(
                new RestMetaData()
                        .setUrl("/shop/profile")
                        .setMethod((req.getMethod()))
                        .setName("KN-P-213 Shop API for carts")
                        .setServerTime(new Date())
                        .setAllowedMethods(new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"})
        );

        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getAttribute("auth-token-user");
        if (user != null) {
            super.sendResponse(200, cartDao.getCartsArrayByUser(user, true));
        }
        else
        {
            super.sendResponse(401, null);
        }
    }
}
