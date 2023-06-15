package org.example.server;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.example.db.DbCreatorInterface;
import org.example.db.DbQueryInterface;
import org.example.patterns.PatternStorage;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.Map;

public class HttpServer{
    public static void init() {
        Undertow.Builder builder = Undertow.builder();
        builder.addHttpListener(8690, "localhost");
        HttpServer server = new HttpServer();
        builder.setHandler(
                Handlers.path()
                        .addExactPath("/aw4c-api/enroll_key", server::enrollKeyHandler)
                        .addExactPath("/aw4c-api/test", server::testKeyHandler)
        );
        builder.setIoThreads(16);
        builder.build().start();
    }

    private void testKeyHandler(HttpServerExchange exchange) {
        Map<String, Deque<String>> params = exchange.getQueryParameters();
        if(!params.containsKey("user")) {
            abort(exchange, StatusCodes.BAD_REQUEST);
        }
        try {
            DbQueryInterface queryInterface = DbQueryInterface.take();
            String user = params.get("user").getFirst();
            String key = queryInterface.getUserKey(user);
            if(key != null) {
                exchange.setStatusCode(StatusCodes.OK);
                byte[] responseData = PatternStorage.aw4c_accountclientserver_rel_pattern.getRollingBuffer();
                exchange.setResponseContentLength(responseData.length);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
                exchange.getResponseSender().send(ByteBuffer.wrap(responseData));
            }else{
                abort(exchange, StatusCodes.NOT_FOUND);
            }
            DbQueryInterface.giveBack(queryInterface);
        }catch (Exception e) {
            e.printStackTrace();
            abort(exchange, StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }

    public void enrollKeyHandler(HttpServerExchange exchange) {
        Map<String, Deque<String>> params = exchange.getQueryParameters();
        if(!params.containsKey("user") || !params.containsKey("key")) {
            abort(exchange, StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            DbQueryInterface queryInterface = DbQueryInterface.take();
            String user = params.get("user").getFirst();
            String key = params.get("key").getFirst();
            String fetchedKey = queryInterface.getUserKey(user);
            if(fetchedKey != null) {
                DbQueryInterface.giveBack(queryInterface);
                abort(exchange, StatusCodes.OK);
                return;
            }
            long remainingRegistrations = queryInterface.getRemainingRegistrations(key);
            DbQueryInterface.giveBack(queryInterface);
            if(remainingRegistrations == -1) {
                abort(exchange, StatusCodes.NOT_FOUND);
                return;
            }
            if(remainingRegistrations > 0) {
                DbCreatorInterface creatorInterface = DbCreatorInterface.take();
                creatorInterface.createUserRecord(user, key);
                creatorInterface.saveUserRegistrations(key, remainingRegistrations-1);
                DbCreatorInterface.giveBack(creatorInterface);
                abort(exchange, StatusCodes.OK);
            }else{
                abort(exchange, StatusCodes.PAYMENT_REQUIRED);
            }
        }catch (Exception e) {
            e.printStackTrace();
            abort(exchange, StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }



    public static void abort(HttpServerExchange exchange, int statusCode) {
        exchange.setStatusCode(statusCode);
        exchange.endExchange();
    }
}
