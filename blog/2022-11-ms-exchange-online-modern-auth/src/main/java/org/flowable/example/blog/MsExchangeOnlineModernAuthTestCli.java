package org.flowable.example.blog;///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.6.3
//DEPS com.microsoft.azure:msal4j:1.13.2
//DEPS com.sun.mail:jakarta.mail:1.6.7
//DEPS org.slf4j:slf4j-simple:1.7.36

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.mail.*;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * A quickly created <a href="https://www.jbang.dev/download/" target="_blank">JBang-ready</a> CLI script to test Microsoft Office 365 Exchange Online SMTP access with OAuth2 authentication for
 * client credentials grant flow. Uses the msal4j API to fetch the access token. 
 * Use the -v flag to get detailed verbose output.
 * <p />
 * Usage:
 * <pre>
 * {@code 
 *  jbang MsExchangeOnlineModernAuthTestCli.java <tenantDomain> -c <clientId> -cs <clientSecret>  -e <email(optional)>
 * }
 * </pre>
 * 
 * @author arthur.hupka-merle@flowable.com
 */
@Command(name = "MsExchangeOnlineModernAuthTestCli", mixinStandardHelpOptions = true, version = "MsExchangeOnlineModernAuthTestCli 0.1", description = "A simple and quickly hacked tool to test accessing Microsoft Office 365 Exchange Online Mailboxes over OAuth2 authentication")
class MsExchangeOnlineModernAuthTestCli implements Callable<Integer> {

    @Parameters(index = "0", description = "The tenant domain e.g. flowable.com")
    String tenant;

    @Option(names = { "-c", "--clientId" }, description = "The clientID", required = true)
    String clientId;

    @Option(names = { "-cs", "--clientSecret" }, description = "The clientSecret", required = false)
    String clientSecret;

    @Option(names = { "-cc", "--clientCert" }, description = "The pkcs12 encoded certificate path", required = false)
    String clientCert;

    @Option(names = { "-ccp", "--clientCertPass" }, description = "The password for the certificate", required = false)
    String clientCertPass;

    @Option(names = { "-v",
            "--verbose" }, description = "For diagnostics.", required = false, defaultValue = "false")
    boolean verbose;

    @Option(names = { "-s",
            "--scope" }, description = "The scope (optional). Separate by comma, when more than one.", required = false, interactive = false, defaultValue = "https://outlook.office365.com/.default")
    String scope;

    @Option(names = { "-e",
            "--email" }, description = "The E-Mail Address. Tries to access E-Mails of that Mailbox, when set.", required = false, interactive = false)
    String email;

    @Option(names = { "-ef",
            "--emailFolder" }, description = "The E-Mail Folder to access over IMAP (lists recent 10 Messages)", defaultValue = "Inbox", required = false, interactive = false)
    String imapFolder;

    public static void main(String... args) {
        int exitCode = new CommandLine(new MsExchangeOnlineModernAuthTestCli()).execute(args);
        System.out.println("\nLet the flow be with you. Brought to you by flowable.com");
        System.exit(exitCode);
    }

    String logLevel() {
       return verbose ? "TRACE" : "FATAL";
    }

    @Override
    public Integer call() throws Exception {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", logLevel());
        int returnCode = 0;
        IAuthenticationResult accessToken = null;
        try {
            System.out.println("Trying to fetch access token ... ");
            accessToken = getAuthenticationWithClientCredentialGrant();
            System.out.println("[OK] SUCCESS! Got OAuth2 access token. Token expires: "+accessToken.expiresOnDate());
            if (verbose) {
                System.out.println(accessToken);
            } else {
                System.out.println("Print token? Use -v option.");
            }
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("[D'OH] ERROR - " + e.getMessage());
            returnCode = 1;
        } catch (Exception e) {
            System.out.println("[D'OH]! ERROR - Could not fetch OAuth2 access token: " + e.getMessage());
            returnCode = 10;
        }
        if (returnCode > 0) {
            System.out.println(" >>> Something went wrong? Enable verbose mode for diagnostics: -v");
            return returnCode;
        }

        try {
            if (email != null) {
                System.out.println("Microsoft gave us an access token. That is half way. Now trying to access mailbox '"+email+"' for folder '"+imapFolder+"' over IMAP.");
                accessMailbox(this.email);
            }
        } catch (Exception e) {
            System.out.println("[D'OH]! ERROR while testing IMAP access to '" + email + "'' " + e.getMessage());
            System.out.println(" >>> Enable verbose mode for diagnostics: -v");
            returnCode = 11;
        }
        return returnCode;
    }

