
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<h1>Файл налаштувань <code>web.xml</code></h1>
<p>
  Файл <code>web.xml</code> дозволяє доналаштувати веб-сервер (Tomcat або інші)
  під даний проєкт.
  <%=request.getAttribute("hash")%>
</p>
<h2>Фільтри та їх область дії</h2>
<p>

  Для фільтрів <code>web.xml</code> особливо важливий, оскільки гарантує порядок
  виконання фільтрів (якщо їх декілька). В області дії фільтрів поширеною є практика
  шаблонних адрес на кшталт <code>/*</code> або <code>/api/*</code>
</p>

<pre>
&lt ;! -- Реєстрація фільтрів -- &gt;
&lt; filter&gt;
&lt; filter-name&gt; charsetFilter&lt;/filter-name&gt;
&lt; filter-class&gt; itstep. learning. filters. CharsetFilter&lt;/filter-class&gt;
&lt;/filter&gt;
&lt; filter-mapping&gt;
&lt; filter-name&gt; charsetFilter&lt;/filter-name&gt;
&lt; url-pattern&gt; /*&lt; /url-pattern&gt;
&lt;/filter-mapping&gt;
</pre>

<h2>Сервлети та маршрутизація</h2>
<p>

  До появи анотацій на кшталт <code>@WebServlet</code> сервлети реєструвались
  у файлі <code>web.xml</code> із зазначенням їх маршрутів (роутингу).
</p>

<pre>
  &lt;p&gt;
  Файл &lt;code&gt;web.xml&lt;/code&gt; дозволяє доналаштувати веб-сервер (Tomcat або інші)
  під даний проєкт.
  &lt;/p&gt;
  &lt;h2&gt;Сервлети та маршрутизація&lt;/h2&gt;
  &lt;p&gt;
  До появи анотацій на кшталт &lt;code&gt;@WebServlet&lt;/code&gt; сервлети реєструвались
  у файлі &lt;code&gt;web.xml&lt;/code&gt; із зазначенням їх маршрутів (роутингу).
  &lt;/p&gt;
</pre>