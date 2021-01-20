package com.tuodi.encoderdecoder.tag

import com.tuodi.encoderdecoder.toStringRadix
import com.tuodi.encoderdecoder.toUDecimalNumber
import kotlin.experimental.and

/**
 * @ClassName:      ShenZhenCoderDecoder$
 * @Description:     java类作用描述
 * @Author:         yuan xin
 * @CreateDate:     2021/1/4 0004$
 * @UpdateUser:     更新者：
 * @UpdateDate:     2021/1/4 0004$
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */
/**
 * Version：1
 * UsageType：
 *      0---Books&CDs
 *      1---Patroncard
 *      2---Shelf’stag
 *
 */
fun ByteArray.shenZhenDecode( ): String = run {
    var barcode = ""
    if (this.size > 8) {
        //协议版本号
        val version = this[3] and 0x0f
        //使用类型
        val usageType = (this[3].toInt() shr 4) and 0x07

        val tmp = arrayListOf<Byte>()
        tmp.addAll(this.slice(IntRange(0, 1)).reversed())
        tmp.addAll(this.slice(IntRange(4, 8)).reversed())

        val tmp1 = ByteArray(8)
        System.arraycopy(tmp.toByteArray(), 0, tmp1, 8 - tmp.size, tmp.size)
        tmp1.reverse()
        barcode = tmp1.toStringRadix(16)
        while (barcode.length < 14) {
            barcode = "0$barcode"
        }
    }
    barcode
}
