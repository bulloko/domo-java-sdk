package com.domo.sdk.request;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Config {
    private final String clientId;
    private final String secret;
    private final List<Scope> scopes;
    private String apiHost;
    private boolean useHttps = true;
    private final OkHttpClient httpClient;
    private HttpLoggingInterceptor.Level httpLoggingLevel;

    public Config(String clientId,
                  String secret,
                  String apiHost,
                  boolean useHttps,
                  List<Scope> scopes,
                  OkHttpClient httpClient,
                  HttpLoggingInterceptor.Level httpLoggingLevel) {
        this.clientId = clientId;
        this.secret = secret;
        this.scopes = Collections.unmodifiableList(scopes);
        this.apiHost = apiHost;
        this.useHttps = useHttps;

        if(httpLoggingLevel == null) {
            this.httpLoggingLevel = HttpLoggingInterceptor.Level.NONE;
        } else {
            this.httpLoggingLevel = httpLoggingLevel;
        }

        if(httpClient == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new Slf4jLoggingInterceptor());
            logging.setLevel(this.httpLoggingLevel);
            this.httpClient = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(new OAuthInterceptor(new UrlBuilder(this), this))
                    .addInterceptor(logging)
                    .build();
        } else {
            this.httpClient = httpClient;
        }

    }

    public String getClientId() {
        return clientId;
    }

    public String getSecret() {
        return secret;
    }

    public String getApiHost() {
        return apiHost;
    }

    public boolean useHttps() {
        return useHttps;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public OkHttpClient okHttpClient() { return httpClient;}

    public HttpLoggingInterceptor.Level level() { return httpLoggingLevel;}

    public static Builder with(){
        return new Builder();
    }

    public static class Builder{
        private String clientId;
        private String secret;
        private String apiHost = "api.domo.com";
        private boolean useHttps = true;
        private List<Scope> scopes = new ArrayList<>();
        private OkHttpClient httpClient;
        private HttpLoggingInterceptor.Level httpLoggingLevel;

        public Builder() {
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder apiHost(String apiHost) {
            this.apiHost = apiHost;
            return this;
        }

        public Builder useHttps(boolean useHttps) {
            this.useHttps = useHttps;
            return this;
        }

        public Builder httpClient(OkHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder httpLoggingLevel(HttpLoggingInterceptor.Level level) {
            this.httpLoggingLevel = level;
            return this;
        }

        public Config build(){
            require("Client ID", clientId);
            require("Client Secret", secret);
            if(scopes.isEmpty()) {
                throw new ConfigException("At lease one scope is required");
            }

            return new Config(this.clientId,
                    this.secret,
                    this.apiHost,
                    this.useHttps,
                    this.scopes,
                    this.httpClient,
                    this.httpLoggingLevel);
        }

        private void require(String name, String value){
            if(value == null){
                throw new ConfigException(name + " must not be null");
            }
        }

        public Builder scope(Scope... newScopes) {
            scopes.clear();
            Collections.addAll(scopes, newScopes);
            return this;
        }
    }
}