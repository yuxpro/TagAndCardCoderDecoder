package com.tuodi.encoderdecoder

import com.tuodi.encoderdecoder.CommonUtil.*
import com.tuodi.encoderdecoder.tag.hf.tuChuangDecode
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
        val tmp1=0xf1.toByte()
        println(tmp1.toUnsignedInt())
        println(byte2int(tmp1))
        val tmp =
            "610854443132333435360201B803091B81E440100A0A9999650110"
                .hex2ByteArray()
                .tuChuangDecode()
        println(tmp)
        println("TD123456".tuChuangEncoder().toTargetRadixString(16))
        assertEquals(4, 2 + 2)
    }
}