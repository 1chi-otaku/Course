package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.ReviewDao;
import itstep.learning.dal.dto.shop.Review;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class ReviewServlet extends RestServlet {
    private final Logger logger;
    private final ReviewDao reviewDao;

    @Inject
    public ReviewServlet(Logger logger, ReviewDao reviewDao) {
        this.logger = logger;
        this.reviewDao = reviewDao;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.restResponse = new RestResponse().setMeta(
                new RestMetaData()
                        .setUrl("/shop/review")
                        .setMethod(req.getMethod())
                        .setName("KN-P-213 Shop API for reviews")
                        .setServerTime(new Date())
                        .setAllowedMethods(new String[]{"GET", "POST", "OPTIONS"})
        );

        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String productId = req.getParameter("product_id");
        if (productId != null && !productId.trim().isEmpty()) {
            this.getReviewsByProductId(productId);
            return;
        }
        super.sendResponse(400, "Missing required field: 'product_id'");
    }

    private void getReviewsByProductId(String productId) throws IOException {
        List<Review> reviews = reviewDao.getReviewsByProductId(productId);
        if (reviews.isEmpty()) {
            super.sendResponse(404, "No reviews found for the product with ID: " + productId);
        } else {
            super.sendResponse(200, reviews);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.sendResponse(405, "POST method is not supported for reviews.");
    }
}
