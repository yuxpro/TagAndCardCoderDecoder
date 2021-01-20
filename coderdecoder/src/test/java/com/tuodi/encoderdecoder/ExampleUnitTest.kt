package com.tuodi.encoderdecoder

import com.tuodi.encoderdecoder.CommonUtil.*
import com.tuodi.encoderdecoder.tag.hf.tuChuangEncoder
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        //1.toString(16)
//        val tmp= ByteArray(3)
//        tmp[0]=(0x21).toByte()
//        tmp[1]=(0x21).toByte()
//        tmp[2]=(0x21).toByte()
////        println(tmp.slice(IntRange(0,2)))
////        println(tmp.toUDecimalNumber())
//
//        println(33.toString(16).toAsciiString())
//        println(tmp.toString(Charsets.US_ASCII))
//        println(tmp.toAsciiString())
//        println(tmp[0].toString(16))
//        println(33.toString(16))
//        println(CommonUtil.hexStr2AsciiStr(33.toString(16)))

        val tmp = "TD123456".tuChuangEncoder()
        tmp.forEach {
            println(it)
        }
        println()
        println(tmp.toStringRadix(16))
        //java:   6510353434343331333233333334333533360201B803091B81E440100A0A9999050110
        //kotlin: 6108 5444313233343536 0201 B803091B81E440100A0A9999   650110
        //正确答案:6108 5444313233343536 0201 B803091B81E440100A0A9999FF 6501102D 39393939050180000000000000000000
        //        前导码OID1             OID2 OID3                       OID5     OID13

        assertEquals(4, 2 + 2)
    }
}