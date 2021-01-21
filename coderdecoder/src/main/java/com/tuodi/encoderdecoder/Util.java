package com.tuodi.encoderdecoder;

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
        if (scale <= 10) {
            checkNumber(num);
        }
        if (scale < 2 || scale > 32) {
            throw new IllegalArgumentException("scale is not in range");
        }
        // 不同其他进制转十进制,修改这里即可
        int total = 0;
        char[] ch = num.toCharArray();
        int chLength = ch.length;
        for (int i = 0; i < chLength; i++) {
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
