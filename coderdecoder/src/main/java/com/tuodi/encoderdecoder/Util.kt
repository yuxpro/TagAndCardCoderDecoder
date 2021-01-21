package com.tuodi.encoderdecoder

import android.text.TextUtils
import com.tuodi.encoderdecoder.CommonUtil.*
import com.tuodi.encoderdecoder.Util.binaryString2byteArray
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

/**
 * @ClassName:      Util$
 * @Description:     java类作用描述
 * @Author:         yuan xin
 * @CreateDate:     2020/12/30 0030$
 * @UpdateUser:     更新者：
 * @UpdateDate:     2020/12/30 0030$
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

/**
 * 2~36进制可以正常转换，2进制以下或者36进制以上的会被转成10进制
 */
fun ByteArray.toStringRadix(
    radix: Int = 10,
    isNegative: Boolean = false,
    isUpper: Boolean = true
): String =
    if (isUpper) {
        BigInteger(if (isNegative) -1 else 1, this).toString(radix).toUpperCase(Locale.CHINA)
    } else {
        BigInteger(if (isNegative) -1 else 1, this).toString(radix).toLowerCase(Locale.CHINA)
    }


/**
 * 字符串转成ASCII字符集的byte数组
 */
fun String.toAsciiByteArray() = this.toByteArray(Charsets.US_ASCII)

/**
 * byte数组转成ASCII字符串
 */
fun ByteArray.toAsciiString() = this.toString(Charsets.US_ASCII)

/**
 * String转成ASCII字符串
 */
fun String.toAsciiString(): String = hexStr2AsciiStr(this)

/**
 * 字符串转成UTF16字符集的byte数组
 */
fun String.toUTF16ByteArray() = this.toByteArray(Charsets.UTF_16)

/**
 * byte数组转成十进制数字
 */
fun ByteArray.toUDecimalNumber() = run {
    val binStr = this.toStringRadix(2).reversed()
    var result = 0L
    var exp2 = 1L
    binStr.reversed().forEach { char ->
        if (char == '1') {
            result += exp2
        }
        exp2 *= 2
    }
    result
}

/**
 * 章誉物联读卡器读取的数据每个块开头会加入一个字节标识是否加锁
 */
fun ByteArray.zhangYuParse() = run {
    if (this.size > 10 &&
        this[0] == 0.toByte() &&
        this[5] == 0.toByte() &&
        this[10] == 0.toByte()
    ) {
        val tmp = arrayListOf<Byte>()
        var i = 1
        while (i + 4 < this.size) {
            tmp.addAll(this.slice(IntRange(i, i + 3)))
            i += 5
        }
        tmp.toByteArray()
    } else {
        this.copyOf()
    }
}

/**
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * ISO/IEC 15962 编解码
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 */
/**
 * 解码
 *
 * decode	名称	        描述
 * 000	    基于应用程序	    由应用程序自描述，默认是十六进制字符串
 * 001	    整型	        十进制字符串
 * 010	    数字	        十六进制字符串
 * 011	    5位码	        大写字母
 * 100	    6位码	        大写字母、数字等
 * 101	    7位码	        GB 1988中规定的字符
 * 110	    8位码字符串	    不变的8-bit（默认GB /T 15273.1），即ASCII解码
 * 111	    GB 13000字符串	GB 13000外部编码
 */
fun ByteArray.decode2Str(codeDecodeType: Int) =
    when (codeDecodeType) {
        DEFAULT -> {
            toStringRadix(16)
        }
        DECIMAL -> {
            toStringRadix(10)
        }
        HEX -> {
            toStringRadix(16)
        }
        BIT5 -> {
            bit5Decode()
        }
        BIT6 -> {
            bit6Decode()
        }
        BIT7 -> {
            bit7Decode()
        }
        ASCII -> {
            toAsciiString()
        }
        else -> {
            toStringRadix(16)
        }
    }

/**
 * 编码
 *
 * 字符串按照不同的编解码格式编成byte数组
 */
fun String.encode2ByteArray(codeDecodeType: Int): ByteArray = run {
    when (codeDecodeType) {
        DEFAULT -> {
            toAsciiByteArray()
        }
        DECIMAL -> {
            IntToByteArray(this.toInt())
        }
        HEX -> {
            decodeHex(this)
        }
        BIT5 -> {
            bit5Encode().toAsciiByteArray()
        }
        BIT6 -> {
            bit6Encode().toAsciiByteArray()
        }
        BIT7 -> {
            bit7Encode().toAsciiByteArray()
        }
        ASCII -> {
            toAsciiByteArray()
        }
        else -> {
            decodeHex(this)
        }
    }
}

