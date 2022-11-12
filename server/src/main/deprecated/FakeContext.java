package org.obapanel.lockfactoryserver.server;

import com.google.common.reflect.TypeToken;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.cookie.Cookie;
import org.reactivestreams.Publisher;
import ratpack.exec.Execution;
import ratpack.exec.Promise;
import ratpack.func.Action;
import ratpack.handling.*;
import ratpack.handling.direct.DirectChannelAccess;
import ratpack.http.*;
import ratpack.parse.Parse;
import ratpack.path.PathBinding;
import ratpack.path.PathTokens;
import ratpack.registry.NotInRegistryException;
import ratpack.registry.Registry;
import ratpack.render.NoSuchRendererException;
import ratpack.server.ServerConfig;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

public class FakeContext implements Context {

    private static final String EMPTY = "";

    private final FakePathTokens pathTokens = new FakePathTokens();
    private final FakeResponse response = new FakeResponse();

    @Override
    public PathTokens getPathTokens() throws NotInRegistryException {
        return pathTokens;
    }


    @Override
    public Context getContext() {
        // Empty on purpose
        return null;
    }

    @Override
    public Execution getExecution() {
        // Empty on purpose
        return null;
    }

    @Override
    public ServerConfig getServerConfig() {
        // Empty on purpose
        return null;
    }

    @Override
    public Request getRequest() {
        // Empty on purpose
        return null;
    }

    @Override
    public FakeResponse getResponse() {
        // Empty on purpose
        return response;
    }

    public String getFakeSentResponse() {
        if ((response.getSentTextResponses() != null) &&
                !response.getSentTextResponses().isEmpty()){
            return response.getSentTextResponses().get(0);
        } else {
            return EMPTY;
        }
    }


    @Override
    public void next() {
        // Empty on purpose
    }

    @Override
    public void next(Registry registry) {
        // Empty on purpose
    }

    @Override
    public void insert(Handler... handlers) {
        // Empty on purpose
    }

    @Override
    public void insert(Registry registry, Handler... handlers) {
        // Empty on purpose
    }

    @Override
    public void byMethod(Action<? super ByMethodSpec> action) throws Exception {
        // Empty on purpose
    }

    @Override
    public void byContent(Action<? super ByContentSpec> action) throws Exception {
        // Empty on purpose
    }

    @Override
    public void error(Throwable throwable) {
        // Empty on purpose
    }

    @Override
    public void clientError(int statusCode) throws NotInRegistryException {
        // Empty on purpose
    }

    @Override
    public void render(Object object) throws NoSuchRendererException {
        // Empty on purpose
    }

    @Override
    public void redirect(Object to) {
        // Empty on purpose
    }

    @Override
    public void redirect(int code, Object to) {
        // Empty on purpose
    }

    @Override
    public void lastModified(Instant lastModified, Runnable serve) {
        // Empty on purpose
    }

    @Override
    public <T> Promise<T> parse(Class<T> type) {
        // Empty on purpose
        return null;
    }

    @Override
    public <T> Promise<T> parse(TypeToken<T> type) {
        // Empty on purpose
        return null;
    }

    @Override
    public <T, O> Promise<T> parse(Class<T> type, O options) {
        // Empty on purpose
        return null;
    }

    @Override
    public <T, O> Promise<T> parse(TypeToken<T> type, O options) {
        // Empty on purpose
        return null;
    }

    @Override
    public <T, O> Promise<T> parse(Parse<T, O> parse) {
        // Empty on purpose
        return null;
    }

    @Override
    public <T, O> T parse(TypedData body, Parse<T, O> parse) throws Exception {
        // Empty on purpose
        return null;
    }

    @Override
    public DirectChannelAccess getDirectChannelAccess() {
        // Empty on purpose
        return null;
    }

    @Override
    public PathBinding getPathBinding() {
        // Empty on purpose
        return null;
    }

    @Override
    public void onClose(Action<? super RequestOutcome> onClose) {
        // Empty on purpose
    }

