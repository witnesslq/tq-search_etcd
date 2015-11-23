package cn.taqu.search.etcd;

import java.net.URI;

import com.justinsb.etcd.EtcdClient;
import com.justinsb.etcd.EtcdClientException;

public class TaquTest {
	public static void main(String[] args) {
		setEtcd();
	}
//	public static void setEtcd(){
//		EtcdClient client = EtcdClientFactory.newInstance("http://10.10.60.211:2379");
//		try {
//		client.set("redis.account.host", "10.10.20.205");
//		client.set("redis.account.port","678900");
//		
//		client.set("mq.baseUrl", "http://10.10.50.173:3002/v2/soa/jService?");
//		client.set("mq.redis.accountMq", "accountDocmentList");
//		client.set("mq.redis.forumMq", "forumDocmentList");
//		
//		client.set("solr.accountServerUrl", "http://localhost:8080/tq-search/account/");
//		client.set("solr.forumServerUrl", "http://localhost:8080/tq-search/forum/");
//		} catch (EtcdException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * curl http://10.10.60.211:2379/v2/keys/search -XPUT -d dir=true
	 * curl http://10.10.60.211:2379/v2/keys/search/redis -XPUT -d dir=true
	 * curl http://10.10.60.211:2379/v2/keys/search/mq -XPUT -d dir=true
	 * curl http://10.10.60.211:2379/v2/keys/search/solr -XPUT -d dir=true
	 * @Title setEtcd
	 * @Description TODO
	 * @author zhengjiaju
	 * @Date 2015年11月17日 上午9:57:15
	 */
	public static void setEtcd(){
		EtcdClient client = new EtcdClient(URI.create("http://10.10.60.211:2379"));

		try {
			client.set("search/redis/001", "{\"host\":\"10.10.20.205\",\"port\":\"6789\"}");
		
			client.set("search/mq/001", "{\"baseUrl\":\"http://10.10.60.45:5001/v2/soa/jService?\",\"accountMq\":\"accountDocmentList\",\"forumMq\":\"forumDocmentList\"}");
		
			client.set("search/solr/001", "{\"accountServerUrl\":\"http://localhost:8080/tq-search/account/\",\"forumServerUrl\":\"http://localhost:8080/tq-search/forum/\"}");

		} catch (EtcdClientException e) {
			e.printStackTrace();
		}
	}
}
