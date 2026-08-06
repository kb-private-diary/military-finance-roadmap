import os
from dotenv import load_dotenv

load_dotenv()

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "3306")
DB_USER = os.getenv("DB_USER", "root")
DB_PASSWORD = os.getenv("DB_PASSWORD", "")
DB_NAME = os.getenv("DB_NAME", "kb_chatbot")

DATABASE_URL = (
    f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}?charset=utf8mb4"
)

# Java 백엔드(JwtProcessor)와 동일한 시크릿 - 로그인 시 발급된 토큰을 여기서도 검증해야 함.
# application-secret.properties에 jwt.secret 오버라이드가 없으면 Java 쪽도 이 기본값을 그대로 쓴다.
JWT_SECRET = os.getenv("JWT_SECRET", "충분히긴임의의(랜덤한) 비밀키문자열배정")

# Java의 Keys.hmacShaKeyFor(secretBytes)는 시크릿의 "바이트 길이"에 따라 서명 알고리즘을 자동으로 고른다
# (~47바이트=HS256, 48~63바이트=HS384, 64바이트+=HS512). 그래서 시크릿을 커스텀 값으로 바꾸면 팀원마다
# 실제로 서명되는 알고리즘이 달라질 수 있다 - 하나로 못박으면 그 팀원만 401/500이 나므로, 후보를 다 허용한다.
JWT_ALGORITHMS = ["HS256", "HS384", "HS512"]

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")

FSS_API_KEY = os.getenv("FSS_API_KEY", "")
CHEONGYAKHOME_API_KEY = os.getenv("CHEONGYAKHOME_API_KEY", "")
FUND_API_KEY = os.getenv("FUND_API_KEY", "")
ONTONG_YOUTH_API_KEY = os.getenv("ONTONG_YOUTH_API_KEY", "")
