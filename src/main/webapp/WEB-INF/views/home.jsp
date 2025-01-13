<%@ page contentType="text/html;charset=UTF-8"  %>

<h2>Випадкові імена файлів</h2>
<p>Ім'я файлу за замовчанням: <%= request.getAttribute("randomFileNameDefault") %></p>
<p>Ім'я файлу довжиною 12 символів: <%= request.getAttribute("randomFileNameWithLength") %></p>


<h1>Java web. JSP</h1>
<img src="img/Java_Logo.svg" alt="logo" style="height: 200px">
<i>Контроль інжекції хешу: <%= request.getAttribute("hash") %></i>
<p>
    JSP - Java Server Pages - технологія веб-розробки з динамічним
    формуванням HTML сторінок. Аналогічно до PHP, ранніх ASP є
    надбудовою над HTML, що розширює його додаючи
</p>
<ul>
    <li>Вирази</li>
    <li>Змінні</Li>
    <li>Алгоритмічні конструкції (умови, цикли)</li>
    <li>Взаємодію з іншими файлами-сторінками</li>
</ul>
<p>
    Основу JSP складають спеціалізовані теги &lt;% %&gt; та &lt;%= %&gt; <br/>
    Тег &lt;% %&gt; включає в себе Java-код, тег &lt;%= %&gt; виводить
    результат (є скороченою формою оператора <code>print()</code>).
</p>
<p>

    Вирази частіше все задаються тегом, що виводить, у якому може бути
    довільний вираз (коректний для Java). Виведення результату здійснюється
    у тому місці, де знаходиться тег: <br/>
    &lt; %= 2 + 3 %&gt; =< %= 2+3%>
</p>
<h2>3мiнні</h2>

<p>

    Змінні, їх оголошення та призначення (без виведення результату)
    оформлюється у блоці &lt;% %&gt;
        <%

String str = "Hello, World!";
double[] prices = { 10.0, 20.0, 30.0, 40.0 };

%>
<pre>

    &lt;%
    String str = "Hello, World!";
    double[] prices = { 10.0, 20.0, 30.0, 40.0 };
    %&gt;
</pre>
<p>

    Виведення значень змінних - знов тег <br/>
    &lt;%= str %&gt; &rarr; <%= str %>
</p>

<h2>Алгоритмічні конструкції</h2>
<pre>
    &lt;% for (int i = 0; i < prices.length; i++) { %&gt;
        &lt;i&gt; &lt;%= prices[i] %&gt;&lt;/i&gt;&amp; emsp;
    &lt;% } %&gt;
</pre>
&rarr;
<% for (int i = 0; i < prices.length; i++) { %>
<i><%= prices[i] %></i>&emsp;
<% } %>

<h2>Взаємодія з файлами</h2>
&lt;jsp:include page="WEB-INF/fragment.jsp" /&gt; &rarr;
<jsp:include page="../fragment.jsp" />

<p>
    Д.3. Реалізувати виведення масиву double[] prices у вигляді HTML-таблиці
    1 10,0
    2 20,0
    ...
    Створити файл "not_found.jsp", реалізувати у ньому сторінку 404
</p>


<h1>Prices</h1>
<table border="1">
    <tr>
        <th>#</th>
        <th>Price</th>
    </tr>
    <%
        for (int i = 0; i < prices.length; i++) {
    %>
    <tr>
        <td><%= i + 1 %></td>
        <td><%= prices[i] %></td>
    </tr>
    <%
        }
    %>
</table>

