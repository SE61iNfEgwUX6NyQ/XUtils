package com.remainer.test.spring;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 根据random.org生成随机数
 * @author 李东坡
 *
 */
public class RandomUtil {
	/**
	 * 最大的优点是基于random.org网站api，生成的是真随机数，不是random函数生成的伪随机数
	 * 缺点是也是基于random.org网站api，需要联网，而且不是很快。
	 * @param min最小值
	 * @param max最大值
	 * @param num生成随机数个数
	 * @return
	 */
	public StringBuilder getRandom(String min, String max, String num){
		String GET_URL =  "https://www.random.org/integers/?num="+num+"&min="+min+"&max="+max+"&col="+num+"&base=10&format=plain&rnd=new";
		try {
	        URL url = new URL(GET_URL);    // 把字符串转换为URL请求地址
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();// 打开连接
	        connection.connect();// 连接会话
	        // 获取输入流
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line;
	        StringBuilder buffer = new StringBuilder();
	        while ((line = br.readLine()) != null) {// 循环读取流
	            buffer.append(line);
	        }
	        br.close();// 关闭流
	        connection.disconnect();// 断开连接
	        return buffer;
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("失败!");
	    }
		return null;
    }
}
