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

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")

FSS_API_KEY = os.getenv("FSS_API_KEY", "")
CHEONGYAKHOME_API_KEY = os.getenv("CHEONGYAKHOME_API_KEY", "")
FUND_API_KEY = os.getenv("FUND_API_KEY", "")
ONTONG_YOUTH_API_KEY = os.getenv("ONTONG_YOUTH_API_KEY", "")