/**
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * 5bit编解码
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 */
/**
 * 高校图书馆UHF RFID标准 5bit编码
 */
fun String.bit5Encode(): String = run {
    if ("" == this) {
        return@run ""
    }
    val stringBuffer = StringBuffer()
    val dataBytes: ByteArray = this.toAsciiByteArray()
    for (dataByte in dataBytes) {
        val intNum = CommonUtil.byte2int(dataByte)
        if (intNum >= 65 || intNum <= 95) {
            val binaryString = CommonUtil.hexStringToBinary(CommonUtil.Byte2Hex(dataByte))
            stringBuffer.append(binaryString.substring(3, binaryString.length))
        } else {
            return@run ""
        }
    }
    var tranString = stringBuffer.toString()
    var lenght = tranString.length
    val remainder = lenght % 8
    var index = lenght / 8
    //判断余数添加0
    if (remainder > 0) {
        index++
        for (i in 0 until 8 - remainder) {
            tranString += "0"
        }
    }

    lenght += 8 - remainder
    var bitData: String
    stringBuffer.setLength(0)
    for (i in 1..index) {
        val endIndex = i * 8
        bitData = tranString.substring((i - 1) * 8, if (endIndex > lenght) lenght else endIndex)
        stringBuffer.append(binaryStringToHex(bitData))
    }
    return@run stringBuffer.toString()
}

/**
 * 高校图书馆UHF RFID标准 5bit解码
 */
fun ByteArray.bit5Decode(): String = run {
    val binStr = toStringRadix(2)
    val dataLength: Int = binStr.length
    val index = dataLength / 5 + if (dataLength % 5 > 0) 1 else 0
    var bitData: String
    val stringBuffer = StringBuffer()
    for (i in 1..index) {
        val endIndex = i * 5
        bitData = binStr.substring((i - 1) * 5, min(dataLength, endIndex))
        if (i == index && !bitData.contains("1")) {
            continue
        }
        bitData = "010$bitData"
        stringBuffer.append(binaryStringToHex(bitData))
    }
    stringBuffer.toString().toAsciiString()
}

/**
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * 6bit编解码
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 */
/**
 * 高校图书馆UHF RFID标准 6bit编码
 */
fun String.bit6Encode() = run {
    if ("" == this) {
        return@run ""
    }
    val stringBuffer = StringBuffer()
    val dataBytes: ByteArray = this.toAsciiByteArray()
    for (dataByte in dataBytes) {
        val intNum = CommonUtil.byte2int(dataByte)
        if (intNum >= 32 || intNum <= 95) {
            val binaryString = CommonUtil.hexStringToBinary(CommonUtil.Byte2Hex(dataByte))
            //截取最高位两位bit
            stringBuffer.append(binaryString.substring(2, binaryString.length))
        } else {
            return@run ""
        }
    }
    var tranString = stringBuffer.toString()
    var lenght = tranString.length
    val remainder = lenght % 8
    var index = lenght / 8
    //判断余数添加0
    //判断余数添加0
    if (remainder > 0) {
        index++
        //补位
        tranString += "100000".substring(0, 8 - remainder)
        lenght += 8 - remainder //补位后补充长度
    }

    var bitData: String
    stringBuffer.setLength(0)
    for (i in 1..index) {
        val endIndex = i * 8
        bitData = tranString.substring((i - 1) * 8, if (endIndex > lenght) lenght else endIndex)
        stringBuffer.append(binaryStringToHex(bitData))
    }
    return stringBuffer.toString()
}

/**
 * 高校图书馆UHF RFID标准 6bit解码
 */
fun ByteArray.bit6Decode(): String = run {
    val binStr = toStringRadix(2)
    val dataLength: Int = binStr.length
    val index = dataLength / 6 + if (dataLength % 6 > 0) 1 else 0
    var bitData: String
    var bitDatafirst: String
    val stringBuffer = StringBuffer()
    for (i in 1..index) {
        val endIndex = i * 6
        bitData = binStr.substring(
            (i - 1) * 6,
            if (endIndex > dataLength) dataLength else endIndex
        )
        if (i == index) {
            //判断是否遇到最后填充数据(结束标识)
            if (bitData == "10" || bitData == "1000" || bitData == "100000") {
                continue
            }
        }
        bitDatafirst = bitData.substring(0, 1)
        //如第一位bit为"1",前补"00",如为"0",前补"01"
        bitData =
            if (bitDatafirst == "1") {
                "00$bitData"
            } else {
                "01$bitData"
            }
        stringBuffer.append(binaryStringToHex(bitData))
    }
    stringBuffer.toString().toAsciiString()
}


