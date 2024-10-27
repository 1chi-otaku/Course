package itstep.learning.filters;

import com.google.inject.Singleton;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
Реєстрація фільтру також можлива або через web.xml, або через анотацію @WebFilter
Перевега надається web.xml, оскільки анотації не гарантують порядок виконання
фільтрів, тоді як у web.xml порядок відповідає послідовності декларацій фільтрів.
 */

@Singleton
public class CharsetFilter implements Filter {

    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        // Прямий хід - від сервера до представлення.

        //Звертаемо увагу, що request/response ідуть з базовими типами (не-HTTP)
        //За потреби роботи з HTTP-функціями необхідно здійснити перетворення.
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        //Встановлюємо кодування, що буде діять при читанні/записі даних.
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        System.out.println("CharsetFilter worked for: " + req.getRequestURI());

        //Якшо не вказати виклик наступного фільтра, то обробка запита зупиняється,
        //Користувач побачить порожню сторінку браузера.
        chain.doFilter(servletRequest, servletResponse); //await Next();
        //Зворотній хід - від прелставлення до сервера.
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}

/*
Фільтри (сервлетні фільтри) - концепція Middleware - код, що
 - передує сервлетам (контролерам)
 - утворює ланцюг викликів (передачі роботи)
 - може припинити оброблення запиту
 - проходиться двічі: "прямо" при обробці запиту, та "зворотньо" - відповідно
 - дозволяє додавання іншіх фільтрів у будь-яку частину ланцюга

Кодування символів:
особливістю роботи з запитом є те, що кодування можна змінити тільки ДО
того, як з нього почнеться читання. Після зчитування хоча б одного символу
зміна кодування призводить до винятку.
Як висновок - кодування слід встановити максимально рано - у найперших
кодах роботи з запитом. Це свідчить на користь використання фільтру.

Задача: створити фільтр SecurityFilter, який буде додавати до запиту
атрибут "signature" зі значенням true.
У HomeController перевіряти наявність цього атрибуту. Якщо відсутній,
то це значить помилка роботи фільтру, яку ми сприймаємо як інцидент безпеки
і не відображаємо сторінку (переводимо на помилкову).
 */