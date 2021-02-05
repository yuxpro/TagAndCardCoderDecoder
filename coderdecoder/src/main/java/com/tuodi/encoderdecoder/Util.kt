package com.tuodi.encoderdecoder

import android.text.TextUtils
import com.tuodi.encoderdecoder.CommonUtil.*
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
 * ////////////////////////////////////////////////////////////////////////////////////////////////
 * @注意：kotlin的toByteArray，是将字符转成ASCII对应的十进制数
 * ////////////////////////////////////////////////////////////////////////////////////////////////
 */


/**
 * 2~36进制可以正常转换，2进制以下或者36进制以上的会被转成10进制
 */
fun ByteArray.toTargetRadixString(
    targetRadix: Int = 10,
    isNegative: Boolean = false,
    isUpper: Boolean = true
): String =
    if (isUpper) {
        BigInteger(if (isNegative) -1 else 1, this).toString(targetRadix).toUpperCase(Locale.ROOT)
    } else {
        BigInteger(if (isNegative) -1 else 1, this).toString(targetRadix).toLowerCase(Locale.ROOT)
    }

/**
 * ByteArray to ASCII String
 */
fun ByteArray.toAsciiString() = this.toString(Charsets.US_ASCII)

/**
 * String to ASCII ByteArray
 */
fun String.toAsciiByteArray() = this.toByteArray(Charsets.US_ASCII)

/**
 * String to ASCII hex ByteArray
 */
fun String.toAsciiHexByteArray() = run {
    val tmp = toAsciiByteArray().toTargetRadixString(16)
    val size = tmp.length / 2 + tmp.length % 2
    val result = ByteArray(size)
    for (i in 0 until tmp.length / 2) {
        result[i] = tmp.substring(i * 2, (i + 1) * 2).toByte()
    }
    if (tmp.length % 2 == 1) {
        result[size - 1] = ("0" + tmp.substring(size - 1, size)).toByte()
    }
    result
}

/**
 * Byte转成无符号整数
 */
fun Byte.toUnsignedInt() =
    if (toInt() < 0) {
        256 + toInt()
    } else {
        toInt()
    }

/**
 * 获取字符串的进制，很笨的方法，只能获取可能的最小进制数，往往不准确
 */
const val radixs = "0123456789abcdefghijklmnopqrstuvwxyz"
fun String.getPossibleRadix() = run {
    var radix = 0
    this.forEach { char ->
        radixs.forEachIndexed { index, c ->
            if (c == char && radix <= index) {
                radix = index + 1
            }
        }
    }
    radix
}

/**
 * X Radix String to Y Radix String (X、Y can be same)
 */
fun String.toTargetRadixString(
    originalRadix: Int = 10,
    targetRadix: Int = 10,
    isUpper: Boolean = true
) =
    if (isUpper) {
        BigInteger(this, originalRadix).toString(targetRadix).toUpperCase(Locale.ROOT)
    } else {
        BigInteger(this, originalRadix).toString(targetRadix).toLowerCase(Locale.ROOT)
    }

fun String.toAsciiString(originalRadix: Int = 10) =
    toTargetRadixString(originalRadix, 16).hex2AsciiString()

/**
 * hex String to ASCII Sting
 */
fun String.hex2AsciiString(): String = run {
    var sb = ""
    var i = 0
    while (i < this.length - 1) {
        //grab the hex in pairs
        val output: String = this.substring(i, i + 2)
        val hex = output.toInt(16)
        //convert the hex to character
        sb += (hex.toChar())
        i += 2
    }
    return sb
}

/**
 * 16进制字符串转4位2进制字符串
 */
fun String.hexTo4BitBinary(): String = run {
    val hex = this.toUpperCase(Locale.ROOT)
    val result = java.lang.StringBuilder()
    val max: Int = hex.length
    for (i in 0 until max) {
        when (hex[i]) {
            '0' -> result.append("0000")
            '1' -> result.append("0001")
            '2' -> result.append("0010")
            '3' -> result.append("0011")
            '4' -> result.append("0100")
            '5' -> result.append("0101")
            '6' -> result.append("0110")
            '7' -> result.append("0111")
            '8' -> result.append("1000")
            '9' -> result.append("1001")
            'A' -> result.append("1010")
            'B' -> result.append("1011")
            'C' -> result.append("1100")
            'D' -> result.append("1101")
            'E' -> result.append("1110")
            'F' -> result.append("1111")
        }
    }
    return result.toString()
}

/**
 * 2进制字符串转ByteArray
 */
