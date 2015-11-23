package cn.taqu.search.etcd;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.justinsb.etcd.EtcdClient;
import com.justinsb.etcd.EtcdClientException;
import com.justinsb.etcd.EtcdResult;

import cn.taqu.search.cron.SearchDocment;
import cn.taqu.search.service.EtcdService;

/**
 * 用于设置etcd服务监听
 * @ClassName EtcdWatchThread.java
 * @Description TODO
 * @author zhengjiaju
 * @date 2015年11月16日 下午2:31:22
 */
public class EtcdWatchThread implements Runnable{
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchDocment.class);
	private EtcdClient client = null;
	private TaquProperties taquProperties = null;
	public EtcdWatchThread(TaquProperties taquProperties){
		try {
			this.client = new EtcdClient(URI.create(taquProperties.getPropery("etcd.url")));
		} catch (Exception e) {
			LOGGER.error("Etcd服务初始化失败");
			e.printStackTrace();
		}
		this.taquProperties = taquProperties;
	}
	@Override
	public void run() {
		for(;;){

			try {
				ListenableFuture<EtcdResult> lf =client.watch("search",null,true);
				EtcdResult er = lf.get();
				String key =  er.node.key;
				String value= er.node.value;
				if(key.equals("/search/redis/001")){
					JSONObject jRedis = new JSONObject(client.get("search/redis/001").node.value);
					taquProperties.setProperty("search/redis/001/host", jRedis.getString("host"));
					taquProperties.setProperty("search/redis/001/port", jRedis.getString("port"));
				}else if(key.equals("/search/mq/001")){
					JSONObject jMq = new JSONObject(client.get("search/mq/001").node.value);
					taquProperties.setProperty("search/mq/001/baseUrl", jMq.getString("baseUrl"));
					taquProperties.setProperty("search/mq/001/accountMq", jMq.getString("accountMq"));
					taquProperties.setProperty("search/mq/001/forumMq", jMq.getString("forumMq"));
				}else if(key.equals("/search/solr/001")){
					JSONObject jSolr = new JSONObject(client.get("search/solr/001").node.value);
					taquProperties.setProperty("search/solr/001/accountServerUrl", jSolr.getString("accountServerUrl"));
					taquProperties.setProperty("search/solr/001/forumServerUrl", jSolr.getString("forumServerUrl"));
				}
				//重设上下文
				new EtcdService().contextProperties(taquProperties);
				LOGGER.info("配置信息已修改:"+taquProperties.getObj().toString());
			} catch (EtcdClientException | InterruptedException | ExecutionException | JSONException e) {
				LOGGER.error("配置信息修改失败:"+taquProperties.getObj().toString());
				e.printStackTrace();
			}
		
		}
	}
	
}
