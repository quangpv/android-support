package com.android.support.app

class InternalServerException : RuntimeException("Error internal server")
class ServerResponseNullException : RuntimeException("Server response no Content")
class ParameterInvalidException(message: String) : RuntimeException(message)

open class ApiRequestException(message: String?) : RuntimeException(message)

class TokenExpiredException :
    RuntimeException("Truy cập hết hạn, hãy đăng nhập lại để sử dụng tiếp")
