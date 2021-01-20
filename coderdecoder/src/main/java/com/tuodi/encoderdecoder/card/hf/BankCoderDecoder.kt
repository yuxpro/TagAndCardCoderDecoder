package com.tuodi.encoderdecoder.card

import com.tuodi.encoderdecoder.toAsciiString

/**
 * @ClassName:      BankCoderDecoder$
 * @Description:     java类作用描述
 * @Author:         yuan xin
 * @CreateDate:     2021/1/5 0005$
 * @UpdateUser:     更新者：
 * @UpdateDate:     2021/1/5 0005$
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

fun ByteArray.bankDecoder(): String = run {
    this.toAsciiString()
}

fun String.bankEncoder() {
    this.toByteArray()
}