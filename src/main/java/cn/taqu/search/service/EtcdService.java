package cn.taqu.search.service;

import java.net.URI;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.justinsb.etcd.EtcdClient;
import com.justinsb.etcd.EtcdClientException;

import cn.taqu.core.modules.utils.SpringContextHolder;
import cn.taqu.search.cron.SearchDocment;
import cn.taqu.search.etcd.EtcdWatchThread;
import cn.taqu.search.etcd.TaquProperties;

/**
 * Etcd服务类
 * @ClassName EtcdService.java
 * @Description TODO
 * @author zhengjiaju
 * @date 2015年11月16日 上午8:33:42
 */
public class EtcdService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchDocment.class);
	/**
	 * 用于配置对象初始化数据的构建
	 * @Title initProperties
	 * @Description TODO
	 * @param taquProperties 配置对象
	 * @return
	 * @author zhengjiaju
	 * @Date 2015年11月16日 上午9:45:53
	 */
	public TaquProperties initProperties(TaquProperties taquProperties){
		EtcdClient client = null;
		try {
			client = new EtcdClient(URI.create(taquProperties.getPropery("etcd.url")));
		} catch (Exception e) {
			LOGGER.error("Etcd服务初始化失败");
			e.printStackTrace();
		}
		try {
			JSONObject jRedis = new JSONObject(client.get("search/redis/001").node.value);
			JSONObject jMq = new JSONObject(client.get("search/mq/001").node.value);
			JSONObject jSolr = new JSONObject(client.get("search/solr/001").node.value);
			taquProperties.setProperty("search/redis/001/host", jRedis.getString("host"));
			taquProperties.setProperty("search/redis/001/port", jRedis.getString("port"));
			taquProperties.setProperty("search/mq/001/baseUrl", jMq.getString("baseUrl"));
			taquProperties.setProperty("search/mq/001/accountMq", jMq.getString("accountMq"));
			taquProperties.setProperty("search/mq/001/forumMq", jMq.getString("forumMq"));
			taquProperties.setProperty("search/solr/001/accountServerUrl", jSolr.getString("accountServerUrl"));
			taquProperties.setProperty("search/solr/001/forumServerUrl", jSolr.getString("forumServerUrl"));
		} catch (EtcdClientException | JSONException e) {
			LOGGER.error("Etcd服务参数获取失败");
			e.printStackTrace();
		}
		//启动etcd监听线程
		new Thread(new EtcdWatchThread(taquProperties)).start();
		return taquProperties;
	}
	
	/**
	 * 重设spring上下文Bean类引用配置
	 * @Title contextProperties
	 * @Description TODO
	 * @param taquProperties
	 * @return
	 * @author zhengjiaju
	 * @Date 2015年11月17日 上午9:07:54
	 */
	public TaquProperties contextProperties(TaquProperties taquProperties){
		try {
			ApplicationContext applicationContext = SpringContextHolder.getApplicationContext();
			JedisConnectionFactory jedisConnectionFactory = SpringContextHolder.getBean(JedisConnectionFactory.class);
			jedisConnectionFactory.setHostName(taquProperties.getPropery("search/redis/001/host"));
			jedisConnectionFactory.setPort(Integer.parseInt(taquProperties.getPropery("search/redis/001/port")));
			
			SearchDocment searchDocment = applicationContext.getBean(SearchDocment.class);
			searchDocment.mqBaseUrl = taquProperties.getPropery("search/mq/001/baseUrl");
			searchDocment.accountMq = taquProperties.getPropery("search/mq/001/accountMq");
			searchDocment.forumMq = taquProperties.getPropery("search/mq/001/forumMq");
			searchDocment.accountServerUrl = taquProperties.getPropery("search/solr/001/accountServerUrl");
			searchDocment.forumServerUrl = taquProperties.getPropery("search/solr/001/forumServerUrl");
			
			SearchService searchService = applicationContext.getBean(SearchService.class);
			searchService.accountServerUrl = taquProperties.getPropery("search/solr/001/accountServerUrl");
			searchService.forumServerUrl = taquProperties.getPropery("search/solr/001/forumServerUrl");
		
		} catch (Exception e) {
			LOGGER.error("重设上下文失败");
			e.printStackTrace();
		}
		return taquProperties;
	}
}
