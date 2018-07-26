package com.example.reptile.controller;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RestController
public class equityMarketController{

  @Autowired
  private JavaMailSender mailSender;

  @Value("${mail.fromMail.addr}")
  private String from;

  @Value("${digitalCurrency.type}")
  private String type;

  @Scheduled(fixedRate=3600000L)
  public String query(){
    String url = "https://trans.zb.cn/line/topall";
    try{
      Connection.Response res = Jsoup.connect(url).execute();
      String body = res.body();
      body = body.substring(1, body.length() - 1);
      JSONObject jsStr = JSONObject.parseObject(body);
      JSONArray jsonArray = jsStr.getJSONArray("datas");
      String context = "";
      for (Iterator iterator = jsonArray.iterator(); iterator.hasNext(); ) {
        JSONObject jsonObject = (JSONObject)iterator.next();
        String market = (String)jsonObject.get("market");
        if(type.equals(market)) {
        	String lastPrice = (String)jsonObject.get("lastPrice");
        	if (Double.parseDouble(lastPrice) < 50) {
        		context = this.type + "的价格跌破50，现在价格为：" + lastPrice;
        	}else if(Double.parseDouble(lastPrice) < 70) {
        		context = this.type + "的价格超过70，现在价格为：" + lastPrice;
        	}
        }

	      if (StringUtils.isNotEmpty(context)) {
	    	  SimpleMailMessage m = new SimpleMailMessage();
	          m.setFrom(this.from);
	          m.setTo("435474984@qq.com");
	          m.setSubject(this.type + "价格通知");
	          m.setText(context);
	          this.mailSender.send(m);
	      }
      	}
      }catch (Exception e) {
      e.printStackTrace();
    }
    return "ok";
  }

  @GetMapping({"/test"})
  public String test() {
    return "ok";
  }

  public static Document getHtmlPage(String url, int waitTime)
  {
    WebClient wc = new WebClient(BrowserVersion.CHROME);

    wc.getOptions().setUseInsecureSSL(true);

    wc.getOptions().setJavaScriptEnabled(true);

    wc.getOptions().setCssEnabled(false);

    wc.getOptions().setThrowExceptionOnScriptError(false);

    wc.getOptions().setThrowExceptionOnFailingStatusCode(false);

    wc.getOptions().setActiveXNative(false);

    wc.setJavaScriptTimeout(10000L);
    wc.waitForBackgroundJavaScript(10000L);

    wc.setAjaxController(new NicelyResynchronizingAjaxController());

    wc.getOptions().setTimeout(waitTime);

    wc.getOptions().setDoNotTrackEnabled(false);
    try
    {
      HtmlPage htmlPage = (HtmlPage)wc.getPage(url);

      Thread.sleep(1000L);

      String xml = htmlPage.asXml();

      return Jsoup.parse(xml);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}