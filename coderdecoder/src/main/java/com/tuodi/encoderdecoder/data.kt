package com.tuodi.encoderdecoder

/**
 * @ClassName:      data$
 * @Description:     java类作用描述
 * @Author:         yuan xin
 * @CreateDate:     2020/12/30 0030$
 * @UpdateUser:     更新者：
 * @UpdateDate:     2020/12/30 0030$
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

const val DEFAULT = 0
const val DECIMAL = 1
const val HEX = 2
const val BIT5 = 3
const val BIT6 = 4
const val BIT7 = 5
const val ASCII = 6

data class RFIDInfo(
    var offsetIdentifier: Int = 0,//偏移量标识
    var codeDecodeType: Int = ASCII,//编码范围：0~6
    var oid: Int = 1,//当OID为1-14时(offsetIdentifier + encodeRange + oid)占一个字节；当OID为15-127时OID占一个字节
    var offset: Int = 0,//偏移量
    var length: Int = 0,//数据长度
    var prefix: Int? = null,//前缀
    var data: ByteArray//内容
)


/**
 * 返回可以写入图创、感创标签或卡的byte数组
 */
fun RFIDInfo.getRawByteArray(alignment: Boolean = true): ByteArray = run {
    val tmp = arrayListOf<Byte>()
    if (data.isEmpty()) {
        return@run tmp.toByteArray()
    }
    if (alignment) {//偏移量按照4字节一个地址对齐
        var length = 1 + 1 + data.size
        if (oid > 14) {
            length += 1
        }
        if (length % 4 == 0) {
            offset = 0
            offsetIdentifier = 0
        } else {
            offset = if (length > 4) (length % 4) else (4 - length)
            offsetIdentifier = 1
            if ((length + 1) % 4 == 0) {
                offset = 0
            }
        }
    }
    val lead =
        if (oid > 14) {//前导字节
            ((offsetIdentifier shl 7) or (codeDecodeType shl 4) or 0x0f).toByte()
        } else {
            ((offsetIdentifier shl 7) or (codeDecodeType shl 4) or oid).toByte()
        }
    tmp.add(lead)
    if (oid > 14) {
        tmp.add((oid - 15).toByte())//相对oid
    }
    if (offsetIdentifier == 1) {//有偏移量
        tmp.add(offset.toByte())
    }
    tmp.add(length.toByte())//数据长度
    tmp.addAll(data.toList())//数据
    if (offsetIdentifier == 1 && offset > 0) {//有偏移量
        tmp.addAll(ByteArray(offset).toList())//偏移长度的字节
    }
    tmp.toByteArray()
}

/**
 * 根据oid获取对应编码的data字符串
 */
fun RFIDInfo.getDataEncodeStrByOid() = run {
    if (data.isEmpty()) {
        return@run ""
    }
    when (oid) {
        1 -> {
            data.decode2Str(codeDecodeType)
        }
        3 -> {
            data.isilDecode()
        }
        else -> {
            data.toAsciiString()
        }
    }
}