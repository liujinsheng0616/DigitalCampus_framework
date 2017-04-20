package com.cas.framework.utils.mail;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.springframework.mail.SimpleMailMessage;

/**
 * @Creat 2017年04月8日
 * @Author:kingson·liu
 * 发送邮件主逻辑
 */
@SuppressWarnings("rawtypes")
public interface MailService{
	/**
	 * 发送SimpleMailMessage的接口.
	 */
	public void send(SimpleMailMessage msg);
	/*
	 * 发送简单SSL邮件。
	 */
	public void sendBySSL(String from, String[] to, String[] cc,String subject, String text);
	/**
	 * 简单邮件发送接口.
	 * @param from 发件人
	 * @param to 接受人
	 * @param subject 标题
	 * @param text 邮件主体
	 */
	public void send(String from, String[] to,String[] cc, String subject, String text);
	/**
	 * 简单邮件发送接口,可以发送附件.
	 * @param from 发件人
	 * @param to 接受人
	 * @param cc 抄送人
	 * @param subject 标题
	 * @param text 邮件主体
	 * @param fileName[] 文件名数组
	 * @param file[] 文件数组与上面的文件名必须对应
	 */
	public void send(String from, String[] to,String[] cc, String subject, String text,String[] fileName,File[] file);
	/**
	 * 使用模版发送HTML格式的邮件.
	 * @param from 发件人
	 * @param to 接受人
	 * @param subject 标题
	 * @param templateName 模版名,模版根路径已在配置文件定义于freemakarengine中
	 * @param model		渲染模版所需的数据
	 */
	
	public void send(String from, String[] to, String[] cc,String subject,String templateName, Map model);
	/**
	 * 使用Freemarker 根据模版生成邮件内容.
	 */
	public String generateEmailContent(String templateName, Map map);
	/**
	 * 在点对点发送邮件时候,需要重新设置邮件信息
	 */	
	public void setJavaMailProperties(Properties javaMailProperties);
	/**
	 * Set the mail protocol. Default is "smtp".
	 */
	public void setProtocol(String protocol);
	/**
	 * Set the mail server host, typically an SMTP host.
	 * <p>Default is the default host of the underlying JavaMail Session.
	 */
	public void setHost(String host);
	/**
	 * Set the mail server port.
	 * <p>Default is {@link #DEFAULT_PORT}, letting JavaMail use the default
	 * SMTP port (25).
	*/
	public void setPort(int port);
	/**
	 * Set the username for the account at the mail host, if any.
	 * <p>Note that the underlying JavaMail <code>Session</code> has to be
	 * configured with the property <code>"mail.smtp.auth"</code> set to
	 * <code>true</code>, else the specified username will not be sent to the
	 * mail server by the JavaMail runtime. If you are not explicitly passing
	 * in a <code>Session</code> to use, simply specify this setting via
	 * {@link #setJavaMailProperties}.
	 * @see #setSession
	 * @see #setPassword
	 */
	public void setUsername(String username);
	/**
	 * Set the password for the account at the mail host, if any.
	 * <p>Note that the underlying JavaMail <code>Session</code> has to be
	 * configured with the property <code>"mail.smtp.auth"</code> set to
	 * <code>true</code>, else the specified password will not be sent to the
	 * mail server by the JavaMail runtime. If you are not explicitly passing
	 * in a <code>Session</code> to use, simply specify this setting via
	 * {@link #setJavaMailProperties}.
	 * @see #setPassword
	 */
	public void setPassword(String password);
}
