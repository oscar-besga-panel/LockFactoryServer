package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.HttpHandler;
import com.github.arteam.embedhttp.HttpRequest;
import com.github.arteam.embedhttp.HttpResponse;

import java.util.Arrays;
import java.util.List;

class RestConnectionHelper {

    private RestConnectionHelper() {
        //Empty on purpose
    }

    static String removeLeadingTrailingSlash(String text) {
        if (text == null) {
            throw new IllegalStateException("Text should not be null");
        } else if (text.isEmpty() || text.isBlank()) {
            return "";
        } else if (text.equals("/")) {
            return "";
        } else {
            while (text.startsWith("/")) {
                text = text.substring(1);
            }
            while (text.endsWith("/")) {
                text = text.substring(0, text.length() - 1);
            }
            return text;
        }
    }

    static List<String> transformPathToParameters(String prefix, String path) {
        String resultPath = path.replace(prefix,"");
        resultPath = removeLeadingTrailingSlash(resultPath);
        return Arrays.asList(resultPath.split("/"));
    }

    @FunctionalInterface
    interface PlainTextHandlerWithPrefix {

        default String execute(String prefix, HttpRequest request) {
            List<String> parameters = transformPathToParameters(prefix, request.getUri().getPath());
            return getPlainTextResponse(prefix, parameters, request);
        }

        String getPlainTextResponse(String prefix, List<String> parameters, HttpRequest request);

    }

    @FunctionalInterface
    interface PlainTextHandler extends HttpHandler {

        @Override
        default void handle(HttpRequest request, HttpResponse response) {
            String body = getPlainTextResponse(request);
            response.setBody(body);
            response.addHeader("content-type", "text/plain");
        }

        String getPlainTextResponse(HttpRequest request);

    }
}
