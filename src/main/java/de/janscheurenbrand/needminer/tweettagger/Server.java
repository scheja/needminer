package de.janscheurenbrand.needminer.tweettagger;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.AccessLogReceiver;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;

import java.io.File;
import java.io.IOException;


/**
 * Created by janscheurenbrand on 17/07/15.
 */
public class Server {

    public static void main(final String[] args) throws IOException {

        AccessLogReceiver logReceiver = message -> {};

        FileResourceManager fileResourceManager = new FileResourceManager(new File(Server.class.getClassLoader().getResource("static/").getFile()), 100);

        ResourceHandler resourceHandler = Handlers.resource(fileResourceManager).addWelcomeFiles("application.html");
        resourceHandler.setCanonicalizePaths(true);
        resourceHandler.setCacheTime(6000);

        HttpHandler routingHandlers = Handlers.path()
                .addPrefixPath("/", new TweetTaggerHandler())
                .addPrefixPath("/static", resourceHandler);

        SessionManager sessionManager = new InMemorySessionManager("SESSION_MANAGER");
        SessionCookieConfig sessionConfig = new SessionCookieConfig();

        // Use the sessionAttachmentHandler to add the sessionManager and
        // sessionCofing to the exchange of every request
        SessionAttachmentHandler sessionAttachmentHandler = new SessionAttachmentHandler(sessionManager, sessionConfig);
        // set as next handler your root handler
        sessionAttachmentHandler.setNext(routingHandlers);

        Undertow server = Undertow.builder()
                .addHttpListener(9090, "0.0.0.0")
                .setHandler(new AccessLogHandler(sessionAttachmentHandler, logReceiver, "common", Server.class.getClassLoader()))
                .build();
        server.start();
    }
}