/**
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * 7bit编解码
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 */
/**
 * 高校图书馆UHF RFID标准 7bit编码
 */
fun String.bit7Encode(): String = run {
    if (TextUtils.isEmpty(this)) {
        return@run ""
    }
    val stringBuffer = StringBuffer()
    val dataBytes = this.toAsciiByteArray()
    for (dataByte in dataBytes) {
        val intNum = CommonUtil.byte2int(dataByte)
        if (intNum >= 0 || intNum <= 126) {
            val binaryString = CommonUtil.hexStringToBinary(CommonUtil.Byte2Hex(dataByte))
            stringBuffer.append(binaryString.substring(1, binaryString.length))
        } else {
            return@run ""
        }
    }
    var tranString = stringBuffer.toString()
    var lenght = tranString.length
    val remainder = 8 - lenght % 8
    var index = lenght / 8
    //判断余数添加0
    //判断余数添加0
    if (remainder > 0) {
        index++
        for (i in 0 until remainder) {
            tranString += "1"
        }
    }
    lenght = tranString.length
    var bitData: String
    stringBuffer.setLength(0)
    for (i in 1..index) {
        val endIndex = i * 8
        bitData = tranString.substring((i - 1) * 8, if (endIndex > lenght) lenght else endIndex)
        stringBuffer.append(binaryStringToHex(bitData))
    }
    return@run stringBuffer.toString()
}

/**
 * 高校图书馆UHF RFID标准 7bit解码
 */
fun ByteArray.bit7Decode(): String = run {
    val binStr = toStringRadix(2)
    val dataLength: Int = binStr.length
    val index = dataLength / 7 + if (dataLength % 7 > 0) 1 else 0
    var bitData: String
    val stringBuffer = StringBuffer()
    for (i in 1..index) {
        val endIndex = i * 7
        bitData = binStr.substring(
            (i - 1) * 7,
            if (endIndex > dataLength) dataLength else endIndex
        )
        if (i == index) {
            if (TextUtils.equals(
                    "1",
                    if (bitData.isNotEmpty()) bitData.substring(0, 1) else bitData
                )
            ) continue
        }
        bitData = "0$bitData"
        stringBuffer.append(binaryStringToHex(bitData))
    }
    stringBuffer.toString().toAsciiString()
}


/**
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * ISIL预编码
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 */
private var step = 5

//0:大写,1:小写,2:数字
private var letterType = 0
private var letterTypeOld = 0
private var isShift = false
private val upperLetter = arrayOf(
    "-", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
    "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", ":"
)
private val lowerLetter = arrayOf(
    "-", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
    "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "/"
)
private val digits = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-", ";")

/**
 * ISIL解码
 *
 */
fun ByteArray.isilDecode(): String {
    val binStrings = this.toStringRadix(2)
    val length = binStrings.length
    step = 5
    letterType = 0
    letterTypeOld = 0
    isShift = false
    val data = StringBuilder()
    var pos = 0
    while (pos + step < length) {
        val str = binStrings.substring(pos, pos + step)
        pos += step
        val covert: Int = Util.binary2Decimal(str)
        if (letterType == 0 || letterType == 1) {
            if (covert == 28 || covert == 29) {
                if (covert == 29) {
                    //暂时转换
                    isShift = true
                    letterTypeOld = letterType
                }
                letterType = if (letterType == 0) {
                    1
                } else {
                    0
                }
                step = 5
            } else if (covert == 30 || covert == 31) {
                if (covert == 31) {
                    isShift = true
                    letterTypeOld = letterType
                }
                letterType = 2
                step = 4
            } else {
                if (letterType == 0) {
                    data.append(upperLetter[covert])
                } else {
                    data.append(lowerLetter[covert])
                }
                switchLetterType()
            }
        } else {
            if (covert == 12 || covert == 13) {
                if (covert == 13) {
                    isShift = true
                    letterTypeOld = letterType
                }
                letterType = 0
                step = 5
            } else if (covert == 14 || covert == 15) {
                if (covert == 15) {
                    isShift = true
                    letterTypeOld = letterType
                }
                letterType = 1
                step = 5
            } else {
                data.append(digits[covert])
                switchLetterType()
            }
        }
    }
    return data.toString()
}

