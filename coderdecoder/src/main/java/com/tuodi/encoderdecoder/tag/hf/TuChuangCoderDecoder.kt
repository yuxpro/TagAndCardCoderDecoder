package com.tuodi.encoderdecoder.tag.hf

import com.tuodi.encoderdecoder.*
import kotlin.experimental.and

/**
 * @ClassName:      TuChuangCoderDecoder$
 * @Description:     java类作用描述
 * @Author:         yuan xin
 * @CreateDate:     2021/1/4 0004$
 * @UpdateUser:     更新者：
 * @UpdateDate:     2021/1/4 0004$
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

/**
 * string：
 *      TD123456
 * byteArray：
 *      610854443132333435360201B8030A1B81E440100A0A9999FF6501102D39393939050180000000000000000000(后面的0是补全，不同位数的0会不同）
 */
fun ByteArray.tuChuangDecode(decodeStrLength: Int? = null): String = run {
    var result = ""
    //章誉物联读卡器读取的数据每个块开头会加入一个字节标识是否加锁
    val newData = this.zhangYuParse()
    if (newData.size > 4) {
        var index = 0
        val rfids = arrayListOf<RFIDInfo>()
        while (newData[index] != 0.toByte()) {
            val rfid = RFIDInfo(data = ByteArray(0))
            rfid.offset = (newData[index] and (0x80).toByte()).toInt() shr 7
            rfid.codeDecodeType = (newData[index] and (0x70).toByte()).toInt() shr 4
            rfid.oid = (newData[index] and 0x0f).toInt()

            var dataSizeOffset: Int
            var offset: Int
            if (rfid.offset == 1) {
                if (index >= newData.size - 4) {
                    break
                }
                dataSizeOffset = 1
                offset = newData[index + 1].toInt()
            } else {
                if (index >= newData.size - 3) {
                    break
                }
                dataSizeOffset = 0
                offset = 0
            }
            val dataSize = newData[index + 1 + dataSizeOffset].toInt()
            rfid.data = ByteArray(dataSize)
            if (rfid.oid == 0x0f) {
                rfid.oid = dataSize + 15
                System.arraycopy(newData, index + 3 + dataSizeOffset, rfid.data, 0, dataSize)
                index += 3 + dataSizeOffset + dataSize + offset
            } else {
                System.arraycopy(newData, index + 2 + dataSizeOffset, rfid.data, 0, dataSize)
                index += 2 + dataSizeOffset + dataSize
            }
            rfids.add(rfid)
        }
        if (index != 0) {//说明经过了上述while循环操作
            val rfid1 = rfids.firstOrNull { it.oid == 1 }
            val rfid3 = rfids.firstOrNull { it.oid == 3 }
            val rfid5 = rfids.firstOrNull { it.oid == 5 }
            rfid3?.let {
                val libCode = it.getDataEncodeStrByOid()
                if (libCode.isNotEmpty()) {

                }
            }
            rfid1?.let {
                result = it.getDataEncodeStrByOid()
            }
        }
    }
    decodeStrLength?.let {
        if (result.length > decodeStrLength) {
            result = result.substring(0, decodeStrLength)
        }
    }
    result
}

/**
 * 编码
 *
 * string：
 *      TD123456
 * byteArray：
 *      610854443132333435360201B8030A1B81E440100A0A9999FF6501102D39393939050180000000000000000000(后面的0是补全，不同位数的0会不同）
 *
 * @param libCode ：馆代码
 * @param rfidType :标签或卡的标识符 读者证 0x80 图书标签 0x10/0x12 层架标 0x4f
 * @param codeDecodeType ：编解码方式
 */
fun String.tuChuangEncoder(
    libCode: String = "CN-440100-0-9999",
    rfidType: Byte = 0x10,
    codeDecodeType: Int = ASCII
): ByteArray = run {

    val rfids = arrayListOf<RFIDInfo>()
    var dataByteArr = encode2ByteArray(codeDecodeType)
    rfids.add(
        RFIDInfo(
            oid = 1,
            data = dataByteArr,
            length = dataByteArr.size,
            codeDecodeType = codeDecodeType
        )
    )

    dataByteArr = ByteArray(1) {0XB8.toByte() }
    rfids.add(
        RFIDInfo(
            oid = 2,
            data = dataByteArr,
            length = dataByteArr.size,
            codeDecodeType = DEFAULT
        )
    )
    dataByteArr = libCode.isilEncode()
    rfids.add(
        RFIDInfo(
            oid = 3,
            data = dataByteArr,
            length = dataByteArr.size,
            codeDecodeType = DEFAULT
        )
    )
    dataByteArr = ByteArray(1) { rfidType }
    rfids.add(
        RFIDInfo(
            oid = 5,
            data = dataByteArr,
            length = dataByteArr.size,
            codeDecodeType = DEFAULT
        )
    )

    val result = arrayListOf<Byte>()

    rfids.sortedBy { it.oid }.forEach {
        val tmp=it.getRawByteArray(false)
        if (it.oid == 1) {
//            result[0] = result[0] or (0x80).toByte()
//            val fixByte = result[1] % 2
//            result.add(1, fixByte.toByte())
        }
        if (it.oid == 5) {
            //前导字节
            val lead = (6 shl 4 or it.oid).toByte()
            tmp[0] = lead
        }
        result.addAll(tmp.toTypedArray())
    }
    result.toByteArray()
}