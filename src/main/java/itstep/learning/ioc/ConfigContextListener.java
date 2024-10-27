package itstep.learning.ioc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class ConfigContextListener extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(
            new ServiceModule(),
                new WebModule()
        );
    }
}


/*
* IoC - Інверсія управління: веб-версія.
*
*
* Особливості:
*
* - веб-проєект має ішний життєвий цикл, ніж консольний/віконний
*   кожен запит оброблюється наче як перезапуск
*   однак, якась частина проєекту є постійною і не змінюється при
*   оброблені запитів.
*
* - окрім реєстрації служб (сервісів) також бажано інжектувати
*   залежності і в фільтри/сервлети.
*
* Загальна сеха:
*   [Створення контексту - deploy] (реєстрація залежності)
*   [Запит] Інжектуємо залежності (через фільтр)
* */