fun String.bin2ByteArray(): ByteArray = run {
    val stringBuilder = StringBuilder(this)
    // 注：这里in.length() 不可在for循环内调用，因为长度在变化
    val remainder = stringBuilder.length % 8
    if (remainder > 0) {
        for (i in 0 until 8 - remainder) {
            stringBuilder.append("0")//不是8的倍数，在后面补0
        }
    }
    val bts = ByteArray(stringBuilder.length / 8)
    // Step 8 Apply compression
    for (i in bts.indices) {
        bts[i] = stringBuilder.substring(i * 8, (i + 1) * 8).toInt(2).toByte()
    }
    return bts
}

/**
 * 16进制字符串转ByteArray
 */
fun String.hex2ByteArray(): ByteArray = run {
    hexTo4BitBinary().bin2ByteArray()
}

/**
 * byte数组转成十进制数字
 */
fun ByteArray.toUDecimalNumber() = run {
    val binStr = this.toTargetRadixString(2).reversed()
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
            toTargetRadixString(16)
        }
        DECIMAL -> {
            toTargetRadixString(10)
        }
        HEX -> {
            toTargetRadixString(16)
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
            toTargetRadixString(16)
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
 * 自己编写的ISO/IEC 15962
 * <p>
 * ISO/IEC 15962数据结构：
 * 前导字节(1byte或2byte)+[偏移量(1byte)]+压缩后的数据长度+压缩后的数据+[补齐的空字节]
 * <p>
 * 压缩方式：
 * 0：自定义，这里是转成16进制字符串
 * 1：整型压缩，2~19位十进制数字
 * 2：十进制数字压缩，任意十进制数字
 * 3：5bit压缩，字符都在ASCII的41~5F之间
 * 4：6bit压缩，字符都在ASCII的20~5F之间
 * 5：7bit压缩，字符都在ASCII的00~7F之间
 * 6：8bit压缩，字符都在ASCII的00~FF之间
 * 7：GB 13000外部压缩：暂时没有具体编解码方式
 * <p>
 * OID类型：
 * 1：馆藏
 * 3：馆代码
 * 5：标签类型
 *
 * <p>
 * 默认不对OID加锁，即不需要补齐4n个byte，没有偏移
 */
fun String.iso15962Encode(
    @androidx.annotation.IntRange(
        from = 0,
        to = 6
    ) compressType: Int? = null,
    oid: Int
) {
    var compressMethod = getCompressType(compressType)
    var preamble="0"+compressMethod
}

/**
 * 获取ISO/IEC 15962的压缩方式
 */
fun String.getCompressType(compressType: Int?) = run {
    var compress: String
    when (compressType) {
        null -> {
            compress = "000"
            if (this.matches(Regex("^[0~9]{2,19}$"))) {
                compress = "001"
            } else if (this.matches(Regex("^[0~9]+$"))) {
                compress = "010"
            }
            this.forEach {
                if (0x41 <= it.toByte() && it.toByte() <= (0x5F).toByte()) {
                    compress = "011"
                } else if (0x20 <= it.toByte() && it.toByte() <= (0x5F).toByte()) {
                    compress = "100"
                } else if (0x00 <= it.toByte() && it.toByte() <= (0x7F).toByte()) {
                    compress = "101"
                } else if (0x00 <= it.toByte() && it.toByte() <= (0xFF).toByte()) {
                    compress = "110"
                }
            }
            compress
        }
        0 -> "000"
        1 -> "001"
        2 -> "010"
        3 -> "011"
        4 -> "100"
        5 -> "101"
        6 -> "110"
        else -> "000"
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
    val dataBytes: ByteArray = this.toByteArray()
    for (dataByte in dataBytes) {
        val intNum = dataByte.toUnsignedInt()
        if (intNum >= 65 || intNum <= 95) {
            val binaryString = hexStringToBinary(Byte2Hex(dataByte))
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
    val binStr = toTargetRadixString(2)
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
    stringBuffer.toString().hex2AsciiString()
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
        val intNum = dataByte.toUnsignedInt()
        if (intNum >= 32 || intNum <= 95) {
            val binaryString = hexStringToBinary(Byte2Hex(dataByte))
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
    val binStr = toTargetRadixString(2)
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
    stringBuffer.toString().hex2AsciiString()
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
        val intNum = dataByte.toUnsignedInt()
        if (intNum >= 0 || intNum <= 126) {
            val binaryString = hexStringToBinary(Byte2Hex(dataByte))
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
    val binStr = toTargetRadixString(2)
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
    stringBuffer.toString().hex2AsciiString()
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
    val binStrings = this.toTargetRadixString(2)
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
        val covert: Int = str.toTargetRadixString(2, 10).toInt()
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
            val binaryStr = Byte2Hex(binary.toByte()).hexTo4BitBinary()
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
    datasStr.bin2ByteArray()
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

