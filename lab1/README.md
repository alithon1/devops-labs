Лабораторная работа №1 — nginx

brew install nginx
<img width="1190" height="356" alt="image" src="https://github.com/user-attachments/assets/d9d953b4-af24-4f1f-b641-fa8ed60d3358" />


brew install mkcert nss

<img width="1406" height="666" alt="image" src="https://github.com/user-attachments/assets/11875b5b-44c0-4c3e-8edf-ac38eb8373ed" />


mkcert -install

<img width="884" height="36" alt="image" src="https://github.com/user-attachments/assets/3777edab-b056-410e-a665-174fad1d2aa8" />

боль в том что я не могла вспомнить пароль sudo так как думала что это отдельный пароль пока не поняла что он и есть пароль при входе в учетную запись


unset JAVA_HOME
mkcert localhost

<img width="1188" height="190" alt="image" src="https://github.com/user-attachments/assets/ed5e2fe8-df99-463c-85bb-52a05812d885" />

nginx -t

<img width="1146" height="58" alt="image" src="https://github.com/user-attachments/assets/700db6cb-e88f-4c24-9b63-a2b1e17fe397" />

brew services restart nginx

<img width="1268" height="32" alt="image" src="https://github.com/user-attachments/assets/b212d117-93c5-4a2e-8587-580820ef13bf" />

curl -I http://localhost
<img width="804" height="204" alt="image" src="https://github.com/user-attachments/assets/2e1940a3-c07b-46c8-8bf5-82339fb68fb8" />

Он не видит наш новый конфиг

<img width="1444" height="596" alt="image" src="https://github.com/user-attachments/assets/a87cc0ed-d1f0-41a3-8cc2-b773e13bdce5" />

brew services restart nginx

<img width="1170" height="86" alt="image" src="https://github.com/user-attachments/assets/4d0c76f6-4ad1-4286-be3c-87b9b816cd89" />

curl -I http://localhost
<img width="1410" height="34" alt="image" src="https://github.com/user-attachments/assets/7fee7cb4-dda1-4e7d-bc87-3c9ff8461bb2" />

lsof -i :8080

<img width="1354" height="62" alt="image" src="https://github.com/user-attachments/assets/d995d816-1d50-4f3d-b3a5-f9d287adf738" />

curl http://localhost:8080/api/cats - отобразилось []  


curl -k https://localhost/api/cats

<img width="1420" height="32" alt="image" src="https://github.com/user-attachments/assets/fcc29c23-af81-4efd-99eb-06d018f16dc0" />

tail -n 15 /opt/homebrew/var/log/nginx/error.log
<img width="1550" height="592" alt="image" src="https://github.com/user-attachments/assets/b1c7e4fa-f624-4858-bd5a-bec6926020c1" />

grep -rn "8080" /opt/homebrew/etc/nginx/ 

<img width="1312" height="148" alt="image" src="https://github.com/user-attachments/assets/b5df70a0-14d8-4ff9-bd84-30fa1822fe3b" />


curl -k https://localhost/api/cats - отобразилось []


<img width="1666" height="1334" alt="image" src="https://github.com/user-attachments/assets/afc66b4e-8cb7-4b3d-b6d9-37ffb8dc10c1" />

<img width="972" height="976" alt="image" src="https://github.com/user-attachments/assets/22bc9ae3-f127-4cad-a9f4-6f5f0b3f9336" />

<img width="550" height="330" alt="image" src="https://github.com/user-attachments/assets/0ffd3206-bf7b-45ee-9af6-47bf935b4143" />

nano /opt/homebrew/etc/nginx/servers/lab1.conf
изменила файл

nginx -t

<img width="1142" height="56" alt="image" src="https://github.com/user-attachments/assets/30aa1344-f0df-4051-90d4-a7eca30ef995" />


nginx -t

<img width="1260" height="62" alt="image" src="https://github.com/user-attachments/assets/fc3508b2-f128-4a79-a97c-6ef1ab026903" />

brew services restart nginx

for i in {1..4}; do curl -sk -i https://localhost/api/health | grep X-Backend-Port; done

<img width="620" height="136" alt="image" src="https://github.com/user-attachments/assets/875cf4f1-b224-4f9f-b011-c3762cb29777" />


mkdir -p ~/Desktop/devops-labs/lab1/docs
echo "Это документация нашего проекта" > ~/Desktop/devops-labs/lab1/docs/readme.txt

nginx -t
<img width="1260" height="58" alt="image" src="https://github.com/user-attachments/assets/be5d4880-9267-40ec-834d-b64f5c772555" />

brew services restart nginx

<img width="1084" height="80" alt="image" src="https://github.com/user-attachments/assets/4256df7a-b52e-4f7f-8d7e-3135ac17538e" />

<img width="1118" height="644" alt="image" src="https://github.com/user-attachments/assets/29ec735b-22a1-474e-bcaa-40c9d6c06ce4" />

Решение проблем :
sudo chmod 644 /opt/homebrew/etc/nginx/.htpasswd
добавление строчки location /api {
        limit_req_status 429;

<img width="952" height="642" alt="image" src="https://github.com/user-attachments/assets/55d40f85-a0f0-403b-99ab-e08771279729" />

ls -la /opt/homebrew/etc/nginx/.htpasswd

<img width="1248" height="32" alt="image" src="https://github.com/user-attachments/assets/368f54e1-2588-44d0-9434-7ab8f5eb36d2" />


tail -n 20 /opt/homebrew/var/log/nginx/error.log

<img width="2622" height="564" alt="image" src="https://github.com/user-attachments/assets/92c1ac42-5fbb-4a1d-af83-1508705e7f1b" />

nginx -V 2>&1 | grep -o with-http_auth_basic_module ничего не вывелось - заново переустановили

htpasswd -c -b /opt/homebrew/etc/nginx/.htpasswd admin qwerty123
chmod 644 /opt/homebrew/etc/nginx/.htpasswd
перезапускаем
 
curl -kI -u admin:qwerty123 https://localhost/admin

<img width="784" height="206" alt="image" src="https://github.com/user-attachments/assets/e3432a50-97e8-43be-9456-2ee9da3c2a02" />

Выяснилось что у меня прблема в самом скрипте у меня там была отпечатка в самом пароле






