package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.dal.dto.User;
import itstep.learning.dal.dto.shop.Cart;
import itstep.learning.dal.dto.shop.CartItem;
import itstep.learning.dal.dto.shop.Product;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;
import itstep.learning.services.stream.StreamService;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Singleton
public class CartServlet extends RestServlet {
    private final CartDao cartDao;
    private final ProductDao productDao;
    private final StreamService streamService;

    @Inject
    public CartServlet(CartDao cartDao, ProductDao productDao, StreamService streamService) {
        this.cartDao = cartDao;
        this.productDao = productDao;
        this.streamService = streamService;
    }


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.restResponse = new RestResponse().setMeta(
                new RestMetaData()
                        .setUrl("/shop/cart")
                        .setMethod((req.getMethod()))
                        .setName("KN-P-213 Shop API for carts")
                        .setServerTime(new Date())
                        .setAllowedMethods(new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"})
        );
        super.service(req, resp);
    }

    private void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getAttribute("auth-token-user");
        if(user == null) {
            super.sendResponse(401);
            return;

        }
        Cart openCart = cartDao.getCartByUser(user, true);
        String json = streamService.readAsString( req.getInputStream() );
        CartItem[] cartItems = super.gson.fromJson(json, CartItem[].class);
        List<CartItem> presentItems = new ArrayList<>();
        List<CartItem> absentItems = new ArrayList<>();

        for(CartItem cartItem : cartItems) {
            UUID productId = cartItem.getProductId();
            if( productId == null  ){
                super. sendResponse( 400, "Missing item data 'product-id'" );
                return;
            }
            Product product = productDao.getByIdOrSlug( productId.toString() );
            if( product == null ) {
                presentItems.add( cartItem );
                continue;
            }
            cartItem.setProduct(product);
            int inCart = 0;
            if (openCart != null) {
                Optional<CartItem> ci = Arrays
                        .stream(openCart.getCartItems())
                        .filter(i -> i.getProductId().equals(productId))
                        .findFirst();
                if( ci.isPresent() ) {
                    inCart = ci.get().getQuantity();
                }
            }
            if( product. getQuantity() < cartItem. getQuantity() +inCart ) {
// перевірка на достатню кількість / наявність товарів
                if (product.getQuantity() > 0) {
// те, що є додаємо до кошику, а залишок (дозамовлення)
// переносимо до відсутніх елементів
                    CartItem item = new CartItem();
                    item.setProduct(product);
                    item.setQuantity(product.getQuantity() - inCart);
                    presentItems.add(item);
                    cartItem.setQuantity(cartItem.getQuantity() + inCart -
                            product.getQuantity());
                }
                absentItems.add(cartItem);
            }
        }

        try
        {
            for(CartItem cartItem : presentItems) {
                cartDao.add(user, cartItem.getProduct(), cartItem.getQuantity());
            }
        }
        catch (Exception ex)
        {
            super.sendResponse(200, ex.getMessage());
            return;
        }

        super.sendResponse(200, cartItems);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getAttribute("auth-token-user");
        if (user != null) {
            if(req.getParameterMap().containsKey("all"))
            {
                super.sendResponse(200, cartDao.getCartsArrayByUser(user, false));
                return;
            }
            else
            {
                super.sendResponse(200, cartDao.getCartByUser(user, true));
                return;
            }
        }
        else
        {
            super.sendResponse(401, null);
            return;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // додати товар до кошику (CREATE)
        // 1. Встановити користувача (за токеном)
        // 1.1. Якщо приходить запит від неавторизованого користувача, то ...
        // 2. Дізнатись чи є в користувача відкритий кошик
        //   якщо е, то додаємо до нього,
         //       якщо немає, то створюемо новий
        // 3. Перевіряємо чи є зазначений товар у кошику
       // якщо є - збільшуємо кількість
       // якщо немає - додаємо

        String productId = req.getParameter( "product-id" );
        if( productId == null || productId.isEmpty() ){
            super. sendResponse( 400, "Missing required parameter 'product-id'" );
            return;
        }
        Product product = productDao.getByIdOrSlug( productId );
        if( product == null ) {
            super.sendResponse(404, "Product not found");
            return;
        }
        User user = (User) req.getAttribute("auth-token-user");
        if(user == null) {
            super.sendResponse(401);
            return;

        }
            try
            {
                if (cartDao.add(user, product))
                {
                    super.sendResponse( 201, productId );
                }
                else
                {
                    super.sendResponse( 500, "Error adding product" );
                }
            }
            catch ( Exception ex )
            {
                super.sendResponse(400, ex.getMessage());
            }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getAttribute("auth-token-user");
        if (user == null) {
            super.sendResponse(401, null);
            return;
        }
        String cartId = req.getParameter( "cart-id" );
        if( cartId == null || cartId.isEmpty() ){
            super.sendResponse(400, "Missing required parameter 'cart-id'" );
            return;
        }
        String productId = req.getParameter( "product-id" );
        if( productId == null || productId.isEmpty() ){
            super.sendResponse(400, "Missing required parameter 'product-id'" );
            return;
        }

        int increment;
        String delta = req.getParameter( "delta" );
        if( delta == null || delta.isEmpty() ){
            increment = 1;
        }
        else {
            try
            {
                increment = Integer.parseInt( delta );
            }
            catch ( NumberFormatException ignored )
            {
                super.sendResponse(400, "Invalid number format param 'delta'" );
                return;
            }
        }
        try
        {
            if (cartDao.update(cartId,productId, increment))
            {
                super.sendResponse( 202, "Update" );
            }
            else {
                super.sendResponse( 500, "Error updating product" );
            }
        }
        catch (Exception ex)
        {
            super.sendResponse( 400, ex.getMessage() );
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getAttribute("auth-token-user");
        if (user == null) {
            super.sendResponse(401, null);
            return;
        }
        Cart cart = cartDao.getCartByUser(user);
        if (cart == null) {
            super.sendResponse(404, "User cart not found");
            return;
        }

        String cartId = req.getParameter( "cart-id" );
        if( cartId == null || cartId.isEmpty() ){
            super.sendResponse(400, "Missing required parameter 'cart-id'" );
            return;
        }

        if(!cart.getId().toString().equals(cartId))
        {
            super.sendResponse( 403, "Access forbidden to 'cart-id'" );
            return;
        }

        String productId = req.getParameter( "product-id" );
        if( productId == null || productId.isEmpty() ){
            cartDao.deleteCart(cartId);
        }
        else
        {
            cartDao.deleteCartItem(cartId, productId);
        }
        super.sendResponse( 202, "Data to be processed" );
    }
}