    IAuthenticationResult getAuthenticationWithClientCredentialGrant() throws Exception {
        ConfidentialClientApplication app = null;

        String authority = String.format("https://login.microsoftonline.com/%s/", this.tenant);
        IClientCredential secret = null;
        if (this.clientCert != null) {
            InputStream stream = Files.newInputStream(Paths.get(this.clientCert));
            secret = ClientCredentialFactory.createFromCertificate(stream, this.clientCertPass);
        } else if (this.clientSecret != null) {
            secret = ClientCredentialFactory.createFromSecret(this.clientSecret);
        } else {
            throw new IllegalArgumentException("Either clientSecret or clientCert has to be provided.");
        }
        try {
            app = ConfidentialClientApplication.builder(clientId, secret)
                    .authority(authority)
                    .build();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Authority URL is malformed: " + authority, e);
        }

        ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(
                Arrays.asList(this.scope.split(","))
                        .stream()
                        .map(String::trim).collect(Collectors.toSet()))
                .build();

        CompletableFuture<IAuthenticationResult> future = app.acquireToken(clientCredentialParam);
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Error acquiringToken: ", e);
        }
    }

    void accessMailbox(String email) throws Exception {
        Properties props = new Properties();
        if(verbose){
            props.put("mail.debug", verbose+"");
            props.put("mail.debug.auth", verbose+"");
        }
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "outlook.office365.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");
        props.put("mail.imaps.starttls.enable", "true");
        props.put("mail.imaps.auth", "true");
        props.put("mail.imaps.auth.mechanisms", "XOAUTH2");
        props.put("mail.imaps.user", email);

        props.put("mail.imaps.auth.plain.disable", "true");
        props.put("mail.imaps.auth.xoauth2.disable", "false");

        System.out.println("Trying to access IMAP mailbox with properties ");
        props.forEach((k, v) -> System.out.println(k + "=" + v));

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                try {
                    String accessToken =  getAuthenticationWithClientCredentialGrant().accessToken();
                    return new PasswordAuthentication(email, accessToken);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        // access mailbox....
        Session session = Session.getInstance(props, authenticator);
        session.setDebug(verbose);
        Store store = session.getStore("imaps");
        store.connect();
        System.out.println("[OK] Mail Store connected.");
        System.out.println("Now opening " + imapFolder);
        Folder folder = store.getFolder(imapFolder);
        try {
            folder.open(Folder.READ_ONLY);
            System.out.println("[OK] SUCCESS! IMAP Folder " + imapFolder + " opened.");
            System.out.println("[OK] Nr. of Messages in Mailbox: " + folder.getMessageCount());
            System.out.println("------------ Listing Mails ----------------");
            Arrays.asList(folder.getMessages(1 /* Heck, really? OneE?! */ , Math.min(5, folder.getMessageCount())))
            .forEach(m -> System.out.println("[OK]" + formatMessage(m)));
            System.out.println("------------ Done Listing ----------------");
        } finally {
            folder.close(false);
            store.close();
        }
        System.out.println("[OK] Congrats! Your Mailbox " + email + " is ready to go with OAuth2 authentication.");
    }

    static String formatMessage(Message message) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("  > Mail Received: %s -[%s] From: %s",
                    Arrays.asList(message.getFrom()).stream().map(a -> a.toString()).collect(Collectors.joining(",")),
                    dateFormat.format(message.getReceivedDate()),
                    message.getSubject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
