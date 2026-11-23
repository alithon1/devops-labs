Лаба номер 2

docker --version ввела но не нашлось поэтому включила сайт
<img width="1712" height="1270" alt="image" src="https://github.com/user-attachments/assets/225809c0-f9c7-4f59-9c91-d64cd2ad355c" />

и скачала и запустила приложение и появились животные разные
<img width="1554" height="964" alt="image" src="https://github.com/user-attachments/assets/54e5be48-93d5-4ae1-af8c-833193d65256" />

авторизовалась и попала в свой кабинет
<img width="2510" height="1400" alt="image" src="https://github.com/user-attachments/assets/abd2d1bc-75f1-46a0-a613-3a96bf6f027b" />

Зашла обратно в термирнал написала docker --version но отобразилось что докер не найден
<img width="678" height="28" alt="image" src="https://github.com/user-attachments/assets/84228680-ad73-4fa6-8207-ca34dbaf67b5" />

перезапус тила окно терминала и в итоге отобразилась версия 29.9.3
<img width="800" height="30" alt="image" src="https://github.com/user-attachments/assets/fcf31ed6-3f93-467d-9637-cc381e413637" />

исправила на (в коде)
application {
    mainClass.set("com.example.MainKt")
}

ввела chmod +x gradlew в терминале
затем ./gradlew tasks --all | grep -E 'buildFatJar|shadowJar' и успешно вывелось 
buildFatJar - Builds a combined JAR of project and runtime dependencies.
shadowJar - Create a combined JAR of project and runtime dependencies - вот фото
<img width="1122" height="60" alt="image" src="https://github.com/user-attachments/assets/2fc365c1-ca95-4a00-8bd1-58a991f2fb9e" />

ввела ./gradlew buildFatJar и вывелось 
BUILD SUCCESSFUL in 57s
3 actionable tasks: 3 executed - вот фото
<img width="974" height="70" alt="image" src="https://github.com/user-attachments/assets/a483f4e1-92cd-4000-a20a-8971af039ed8" />


ввела ls -lh build/libs/ вывелось 
total 36216
-rw-r--r--@ 1 cubelime  staff    17M 23 ноя 16:10 devops-lab0-all.jar вот фото
<img width="1130" height="60" alt="image" src="https://github.com/user-attachments/assets/afbca2a1-7a7a-4781-8827-0b9ae68f849f" />

создала папки
mkdir -p frontend/html
mkdir -p nginx

скопировала cp src/main/resources/static/index.html frontend/html/

ls frontend/html/ проверила и вывелось index.html вот фото
<img width="672" height="30" alt="image" src="https://github.com/user-attachments/assets/7456f03a-3b58-4594-9c62-6153d72dec5d" />


nano .dockerignore
.git
.gradle
build
.idea
.vscode
.DS_Store
*.iml
*.log

nano Dockerfile.bad 
FROM gradle:8.14.3-jdk21
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew buildFatJar
EXPOSE 8080
CMD ["java", "-jar", "build/libs/devops-lab0-all.jar"]


nano Dockerfile.good {

# ЭТАП 1. СБОРКА ПРИЛОЖЕНИЯ
FROM gradle:8.14.3-jdk21 AS builder

WORKDIR /home/gradle/project

# Сначала копируем только файлы Gradle
COPY --chown=gradle:gradle gradlew settings.gradle.kts build.gradle.kts ./
COPY --chown=gradle:gradle gradle ./gradle

USER gradle

# Загружаем зависимости отдельным слоем
RUN ./gradlew dependencies --no-daemon

# Теперь копируем исходный код
COPY --chown=gradle:gradle src ./src

# Собираем приложение
RUN ./gradlew buildFatJar --no-daemon


# ЭТАП 2. ЗАПУСК ПРИЛОЖЕНИЯ
FROM eclipse-temurin:21-jre-jammy

# Устанавливаем curl для HEALTHCHECK
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Создаём обычного пользователя
RUN groupadd --system app \
    && useradd --system --gid app --home-dir /app \
       --shell /usr/sbin/nologin app \
    && mkdir -p /app \
    && chown app:app /app

WORKDIR /app

# Копируем только готовый JAR
COPY --from=builder --chown=app:app \
    /home/gradle/project/build/libs/devops-lab0-all.jar \
    /app/app.jar

# Запускаем не от root
USER app

EXPOSE 8080

# Проверяем работоспособность Backend
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -fsS http://127.0.0.1:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
}

