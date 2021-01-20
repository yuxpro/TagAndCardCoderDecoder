package com.tuodi.encoderdecoder.tag

import com.tuodi.encoderdecoder.toStringRadix
import com.tuodi.encoderdecoder.toUDecimalNumber

/**
 * @ClassName:      YuanWangGuCoderDecoder$
 * @Description:     java类作用描述
 * @Author:         yuan xin
 * @CreateDate:     2021/1/5 0005$
 * @UpdateUser:     更新者：
 * @UpdateDate:     2021/1/5 0005$
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

fun ByteArray.yuanWangGuDecoder(): String = run {
    if (this.size < 12) {
        return@run ""
    }
    val tmp = ByteArray(4)
    System.arraycopy(this, 6, tmp, 0, tmp.size)
    tmp.reverse()
    val tmpInt = tmp.toUDecimalNumber()
    if (tmpInt < 12) {
        return@run ""
    }
    tmpInt.toString().substring(1)
}

fun String.yuanWangGuEncoder(): ByteArray = run {
    val data = "1$this".toByteArray()
    data.reverse()
    ("C20019002000" + data.toStringRadix(16)).toByteArray()
}