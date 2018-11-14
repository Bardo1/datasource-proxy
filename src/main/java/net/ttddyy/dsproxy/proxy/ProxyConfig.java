package net.ttddyy.dsproxy.proxy;

import net.ttddyy.dsproxy.ConnectionIdManager;
import net.ttddyy.dsproxy.listener.QueryExecutionContext;
import net.ttddyy.dsproxy.listener.CompositeProxyDataSourceListener;
import net.ttddyy.dsproxy.listener.ProxyDataSourceListener;
import net.ttddyy.dsproxy.transform.QueryTransformer;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Hold configuration objects for creating a proxy.
 *
 * @author Tadaya Tsuyukubo
 * @since 1.4.3
 */
public class ProxyConfig {

    private static class GeneratedKeysConfig {
        private ResultSetProxyLogicFactory proxyLogicFactory; // can be null if generated keys proxy is disabled
        private boolean autoRetrieve;
        private boolean retrieveForBatchStatement = false;  // default false
        private boolean retrieveForBatchPreparedOrCallable = true;  // default true
        private boolean autoClose;
    }

    public static class Builder {
        private String dataSourceName = "";
        private CompositeProxyDataSourceListener listeners = new CompositeProxyDataSourceListener();  // empty default
        private QueryTransformer queryTransformer = QueryTransformer.DEFAULT;
        private JdbcProxyFactory jdbcProxyFactory = JdbcProxyFactory.DEFAULT;
        private ResultSetProxyLogicFactory resultSetProxyLogicFactory;  // can be null if resultset proxy is disabled
        private ConnectionIdManager connectionIdManager = new DefaultConnectionIdManager();  // create instance every time
        private GeneratedKeysConfig generatedKeysConfig = new GeneratedKeysConfig();

        public static Builder create() {
            return new Builder();
        }

        public static Builder from(ProxyConfig proxyConfig) {
            return new Builder()
                    .dataSourceName(proxyConfig.dataSourceName)
                    .listener(proxyConfig.listeners)
                    .queryTransformer(proxyConfig.queryTransformer)
                    .jdbcProxyFactory(proxyConfig.jdbcProxyFactory)
                    .resultSetProxyLogicFactory(proxyConfig.resultSetProxyLogicFactory)
                    .connectionIdManager(proxyConfig.connectionIdManager)
                    .generatedKeysProxyLogicFactory(proxyConfig.generatedKeysConfig.proxyLogicFactory)
                    .autoRetrieveGeneratedKeys(proxyConfig.generatedKeysConfig.autoRetrieve)
                    .retrieveGeneratedKeysForBatchStatement(proxyConfig.generatedKeysConfig.retrieveForBatchStatement)
                    .retrieveGeneratedKeysForBatchPreparedOrCallable(proxyConfig.generatedKeysConfig.retrieveForBatchPreparedOrCallable)
                    .autoCloseGeneratedKeys(proxyConfig.generatedKeysConfig.autoClose)
                    ;
        }

        public ProxyConfig build() {
            ProxyConfig proxyConfig = new ProxyConfig();
            proxyConfig.dataSourceName = this.dataSourceName;
            proxyConfig.listeners = this.listeners;
            proxyConfig.queryTransformer = this.queryTransformer;
            proxyConfig.jdbcProxyFactory = this.jdbcProxyFactory;
            proxyConfig.resultSetProxyLogicFactory = this.resultSetProxyLogicFactory;
            proxyConfig.connectionIdManager = this.connectionIdManager;

            // generated keys
            proxyConfig.generatedKeysConfig.proxyLogicFactory = this.generatedKeysConfig.proxyLogicFactory;
            proxyConfig.generatedKeysConfig.autoRetrieve = this.generatedKeysConfig.autoRetrieve;
            proxyConfig.generatedKeysConfig.autoClose = this.generatedKeysConfig.autoClose;
            proxyConfig.generatedKeysConfig.retrieveForBatchStatement = this.generatedKeysConfig.retrieveForBatchStatement;
            proxyConfig.generatedKeysConfig.retrieveForBatchPreparedOrCallable = this.generatedKeysConfig.retrieveForBatchPreparedOrCallable;

            return proxyConfig;
        }

        public Builder dataSourceName(String dataSourceName) {
            this.dataSourceName = dataSourceName;
            return this;
        }

        public Builder listener(ProxyDataSourceListener listener) {
            if (listener instanceof CompositeProxyDataSourceListener) {
                this.listeners.addListeners(((CompositeProxyDataSourceListener) listener).getListeners());
            } else {
                this.listeners.addListener(listener);
            }
            return this;
        }

        public Builder queryTransformer(QueryTransformer queryTransformer) {
            this.queryTransformer = queryTransformer;
            return this;
        }

        public Builder jdbcProxyFactory(JdbcProxyFactory jdbcProxyFactory) {
            this.jdbcProxyFactory = jdbcProxyFactory;
            return this;
        }

        public Builder resultSetProxyLogicFactory(ResultSetProxyLogicFactory resultSetProxyLogicFactory) {
            this.resultSetProxyLogicFactory = resultSetProxyLogicFactory;
            return this;
        }

        public Builder autoRetrieveGeneratedKeys(boolean autoRetrieveGeneratedKeys) {
            this.generatedKeysConfig.autoRetrieve = autoRetrieveGeneratedKeys;
            return this;
        }

