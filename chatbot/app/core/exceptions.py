from datetime import datetime

from fastapi import Request
from fastapi.responses import JSONResponse


class BusinessException(Exception):
    """도메인 에러 코드(CHAT_번호)를 포함하는 예외.

    백엔드(Spring) BusinessException/ApiResponse와 동일한 응답 형태
    ({success, data, message, code, timestamp})로 변환된다.
    """

    def __init__(self, message: str, status_code: int, code: str):
        self.message = message
        self.status_code = status_code
        self.code = code
        super().__init__(message)


async def business_exception_handler(request: Request, exc: BusinessException) -> JSONResponse:
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "success": False,
            "data": None,
            "message": exc.message,
            "code": exc.code,
            "timestamp": datetime.now().isoformat(timespec="seconds"),
        },
    )
