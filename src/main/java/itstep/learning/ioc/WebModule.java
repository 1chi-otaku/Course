package itstep.learning.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import itstep.learning.filters.*;
import itstep.learning.servlets.*;

public class WebModule extends ServletModule{

    @Override
    protected void configureServlets() {
        //За наявності IoC реєстрація фільтрів та сервлетів здійснюєть через
        // Не забути !! прибрати реєстрацію фільтрів з web.xml та додати Singleton
        // До класiв фільтрів.
        filter("/*").through(CharsetFilter.class);
        filter("/*").through(SecurityFilter.class);

        // те ж з сервлетами
        serve("/"            ).with(HomeServlet.class);
        serve("/auth"        ).with(AuthServlet.class);
        serve("/storage/*"   ).with(StorageServlet.class);
    }


}