        public Builder autoCloseGeneratedKeys(boolean autoCloseGeneratedKeys) {
            this.generatedKeysConfig.autoClose = autoCloseGeneratedKeys;
            return this;
        }

        public Builder retrieveGeneratedKeysForBatchStatement(boolean retrieveForBatchStatement) {
            this.generatedKeysConfig.retrieveForBatchStatement = retrieveForBatchStatement;
            return this;
        }

        public Builder retrieveGeneratedKeysForBatchPreparedOrCallable(boolean retrieveForBatchPreparedOrCallable) {
            this.generatedKeysConfig.retrieveForBatchPreparedOrCallable = retrieveForBatchPreparedOrCallable;
            return this;
        }

        public Builder generatedKeysProxyLogicFactory(ResultSetProxyLogicFactory generatedKeysProxyLogicFactory) {
            this.generatedKeysConfig.proxyLogicFactory = generatedKeysProxyLogicFactory;
            return this;
        }

        public Builder connectionIdManager(ConnectionIdManager connectionIdManager) {
            this.connectionIdManager = connectionIdManager;
            return this;
        }

    }

    private String dataSourceName;
    private CompositeProxyDataSourceListener listeners;
    private QueryTransformer queryTransformer;
    private JdbcProxyFactory jdbcProxyFactory;
    private ResultSetProxyLogicFactory resultSetProxyLogicFactory;
    private ConnectionIdManager connectionIdManager;
    private GeneratedKeysConfig generatedKeysConfig = new GeneratedKeysConfig();

    public String getDataSourceName() {
        return dataSourceName;
    }

    public CompositeProxyDataSourceListener getListeners() {
        return listeners;
    }

    public QueryTransformer getQueryTransformer() {
        return queryTransformer;
    }

    public JdbcProxyFactory getJdbcProxyFactory() {
        return jdbcProxyFactory;
    }

    public ResultSetProxyLogicFactory getResultSetProxyLogicFactory() {
        return resultSetProxyLogicFactory;
    }

    /**
     * @return {@code true} when {@link ResultSetProxyLogicFactory} for {@link ResultSet} is specified
     * @since 1.4.5
     */
    public boolean isResultSetProxyEnabled() {
        return this.resultSetProxyLogicFactory != null;
    }


    /**
     * @since 1.4.5
     */
    public ResultSetProxyLogicFactory getGeneratedKeysProxyLogicFactory() {
        return this.generatedKeysConfig.proxyLogicFactory;
    }

    /**
     * When this returns {@code true}, the proxy logic always call {@link Statement#getGeneratedKeys()} and set it to
     * {@link QueryExecutionContext}.
     * Also, if {@link Statement#getGeneratedKeys()} is called, it will return cached generated keys {@link ResultSet}
     * when cached {@link ResultSet} is still open. If cached {@link ResultSet} is closed, calling
     * {@link Statement#getGeneratedKeys()} returns a new {@link ResultSet}. (Calling {@link Statement#getGeneratedKeys()}
     * multiple times is not defined in JDBC spec. Therefore, behavior depends on JDBC driver.)
     *
     * If {@code false} is returned, {@link QueryExecutionContext#getGeneratedKeys()} returns {@code null}.
     *
     * @return true if generated-keys retrieval is enabled
     * @since 1.4.5
     */
    public boolean isAutoRetrieveGeneratedKeys() {
        return this.generatedKeysConfig.autoRetrieve;
    }

    /**
     * When {@link JdbcProxyFactory} for generated-keys is specified, return {@code true}.
     *
     * @return {@code true} when {@link ResultSetProxyLogicFactory} for generated keys is specified
     * @see ProxyConfig.Builder#generatedKeysProxyLogicFactory(ResultSetProxyLogicFactory)
     * @since 1.4.5
     */
    public boolean isGeneratedKeysProxyEnabled() {
        return this.generatedKeysConfig.proxyLogicFactory != null;
    }

    /**
     * Whether to auto close {@link ResultSet} for generated-keys that is automatically retrieved.
     *
     * When this returns {@code true}, always close the {@link ResultSet} for generated keys when
     * {@link ProxyDataSourceListener#afterQuery(QueryExecutionContext)} has finished.
     * The result of {@link Statement#getGeneratedKeys()} method will not be closed by this. Only auto retrieved
     * {@link ResultSet} of generated keys is closed.
     *
     * @since 1.4.5
     */
    public boolean isAutoCloseGeneratedKeys() {
        return this.generatedKeysConfig.autoClose;
    }

    /**
     * Perform generated-keys auto retrieval for batch statement.
     *
     * Default is set to {@code false}.
     *
     * @since 1.4.6
     */
    public boolean isRetrieveGeneratedKeysForBatchStatement() {
        return this.generatedKeysConfig.retrieveForBatchStatement;
    }

    /**
     * Perform generated-keys auto retrieval for batch prepared or callable.
     *
     * Default is set to {@code true}.
     *
     * @since 1.4.6
     */
    public boolean isRetrieveGeneratedKeysForBatchPreparedOrCallable() {
        return this.generatedKeysConfig.retrieveForBatchPreparedOrCallable;
    }

    public ConnectionIdManager getConnectionIdManager() {
        return connectionIdManager;
    }


}
