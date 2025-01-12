package itstep.learning.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

public class RestServlet extends HttpServlet {
    protected RestResponse restResponse;
    private HttpServletResponse response;
    protected final Gson gson = new GsonBuilder().serializeNulls().create();


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        response=resp;
        // TODO: перевірити наявність методу do[Method] в об'єкті-сервлеті
        String method = req.getMethod();
        String action = "do" + method.substring(0, 1).toUpperCase() + method.substring(1).toLowerCase();
        try {
           Method mtd = this.getClass().getDeclaredMethod(action, HttpServletRequest.class, HttpServletResponse.class);
           mtd.setAccessible(true);
           mtd.invoke(this, req, resp);
        }
        catch (Exception ignore) {
            this.sendResponse(405);
        }
        //super.service(req, resp);
    }


    protected void sendResponse(int code, Object data, int maxAge) throws IOException
    {
        if(code > 0)
        {
            restResponse.setStatus(new RestStatus(code));
        }
        if(data != null)
            restResponse.setData(data);

        response.setContentType("application/json");
        
        response.setHeader("Cache-Control", maxAge > 0 ? "max-age=" + maxAge : "no-cache");

        response.getWriter().print(gson.toJson(restResponse));
    }

    protected void sendResponse(int code, Object data) throws IOException
    {
        this.sendResponse(code, data, 0);
    }

    protected void sendResponse(Object data) throws IOException
    {
        if( restResponse.getStatus() == null ) {
            this.sendResponse(200, data);
        }

        else {
            this.sendResponse(-1, data);
        }
    }

    protected void sendResponse(int code) throws IOException
    {
        this.sendResponse(code, null);
    }

    protected void sendResponse() throws IOException
    {
        if(restResponse.getStatus() == null)
        {
            this.sendResponse(200);
        }
        else
        {
            this.sendResponse(-1);
        }
    }


}
