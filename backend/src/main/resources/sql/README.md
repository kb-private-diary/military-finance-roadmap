# DB 스크립트

| 파일 | 설명 |
|---|---|
| `kb-schema.sql` | 테이블 생성 DDL (52개 테이블) |
| `kb-data.sql` | 테스트/초기 데이터 (INSERT 53건) |

> DB 이름은 **`scoula_db`** 로 통일합니다. (`backend/src/main/resources/application.properties` 기준)

---

## 1. MySQL 실행 방법

### 방법 A. 터미널(CMD/PowerShell)에서 실행 — 가장 빠름

```bash
# 1) DB 생성 (최초 1회, 한글 깨짐 방지 위해 utf8mb4)
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS scoula_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 2) 이 폴더(sql/)로 이동 후 스크립트 실행
mysql -u root -p scoula_db < kb-schema.sql
mysql -u root -p scoula_db < kb-data.sql
```
> `-u root` 부분은 본인 MySQL 계정으로 바꾸세요. 비밀번호는 실행하면 물어봅니다.

### 방법 B. MySQL Workbench에서 실행

1. Workbench 접속 → 상단 **SQL 편집기**에 아래 입력 후 실행(⚡)
   ```sql
   CREATE DATABASE IF NOT EXISTS scoula_db
     DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   USE scoula_db;
   ```
2. `File → Open SQL Script` 로 **`kb-schema.sql`** 열고 실행(⚡)
3. 같은 방법으로 **`kb-data.sql`** 열고 실행(⚡)

> ⚠️ 반드시 **kb-schema.sql → kb-data.sql 순서**로 실행하세요.

### 2. 잘 들어갔는지 확인

```sql
USE scoula_db;
SHOW TABLES;                      -- 52개 나오면 OK
SELECT COUNT(*) FROM `user`;      -- 3
SELECT COUNT(*) FROM car_goal;    -- 4
SELECT COUNT(*) FROM saving_account; -- 6
```

---

## 3. 백엔드 접속 설정

`backend/src/main/resources/application.properties` 는 기본적으로 아래 계정으로 접속합니다.

```properties
jdbc.url=jdbc:log4jdbc:mysql://localhost:3306/scoula_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
jdbc.username=scoula
jdbc.password=1234
```

로컬에 `scoula` 계정이 없다면 **둘 중 하나**를 하세요.

```sql
-- (1) scoula 계정 만들기 (권장)
CREATE USER IF NOT EXISTS 'scoula'@'localhost' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON scoula_db.* TO 'scoula'@'localhost';
FLUSH PRIVILEGES;
```
```
-- (2) 또는 application.properties 의 username/password 를 본인 계정으로 수정
```

---

## 4. 주의사항

- **데이터를 다시 넣을 땐 `kb-schema.sql` 부터 재실행**하세요.
  `kb-schema.sql` 에 `DROP TABLE IF EXISTS` 가 있어 테이블이 새로 만들어지고 AUTO_INCREMENT 도 1로 리셋됩니다.
  `DELETE` 로만 지우고 다시 넣으면 AUTO_INCREMENT 가 이어져서 번호가 밀리고 참조가 깨질 수 있습니다.
- `kb-data.sql` 은 앞뒤가 `SET FOREIGN_KEY_CHECKS = 0 / 1` 로 감싸져 있어 **INSERT 순서를 신경 쓰지 않아도** 됩니다.
  단, FK 검사를 끄고 넣는 것이므로 **참조 값 자체가 틀려도 에러 없이 들어갑니다.** 데이터 수정 시 참조가 맞는지 직접 확인하세요.
- `user_bookmark` 는 시드 데이터가 없습니다 (참조하는 테이블이 없어 문제 없음, 선택사항).
