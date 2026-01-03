package io.renren.zapi.channel.channels.cxy.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MD5Util{

/**
	 * 签名
	 * @param SortedMap
	 * @param md5Key 注意md5key各家不同
	 * @return
	 */
	public static String signData(Map<String, Object> SortedMap, String md5Key) {
		List<String> stringSort = new ArrayList<String>();

		for (Map.Entry<String, Object> entry : SortedMap.entrySet()) {
			if (entry.getValue() != null && !"".equals(entry.getValue())
					&& !"null".equals(entry.getValue()) && !entry.getKey().equals("sign") && !entry.getKey().equals("pl_sign") ) {
				stringSort.add(entry.getKey());
			//	logger.debug("========"+  entry.getKey()+"========["+entry.getValue()+"]");
			}
		}
		Collections.sort(stringSort, Collator.getInstance(Locale.CHINA));//排序
		
		System.err.println(stringSort);
		StringBuffer signDatas = new StringBuffer();
		for (int i = 0; i < stringSort.size(); i++) {
			signDatas.append(SortedMap.get(stringSort.get(i)));
		}
		signDatas.append(md5Key);
		System.out.println(signDatas.toString());
		String sign = encrypt2MD5(signDatas.toString());
		return sign;
	}

	
	/**
	 * 签名
	 * @param SortedMap
	 * @param md5Key 注意md5key各家不同
	 * @return
	 */
	public static String signData(String md5, Map<String, String> SortedMap, String md5Key) {
		List<String> stringSort = new ArrayList<String>();

		for (Map.Entry<String, String> entry : SortedMap.entrySet()) {
			if (entry.getValue() != null && !"".equals(entry.getValue())
					&& !"null".equals(entry.getValue()) && !entry.getKey().equals("sign") && !entry.getKey().equals("pl_sign") ) {
				stringSort.add(entry.getKey());
			//	logger.debug("========"+  entry.getKey()+"========["+entry.getValue()+"]");
			}
		}
		Collections.sort(stringSort, Collator.getInstance(Locale.CHINA));//排序
		System.err.println(stringSort);
		StringBuffer signDatas = new StringBuffer();
		signDatas.append(md5);
		for (int i = 0; i < stringSort.size(); i++) {
			signDatas.append(stringSort.get(i)+SortedMap.get(stringSort.get(i)));
		}
		signDatas.append(md5Key);
		String sign = encrypt2MD5(signDatas.toString());
		return sign;
	}
	
	public static String encrypt2MD5(String originstr) {
		String result = null;
		// 用来将字节转换成 16 进制表示的字符
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		if (originstr != null && !"".equals(originstr)) {
			try {
				// 返回实现指定摘要算法的 MessageDigest 对象
				MessageDigest md = MessageDigest.getInstance("MD5");
				// 使用utf-8编码将originstr字符串编码并保存到source字节数组
				byte[] source = originstr.getBytes("utf-8");
				// 使用指定的 byte 数组更新摘要
				md.update(source);
				// 通过执行诸如填充之类的最终操作完成哈希计算,结果是一个128位的长整数
				byte[] tmp = md.digest();
				// 用16进制数表示需要32位
				char[] str = new char[32];
				for (int i = 0, j = 0; i < 16; i++) {
					// j表示转换结果中对应的字符位置
					// 从第一个字节开始,对 MD5 的每一个字节
					// 转换成 16 进制字符
					byte b = tmp[i];

					// 取字节中高 4 位的数字转换
					// 无符号右移运算符>>> ,它总是在左边补0
					// 0x代表它后面的是十六进制的数字. f转换成十进制就是15
					str[j++] = hexDigits[b >>> 4 & 0xf];

					// 取字节中低 4 位的数字转换
					str[j++] = hexDigits[b & 0xf];
				}

				// 结果转换成字符串用于返回
				result = new String(str);

			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		}

		return result;
	}

	public static String encode(String content) {
		try {
			return URLEncoder.encode(content, "UTF-8");// content;//
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String decode(String content) {
		try {
			return URLDecoder.decode(content, "UTF-8");// content;//
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
//	
//public String sign1(Map<String, String> SortedMap){
//	//参数
//	Map header = new HashMap();
//	header.put("b","b");
//	header.put("a","a");
////	params.put("header", header);
//
//	//按字典排序
//
////	String unsign_params = sign1(pm);
////	System.out.println("字典排序后参数串为："+ unsign_params);
//	
//   //获取header元素集合
//   Set headerIt = header.entrySet(); 
//   List<String> list = new ArrayList<String>();
//   //把header元素集合迭代 出 "param=value"形式字符串放入list集合中
//   while (headerIt.hasNext()) {  
//       String param = headerIt.next().toString();  
//       String value = header.getString(param);  
//         
//       if(value == null){                
//           continue ;        
//       }else if("".equals(value.trim())){                
//           continue ; 
//       }else if("sign".equals(param.trim())){                
//           continue ;     
//       }else{  
//         list.add(param+"="+value);
//       }  
//   }  
//   
//   Collections.sort(list);
//   String paramStr="";
//   //迭代list拼装签名sign
//   for(int i=0;i<list.size();i++){
//    if(i!=list.size()-1){
//    paramStr+=list.get(i)+"&";
//    }
//    else{
//    paramStr+=list.get(i);
//    }
//   }
//   String signStr =paramStr;
//   return signStr;
//}
}
