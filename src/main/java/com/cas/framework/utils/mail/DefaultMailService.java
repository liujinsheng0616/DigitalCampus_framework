package com.cas.framework.utils.mail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import freemarker.template.Template;
import freemarker.template.TemplateException;
/**
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 * 发送邮件
 */
@SuppressWarnings("rawtypes")
public class DefaultMailService extends JavaMailSenderImpl implements MailService{
	protected Logger log = Logger.getLogger(this.getClass());
	FreeMarkerConfigurer mailTemplateEngine;
	/**
	 * @return the mailTemplateEngine
	 */
	public FreeMarkerConfigurer getMailTemplateEngine() {
		return mailTemplateEngine;
	}
	/**
	 * @param mailTemplateEngine the mailTemplateEngine to set
	 */
	public void setMailTemplateEngine(FreeMarkerConfigurer mailTemplateEngine) {
		this.mailTemplateEngine = mailTemplateEngine;
	}
	@Override
	public String generateEmailContent(String templateName, Map map) {
		try {
			Template t = mailTemplateEngine.getConfiguration().getTemplate(templateName);
			return FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
		} catch (TemplateException e) {
			log.error("Error while processing FreeMarker template ", e);
		} catch (FileNotFoundException e) {
			log.error("Error while open template file ", e);
		} catch (IOException e) {
			log.error("Error while generate Email Content ", e);
		}
		return null;
	}
	public void sendBySSL(String from, String[] to, String[] cc,String subject, String text) {
		//Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());  
		Properties props= getJavaMailProperties();
		Session session = Session.getDefaultInstance(props, new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(getUsername(), getPassword());
		}});
		MimeMessage msg = new MimeMessage(session);
		try {
			MimeMessageHelper messageHelper=new MimeMessageHelper(msg,true, "utf-8");
			messageHelper.setFrom(from);
			messageHelper.setTo(to);
			if(cc!=null){
				messageHelper.setCc(cc);
			}
			messageHelper.setSubject(subject);
			messageHelper.setText(text, Boolean.TRUE);
			Transport.send(msg);
		} catch (MessagingException e) {
			e.printStackTrace();
			return ;
		}
	}
	/**
	 * 使用模版发送HTML格式的邮件.
	 * @param from 发件人
	 * @param to 接受人
	 * @param subject 标题
	 * @param text 邮件主体
	 */
	@Override
	public void send(String from, String[] to, String[] cc,String subject, String text) {
		if(StringUtils.isEmpty(from)||to==null||StringUtils.isEmpty(subject)){
			return ;
		}
		MimeMessage mmm=this.createMimeMessage();
		try {
			MimeMessageHelper messageHelper=new MimeMessageHelper(mmm,true, "utf-8");
			messageHelper.setFrom(from);
			messageHelper.setTo(to);
			if(cc!=null){
				messageHelper.setCc(cc);
			}
			messageHelper.setSubject(subject);
			messageHelper.setText(text, Boolean.TRUE);
			send(mmm);
		} catch (MessagingException e) {
			e.printStackTrace();
			return ;
		}
		
	}
	/**
	 * 使用模版发送HTML格式的邮件.
	 * @param from 发件人
	 * @param to 接受人
	 * @param cc 抄送人
	 * @param subject 标题
	 * @param text 邮件主体
	 * @param fileName[] 文件名数组
	 * @param file[] 文件数组与上面的文件名必须对应
	 */
	@Override
	public void send(String from, String[] to,String[] cc, String subject, String text,String[] fileName,File[] file){
		if(StringUtils.isEmpty(from)||to==null||StringUtils.isEmpty(subject)){
			return ;
		}
		MimeMessage mmm=this.createMimeMessage();
		try {
			MimeMessageHelper messageHelper=new MimeMessageHelper(mmm,true, "utf-8");
			for(int i=0;i<file.length;i++){
				messageHelper.addAttachment(fileName[i], file[i]);
			}
			messageHelper.setFrom(from);
			messageHelper.setTo(to);
			if(cc!=null){
				messageHelper.setCc(cc);
			}
			messageHelper.setSubject(subject);
			messageHelper.setText(text, Boolean.TRUE);
		} catch (MessagingException e) {
			return ;
		}
		send(mmm);
	}
	/**
	 * 使用模版发送HTML格式的邮件.
	 * @param from 发件人
	 * @param to 接受人
	 * @param subject 标题
	 * @param templateName 模版名,模版根路径已在配置文件定义于freemakarengine中
	 * @param model		渲染模版所需的数据
	 */
	@Override
	public void send(String from, String[] to,String[] cc, String subject, String templateName, Map model) {
		if(StringUtils.isEmpty(from)||to==null||StringUtils.isEmpty(subject)){
			return ;
		}
		String content = generateEmailContent(templateName, model);
		MimeMessage mimeMsg = this.createMimeMessage();
		try {
			mimeMsg.setText(content);
			mimeMsg.setContent(content,"text/html; charset=UTF-8");
			MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true, "utf-8");
			helper.setTo(to);
			if(cc!=null){
				helper.setCc(cc);
			}
			helper.setSubject(subject);
			helper.setFrom(from);
			helper.setText(content, true);
		} catch (MessagingException ex) {
			log.error(ex.getMessage(), ex);
		}
		send(mimeMsg);
	}
	public static void main(String[] args) {
	}
}
