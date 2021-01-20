package com.tuodi.encoderdecoder.tag

import com.tuodi.encoderdecoder.*
import kotlin.experimental.and
import kotlin.experimental.xor

/**
 * 解码
 *
 * 将读取到的byte数组转成String
 */
fun ByteArray.ganChuangDecode( ): String = run {
    var result = ""
    //章誉物联读卡器读取的数据每个块开头会加入一个字节标识是否加锁
    val newData = zhangYuParse()
    val rfidInfo = newData.getRFIDInfo().firstOrNull { it.oid == 1 }
    rfidInfo?.let {
        result = it.data.decode2Str(it.codeDecodeType)
        if (it.prefix ?: 0 > 0) {
            result = it.data.toStringRadix(10)
        }
    }
    result
}

/**
 * 编码
 *
 * @param libCode ：馆代码
 * @param rfidType :标签或卡的标识符 读者证 0x80 图书标签 0x10/0x12 层架标 0x4f
 * @param codeDecodeType ：编解码方式
 */
fun String.ganCHuangEncode(libCode: String, rfidType: Byte, codeDecodeType: Int): ByteArray =
    run {
        val rfids = arrayListOf<RFIDInfo>()
        var dataByteArr = this.encode2ByteArray(codeDecodeType)
        rfids.add(
            RFIDInfo(
                oid = 1,
                data = dataByteArr,
                length = dataByteArr.size,
                codeDecodeType = codeDecodeType
            )
        )
        dataByteArr = ByteArray(1) { 0xa0.toByte() }
        rfids.add(
            RFIDInfo(
                oid = 2,
                data = dataByteArr,
                length = dataByteArr.size,
                codeDecodeType = codeDecodeType
            )
        )
        dataByteArr = libCode.encode2ByteArray(ASCII)
        rfids.add(
            RFIDInfo(
                oid = 3,
                data = dataByteArr,
                length = dataByteArr.size,
                codeDecodeType = codeDecodeType
            )
        )
        dataByteArr = ByteArray(1) { rfidType }
        rfids.add(
            RFIDInfo(
                oid = 5,
                data = dataByteArr,
                length = dataByteArr.size,
                codeDecodeType = codeDecodeType
            )
        )

        val tmp = arrayListOf<Byte>()
        rfids.sortedByDescending { it.oid }.forEach {
            tmp.addAll(it.getRawByteArray().toTypedArray())
        }
        val dataByteArray = tmp.toByteArray()

        dataByteArray.getNewData()
    }

/**
 * 获取RFID信息
 */
fun ByteArray.getRFIDInfo( ): ArrayList<RFIDInfo> = run {
    val newData = getNewData()

    val rfidInfoList = arrayListOf<RFIDInfo>()
    var i = 0
    newData.forEach { byte ->
        if (i >= newData.size) {
            return@forEach
        }
        var oid = (byte and 0xff.toByte()).toInt()
        if (oid == 0) {
            return@forEach
        }
        if (oid > 14) {
            i++
            oid += byte
        }

        val offsetIdentifier = byte.toInt().shr(7)
        val encodeRange = (byte.toInt().shl(1) and 0xff).shr(5)
        val offset: Int
        var prefix: Int? = null
        val datas: ByteArray

        i++
        val length: Int = byte.toInt()
        if (offsetIdentifier == 1) {
            offset = byte.toInt()
            i++
            if (oid == 1 && encodeRange == 2) {
                prefix = byte.toInt()
                i++
            }
        } else {
            offset = 0
            if (length > newData.size) {
                return@forEach
            }
        }
        i++
        datas = ByteArray(length)
        System.arraycopy(newData, i, datas, 0, length)
        rfidInfoList.add(
            RFIDInfo(
                offsetIdentifier = offsetIdentifier,
                codeDecodeType = encodeRange,
                oid = oid,
                offset = offset,
                length = length,
                prefix = prefix,
                data = datas
            )
        )
        i += offset + length
    }
    rfidInfoList
}


fun ByteArray.getNewData( ) = run {
    val key = "This is SENSEIT RFID ".toAsciiByteArray()
    val hexTmp = arrayListOf<Byte>()
    hexTmp.add(this[0])
    key.forEachIndexed { index, byte ->
        if (index >= key.size) {
            return@forEachIndexed
        }
        if (index >= this.size-2) {
            return@forEachIndexed
        }
        hexTmp.add(this[index + 1] xor byte)
    }
    hexTmp.toByteArray()
}
