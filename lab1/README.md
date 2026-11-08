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



