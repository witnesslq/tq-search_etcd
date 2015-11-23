package cn.taqu.search.etcd;

import java.util.Properties;
/**
 * 用于存放etcd服务
 * @ClassName TaquProperties.java
 * @Description TODO
 * @author Chenquanan
 * @date 2015年11月13日 下午4:48:02
 */
public class TaquProperties {
	public static Properties properties = null;
	
	public TaquProperties(String url){
		this.setProperty("etcd.url", url);
	}
	
	/**
	 * 获取服务路径
	 * @Title setProperty
	 * @Description TODO
	 * @param key
	 * @param value
	 * @author zhengjiaju
	 * @Date 2015年11月13日 下午4:48:05
	 */
	public void setProperty(String key,String value){
		if(properties == null){
			properties = new Properties();
		}
		properties.setProperty(key, value);
	}
	/**
	 * 获取服务路径
	 * @Title getPropery
	 * @Description TODO
	 * @param key
	 * @return
	 * @author zhengjiaju
	 * @Date 2015年11月13日 下午4:48:07
	 */
	public String getPropery(String key){
		if(properties == null){
			return null;
		}
		return properties.getProperty(key);
	}
	
	public Properties getObj(){
		return properties;
	}
}
