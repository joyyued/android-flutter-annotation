package com.joyy.neza_annotation.model

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 2:55 下午
 * @email: 56002982@qq.com
 * @des: Method channel 的回调结果
 */
data class MethodChannelResult(
    val resultType: MethodChannelResultType,
    val successResult: SuccessResult? = null,
    val errorResult: ErrorResult? = null,
)

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 2:57 下午
 * @email: 56002982@qq.com
 * @des: 成功的数据
 */
data class SuccessResult(
    val result: Any?
)

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 2:57 下午
 * @email: 56002982@qq.com
 * @des: 失败的数据
 */
data class ErrorResult(
    val errorCode: String?,
    val errorMessage: String?,
    val errorDetails: Any?,
)

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 2:54 下午
 * @email: 56002982@qq.com
 * @des: Method channel 的回调结果类型
 */
enum class MethodChannelResultType {
    SUCCESS,
    ERROR,
    NOT_IMPLEMENTED,
}