nano lab0/frontend/Dockerfile {
FROM nginx:1.28-alpine
COPY frontend/html/ /usr/share/nginx/html/
COPY nginx/default.conf /etc/nginx/conf.d/default.conf


cd nginx
nano default.conf
server {
    listen 80;
    server_name localhost;

    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}

в корне проекта я пишу nano docker-compose.yml

services:

  # 1. DATABASE
  db:
    image: postgres:16-alpine
    container_name: vet-db

    environment:
      POSTGRES_DB: vet_clinic
      POSTGRES_USER: vet_user
      POSTGRES_PASSWORD: vet_password

    volumes:
      - postgres_data:/var/lib/postgresql/data

    networks:
      - backend-network

    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U vet_user -d vet_clinic"]
      interval: 10s
      timeout: 5s
      retries: 5

    restart: unless-stopped


  # 2. BACKEND
  backend:
    build:
      context: .
      dockerfile: Dockerfile.good

    container_name: vet-backend

    environment:
      PORT: 8080
      DB_URL: jdbc:postgresql://db:5432/vet_clinic
      DB_USER: vet_user
      DB_PASSWORD: vet_password

    expose:
      - "8080"

    depends_on:
      db:
        condition: service_healthy

    networks:
      - frontend-network
      - backend-network

    restart: unless-stopped


  # 3. FRONTEND
  frontend:
    build:
      context: .
      dockerfile: frontend/Dockerfile

    container_name: vet-frontend

    ports:
      - "8088:80"

    depends_on:
      backend:
        condition: service_healthy

    networks:
      - frontend-network

    restart: unless-stopped


# ИМЕНОВАННЫЙ ТОМ
volumes:
  postgres_data:


# СЕТИ
networks:
  frontend-network:
    driver: bridge

  backend-network:
    driver: bridge
    internal: true

docker compose version все хорошо вот фото
<img width="902" height="24" alt="image" src="https://github.com/user-attachments/assets/5c24c334-661b-4fc1-a457-b5a63cad06ad" />


docker build -f Dockerfile.bad -t vet-backend:bad . вот фото
<img width="1338" height="472" alt="image" src="https://github.com/user-attachments/assets/7c617700-223c-47ff-a5ee-3f134c89013f" />


docker build -f Dockerfile.good -t vet-backend:good . ввожу эту команду
 > [builder 5/7] RUN ./gradlew dependencies --no-daemon:
0.123 Downloading https://services.gradle.org/distributions/gradle-8.14-bin.zip
10.53 .............10%.............20%.............30%.............40%.............50%.............60%.............70%.............80%.............90%..............100%
87.00 
87.00 Welcome to Gradle 8.14!
87.00 
87.00 Here are the highlights of this release:
87.00  - Java 24 support
87.00  - GraalVM Native Image toolchain selection
87.00  - Enhancements to test reporting
87.00  - Build Authoring improvements
87.00 
87.00 For more details see https://docs.gradle.org/8.14/release-notes.html
87.00 
87.00 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.14/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
87.40 Daemon will be stopped at the end of the build 
87.73 
87.73 FAILURE: Build failed with an exception.
87.73 
87.73 * What went wrong:
87.73 Gradle could not start your build.
87.73 > Could not create service of type BuildLifecycleController using ServicesProvider.createBuildLifecycleController().
87.73    > Could not create service of type BuildModelController using VintageBuildControllerProvider.createBuildModelController().
87.73       > Could not create service of type CrossBuildFileHashCache using BuildSessionServices.createCrossBuildFileHashCache().
87.73          > Could not open file hash cache (/home/gradle/project/.gradle/8.14/fileHashes).
87.73             > Cannot create directory '/home/gradle/project/.gradle/8.14/fileHashes'.
87.73 
87.73 * Try:
87.73 > Run with --stacktrace option to get the stack trace.
87.73 > Run with --info or --debug option to get more log output.
87.73 > Run with --scan to get full insights.
87.73 > Get more help at https://help.gradle.org.
87.73 
87.73 BUILD FAILED in 1m 27s
------
Dockerfile.good:14
--------------------
  12 |     
  13 |     # Загружаем зависимости отдельным слоем
  14 | >>> RUN ./gradlew dependencies --no-daemon
  15 |     
  16 |     # Теперь копируем исходный код
--------------------
ERROR: failed to build: failed to solve: process "/bin/sh -c ./gradlew dependencies --no-daemon" did not complete successfully: exit code: 1

What's next:
    Debug this build failure with Gordon → docker ai "help me fix this build failure" вот фото
<img width="2058" height="466" alt="image" src="https://github.com/user-attachments/assets/6308c1dc-4b27-4b7e-bbc5-f22b5bb5da45" />

я добавила 
RUN chmod +x gradlew 
перед USER gradle в файле Dockerfile.good

все равно та же ошибка 

добавляем RUN chown -R gradle:gradle /home/gradle/project
и все прошло успешно вот фото
<img width="1488" height="338" alt="image" src="https://github.com/user-attachments/assets/d7a147d3-f2dc-4106-ae62-b35d2d67f583" />


ввела docker image ls vet-backend и отобразтлось 
                                                                                                                               i Info →   U  In Use
IMAGE              ID             DISK USAGE   CONTENT SIZE   EXTRA
vet-backend:bad    6d38078892c8       2.55GB          901MB        
vet-backend:good   4e398cf71157        427MB          113MB         вот фото
<img width="2122" height="118" alt="image" src="https://github.com/user-attachments/assets/7cc8d2a5-51f4-47eb-ac80-f44e04fcee5c" />

запустила докер docker run -d --name test-backend -p 8080:8080 vet-backend:good но в итоге сразу остановился
<img width="1904" height="224" alt="image" src="https://github.com/user-attachments/assets/f83a6cb6-9b8e-4439-9c73-afdc0f78508f" />
выяснила что это с портом запустила на другом


 запустила docker run -d --name test-backend -p 8081:8080 vet-backend:good
 отобразилось 92b901273ad8dce012a765f37593f55696312fb28f47a1deec10b92cd7d1dcde вот фото
 <img width="1508" height="28" alt="image" src="https://github.com/user-attachments/assets/93287494-db02-4a91-a472-b3b9e1b7915d" />

запутсила docker exec test-backend whoami но 
Error response from daemon: container 92b901273ad8dce012a765f37593f55696312fb28f47a1deec10b92cd7d1dcde is not running вот фото
<img width="1856" height="30" alt="image" src="https://github.com/user-attachments/assets/7e713a9e-6fec-4f36-b5ec-ee029878c5a1" />

docker logs test-backend
выяснилось 
Error: Could not find or load main class com.example.ApplicationKt
Caused by: java.lang.ClassNotFoundException: com.example.ApplicationKt 
вот фото
<img width="1358" height="56" alt="image" src="https://github.com/user-attachments/assets/457684bd-39b4-43cb-a0ef-e80aec49a59e" />

хотя я меняла на MainKt 

удалила docker rm -f test-backend

заустила ./gradlew buildFatJar 
перезупустила докеры docker build -f Dockerfile.good -t vet-backend:good . и docker build -f Dockerfile.bad -t vet-backend:bad .

увидела что я не очищала кеши ох думаю так давай снова  ./gradlew clean buildFatJar
и снова перезапутсила докеры
и все равно та же ошибка 

И до меня доходит что я то меняла в IDEA ApplicationKt на MainKt но не сохранила на диск. Эхх
Меняю в самом корне  нашего проекта с помощью nanp затем снова все пересобираю чтобы удалить кэши

и ничего не срабатывает

не знаю как написать но во первых я оменяла команду docker run -d --name test-backend -p 8081:8080 -e DB_URL=jdbc:postgresql://host.docker.internal:5432/vet_clinic -e DB_USER=vet_user -e DB_PASSWORD=vet_password vet-backend:good то есть которая вместе с базой напиши как перейти к этому

каждый раз я вводила docker exec test-backend whoami и получала эту ошибку
<img width="1742" height="28" alt="image" src="https://github.com/user-attachments/assets/54d30199-b0b4-4381-9c67-13043edb1d18" />

я решила логи посмотреть заново и уже была хорошая новость что старых ошибок уже не было но появлиась новая
<img width="1858" height="164" alt="image" src="https://github.com/user-attachments/assets/5570df6e-bce6-4ea6-9116-2fc186ae3b5f" /> (15:39:28.981 [main] INFO io.ktor.server.Application -- Autoreload is disabled because the development mode is off.
15:39:29.035 [main] WARN Exposed -- Transaction attempt #0 failed: FATAL: role "vet_user" does not exist. Statement(s): null
org.postgresql.util.PSQLException: FATAL: role "vet_user" does not exist)

ввела sudo lsof -i :5432
оказалось COMMAND    PID     USER   FD   TYPE             DEVICE SIZE/OFF NODE NAME
postgres   707 cubelime    7u  IPv6 0x538a9e472a033a87      0t0  TCP localhost:postgresql (LISTEN)
postgres   707 cubelime    8u  IPv4 0x7c1dcdd1d4db1457      0t0  TCP localhost:postgresql (LISTEN)
com.docke 1507 cubelime   79u  IPv6 0x3dd095b28c712ebe      0t0  TCP *:postgresql (LISTEN)

вот фото <img width="1652" height="110" alt="image" src="https://github.com/user-attachments/assets/c2164bdf-7830-4500-b485-f5fb15584825" />

я остноавила -  brew services stop postgresql@16 все успешно вот фото
<img width="1766" height="34" alt="image" src="https://github.com/user-attachments/assets/20509fd5-c4bb-4b3f-ba06-da85f6f715b3" />

удалила контейнер и запустила заново и ввела docker exec test-backend whoami
и получила долгожданное app
<img width="456" height="28" alt="image" src="https://github.com/user-attachments/assets/a597778f-6717-4874-bd39-995d95c97486" />

удаляю docker rm -f test-backend

вызываю nano src/main/kotlin/Main.kt добавляю комментарий вот фото
<img width="762" height="134" alt="image" src="https://github.com/user-attachments/assets/b9c62744-18fa-469b-be89-488781a8b291" />


вызвала time docker build -f Dockerfile.bad -t vet-backend:bad .
docker build -f Dockerfile.bad -t vet-backend:bad .  0,25s user 0,23s system 0% cpu 1:20,77 total вот фото
<img width="1678" height="30" alt="image" src="https://github.com/user-attachments/assets/1edf99ea-23e9-4366-9b99-0383db24ec4c" />

вызвала time docker build -f Dockerfile.good -t vet-backend:good .
docker build -f Dockerfile.good -t vet-backend:good .  0,12s user 0,16s system 1% cpu 21,153 total вот фото
<img width="1494" height="34" alt="image" src="https://github.com/user-attachments/assets/a962ec6c-7946-478d-8cd6-22e74580e485" />

разница очевидна

вызываю
docker compose up --build -d
Сначала завершилось с ошибкой, отключила VPN и в итоге была бесконечная загрузка снова включила VPN та к как это более логично оставить включенным и нашла не закрытый контейнер запустила docker rm -f postgres-for-test

и в итоге все загрузилось вот фото 
<img width="1466" height="280" alt="image" src="https://github.com/user-attachments/assets/3b85007f-081d-44f2-88d7-12dc680ad581" />

запустила docker compose ps и в итоге все три контейнера на месте
<img width="2218" height="116" alt="image" src="https://github.com/user-attachments/assets/6f20a959-95b0-4297-a2e5-0ecd68a1454b" />

открыла http://localhost:8088/ и добавила кошку вот вото гдя я это сделала
<img width="1618" height="448" alt="image" src="https://github.com/user-attachments/assets/3607202b-d724-43cd-8c2a-86b57ade17a8" />
<img width="1608" height="532" alt="image" src="https://github.com/user-attachments/assets/170ae6c7-081f-4d1f-969c-a6df803cdb07" />
также я обновила страницу и все осталось

curl http://localhost:8088/api/health возвращает успешно
вот фото <img width="702" height="32" alt="image" src="https://github.com/user-attachments/assets/d8a0d702-bd6b-4fd7-816c-c1ffadeefbd6" />


удаляем контейнер-  docker compose rm -sf db вот фото <img width="772" height="90" alt="image" src="https://github.com/user-attachments/assets/c08c0dfa-9709-45e4-bb7f-e2f7f51ec050" /> все успешно

так же можно посмотреть на сайт - там пропал наш кот вот фото
<img width="1574" height="392" alt="image" src="https://github.com/user-attachments/assets/f64ffed4-a341-4ff5-826d-19e39bcd33fe" />

проверяем что именновынный том не удалился (docker volume ls) вот фото <img width="1218" height="142" alt="image" src="https://github.com/user-attachments/assets/f68d6ba0-ccd9-4a96-8db2-efb60200ea3d" />

создаем контейнер снова  docker compose up -d db
вот фото
<img width="924" height="58" alt="image" src="https://github.com/user-attachments/assets/53f80acc-0d1e-4ac0-b757-a93fedd63875" />

проверяем docker compose ps - контейнер есть 
<img width="2208" height="116" alt="image" src="https://github.com/user-attachments/assets/537be278-a433-4e4e-860e-966592b7c3ab" />

наш Рыжик вернулся!
<img width="1594" height="510" alt="image" src="https://github.com/user-attachments/assets/aa3d5c3d-9176-4319-89c7-12d439ba732f" />

docker network ls - вот фото <img width="1412" height="168" alt="image" src="https://github.com/user-attachments/assets/82cb2d80-f020-4428-85a2-3b6c2ad82877" />

Проверим, к каким сетям подключён Frontend:
docker inspect vet-frontend - 1 сеть вото фото
<img width="1656" height="702" alt="image" src="https://github.com/user-attachments/assets/22acc9d6-c302-4594-9fe5-0009ca9fdd16" />

backend - 2 сети - вот фото <img width="1662" height="1124" alt="image" src="https://github.com/user-attachments/assets/2c6cbff7-6cc1-41df-9231-6ec671df8392" />


db - 1 сеть - вот фото <img width="1344" height="704" alt="image" src="https://github.com/user-attachments/assets/efab9ab6-cff1-42da-9283-8e45737e8cdf" />


проверяем невозможность подключения docker compose exec frontend sh -c "nc -z -w 3 db 5432" получаем nc: bad address 'db' - вот фото
<img width="1510" height="112" alt="image" src="https://github.com/user-attachments/assets/1199f292-e894-4bb2-aea3-bf5a0b19acf8" />

docker exec vet-backend whoami 
получаем app - вот фото
<img width="674" height="36" alt="image" src="https://github.com/user-attachments/assets/711bdf2c-036c-4af8-9428-c77334ab79da" />


docker inspect --format='{{.State.Health.Status}}' vet-backend - вот фото
<img width="644" height="28" alt="image" src="https://github.com/user-attachments/assets/eccf1192-bc06-4942-8de4-f4061913cb97" />


docker inspect --format='{{.State.Health.Status}}' vet-db - вот фото
<img width="644" height="28" alt="image" src="https://github.com/user-attachments/assets/f0b5e4d6-6d8d-4b45-8cdd-9a1ef6181cc1" />

 все отлично


последняя проверка именнованный том docker volume ls
вот фото - <img width="1350" height="144" alt="image" src="https://github.com/user-attachments/assets/fd7adeaa-4e76-46a0-971d-9e9712f64413" /> то есть отобразилось 
DRIVER    VOLUME NAME
local     2a5944f97119fea2e1ee915c3d7b19743064653d7cbcf9ee907dec86c2326fbe
local     9ac24f7dce5c91e7ea85552c7d293f2ebfe6a9d56b5ba920ebfa15564c1e9f6f
local     52799b8673798a1567cee62f2cde3ccaa2ae7d71e054a1422fb810b1e99d8ddb
local     lab0_postgres_data