/**
 * ISIL编码
 */
fun String.isilEncode(): ByteArray = run {
    if (!isISILBarcode(this)) { //首位必需大写
        return@run ByteArray(0)
    }
    var characterSet = 0 //当前处于的字符集,0为大写集,1为小写集,2为数据集
    var nextCharacterSet = 0 //下一字符的字符集
    val sb = StringBuilder()
    //初始化map,根据条码某位值与某字符集中提取int值
    val upperMap: MutableMap<String, Int> = TreeMap()
    val lowerMap: MutableMap<String, Int> = TreeMap()
    val numMap: MutableMap<String, Int> = TreeMap()
    for (i in upperLetter.indices) {
        upperMap[upperLetter[i]] = i
        lowerMap[lowerLetter[i]] = i
        if (i < digits.size) {
            numMap[digits[i]] = i
        }
    }
    val maps: ArrayList<Map<String, Int>> = ArrayList()
    maps.add(0, upperMap)
    maps.add(1, lowerMap)
    maps.add(2, numMap)
    for (i in this.indices) {
        //获取第i位对应的int值
        val binary = maps[nextCharacterSet][this.substring(i, i + 1)]

        binary?.let {
            //把int值转化为二进制字符
            val binaryStr = hexString2binaryString(Byte2Hex(binary.toByte()))
            //判断所用字符集,大小写字符集截取后面5位,数字字符集截取后面4位
            if (nextCharacterSet == 0 || nextCharacterSet == 1) {
                sb.append(binaryStr.substring(3))
            } else {
                sb.append(binaryStr.substring(4))
            }
        }

        if (i + 1 < this.length) {
            //获取下一位字符所处字符集
            nextCharacterSet = getISILCharacterSetForindex(this[i + 1], characterSet)
            //判断是否与现所处字符集一样
            if (characterSet == nextCharacterSet) {
                continue
            }

            //获取下下一位字符所处字符集
            val nnextCharacterSet: Int =
                if (i + 2 < this.length) {
                    getISILCharacterSetForindex(this[i + 2], nextCharacterSet)
                } else {
                    nextCharacterSet
                }
            if (nextCharacterSet == nnextCharacterSet) { //后两位字符属同一字符集,添加锁定符
                if (characterSet < 2) {
                    if (nextCharacterSet < 2) {
                        sb.append("11100")
                    } else {
                        sb.append("11110")
                    }
                } else {
                    if (nextCharacterSet == 0) {
                        sb.append("1100")
                    } else {
                        sb.append("1110")
                    }
                }
                characterSet = nextCharacterSet
            } else { //侯连伟字符不同字符集,添加转换符
                if (characterSet < 2) {
                    if (nextCharacterSet < 2) {
                        sb.append("11101")
                    } else {
                        sb.append("11111")
                    }
                } else {
                    if (nextCharacterSet == 0) {
                        sb.append("1101")
                    } else {
                        sb.append("1111")
                    }
                }
            }
        }
    }
    var datasStr = sb.toString()
    val length = datasStr.length
    if (length % 8 > 0) { //不足8位用1补齐
        for (i in 0 until 8 - length % 8) {
            datasStr += "1"
        }
    }
    binaryString2byteArray(datasStr)
}

private fun switchLetterType() {
    if (isShift) {
        letterType = letterTypeOld
        isShift = false
        step = if (letterType == 0 || letterType == 1) {
            5
        } else {
            4
        }
    }
}

/**
 * 筛选字符属于哪个字符集
 * @param c
 * @param defaultCharacterSet
 */
fun getISILCharacterSetForindex(c: Char, defaultCharacterSet: Int): Int {
    return if (Character.isDigit(c) || c == ';') {
        2
    } else if (Character.isLowerCase(c) || c == '/') {
        1
    } else if (Character.isUpperCase(c) || c == ':') {
        0
    } else {
        defaultCharacterSet
    }
}

/**
 * 判断传入的barcode是否符合ISIL规则
 * @param data
 * @return
 */
fun isISILBarcode(data: String?): Boolean {
    //判断是否为空
    if (data.isNullOrEmpty()) {
        return false
    }
    //判断首字符是否为大写
    if (!Character.isUpperCase(data[0])) {
        return false
    }
    //正则判断字符是否正规,大小写字母 数字 : ; / -
    return data.matches(Regex("^[a-z0-9A-Z:;/-]+$"))
}
