package cn.taqu.search.cron;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import cn.taqu.core.modules.utils.Encodes;
import cn.taqu.search.service.SearchService;
import cn.taqu.search.util.HttpClient;

@Component
@Lazy(value = false)
public class SearchDocment {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchDocment.class);

	@Autowired
	SearchService searchService;

	@Autowired
	@Value("${search/mq/001/baseUrl}")
	public String mqBaseUrl;

	@Autowired
	@Value("${search/mq/001/accountMq}")
	public String accountMq;

	@Autowired
	@Value("${search/mq/001/forumMq}")
	public String forumMq;

	@Autowired
	@Value("${search/solr/001/accountServerUrl}")
	public String accountServerUrl;

	@Autowired
	@Value("${search/solr/001/forumServerUrl}")
	public String forumServerUrl;

	private HttpSolrServer accountServer = null;
	private HttpSolrServer forumServer = null;

	/**
	 * 定时获取MQ修改account索引(每秒一次)
	 * 
	 * @Title accountDeltaDocment
	 * @Description TODO
	 * @author zhengjiaju
	 * @Date 2015年11月2日 下午3:24:10
	 */
	// @Scheduled(cron = "0/1 * * * * *")
	public void accountDeltaDocment() {
		LOGGER.info("delta account start.");
		String flag = "";
		String id = "";
		String account_name = "";
		String email = "";
		String msg_id = "";
		JSONObject jsonObject;
		SolrInputDocument doc = new SolrInputDocument();
		HttpClient httpClient = new HttpClient(mqBaseUrl);
		if (accountServer == null) {
			accountServer = searchService.createSolrServer(accountServerUrl);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("service", "operation");
		params.put("method", "pop");
		String form = MessageFormat.format("[\"{0}\",\"\"]", accountMq);
		params.put("form", Encodes.encodeBase64(form.getBytes()));// "WyJmb3J1bURvY21lbnRMaXN0IiwiIl0="
		String mqJson = httpClient.get("", params);
		try {
			jsonObject = new JSONObject(mqJson);
			String data = jsonObject.getString("data");
			jsonObject = new JSONObject(data);
			msg_id = jsonObject.getString("msg_id");
			String msg_body = jsonObject.getString("msg_body");
			jsonObject = new JSONObject(msg_body);
			flag = jsonObject.getString("flag");
			id = jsonObject.getString("id");
			account_name = jsonObject.getString("account_name");
			email = jsonObject.getString("email");
		} catch (JSONException e) {
			LOGGER.info("mq获取json格式错误");
			e.printStackTrace();
		}
		doc.addField("id", id);
		doc.addField("account_name", account_name);
		doc.addField("email", email);
		// doc.addField("create_time", "22222222");
		try {
			if ("add".equals(flag)) {
				accountServer.add(doc);
			} else if ("delete".equals(flag)) {
				accountServer.deleteById(id.toString());
			} else if ("update".equals(flag)) {
				accountServer.deleteById(id.toString());
				accountServer.add(doc);
			}
			removeMQ(msg_id, accountMq);
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("delta account finished.");
	}

	/**
	 * 定时获取MQ修改forum索引(每秒一次)
	 * 
	 * @Title forumDeltaDocment
	 * @Description TODO
	 * @author zhengjiaju
	 * @Date 2015年11月2日 下午3:25:13
	 */
	@Scheduled(cron = "0/1 * * * * *")
	public void forumDeltaDocment() {
		LOGGER.info("delta forum start.");
		String flag = "";
		String uuid = "";

		String msg_id = "";
		JSONObject jsonObject;
		SolrInputDocument solrDoc;

		HttpClient httpClient = new HttpClient(mqBaseUrl);
		if (forumServer == null) {
			forumServer = searchService.createSolrServer(forumServerUrl);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("service", "operation");
		params.put("method", "pop");
		String form = MessageFormat.format("[\"{0}\",\"\"]", forumMq);
		params.put("form", Encodes.encodeBase64(form.getBytes()));// "WyJhY2NvdW50RG9jbWVudExpc3QiLCIiXQ=="
		String mqJson = httpClient.get("", params);
		try {
			jsonObject = new JSONObject(mqJson);
			String data = jsonObject.getString("data");
			jsonObject = new JSONObject(data);
			msg_id = jsonObject.getString("msg_id");
			if (!Strings.isNullOrEmpty(msg_id)) {
				String msg_body = jsonObject.getString("msg_body");
				jsonObject = new JSONObject(msg_body);
				solrDoc = solrInputDocument(jsonObject);

				flag = jsonObject.getString("flag");
				uuid = jsonObject.getString("uuid");
				if ("add".equals(flag)) {
					forumServer.add(solrDoc);
				} else if ("delete".equals(flag)) {
					forumServer.deleteById(uuid.toString());
				} else if ("update".equals(flag)) {
					forumServer.deleteById(uuid.toString());
					forumServer.add(solrDoc);
				}
				removeMQ(msg_id, forumMq);
			}
		} catch (SolrServerException | IOException | JSONException e) {
			LOGGER.info("mq获取json格式错误");
			e.printStackTrace();
		}

		LOGGER.info("delta forum finished.");
	}

	/**
	 * 获得需要修改的索引document对象
	 * 
	 * @Title solrInputDocument
	 * @Description TODO
	 * @param jsonObject
	 * @return
	 * @author zhengjiaju
	 * @Date 2015年11月5日 上午9:26:52
	 */
	public SolrInputDocument solrInputDocument(JSONObject jsonObject) {
		SolrInputDocument solrDoc = new SolrInputDocument();
		String id = "";
		String uuid = "";
		String talk_title = "";
		String talk_description = "";
		String review_content = "";
		String status = "";
		String create_time = "";
		try {
			
			String doc = jsonObject.getString("doc");

			if (doc.equals("talk")) {
				uuid = jsonObject.getString("uuid");
				talk_title = jsonObject.getString("talk_title");
				talk_description = jsonObject.getString("talk_description");
				status = jsonObject.getString("status");
				create_time = jsonObject.getString("create_time");
				solrDoc.addField("uuid", uuid);
				solrDoc.addField("talk_title", talk_title);
				solrDoc.addField("talk_description", talk_description);
				solrDoc.addField("status", status);
				solrDoc.addField("create_time", create_time);
			} else if (doc.equals("review")) {
				uuid = jsonObject.getString("uuid");
				review_content = jsonObject.getString("review_content");
				status = jsonObject.getString("status");
				create_time = jsonObject.getString("create_time");
				solrDoc.addField("uuid", uuid);
				solrDoc.addField("review_content", review_content);
				solrDoc.addField("status", status);
				solrDoc.addField("create_time", create_time);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return solrDoc;
	}

	/**
	 * 删除队列信息
	 * 
	 * @Title removeMQ
	 * @Description TODO
	 * @param msg_id
	 * @param server
	 * @author zhengjiaju
	 * @Date 2015年11月5日 上午9:24:48
	 */
	public void removeMQ(String msg_id, String server) {
		Map<String, Object> params = new HashMap<String, Object>();
		HttpClient httpClient = new HttpClient(mqBaseUrl);
		String array[] = new String[2];
		array[0] = server;
		array[1] = msg_id;
		params.put("service", "operation");
		params.put("method", "remove");
		String form = MessageFormat.format("[\"{0}\",\"{1}\"]", array);
		params.put("form", Encodes.encodeBase64(form.getBytes()));
		String mqJson = httpClient.get("", params);
		System.err.println(mqJson);
	}

	/**
	 * 
	 * @Title:accountFullBuildIndex（每周一凌晨1点执行）
	 * @Description:account全量重建索引
	 * @author:huangyuehong
	 * @Date:2015年11月2日 上午11:00:29
	 */
	@Scheduled(cron = "0 0 1 ? * MON")
	public void accountFullBuildIndex() {
		searchService.buildIndex(accountServerUrl, "account");
	}

	/**
	 * 
	 * @Title:forumFullBuildIndex（每周一凌晨1点执行）
	 * @Description:forum全量重建索引
	 * @author:huangyuehong
	 * @Date:2015年11月2日 上午11:00:29
	 */
	@Scheduled(cron = "0 0 1 ? * MON")
	public void forumFullBuildIndex() {
		searchService.buildIndex(forumServerUrl, "forum");
	}

}
