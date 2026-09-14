# Лабораторная работа №0 — Ветеринарная база кошек

## Цель работы

Собрать простое веб-приложение из трёх компонентов — фронтенд, бэкенд и база данных, — чтобы в следующих лабораторных настраивать вокруг него инфраструктуру: nginx, Docker и CI/CD. Программирование здесь не является целью, поэтому сервис намеренно простой: главное — понять, как части общаются между собой и как всё это потом контейнеризовать.

## Что делает сервис

Ветеринарная база кошек. Пользователь добавляет кошку (кличка и цвет), данные сохраняются в PostgreSQL, при перезагрузке страницы все добавленные кошки остаются на месте. Также можно удалить кошку из базы.

## Стек

- **Backend:** Kotlin 2.2 + Ktor 3.1.3 + Exposed
- **DB:** PostgreSQL 16 (отдельный сетевой сервис)
- **Frontend:** HTML + JavaScript
- **Сборка:** Gradle

## Установка окружения

Работаем на Mac M4, поэтому первым делом поставили Homebrew — менеджер пакетов, который позволяет устанавливать программы одной командой в терминале, без поиска сайтов и ручной настройки. Перед установкой Homebrew нужны Xcode Command Line Tools — это набор инструментов разработчика, включающий Git и компиляторы; они уже были установлены, так что этот шаг пропустили.


<img width="1488" height="88" alt="image" src="https://github.com/user-attachments/assets/e9d91c35-44dd-4250-b353-7e965e6d592b" />


Сам Homebrew поставили одной командой из официального скрипта, а после установки добавили его в PATH — это переменная окружения, которая говорит терминалу, где искать программы. Проверили установку командой `brew --version`.


<img width="1564" height="550" alt="image" src="https://github.com/user-attachments/assets/a5be64f3-05cb-4edb-80a6-7b95774dc5cb" />


<img width="1612" height="180" alt="image" src="https://github.com/user-attachments/assets/16d9d5c7-9eac-4588-acf6-ecffc221503c" />


Через Homebrew поставили PostgreSQL 16 и JDK 21. PostgreSQL — это база данных, отдельная программа, которая работает в фоне и слушает порт 5432. JDK нужен для запуска Kotlin и Gradle. PostgreSQL запустили как сервис через `brew services start postgresql@16`, чтобы он автоматически поднимался при включении Mac и работал в фоне. После установки добавили пути к PostgreSQL и JDK в `~/.zshrc` — это файл настроек оболочки zsh, который выполняется при каждом открытии терминала. Без этого команды `psql`, `createdb` и `java` не находились бы.


<img width="1232" height="540" alt="image" src="https://github.com/user-attachments/assets/a2b92ee8-45d9-43f7-be29-5da570de16ae" />


<img width="1534" height="166" alt="image" src="https://github.com/user-attachments/assets/cd6568df-b19d-4557-8732-3a16fdbcf16e" />


<img width="1764" height="480" alt="image" src="https://github.com/user-attachments/assets/4813808b-573f-4899-b177-2af54f07b14c" />


<img width="1502" height="174" alt="image" src="https://github.com/user-attachments/assets/8b739d0c-7333-46a6-9835-1af8cd0c29df" />


Проверили версии: `psql --version` показал PostgreSQL 16.15, `java -version` — openjdk 21.0.12.1.

## Создание базы данных

Создали базу `vet_clinic` командой `createdb vet_clinic`. Это отдельная база внутри PostgreSQL, в которой будут храниться наши данные. Зашли в неё через `psql -d vet_clinic` и создали таблицу `cats` с четырьмя колонками: `id` — автоинкрементный первичный ключ, `name` — кличка кошки, `color` — цвет, `created_at` — время создания записи. Поле `created_at` имеет тип `TIMESTAMP` и значение по умолчанию `NOW()`, то есть если при вставке не указывать время, PostgreSQL сам подставит текущий момент. Это стандартная практика: почти в любой таблице в реальных базах есть такое поле, чтобы знать, когда запись появилась. Проверили, что таблица создана, и вышли из `psql` через `\q`.


<img width="1944" height="412" alt="image" src="https://github.com/user-attachments/assets/6c9578c7-155a-43be-94ff-fef5edbe5a3e" />


<img width="1226" height="494" alt="image" src="https://github.com/user-attachments/assets/8721732d-a42c-443b-a5ec-5887999a9a8b" />


<img width="1142" height="116" alt="image" src="https://github.com/user-attachments/assets/2ea01483-d4d9-4c1c-be2c-3608849524d5" />


## Создание проекта в IntelliJ IDEA

Проект создали через IntelliJ IDEA: `File → New → Project`. Указали имя `devops-lab0`, систему сборки Gradle. IDEA сгенерировала базовую структуру, но среди всех файлом нам особенно был нужен файл `build.gradle.kts`, чтобы добавить все нужные зависимости и плагины.

<img width="1560" height="1002" alt="image" src="https://github.com/user-attachments/assets/feef3b8a-8418-4432-ac7c-f5d7d190c63e" />


<img width="1700" height="1400" alt="image" src="https://github.com/user-attachments/assets/4f58165a-4b9f-4e42-b91c-fba384ed9bc2" />


## Настройка build.gradle.kts

