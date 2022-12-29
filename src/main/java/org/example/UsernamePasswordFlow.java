package org.example;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.MsalException;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import com.sun.mail.smtp.SMTPTransport;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.binary.Base64;


import javax.mail.*;
import javax.mail.internet.*;

public class UsernamePasswordFlow {

    private static String authority;
    private static Set<String> scope;
    private static String clientId;
    private static String username;
    private static String password;

    public static void main(String args[]) throws Exception {

        setUpSampleData();

        PublicClientApplication pca = PublicClientApplication.builder(clientId)
                .authority(authority)
                .autoDetectRegion(true)
                .build();

        //Get list of accounts from the application's token cache, and search them for the configured username
        //getAccounts() will be empty on this first call, as accounts are added to the cache when acquiring a token
        Set<IAccount> accountsInCache = pca.getAccounts().join();
        IAccount account = getAccountByUsername(accountsInCache, username);

        //Attempt to acquire token when user's account is not in the application's token cache
        IAuthenticationResult result = acquireTokenUsernamePassword(pca, scope, account, username, password);
        System.out.println("Account username: " + result.account().username());
        System.out.println("Access token:     " + result.accessToken());
        System.out.println("Id token:         " + result.idToken());
        System.out.println();

        accountsInCache = pca.getAccounts().join();
        account = getAccountByUsername(accountsInCache, username);

        //Attempt to acquire token again, now that the user's account and a token are in the application's token cache
        result = acquireTokenUsernamePassword(pca, scope, account, username, password);
        System.out.println("Account username: " + result.account().username());
        System.out.println("Access token:     " + result.accessToken());
        System.out.println("Id token:         " + result.idToken());
        String token = result.accessToken();
        String xOauth = "user="+"jason@majun.fun"+"\\x01auth=Bearer "+token+"\\x01\\x01";
        System.out.println("最开始的样子："+xOauth);
        //xOauth = new String(xOauth.getBytes(StandardCharsets.US_ASCII));
        xOauth = URLDecoder.decode(xOauth.replace("\\x","%"), "UTF-8");


        //encode token for XOAUTH2
        Base64 base64 = new Base64();
        String encodedString = new String(base64.encode(xOauth.getBytes()));
        System.out.println("encode之后的值："+encodedString);

        byte[] bytesEncoded = Base64.encodeBase64(xOauth.getBytes());
        String encodedToken = new String(bytesEncoded);
        System.out.println("encoded value is " + encodedToken );

        try {
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.port", 587);

            Session session = Session.getInstance(props);
            session.setDebug(true);

            SMTPTransport t = new SMTPTransport(session,null);

            t.connect("smtp.partner.outlook.cn",587,"jason@majun.fun",null);
            //t.connect("smtp.partner.outlook.cn","jason@majun.fun",null);

            t.issueCommand("AUTH XOAUTH2 " + encodedString,235);
            System.out.println("**********************t.issueCommand已经执行了**************************");
            // 4、创建邮件
            Message message = createMimeMessage(session, "jason@majun.fun", "hello will", "this test");
            // 5、发送邮件

            t.sendMessage(message, message.getAllRecipients());
            System.out.println("已经发送邮件了");
            t.close();

        } catch (Exception e) {
        }




    }

    private static MimeMessage createMimeMessage(Session session, String to,
                                                 String sub, String content)
            throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(
                new InternetAddress("jason@majun.fun"));
        String[] tos = StringUtils.split(to.replace(" ",""), ";");
        Address aaddress[] = new Address[tos.length];
        for (int i = 0; i < tos.length; i++) {
            aaddress[i] = new InternetAddress(tos[i]);
        }
        message.setRecipients(Message.RecipientType.TO, aaddress);
        message.setSubject("Hello WIll");
        MimeBodyPart text = new MimeBodyPart();
        text.setContent(content, "text/html;charset=UTF-8");
        MimeMultipart mm = new MimeMultipart();
        mm.addBodyPart(text);
        message.setContent(mm);
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }
    private static IAuthenticationResult acquireTokenUsernamePassword(PublicClientApplication pca,
                                                                      Set<String> scope,
                                                                      IAccount account,
                                                                      String username,
                                                                      String password) throws Exception {
        IAuthenticationResult result;
        try {
            SilentParameters silentParameters =
                    SilentParameters
                            .builder(scope)
                            .account(account)
                            .build();
            // Try to acquire token silently. This will fail on the first acquireTokenUsernamePassword() call
            // because the token cache does not have any data for the user you are trying to acquire a token for
            result = pca.acquireTokenSilently(silentParameters).join();
            System.out.println("==acquireTokenSilently call succeeded");
        } catch (Exception ex) {
            if (ex.getCause() instanceof MsalException) {
                System.out.println("==acquireTokenSilently call failed: " + ex.getCause());
                UserNamePasswordParameters parameters =
                        UserNamePasswordParameters
                                .builder(scope, username, password.toCharArray())
                                .build();
                // Try to acquire a token via username/password. If successful, you should see
                // the token and account information printed out to console
                result = pca.acquireToken(parameters).join();
                System.out.println("==username/password flow succeeded");
            } else {
                // Handle other exceptions accordingly
                throw ex;
            }
        }
        return result;
    }

    /**
     * Helper function to return an account from a given set of accounts based on the given username,
     * or return null if no accounts in the set match
     */
    private static IAccount getAccountByUsername(Set<IAccount> accounts, String username) {
        if (accounts.isEmpty()) {
            System.out.println("==No accounts in cache");
        } else {
            System.out.println("==Accounts in cache: " + accounts.size());
            for (IAccount account : accounts) {
                if (account.username().equals(username)) {
                    return account;
                }
            }
        }
        return null;
    }

    /**
     * Helper function unique to this sample setting. In a real application these wouldn't be so hardcoded, for example
     * values such as username/password would come from the user, and different users may require different scopes
     */
    private static void setUpSampleData() throws IOException {
        // Load properties file and set properties used throughout the sample
        Properties properties = new Properties();
        properties.load(new FileInputStream(Thread.currentThread().getContextClassLoader().getResource("").getPath() + "application.properties"));
        authority = properties.getProperty("AUTHORITY");
        scope = Collections.singleton(properties.getProperty("SCOPE"));
        clientId = properties.getProperty("CLIENT_ID");
        username = properties.getProperty("USER_NAME");
        password = properties.getProperty("USER_PASSWORD");
    }
}