# JavaOauthSMTP
A demo for Java Oauth SMTP.

## 注意事项：
- 本示例适用于世纪互联运营的Office 365，使用微软MSAL认证库进行认证。
- 由于Java代码的特殊性，``\``在Java中有转义字符的特殊含义，所以我们需要转换一下，将``\\x``转换为``%``：
```java
String xOauth = "user="+"jason@majun.fun"+"\\x01auth=Bearer "+token+"\\x01\\x01";
xOauth = URLDecoder.decode(xOauth.replace("\\x","%"), "UTF-8");
```
## 使用方法：
1. 下载本项目，找到``resources`` - ``application.properties``，替换红框内的内容：
![image](https://user-images.githubusercontent.com/18607988/209895884-a6c226da-5dcb-4196-bbcb-fbd685cefd17.png)

2.替换代码中的message参数：
![image](https://user-images.githubusercontent.com/18607988/209896009-9d6d92ef-27cd-47bf-869d-867acd336a44.png)


有任何问题，欢迎提交issue。