    @Override
    public Path file(String path) throws NotInRegistryException {
        // Empty on purpose
        return null;
    }

    @Override
    public <O> Optional<O> maybeGet(TypeToken<O> type) {
        // Empty on purpose
        return Optional.empty();
    }

    @Override
    public <O> Iterable<? extends O> getAll(TypeToken<O> type) {
        // Empty on purpose
        return null;
    }

    private class FakePathTokens implements PathTokens {

        private final Map<String, String> dataMap = new HashMap<>();

        @Override
        public Boolean asBool(String key) {
            return Boolean.valueOf(dataMap.get(key));
        }

        @Override
        public Byte asByte(String key) {
            return Byte.valueOf(dataMap.get(key));
        }

        @Override
        public Short asShort(String key) throws NumberFormatException {
            return Short.valueOf(dataMap.get(key));
        }

        @Override
        public Integer asInt(String key) throws NumberFormatException {
            return Integer.valueOf(dataMap.get(key));
        }

        @Override
        public Long asLong(String key) throws NumberFormatException {
            return Long.valueOf(dataMap.get(key));
        }

        @Override
        public int size() {
            return dataMap.size();
        }

        @Override
        public boolean isEmpty() {
            return dataMap.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return dataMap.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return dataMap.containsValue(value);
        }

        @Override
        public String get(Object key) {
            return dataMap.get(key);
        }

        @Override
        public String put(String key, String value) {
            return dataMap.put(key, value);
        }

        @Override
        public String remove(Object key) {
            return dataMap.remove(key);
        }

        @Override
        public void putAll(Map<? extends String, ? extends String> m) {
            dataMap.putAll(m);
        }

        @Override
        public void clear() {
            dataMap.clear();
        }

        @Override
        public Set<String> keySet() {
            return dataMap.keySet();
        }

        @Override
        public Collection<String> values() {
            return dataMap.values();
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            return dataMap.entrySet();
        }
    }

    public class FakeResponse implements Response {


        private final List<String> textResponses = new ArrayList<>();


        public List<String> getSentTextResponses() {
            return Collections.unmodifiableList(textResponses);
        }

        @Override
        public Cookie cookie(String name, String value) {
            // Empty on purpose
            return null;
        }

        @Override
        public Cookie expireCookie(String name) {
            // Empty on purpose
            return null;
        }

        @Override
        public Set<Cookie> getCookies() {
            // Empty on purpose
            return null;
        }

        @Override
        public MutableHeaders getHeaders() {
            // Empty on purpose
            return null;
        }

        @Override
        public Status getStatus() {
            // Empty on purpose
            return null;
        }

        @Override
        public Response status(Status status) {
            // Empty on purpose
            return null;
        }

        @Override
        public void send() {
            // Empty on purpose
        }

        @Override
        public Response contentTypeIfNotSet(Supplier<CharSequence> contentType) {
            // Empty on purpose
            return null;
        }

        @Override
        public void send(String text) {
            textResponses.add(text);
        }

        @Override
        public void send(CharSequence contentType, String body) {
            // Empty on purpose
        }

        @Override
        public void send(byte[] bytes) {
            // Empty on purpose
        }

        @Override
        public void send(CharSequence contentType, byte[] bytes) {
            // Empty on purpose
        }

        @Override
        public void send(ByteBuf buffer) {
            // Empty on purpose
        }

        @Override
        public void send(CharSequence contentType, ByteBuf buffer) {
            // Empty on purpose
        }

        @Override
        public Response contentType(CharSequence contentType) {
            return null;
        }

        @Override
        public void sendFile(Path file) {
            // Empty on purpose
        }

        @Override
        public void sendStream(Publisher<? extends ByteBuf> stream) {
            // Empty on purpose
        }

        @Override
        public Response beforeSend(Action<? super Response> responseFinalizer) {
            // Empty on purpose
            return null;
        }

        @Override
        public Response noCompress() {
            // Empty on purpose
            return null;
        }
    }
}
