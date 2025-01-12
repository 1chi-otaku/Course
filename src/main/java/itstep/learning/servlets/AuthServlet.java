package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.AuthDao;
import itstep.learning.dal.dto.User;
import itstep.learning.dal.dto.shop.Category;
import itstep.learning.models.SignupFormModel;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;
import itstep.learning.services.db.DbService;
import itstep.learning.services.form.FormParseResult;
import itstep.learning.services.form.FormParseService;
import itstep.learning.services.storage.StorageService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class AuthServlet extends RestServlet {
    private final AuthDao authDao;
    private final FormParseService formParseService;
    private final StorageService storageService;
    private final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final Logger logger;

    @Inject
    public AuthServlet(AuthDao authDao, FormParseService formParseService, StorageService storageService, Logger logger) {
        this.authDao = authDao;
        this.formParseService = formParseService;
        this.storageService = storageService;
        this.logger = logger;
    }

    @Override
    protected  void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        /*
        The 'Basic' HTTP Authentication Scheme
        https://datatracker.ietf.org/doc/html/rfc7617
        */
        // Вилучаємо заголовок Authorization
        // Перевіряємо,що схема Basic
        // Виділяємо дані автентифікації (credentials)
        // Декодуємо їх за Base64
        // Розділяємо за першим символом ':'
        // запитаємо автентифікацію в DAO
        RestResponse restResponse = new RestResponse();
        try
        {
            String authHeader = req.getHeader("Authorization");
            if (authHeader == null)
            {
                throw new ParseException("Authorization header not found", 401);
            }

            String authScheme = "Basic ";
            if (! authHeader.startsWith(authScheme)) {
                throw new ParseException("Invalid Authorization scheme. Required " + authScheme, 400);
            }

            String credentials = authHeader.substring(authScheme.length());

            String decodedCredentials;
            try
            {
                decodedCredentials = new String(Base64.getDecoder().decode(credentials.getBytes(StandardCharsets.UTF_8)),
                        StandardCharsets.UTF_8
                );
            } catch (IllegalAccessError ex) {
                throw new ParseException("Invalid credentials format", 400);
            }

            String[] parts = decodedCredentials.split(":", 2);
            if (parts.length != 2)
            {
                throw new ParseException("Invalid credentials composition", 400);
            }

            User user = authDao.authenticate(parts[0], parts[1]);
            if (user == null)
            {
                throw new ParseException("Credentials rejected", 401);
            }

            super.sendResponse( user );


        }
        catch (ParseException ex){
            super.sendResponse( ex.getErrorOffset(), ex. getMessage() );
        }

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.restResponse = new RestResponse().setMeta(
                new RestMetaData()
                        .setUrl("/auth")
                        .setMethod((req.getMethod()))
                        . setName ( "KN-P-213 Authentication API" )
                        . setServerTime( new Date() )
                        .setAllowedMethods(new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"})
        );

        super.service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Sign Up
        // req.getParameter("name") - параметри запиту: URL - або form-дані
        SignupFormModel model;
        try {
            model = getSignupFormModel(req);
        }
        catch( Exception ex ) {
            super.sendResponse(400, ex.getMessage());
            return;
        }
        User user = authDao.signUp( model );if( user == null ) {
            super.sendResponse( 400, "Signup error" );
        }
        else {
            super.sendResponse( 201, user );
        }



    }



    private SignupFormModel getSignupFormModel(HttpServletRequest req) throws Exception {
        // АЛЕ! за умови, що форма передається як х-www-form-urlencoded
        // і не працює для multipart/form-data
        FormParseResult formParseResult = formParseService.parse( req );
        SignupFormModel model = new SignupFormModel();
        String data = formParseResult.getFields().get("signup-name");
        if (data == null || data.isEmpty())
        {
            throw new Exception("Missing or empty required field 'signup-name'");
        }
        model.setName(data);

        data = formParseResult.getFields().get("signup-email");
        if (data == null || data.isEmpty())
        {
            throw new Exception("Missing or empty required field 'signup-email'");
        }
        model.setEmail(data);

        data = formParseResult.getFields().get("signup-phone");
        if (data == null || data.isEmpty())
        {
            throw new Exception("Missing or empty required field 'signup-phone'");
        }
        model.setPhone(data);

        data = formParseResult.getFields().get("signup-login");
        if (data == null || data.isEmpty())
        {
            throw new Exception("Missing or empty required field 'signup-login'");
        }
        model.setLogin(data);

        data = formParseResult.getFields().get("signup-password");
        if (data == null || data.isEmpty())
        {
            throw new Exception("Missing or empty required field 'signup-password'");
        }
        model.setPassword(data);

        data = formParseResult.getFields().get("signup-repeat");
        if( data == null || data. isEmpty() ){
            throw new Exception("Missing or empty required field 'signup-repeat'");
        }
        if( ! model. getPassword ().equals( data ) ) {
            throw new Exception( "Password and repeat do not match" );
        }
        model.setRepeat( data );

        data = formParseResult.getFields().get("signup-birthdate");
        if (data == null || data.isEmpty())
        {
            throw new Exception("Missing or empty required field 'signup-birthdate'");
        }
        try
        {
            model.setBirthdate( sqlDateFormat.parse(data) );
        }
        catch( ParseException ex) {
            throw new Exception("Invalid birthdate", ex);
        }

        try {
            data = storageService.saveFile(
                    formParseResult.getFiles().get("signup-avatar") );
        }
        catch( IOException ex ) {
            logger.warning(ex.getMessage());
            throw new Exception( "Error processing 'signup-avatar'");
        }
        model.setAvatar(data);


        return model;
    }

    /*

Д.3. Реалізувати сервіси парсингу форм (FormParseService)
та управління збереженням/видачею файлів (StorageService)
у власних курсових проєктах
*/
}

/*
application/x-www-form-urlencoded

HTTP/1.1 200 0K
Content-Type: application/x-www-form-urlencoded

name=User&password=123

*/

/*
HTTP/1.1 200 0K
Content-Type: multipart/form-data; delimiter=asdf...w
--asdf...w

Content-Disposition: form-field; name=user-name

user
--asdf...w
Content-Disposition: form-field; name=user-avatar

123
--asdf ..w
Content-Disposition: form-file; name=user-avatar

PNGd45sf4ghsem8jz'dflhgsd4f
Gzz;jd'
--asdf...w--

*/