package cn.taqu.search.util;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.util.CollectionUtils;

import cn.taqu.search.etcd.TaquProperties;
import cn.taqu.search.service.EtcdService;

/**
 * 继承PropertiesFactoryBean用于重写createProperties方法加载etcd配置数据
 * @ClassName EtcdPropertiesFactoryBean.java
 * @Description TODO
 * @author zhengjiaju
 * @date 2015年11月16日 上午8:58:53
 */
public class EtcdPropertiesFactoryBean extends PropertiesFactoryBean {
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.config.PropertiesFactoryBean#createProperties()
	 */
	@Override
	protected Properties createProperties() throws IOException {	
		//ectd properties
		Properties result = mergeProperties();
		String url = result.getProperty("etcd.url");
		Properties ectdProperties = new EtcdService().initProperties(new TaquProperties(url)).getObj();
		CollectionUtils.mergePropertiesIntoMap(ectdProperties, result);
		return result;
	}
	
}
