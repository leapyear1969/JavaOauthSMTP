# JavaOauthSMTP
A demo for Java Oauth SMTP.

## 注意事项：
- 本示例适用于世纪互联运营的Office 365，使用微软MSAL认证库进行认证。
- 本示例是根据微软[官方说明](https://learn.microsoft.com/en-us/exchange/client-developer/legacy-protocols/how-to-authenticate-an-imap-pop-smtp-application-by-using-oauth)进行编写，通过base64编码组合XOAUTH2的密钥之后再进行验证。
  由于Java代码的特殊性，``\``在Java中有转义字符的特殊含义，所以我们需要转换一下，将``\\x``转换为``%``：
```java
String xOauth = "user="+"jason@majun.fun"+"\\x01auth=Bearer "+token+"\\x01\\x01";
xOauth = URLDecoder.decode(xOauth.replace("\\x","%"), "UTF-8");
```
- 🌠🌠🌠从JavaMail1.5.5开始，内置的JavaMail默认支持XOAUTH2认证机制，也可不通过Base64编码，可以直接将access token作为身份验证，参考如下(``推荐使用``)：
```java
//add props parameters
Properties props = new Properties();
props.put("mail.transport.protocol", "smtp");
props.put("mail.smtp.starttls.enable", "true");
//using ``mail.smtp.auth.mechanisms``
props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
props.put("mail.smtp.port", 587);
```
发送邮件,直接使用access token验证：
```java
//create session and send email by SMTPTransport
Session session = Session.getInstance(props);
session.setDebug(true);
SMTPTransport t = new SMTPTransport(session,null);
t.connect("smtp.partner.outlook.cn",587,"jason@majun.fun",access_token);
```


## 使用方法：
1. 下载本项目，找到``resources`` - ``application.properties``，替换红框内的内容：
![image](https://user-images.githubusercontent.com/18607988/209895884-a6c226da-5dcb-4196-bbcb-fbd685cefd17.png)

2.替换代码中的message参数：
![image](https://user-images.githubusercontent.com/18607988/209896009-9d6d92ef-27cd-47bf-869d-867acd336a44.png)


有任何问题，欢迎提交issue。
