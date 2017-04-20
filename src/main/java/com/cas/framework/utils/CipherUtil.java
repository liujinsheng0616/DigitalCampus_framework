package com.cas.framework.utils;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 *	对密码进行加密和验证的类 
 */
public final class CipherUtil {
	private static Logger logger = Logger.getLogger(CipherUtil.class);

	private CipherUtil() {
	}

	private final static String ALGORITHM_md5 = "MD5";
	/**
	 * <p>
	 * 验证加密数据和数据是否匹配。
	 * </p>
	 * 
	 * @param encoded
	 *            加密后的数据
	 * @param date
	 *            需要鉴别的数据
	 * @return
	 */
	public final static boolean validate(String encoded, String data) {
		if (encoded.equalsIgnoreCase(encode(data))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <p>
	 * 加密数据，返回大写字母。
	 * </p>
	 * 
	 * @param date
	 *            需要加密的数据
	 * @return 加密后的数据
	 */
	public final static String encode(String data) {
		if (StringUtil.isNotEmpty(data)) {
			try {
				MessageDigest md = MessageDigest.getInstance(ALGORITHM_md5);
				md.reset();
				byte[] results = md.digest(data.getBytes("UTF-16LE"));
				StringBuffer buf = new StringBuffer();
		        for (int i = 0; i < results.length; i++) {
		            if ((results[i] & 0xff) < 0x10) {
		                buf.append("0");
		            }
		            buf.append(Long.toString(results[i] & 0xff, 16));
		        }
				return buf.toString().toUpperCase();
			} catch (Exception e) {
				logger.error("CipherUtil encode Exception " + data, e);
			}
		}
		return data.toUpperCase();
	}
	public final static String encode(String algorithm,String data) {
		if (StringUtil.isNotEmpty(data)) {
			try {
				if(StringUtil.isEmpty(algorithm)){
					algorithm=ALGORITHM_md5;
				}
				MessageDigest digest = MessageDigest.getInstance(algorithm);
				digest.update(data.getBytes());
	            byte messageDigest[] = digest.digest();
	            // Create Hex String
	            StringBuffer hexString = new StringBuffer();
	            // 字节数组转换为 十六进制 数
	            for (int i = 0; i < messageDigest.length; i++) {
	                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
	                if (shaHex.length() < 2) {
	                    hexString.append(0);
	                }
	                hexString.append(shaHex);
	            }
	            return hexString.toString();
			} catch (Exception e) {
				logger.error("CipherUtil encode Exception " + data, e);
			}
		}
		return data.toUpperCase();
	}
	public static void main(String[] args) {
		List<Integer> pwdList=new ArrayList<Integer>();
				pwdList.add(817396);
				pwdList.add(353975);
				pwdList.add(547997);
				pwdList.add(918287);
				pwdList.add(488398);
				pwdList.add(328848);
				pwdList.add(550578);
				pwdList.add(987740);
				pwdList.add(854168);
				pwdList.add(178463);
				pwdList.add(283728);
				pwdList.add(874766);
				pwdList.add(495030);
				pwdList.add(441883);
				pwdList.add(456972);
				pwdList.add(805987);
				pwdList.add(848865);
				pwdList.add(282958);
				pwdList.add(882736);
				pwdList.add(921057);
				pwdList.add(884885);
				pwdList.add(921985);
				pwdList.add(446296);
				pwdList.add(355669);
				pwdList.add(469534);
				pwdList.add(809032);
				pwdList.add(866500);
				pwdList.add(566994);
				pwdList.add(289862);
				pwdList.add(859616);
				pwdList.add(921906);
				pwdList.add(806545);
				pwdList.add(286978);
				pwdList.add(554162);
				pwdList.add(450286);
				pwdList.add(921950);
				pwdList.add(566972);
				pwdList.add(290081);
				pwdList.add(593913);
				pwdList.add(592250);
				pwdList.add(983986);
				pwdList.add(876199);
				pwdList.add(468608);
				pwdList.add(375060);
				pwdList.add(807620);
				pwdList.add(289975);
				pwdList.add(581330);
				pwdList.add(582533);
				pwdList.add(901379);
				pwdList.add(172251);
				pwdList.add(886166);
				pwdList.add(921031);
				pwdList.add(556880);
				pwdList.add(691058);
				pwdList.add(950598);
				pwdList.add(365265);
				pwdList.add(371873);
				pwdList.add(530733);
				pwdList.add(284962);
				pwdList.add(904593);
				pwdList.add(846591);
				pwdList.add(143586);
				pwdList.add(848017);
				pwdList.add(487871);
				pwdList.add(580632);
				pwdList.add(922120);
				pwdList.add(907888);
				pwdList.add(354550);
				pwdList.add(860289);
				pwdList.add(287676);
				pwdList.add(272788);
				pwdList.add(551975);
				pwdList.add(454215);
				pwdList.add(922127);
				pwdList.add(455427);
				pwdList.add(114297);
				pwdList.add(599747);
				pwdList.add(896967);
				pwdList.add(868089);
				pwdList.add(842519);
				pwdList.add(287399);
				pwdList.add(158570);
				pwdList.add(994107);
				pwdList.add(922117);
				pwdList.add(356958);
				pwdList.add(989671);
				pwdList.add(283688);
				pwdList.add(878392);
				pwdList.add(436251);
				pwdList.add(476312);
				pwdList.add(806475);
				pwdList.add(288397);
				pwdList.add(846143);
				pwdList.add(452531);
				pwdList.add(328786);
				pwdList.add(663727);
				pwdList.add(373604);
				pwdList.add(469168);
				pwdList.add(921480);
				pwdList.add(923562);
				pwdList.add(284762);
				pwdList.add(175525);
				pwdList.add(800283);
				pwdList.add(866851);
				pwdList.add(282733);
				pwdList.add(928168);
				pwdList.add(815939);
				pwdList.add(882598);
				pwdList.add(913202);
				pwdList.add(564970);
				pwdList.add(439716);
				pwdList.add(590528);
				pwdList.add(157511);
				pwdList.add(865005);
				pwdList.add(922133);
				pwdList.add(921971);
				pwdList.add(116306);
				pwdList.add(289687);
				pwdList.add(823621);
				pwdList.add(800225);
				pwdList.add(922113);
				pwdList.add(909828);
				for(Integer i:pwdList){
					System.out.println(i+" "+CipherUtil.encode(i.toString()));
				}
	}
}
