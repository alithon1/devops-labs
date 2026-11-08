#!/bin/bash
echo "=== 1. Редирект HTTP -> HTTPS ==="
curl -I http://localhost 2>/dev/null | head -n 1

echo -e "\n=== 2. Балансировка (4 запроса) ==="
for i in {1..4}; do curl -sk -i https://localhost/api/health | grep X-Backend-Port; done

echo -e "\n=== 3. /admin без пароля (ожидаем 401) ==="
curl -skI https://localhost/admin | head -n 1

echo -e "\n=== 4. /admin с паролем (ожидаем 200) ==="
curl -skI -u admin:qwerty123 https://localhost/admin | head -n 1

echo -e "\n=== 5. Флуд запросами (ожидаем 429) ==="
for i in {1..10}; do curl -sk -o /dev/null -w "%{http_code} " https://localhost/api/health; done
echo ""

echo -e "\n=== 6. Чужой Host (second.local) ==="
curl -sk https://localhost -H "Host: second.local"

echo -e "\n=== 7. Страница 404 ==="
curl -sk https://localhost/nonexistent