Самым сложным этапом оказалась настройка зависимостей. Ktor разбит на много модулей, и не все из них включены в BOM (Bill of Materials) — это специальный список, который автоматически подставляет версии для зависимостей. Сначала мы подключили `ktor-server-static-content-jvm` без версии, надеясь, что BOM её подставит, но Gradle падал с ошибкой `Could not find io.ktor:ktor-server-static-content-jvm`. 

<img width="1986" height="320" alt="image" src="https://github.com/user-attachments/assets/487d776d-c3fc-4d56-ac76-b0dd88abd257" />


Оказалось, что отдельный модуль для статики в новых версиях Ktor не нужен — функция `staticResources()`, которую мы используем для отдачи `index.html`, находится внутри `ktor-server-core`. Убрали лишнюю зависимость. Заодно выяснили, что версия Ktor должна совпадать у плагина, BOM и всех артефактов: у нас сначала были разные версии, и Gradle не мог разрешить зависимости. Привели всё к `3.1.3`, и сборка прошла. В итоге в `build.gradle.kts` подключили плагины Kotlin, Ktor и kotlinx.serialization, а в зависимостях — `ktor-server-core`, `ktor-server-netty`, `ktor-server-content-negotiation`, `ktor-serialization-kotlinx-json`, Exposed для работы с БД, драйвер PostgreSQL и logback для логирования. 


<img width="1118" height="226" alt="image" src="https://github.com/user-attachments/assets/10e7fd85-6122-4bf0-bf97-bb59f9a0300f" />


<img width="1486" height="878" alt="image" src="https://github.com/user-attachments/assets/d9223993-aca9-43c6-b142-35859077c969" />


## Написание бэкенда

Бэкенд состоит из двух файлов. В `Database.kt` описали таблицу `cats` через Kotlin DSL от Exposed: `object Cats : Table("cats")` с колонками `id`, `name`, `color`, `createdAt`. Там же функция `initDatabase()`, которая подключается к PostgreSQL, беря параметры из переменных окружения или значений по умолчанию. На Mac пользователь по умолчанию — системный логин, а не `postgres`, поэтому используем `System.getProperty("user.name")`. После подключения вызываем `SchemaUtils.createMissingTablesAndColumns(Cats)` — это создаёт таблицу, если её нет, и ничего не делает, если она уже есть. Это удобно: не нужно следить за тем, создана ли таблица, база сама приведёт схему в порядок.

<img width="1876" height="1406" alt="image" src="https://github.com/user-attachments/assets/74edfb89-ef06-43a0-956a-081b7923b8e4" />


В `Application.kt` описали точку входа и маршруты. Функция `main` запускает `embeddedServer(Netty, port = 8080)` — это встроенный сервер Ktor на движке Netty. Внутри модуля вызываем `initDatabase()`, устанавливаем `ContentNegotiation` с JSON — это плагин, который автоматически превращает Kotlin-объекты в JSON и обратно. В маршрутах описали четыре эндпоинта: `GET /api/health` возвращает `{"status":"ok"}` для проверки, что сервер жив; `GET /api/cats` читает всех кошек из базы и отдаёт списком; `POST /api/cats` принимает JSON с именем и цветом, вставляет запись в базу и возвращает её с присвоенным id; `DELETE /api/cats/{id}` удаляет кошку по id. Также подключили `staticResources("/", "static")`, чтобы Ktor отдавал `index.html` из папки `resources/static/` по корневому пути. Это удобно: фронт и API на одном порту, нет проблем с CORS.

Для сериализации описали два data-класса: `Cat` с полями `id`, `name`, `color` и `CreateCatRequest` с полями `name`, `color`. Аннотация `@Serializable` из kotlinx.serialization говорит компилятору сгенерировать код для превращения объекта в JSON и обратно. Поле `id` сделали nullable (`Int?`) и со значением по умолчанию `null`, потому что до вставки в базу id ещё нет — его генерирует PostgreSQL.


<img width="2036" height="1720" alt="image" src="https://github.com/user-attachments/assets/149b004a-8b27-44be-80c6-b2238b246515" />


<img width="1834" height="1592" alt="image" src="https://github.com/user-attachments/assets/daf53c48-3e90-41de-bb78-c0956dad9b93" />


<img width="1744" height="980" alt="image" src="https://github.com/user-attachments/assets/99e8b586-133d-496c-90c4-ad543d324192" />


При написании бэкенда столкнулись с двумя ошибками компиляции. Первая — `Unresolved reference 'javatime'`: оказалось, что для работы с датой и временем в Exposed нужен отдельный модуль `exposed-java-time`, которого у нас не было. Добавили его в `build.gradle.kts` с той же версией, что и остальные модули Exposed. Вторая — `Unresolved reference 'eq'`: функция сравнения `eq`, используемая в `deleteWhere`, не подтягивалась через `import org.jetbrains.exposed.sql.*`. Добавили явный импорт `org.jetbrains.exposed.sql.SqlExpressionBuilder.eq`, и ошибка исчезла.

## Запуск и проверка

Запустили сервер прямо в терминале IntelliJ IDEA, нажав на зеленый треугольник пуска. Не выходя из терминала, проверили работу API: добавили кошку, получили список, удалили запись — все маршруты отвечают корректно. Затем зашли в базу через `psql` и убедились, что данные действительно сохраняются в PostgreSQL, а не хранятся в памяти: добавленные записи видны в таблице.


<img width="1440" height="660" alt="image" src="https://github.com/user-attachments/assets/e5f99905-6b40-4251-87c4-49b215e391b7" />

