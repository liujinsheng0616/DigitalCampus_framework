/**
                   _ooOoo_
                  o8888888o
                  88" . "88
                  (| -_- |)
                  O\  =  /O
               ____/`---'\____
             .'  \\|     |//  `.
            /  \\|||  :  |||//  \
           /  _||||| -:- |||||-  \
           |   | \\\  -  /// |   |
           | \_|  ''\---/''  |   |
           \  .-\__  `-`  ___/-. /
         ___`. .'  /--.--\  `. . __
      ."" '<  `.___\_<|>_/___.'  >'"".
     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
     \  \ `-.   \_ __\ /__ _/   .-` /  /
======`-.____`-.___\_____/___.-`____.-'======
                   `=---='
                                     佛祖保佑       永无BUG
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
	Copyright (c) 2016,  email:14902300@qq.com All Rights Reserved. 
*/
package com.cas.framework.utils.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.cas.framework.utils.SerializeUtil;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 * kryo序列化类
 */
public class KryoSerializer {
	
	private static final  Kryo kryo = new Kryo();
	
	public static byte[] serializeByKryo(Object object) throws SerializationException {
		Output output = null;
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			output = new Output(bos, SerializeUtil.BUFFER_SIZE);
			kryo.writeObject(output, object);
			byte[] b = output.toBytes();
			return b;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(output != null){
				output.close();
			}	
		}
		return null;
	}
	
	public static Object unserializeByKryo(byte[] b) throws SerializationException {
		Input input = null;
		try{
			ByteArrayInputStream bis = new ByteArrayInputStream(b);
			input = new Input(bis, SerializeUtil.BUFFER_SIZE);
			return kryo.readClassAndObject(input);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(input != null){
				input.close();
			}
		}
		return null;
	}
}
