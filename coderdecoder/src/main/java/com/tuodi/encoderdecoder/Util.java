package com.tuodi.encoderdecoder;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.tuodi.encoderdecoder.CommonUtil.Byte2Hex;

/**
 * @ClassName: Util$
 * @Description: java类作用描述
 * @Author: yuan xin
 * @CreateDate: 2021/1/19 0019$
 * @UpdateUser: 更新者：
 * @UpdateDate: 2021/1/19 0019$
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
class Util {

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////
     * default编解码 0
     * ////////////////////////////////////////////////////////////////////////////////////////////
     */
    public static String defaultDecode(byte[] data, int type) {
        return  CommonUtil.ByteArrToHex(data);
    }

    public static String defaultEncode(String data) {
        return data;
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////
     * Decimal编解码 1
     * ////////////////////////////////////////////////////////////////////////////////////////////
     */
    public static String decimalDecode(byte[] data, int type) {
        return String.valueOf(Long.parseLong(CommonUtil.encodeHexStr(data), 16));
    }

    public static String decimalEncode(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        byte[] datas = CommonUtil.IntToByteArray(Integer.parseInt(data));
        return CommonUtil.ByteArrToHexToNoNULL(datas);
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////
     * Hex编解码 2
     * ////////////////////////////////////////////////////////////////////////////////////////////
     */
    public static String hexDecode(byte[] data,int type) {
        return  CommonUtil.ByteArrToHex(data);
    }

    public static  String hexEncode(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        return data;
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////
     * 5bit编解码 3
     * ////////////////////////////////////////////////////////////////////////////////////////////
     */
    public static String GIVE_UP_BIT_5_FIRST = "010";

    public static String bit5Decode(byte[] data, int type) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            stringBuilder.append(CommonUtil.hexStringToBinary(CommonUtil.Byte2Hex(data[i])));
        }
        String dataString = stringBuilder.toString();
        return handlerBit5(dataString);

    }

    public static String bit5Encode(String data) {
        return processDataBit5(data);
    }

    private static String handlerBit5(String dataString) {
        int dataLenght = dataString.length();
        int index = dataLenght / 5 + ((dataLenght % 5) > 0 ? 1 : 0);
        String bitData = "";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= index; i++) {
            int endIndex = i * 5;
            bitData = dataString.substring((i - 1) * 5, Math.min(endIndex, dataLenght));
            if (i == index) {
                if (!bitData.contains("1"))
                    continue;
            }
            bitData = GIVE_UP_BIT_5_FIRST + bitData;
            stringBuilder.append(CommonUtil.binaryStringToHex(bitData));
        }

        return CommonUtil.hex2Ascii(stringBuilder.toString());
    }

    private static int min5Value = 65;
    private static int max5Value = 95;

    public static String processDataBit5(String data) {
        if ("".equals(data) || data == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        byte[] dataBytes = data.getBytes();
        for (byte dataByte : dataBytes) {
            int intNum = CommonUtil.byte2int(dataByte);
            if (intNum >= min5Value || intNum <= max5Value) {
                String binaryString = CommonUtil.hexStringToBinary(CommonUtil.Byte2Hex(dataByte));
                stringBuilder.append(binaryString.substring(3, binaryString.length()));
            } else {
                //                break;
                return "";
            }
        }
        String tranString = stringBuilder.toString();
        int lenght = tranString.length();
        int remainder = (lenght % 8);
        int index = lenght / 8;
        //判断余数添加0
        if (remainder > 0) {
            index++;
            for (int i = 0; i < 8 - remainder; i++) {
                tranString += "0";
            }
        }

        lenght += 8 - remainder;
        String bitData = "";
        stringBuilder.setLength(0);
        for (int i = 1; i <= index; i++) {
            int endIndex = i * 8;
            bitData = tranString.substring((i - 1) * 8, endIndex > lenght ? lenght : endIndex);
            stringBuilder.append(CommonUtil.binaryStringToHex(bitData));
        }
        return stringBuilder.toString();
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////
     * 6bit编解码 4
     * ////////////////////////////////////////////////////////////////////////////////////////////
     */

    public static String GIVE_UP_BIT_6_1 = "10";
    public static String GIVE_UP_BIT_6_2 = "1000";
    public static String GIVE_UP_BIT_6_3 = "100000";
    public static String GIVE_UP_BIT_6_FIRST_1 = "1";
    public static String GIVE_UP_BIT_6_FIRST_0 = "0";
    public static String GIVE_UP_BIT_6_FIRST_00 = "00";
    public static String GIVE_UP_BIT_6_FIRST_01 = "01";

    public static String bit6Decode(byte[] data, int type) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            stringBuilder.append(CommonUtil.hexStringToBinary(CommonUtil.Byte2Hex(data[i])));
        }
        String dataString = stringBuilder.toString();

        return handlerBit6(dataString);

    }

    public static String bit6Encode(String data) {
        return processDataBit6(data);
    }

    private static String handlerBit6(String dataString) {
        int dataLenght = dataString.length();
        //int index = dataLenght / 6 + (dataLenght % 6) > 0 ? 1 : 0;//判断不明作用
        int index = dataLenght / 6 + ((dataLenght % 6) > 0 ? 1 : 0);
        String bitData = "";
        String bitDatafirst = "";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= index; i++) {
            int endIndex = i * 6;
            bitData = dataString.substring((i - 1) * 6, Math.min(endIndex, dataLenght));
            if (i == index) {
                //判断是否遇到最后填充数据(结束标识)
                if (bitData.equals(GIVE_UP_BIT_6_1) || bitData.equals(GIVE_UP_BIT_6_2) ||
                        bitData.equals(GIVE_UP_BIT_6_3)) {

                    continue;
                }

            }
            bitDatafirst = bitData.substring(0, 1);
            //            if (TextUtils.equals(bitDatafirst, GIVE_UP_BIT_6_FIRST_1)) {
            //如第一位bit为"1",前补"00",如为"0",前补"01"
            if (bitDatafirst.equals(GIVE_UP_BIT_6_FIRST_1)) {
                bitData = GIVE_UP_BIT_6_FIRST_00 + bitData;
            } else {
                bitData = GIVE_UP_BIT_6_FIRST_01 + bitData;
            }
            stringBuilder.append(CommonUtil.binaryStringToHex(bitData));
        }
        return CommonUtil.hex2Ascii(stringBuilder.toString());
    }

    private static int min6Value = 32;
    private static int max6Value = 95;

    public static String processDataBit6(String data) {
        if ("".equals(data) || data == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        byte[] dataBytes = data.getBytes();
        for (byte dataByte : dataBytes) {
            int intNum = CommonUtil.byte2int(dataByte);
            if (intNum >= min6Value || intNum <= max6Value) {
                String binaryString = CommonUtil.hexStringToBinary(CommonUtil.Byte2Hex(dataByte));
                //截取最高位两位bit
                stringBuilder.append(binaryString.substring(2, binaryString.length()));
            } else {
                return "";
            }
        }
        String tranString = stringBuilder.toString();
        int lenght = tranString.length();
        int remainder = (lenght % 8);
        int index = lenght / 8;
        //判断余数添加0
        if (remainder > 0) {
            index++;
            //补位
            tranString += GIVE_UP_BIT_6_3.substring(0, 8 - remainder);
            lenght += 8 - remainder;//补位后补充长度
        }

        String bitData = "";
        stringBuilder.setLength(0);
        for (int i = 1; i <= index; i++) {
            int endIndex = i * 8;
            bitData = tranString.substring((i - 1) * 8, Math.min(endIndex, lenght));
            stringBuilder.append(CommonUtil.binaryStringToHex(bitData));
        }
        return stringBuilder.toString();
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////
     * 7bit编解码 5
     * ////////////////////////////////////////////////////////////////////////////////////////////
     */
    public static String GIVE_UP_BIT_7_FIRST = "0";

    public static String bit7Decode(byte[] data, int type) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte datum : data) {
            stringBuilder.append(CommonUtil.hexStringToBinary(CommonUtil.Byte2Hex(datum)));
        }
        String dataString = stringBuilder.toString();

        return handlerBit7(dataString);

    }

    public static String bit7Encode(String data) {
        return processDataBit7(data);
    }

    private static String handlerBit7(String dataString) {
        int dataLenght = dataString.length();
        int index = dataLenght / 7 + ((dataLenght % 7) > 0 ? 1 : 0);
        String bitData = "";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= index; i++) {
            int endIndex = i * 7;
            bitData = dataString.substring((i - 1) * 7, Math.min(endIndex, dataLenght));
            if (i == index) {
                if (TextUtils.equals("1", bitData.length()>0?bitData.substring(0,1):bitData))
                    continue;
            }
            bitData = GIVE_UP_BIT_7_FIRST + bitData;
            stringBuilder.append(CommonUtil.binaryStringToHex(bitData));
        }
        return CommonUtil.hex2Ascii(stringBuilder.toString());
        //return stringBuilder.toString();
    }

    private static int min7Value = 0;
    private static int max7Value = 126;
    public static String processDataBit7(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        byte[] dataBytes = data.getBytes();
        for (byte dataByte : dataBytes) {
            int intNum = CommonUtil.byte2int(dataByte);
            if (intNum >= min7Value || intNum <= max7Value) {
                String binaryString = CommonUtil.hexStringToBinary(CommonUtil.Byte2Hex(dataByte));
                stringBuilder.append(binaryString.substring(1));
            } else {
                return "";
            }
        }
        StringBuilder tranString = new StringBuilder(stringBuilder.toString());
        int lenght = tranString.length();
        int remainder = 8-(lenght % 8);
        int index = lenght / 8;
        //判断余数添加0
        if (remainder > 0) {
            index++;
            for (int i = 0; i < remainder; i++) {
                tranString.append("1");
            }
        }
        lenght=tranString.length();
        String bitData = "";
        stringBuilder.setLength(0);
        for (int i = 1; i <= index; i++) {
            int endIndex = i * 8;
            bitData = tranString.substring((i - 1) * 8, Math.min(endIndex, lenght));
            stringBuilder.append(CommonUtil.binaryStringToHex(bitData));
        }
        return stringBuilder.toString();
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////
     * ASCII编解码 6
     * ////////////////////////////////////////////////////////////////////////////////////////////
     */
    public static String asciiDecode(byte[] data, int type) {
        return new String(data);
    }

    public static String asciiEncode(String data) {
        if ("".equals(data) || data == null) {
            return "";
        }
        return CommonUtil.ByteArrToHexToNoNULL(
                data.getBytes());
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////
     * ISIL编解码
     * ////////////////////////////////////////////////////////////////////////////////////////////
     */
    private static int step = 5;
    //0:大写,1:小写,2:数字
    private static int letterType = 0;
    private static int letterTypeOld = 0;
    private static boolean isShift = false;
    private static final String[] upperLetter = {"-", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", ":"};
    private static final String[] lowerLetter = {"-", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
            "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "/"};
    private static final String[] digits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-", ";"};

    public static String ISILDecode(String byteStrings) {
        int length = byteStrings.length();
        step = 5;
        letterType = 0;
        letterTypeOld = 0;
        isShift = false;
        StringBuilder barcode = new StringBuilder();
        for (int pos = 0; pos + step < length; ) {
            String str = byteStrings.substring(pos, pos + step);
            System.out.println(str);
            pos += step;
            int covert = binary2Decimal(str);
            if (letterType == 0 || letterType == 1) {
                if (covert == 28 || covert == 29) {
                    if (covert == 29) {
                        //暂时转换
                        isShift = true;
                        letterTypeOld = letterType;
                    }
                    if (letterType == 0) {
                        letterType = 1;
                    } else {
                        letterType = 0;
                    }
                    step = 5;
                } else if (covert == 30 || covert == 31) {
                    if (covert == 31) {
                        isShift = true;
                        letterTypeOld = letterType;
                    }
                    letterType = 2;
                    step = 4;
                } else {
                    if (letterType == 0) {
                        barcode.append(upperLetter[covert]);
                    } else {
                        barcode.append(lowerLetter[covert]);
                    }
                    switchLetterType();
                }
            } else {
                if (covert == 12 || covert == 13) {
                    if (covert == 13) {
                        isShift = true;
                        letterTypeOld = letterType;
                    }
                    letterType = 0;
                    step = 5;
                } else if (covert == 14 || covert == 15) {
                    if (covert == 15) {
                        isShift = true;
                        letterTypeOld = letterType;
                    }
                    letterType = 1;
                    step = 5;
                } else {
                    barcode.append(digits[covert]);
                    switchLetterType();
                }
            }
        }
        return barcode.toString();
    }

    public static byte[] ISILEncode(String barcode) {

        if (!isISILBarcode(barcode)) {//首位必需大写
            return null;
        }
        byte[] datas;
        int characterSet = 0;//当前处于的字符集,0为大写集,1为小写集,2为数据集
        int nextCharacterSet = 0;//下一字符的字符集
        StringBuilder sb = new StringBuilder();
        //初始化map,根据条码某位值与某字符集中提取int值
        Map<String, Integer> upperMap = new TreeMap<>();
        Map<String, Integer> lowerMap = new TreeMap<>();
        Map<String, Integer> numMap = new TreeMap<>();
        for (int i = 0; i < upperLetter.length; i++) {
            upperMap.put(upperLetter[i], i);
            lowerMap.put(lowerLetter[i], i);
            if (i < digits.length) {
                numMap.put(digits[i], i);
            }
        }
        List<Map<String, Integer>> maps = new ArrayList<>();
        maps.add(0, upperMap);
        maps.add(1, lowerMap);
        maps.add(2, numMap);


        for (int i = 0; i < barcode.length(); i++) {
            //获取第i位对应的int值
            int binary = maps.get(nextCharacterSet).get(barcode.substring(i, i + 1));
            //把int值转化为二进制字符串
            String binaryStr = hexStringToBinary(Byte2Hex((byte) binary));
            //判断所用字符集,大小写字符集截取后面5位,数字字符集截取后面4位
            if (nextCharacterSet == 0 || nextCharacterSet == 1) {
                sb.append(binaryStr.substring(3));
            } else {
                sb.append(binaryStr.substring(4));
            }


            if (i + 1 < barcode.length()) {
                //获取下一位字符所处字符集
                nextCharacterSet = getISILCharacterSetForindex(barcode.charAt(i + 1), characterSet);
                int nnextCharacterSet;
                //判断是否与现所处字符集一样
                if (characterSet == nextCharacterSet) {
                    continue;
                }

                //获取下下一位字符所处字符集
                if (i + 2 < barcode.length()) {
                    nnextCharacterSet = getISILCharacterSetForindex(barcode.charAt(i + 2), nextCharacterSet);
                } else {
                    nnextCharacterSet = nextCharacterSet;
                }
                if (nextCharacterSet == nnextCharacterSet) {//后两位字符属同一字符集,添加锁定符
                    if (characterSet < 2) {
                        if (nextCharacterSet < 2) {
                            sb.append("11100");
                        } else {
                            sb.append("11110");
                        }
                    } else {
                        if (nextCharacterSet == 0) {
                            sb.append("1100");
                        } else {
                            sb.append("1110");
                        }
                    }
                    characterSet = nextCharacterSet;
                } else {//侯连伟字符不同字符集,添加转换符
                    if (characterSet < 2) {
                        if (nextCharacterSet < 2) {
                            sb.append("11101");
                        } else {
                            sb.append("11111");
                        }
                    } else {
                        if (nextCharacterSet == 0) {
                            sb.append("1101");
                        } else {
                            sb.append("1111");
                        }
                    }
                }
            }

        }
        String datasStr = sb.toString();
        int length = datasStr.length();
        if (length % 8 > 0) {//不足8位用1补齐
            for (int i = 0; i < 8 - length % 8; i++) {
                datasStr = datasStr + "1";
            }
        }
        datas = binaryString2byteArray(datasStr);
        return datas;
    }

    private static void switchLetterType() {
        if (isShift) {
            letterType = letterTypeOld;
            isShift = false;
            if (letterType == 0 || letterType == 1) {
                step = 5;
            } else {
                step = 4;
            }
        }
    }

    /**
     * 筛选字符属于哪个字符集
     */
    public static int getISILCharacterSetForindex(char c, int defaultCharacterSet) {
        int nextCharacterSet;
        if (Character.isDigit(c) || c == ';') {
            nextCharacterSet = 2;
        } else if (Character.isLowerCase(c) || c == '/') {
            nextCharacterSet = 1;
        } else if (Character.isUpperCase(c) || c == ':') {
            nextCharacterSet = 0;
        } else {
            nextCharacterSet = defaultCharacterSet;
        }
        return nextCharacterSet;
    }

    /**
     * 判断传入的barcode是否符合ISIL规则
     */
    public static boolean isISILBarcode(String barcode) {
        //判断是否为空
        if (barcode == null || "".equals(barcode)) {
            return false;
        }
        //判断首字符是否为大写
        if (!Character.isUpperCase(barcode.charAt(0))) {
            return false;
        }
        //正则判断字符是否正规,大小写字母 数字 : ; / -
        String rex = "^[a-z0-9A-Z:;/-]+$";
        return barcode.matches(rex);
    }

    /**
     * 二进制字符串转byte数组
     */
    public static byte[] binaryString2byteArray(String input) {
        StringBuilder in = new StringBuilder(input);
        // 注：这里in.length() 不可在for循环内调用，因为长度在变化
        int remainder = in.length() % 8;
        if (remainder > 0) {
            for (int i = 0; i < 8 - remainder; i++) {
                in.append("0");
            }
        }

        byte[] bts = new byte[in.length() / 8];

        // Step 8 Apply compression
        for (int i = 0; i < bts.length; i++) {
            bts[i] = (byte) Integer.parseInt(in.substring(i * 8, i * 8 + 8), 2);
        }

        return bts;
    }

    /**
     * 十六转二进制
     */
    public static String hexStringToBinary(String hex) {
        hex = hex.toUpperCase();
        StringBuilder result = new StringBuilder();
        int max = hex.length();
        for (int i = 0; i < max; i++) {
            char c = hex.charAt(i);
            switch (c) {
                case '0':
                    result.append("0000");
                    break;
                case '1':
                    result.append("0001");
                    break;
                case '2':
                    result.append("0010");
                    break;
                case '3':
                    result.append("0011");
                    break;
                case '4':
                    result.append("0100");
                    break;
                case '5':
                    result.append("0101");
                    break;
                case '6':
                    result.append("0110");
                    break;
                case '7':
                    result.append("0111");
                    break;
                case '8':
                    result.append("1000");
                    break;
                case '9':
                    result.append("1001");
                    break;
                case 'A':
                    result.append("1010");
                    break;
                case 'B':
                    result.append("1011");
                    break;
                case 'C':
                    result.append("1100");
                    break;
                case 'D':
                    result.append("1101");
                    break;
                case 'E':
                    result.append("1110");
                    break;
                case 'F':
                    result.append("1111");
                    break;
            }
        }
        return result.toString();
    }


    /**
     * 二进制转十进制
     */
    public static int binary2Decimal(String number) {
        return scale2Decimal(number, 2);
    }

    /**
     * 其他进制转十进制
     */
    public static int scale2Decimal(String num, int scale) {
        String number = num;
        System.out.println(number);
        if (scale <= 10) {
            checkNumber(number);
        }
        if (2 > scale || scale > 32) {
            throw new IllegalArgumentException("scale is not in range");
        }
        // 不同其他进制转十进制,修改这里即可
        int total = 0;
        char[] ch = number.toCharArray();
        int chLength = ch.length;
        System.out.println(chLength);
        for (int i = 0; i < chLength; i++) {
            //Log.i("binbin", "scale2Decimal: "+ch[i]);
            System.out.println("ch[i]" + ch[i]);
            total += Integer.parseInt(ch[i] + "", 16) * Math.pow(scale, chLength - 1 - i);
        }
        return total;

    }

    /**
     * 检查是否是数字
     */
    public static void checkNumber(String number) {
        String regexp = "^\\d+$";
        if (null == number || !number.matches(regexp)) {
            throw new IllegalArgumentException("input is not a number");
        }
    }
}
