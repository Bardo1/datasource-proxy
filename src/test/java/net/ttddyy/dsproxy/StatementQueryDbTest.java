package net.ttddyy.dsproxy;

import net.ttddyy.dsproxy.listener.ProxyDataSourceListener;
import net.ttddyy.dsproxy.listener.ProxyDataSourceListenerAdapter;
import net.ttddyy.dsproxy.listener.QueryExecutionContext;
import net.ttddyy.dsproxy.proxy.JdbcProxyFactory;
import net.ttddyy.dsproxy.proxy.ProxyConfig;
import net.ttddyy.dsproxy.proxy.SimpleResultSetProxyLogicFactory;
import net.ttddyy.dsproxy.proxy.jdk.JdkJdbcProxyFactory;
import net.ttddyy.dsproxy.proxy.jdk.ResultSetInvocationHandler;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Tadaya Tsuyukubo
 */
@DatabaseTest
public class StatementQueryDbTest {

    private DataSource jdbcDataSource;

    private DbResourceCleaner cleaner;

    public StatementQueryDbTest(DataSource jdbcDataSource, DbResourceCleaner cleaner) {
        this.jdbcDataSource = jdbcDataSource;
        this.cleaner = cleaner;
    }

    @Test
    public void resultSetProxy() throws Throwable {
        Connection conn = this.jdbcDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        JdbcProxyFactory proxyFactory = new JdkJdbcProxyFactory();
        ProxyConfig proxyConfig = ProxyConfig.Builder.create().resultSetProxyLogicFactory(new SimpleResultSetProxyLogicFactory()).build();
        Statement proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        // verify executeQuery
        ResultSet result = proxySt.executeQuery("select * from emp;");
        assertThat(result).isInstanceOf(ResultSet.class);
        assertThat(Proxy.isProxyClass(result.getClass())).isTrue();
        assertThat(Proxy.getInvocationHandler(result)).isExactlyInstanceOf(ResultSetInvocationHandler.class);

        // verify getResultSet
        proxySt.execute("select * from emp;", Statement.RETURN_GENERATED_KEYS);
        result = proxySt.getResultSet();
        assertThat(result).isInstanceOf(ResultSet.class);
        assertThat(Proxy.isProxyClass(result.getClass())).isTrue();
        assertThat(Proxy.getInvocationHandler(result)).isExactlyInstanceOf(ResultSetInvocationHandler.class);

        // verify getGeneratedKeys
        // generatedKeys have own proxy factory, thus expecting non-proxy to be returned
        result = proxySt.getGeneratedKeys();
        assertThat(result).isInstanceOf(ResultSet.class);
        assertThat(Proxy.isProxyClass(result.getClass())).isFalse();
    }

    @Test
    public void generatedKeysProxy() throws Throwable {
        Connection conn = this.jdbcDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        JdbcProxyFactory proxyFactory = new JdkJdbcProxyFactory();
        ProxyConfig proxyConfig = ProxyConfig.Builder.create().generatedKeysProxyLogicFactory(new SimpleResultSetProxyLogicFactory()).build();
        Statement proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        ResultSet generatedKeys;
        ResultSet result;

        // just calling getGeneratedKeys is not allowed in MySQL
        // TODO: cleanup
        if (!DbTestUtils.isMysql()) {

            // verify getGeneratedKeys
            generatedKeys = proxySt.getGeneratedKeys();
            assertThat(generatedKeys).isInstanceOf(ResultSet.class);
            assertThat(Proxy.isProxyClass(generatedKeys.getClass())).isTrue();
            assertThat(Proxy.getInvocationHandler(generatedKeys)).isExactlyInstanceOf(ResultSetInvocationHandler.class);

            // other ResultSet returning methods should not return proxy

            // verify executeQuery
            result = proxySt.executeQuery("select * from emp;");
            assertThat(result).isInstanceOf(ResultSet.class);
            assertThat(Proxy.isProxyClass(result.getClass())).isFalse();

            // generated keys should have empty proxied result set
            generatedKeys = proxySt.getGeneratedKeys();
            assertThat(generatedKeys).isInstanceOf(ResultSet.class);
            assertThat(Proxy.isProxyClass(generatedKeys.getClass())).isTrue();
            assertThat(generatedKeys.next()).isFalse();
        }

        // verify getResultSet
        proxySt.execute("select * from emp;", Statement.RETURN_GENERATED_KEYS);
        result = proxySt.getResultSet();
        assertThat(result).isInstanceOf(ResultSet.class);
        assertThat(Proxy.isProxyClass(result.getClass())).isFalse();

        // generated keys should have empty proxied result set
        generatedKeys = proxySt.getGeneratedKeys();
        assertThat(generatedKeys).isInstanceOf(ResultSet.class);
        assertThat(Proxy.isProxyClass(generatedKeys.getClass())).isTrue();
        assertThat(generatedKeys.next()).isFalse();
    }

    @Test
    public void autoRetrieveGeneratedKeys() throws Throwable {
        Connection conn = this.jdbcDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        final AtomicReference<QueryExecutionContext> listenerReceivedExecutionContext = new AtomicReference<>();
        ProxyDataSourceListener listener = new ProxyDataSourceListenerAdapter() {
            @Override
            public void afterQuery(QueryExecutionContext executionContext) {
                // since generatedKeys will NOT be closed, they can be read afterwards.
                listenerReceivedExecutionContext.set(executionContext);
            }
        };

        // autoRetrieveGeneratedKeys=true
        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .listener(listener)
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(false)
                .build();
        JdbcProxyFactory proxyFactory = new JdkJdbcProxyFactory();
        Statement proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.RETURN_GENERATED_KEYS);

        QueryExecutionContext info = listenerReceivedExecutionContext.get();
        assertThat(info).isNotNull();

        ResultSet generatedKeys = info.getGeneratedKeys();
        assertThat(generatedKeys).isInstanceOf(ResultSet.class);
        assertThat(Proxy.isProxyClass(generatedKeys.getClass())).isFalse();

        // calling "statement.getGeneratedKeys()" should return the same object
        ResultSet directGeneratedKeys = proxySt.getGeneratedKeys();
        assertThat(directGeneratedKeys).isSameAs(generatedKeys);

        // verify generated keys ResultSet
        generatedKeys.next();
        int generatedId = generatedKeys.getInt(1);
        assertThat(generatedId).as("generated ID").isEqualTo(3);  // sequence starts from 1. (two rows are inserted as initial data)

        // reset
        listenerReceivedExecutionContext.set(null);

        // autoRetrieveGeneratedKeys=false
        proxyConfig = ProxyConfig.Builder.create()
                .listener(listener)
                .autoRetrieveGeneratedKeys(false)
                .autoCloseGeneratedKeys(false)
                .build();
        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.RETURN_GENERATED_KEYS);

        info = listenerReceivedExecutionContext.get();
        assertThat(info).isNotNull();

        assertThat(info.getGeneratedKeys()).isNull();
    }

    @Test
    public void autoRetrieveGeneratedKeysWithExecuteQueryMethod() throws Throwable {
        Connection conn = this.jdbcDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        final AtomicReference<QueryExecutionContext> listenerReceivedExecutionContext = new AtomicReference<QueryExecutionContext>();
        ProxyDataSourceListener listener = new ProxyDataSourceListenerAdapter() {
            @Override
            public void afterQuery(QueryExecutionContext executionContext) {
                // since generatedKeys will NOT be closed, they can be read afterwards.
                listenerReceivedExecutionContext.set(executionContext);
            }
        };

        // autoRetrieveGeneratedKeys=true
        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .listener(listener)
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(false)
                .build();
        JdbcProxyFactory proxyFactory = new JdkJdbcProxyFactory();
        Statement proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        // it should NOT generate keys
        proxySt.execute("insert into emp ( id, name ) values (3, 'baz');");
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNull();


    }

    @Test
    public void autoRetrieveGeneratedKeysWithQueryExecutionMethods() throws Throwable {
        Connection conn = this.jdbcDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        final AtomicReference<QueryExecutionContext> listenerReceivedExecutionContext = new AtomicReference<QueryExecutionContext>();
        ProxyDataSourceListener listener = new ProxyDataSourceListenerAdapter() {
            @Override
            public void afterQuery(QueryExecutionContext executionContext) {
                // since generatedKeys will NOT be closed, they can be read afterwards.
                listenerReceivedExecutionContext.set(executionContext);
            }
        };

        // autoRetrieveGeneratedKeys=true
        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .listener(listener)
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(false)
                .build();
        JdbcProxyFactory proxyFactory = new JdkJdbcProxyFactory();
        Statement proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);


        // Test with NOT enabling generated-keys
        proxySt.execute("insert into emp_with_auto_id ( name ) values ('BAZ');");
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNull();

        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);
        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');");
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNull();

        // Statement#executeLargeUpdate is not implemented in HSQL yet

        // Specify NO_GENERATED_KEYS
        proxySt.execute("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.NO_GENERATED_KEYS);
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNull();

        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);
        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.NO_GENERATED_KEYS);
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNull();


        // Test with enabling generated-keys

        // with Statement.RETURN_GENERATED_KEYS
        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);
        proxySt.execute("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.RETURN_GENERATED_KEYS);
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNotNull();
        listenerReceivedExecutionContext.set(null);

        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);
        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.RETURN_GENERATED_KEYS);
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNotNull();
        listenerReceivedExecutionContext.set(null);


        if (!DbTestUtils.isPostgres()) {
            // for Postgres, returning autogenerated keys by column index is not supported

            // with int[]
            proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);
            proxySt.execute("insert into emp_with_auto_id ( name ) values ('BAZ');", new int[]{1});
            assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNotNull();
            listenerReceivedExecutionContext.set(null);

            proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);
            proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", new int[]{1});
            assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNotNull();
            listenerReceivedExecutionContext.set(null);
        }

        // with String[]
        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);
        proxySt.execute("insert into emp_with_auto_id ( name ) values ('BAZ');", new String[]{"id"});
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNotNull();
        listenerReceivedExecutionContext.set(null);

        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);
        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", new String[]{"id"});
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNotNull();
        listenerReceivedExecutionContext.set(null);

    }

    @Test
    public void autoRetrieveGeneratedKeysWithBatchStatement() throws Throwable {
        Connection conn = this.jdbcDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        final AtomicReference<QueryExecutionContext> listenerReceivedExecutionContext = new AtomicReference<QueryExecutionContext>();
        ProxyDataSourceListener listener = new ProxyDataSourceListenerAdapter() {
            @Override
            public void afterQuery(QueryExecutionContext executionContext) {
                // since generatedKeys will NOT be closed, they can be read afterwards.
                listenerReceivedExecutionContext.set(executionContext);
            }
        };

        JdbcProxyFactory proxyFactory = new JdkJdbcProxyFactory();
        ProxyConfig proxyConfig;
        Statement proxySt;

        // default value (expected to NOT auto-retrieve)
        proxyConfig = ProxyConfig.Builder.create()
                .listener(listener)
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(false)
                .build();
        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.addBatch("insert into emp_with_auto_id ( name ) values ('BAZ');");
        proxySt.addBatch("insert into emp_with_auto_id ( name ) values ('BAZ');");
        proxySt.addBatch("insert into emp_with_auto_id ( name ) values ('BAZ');");
        proxySt.executeBatch();
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNull();
        listenerReceivedExecutionContext.set(null);

        // executeLargeBatch is not implemented for HSQLDB

        // autoRetrieve for batch statement = true
        proxyConfig = ProxyConfig.Builder.create()
                .listener(listener)
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(false)
                .retrieveGeneratedKeysForBatchStatement(true)  // set true
                .build();
        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.addBatch("insert into emp_with_auto_id ( name ) values ('BAZ');");
        proxySt.addBatch("insert into emp_with_auto_id ( name ) values ('BAZ');");
        proxySt.addBatch("insert into emp_with_auto_id ( name ) values ('BAZ');");
        proxySt.executeBatch();
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNotNull();
        listenerReceivedExecutionContext.set(null);


        // autoRetrieve for batch statement = false
        proxyConfig = ProxyConfig.Builder.create()
                .listener(listener)
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(false)
                .retrieveGeneratedKeysForBatchStatement(false)  // set false
                .build();
        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.addBatch("insert into emp_with_auto_id ( name ) values ('BAZ');");
        proxySt.addBatch("insert into emp_with_auto_id ( name ) values ('BAZ');");
        proxySt.addBatch("insert into emp_with_auto_id ( name ) values ('BAZ');");
        proxySt.executeBatch();
        assertThat(listenerReceivedExecutionContext.get().getGeneratedKeys()).isNull();
    }


    @Test
    public void getGeneratedKeys() throws Throwable {
        Connection conn = this.jdbcDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        // when no configuration is specified for generated keys (disabling generated keys related feature)
        ProxyConfig proxyConfig = ProxyConfig.Builder.create().build();
        JdbcProxyFactory proxyFactory = new JdkJdbcProxyFactory();
        Statement proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.RETURN_GENERATED_KEYS);

        // calling getGeneratedKeys() multiple time is not defined in JDBC spec
        // For hsqldb, calling second time closes previously returned ResultSet and returns new ResultSet.
        ResultSet generatedKeys1 = proxySt.getGeneratedKeys();
        assertThat(generatedKeys1.isClosed()).isFalse();

        ResultSet generatedKeys2 = proxySt.getGeneratedKeys();
        assertThat(generatedKeys2.isClosed()).isFalse();

        if (DbTestUtils.isHsql()) {
            // everytime it should return a new generatedKeys
            assertThat(generatedKeys2).isNotSameAs(generatedKeys1);
        } else if (DbTestUtils.isPostgres()) {
            // postgres returns same ResultSet
            assertThat(generatedKeys2).isSameAs(generatedKeys1);
        }


        // only specify autoRetrieveGeneratedKeys=true
        proxyConfig = ProxyConfig.Builder.create()
                .autoRetrieveGeneratedKeys(true)
                .build();
        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.RETURN_GENERATED_KEYS);

        ResultSet generatedKeys3 = proxySt.getGeneratedKeys();
        assertThat(generatedKeys3.isClosed()).isFalse();

        ResultSet generatedKeys4 = proxySt.getGeneratedKeys();
        assertThat(generatedKeys4.isClosed()).isFalse();

        // From here, currently testing only HSQL specific behavior
        // TODO: check behavior for other database
        if (!DbTestUtils.isHsql()) {
            return;
        }


        // since first generated-keys is open, second call should return the same one
        assertThat(generatedKeys4).isSameAs(generatedKeys3);

        generatedKeys4.close();
        ResultSet generatedKeys5 = proxySt.getGeneratedKeys();
        assertThat(generatedKeys5.isClosed()).isFalse();

        // once it is closed, getGeneratedKeys should return a new ResultSet
        assertThat(generatedKeys5).isNotSameAs(generatedKeys4);

        ResultSet generatedKeys6 = proxySt.getGeneratedKeys();
        assertThat(generatedKeys6.isClosed()).isFalse();

        // again it's not closed, thus same ResultSet should be returned
        assertThat(generatedKeys6).isSameAs(generatedKeys5);

    }

    @Test
    public void getGeneratedKeysWithAutoRetrievalAndAutoCloseFalse() throws Throwable {
        Connection conn = this.jdbcDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        // autoCloseGeneratedKeys=false
        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(false)
                .build();
        JdbcProxyFactory proxyFactory = new JdkJdbcProxyFactory();
        Statement proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.RETURN_GENERATED_KEYS);

        // while they are not closed, getGeneratedKeys() should return same object
        ResultSet generatedKeys1 = proxySt.getGeneratedKeys();
        ResultSet generatedKeys2 = proxySt.getGeneratedKeys();

        assertThat(generatedKeys2).isSameAs(generatedKeys1);

        // From here, it checks HSQL specific behavior
        //  - HSQL: everytime returns new ResultSet
        //  - POSTGRES: it returns same ResultSet.
        // TODO: may check behavior for other DB
        if (!DbTestUtils.isHsql()) {
            return;
        }

        // when generatedKeys is closed, getGeneratedKeys() should return new ResultSet
        generatedKeys1.close();
        ResultSet generatedKeys3 = proxySt.getGeneratedKeys();

        assertThat(generatedKeys3).isNotSameAs(generatedKeys1);
        assertThat(generatedKeys3.isClosed()).isFalse();

        // since generatedKeys3 is open, calling getGeneratedKeys() should return the same resultset
        ResultSet generatedKeys4 = proxySt.getGeneratedKeys();
        assertThat(generatedKeys4).isSameAs(generatedKeys3);

    }

    @Test
    public void getGeneratedKeysWithAutoRetrievalAndAutoCloseTrue() throws Throwable {
        Connection conn = this.jdbcDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        // autoCloseGeneratedKeys=true
        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(true)
                .build();
        JdbcProxyFactory proxyFactory = new JdkJdbcProxyFactory();
        Statement proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.RETURN_GENERATED_KEYS);

        ResultSet generatedKeys1 = proxySt.getGeneratedKeys();

        if (DbTestUtils.isPostgres()) {
            // For Postgres, since it returns same ResultSet, it has already auto-closed
            assertThat(generatedKeys1.isClosed()).isTrue();

            // same closed ResultSet will be returned
            ResultSet generatedKeys2 = proxySt.getGeneratedKeys();
            assertThat(generatedKeys2.isClosed()).isTrue();
            assertThat(generatedKeys2).isSameAs(generatedKeys1);
        } else {
            // case for HSQL for now

            // auto close should not affect the result of "getGeneratedKeys" method.
            assertThat(generatedKeys1.isClosed()).isFalse();

            ResultSet generatedKeys2 = proxySt.getGeneratedKeys();
            assertThat(generatedKeys2.isClosed()).isFalse();

            // result of "getGeneratedKeys" is still open, thus second call of "getGeneratedKeys" should return the same one
            assertThat(generatedKeys2).isSameAs(generatedKeys1);
            assertThat(generatedKeys1.isClosed()).isFalse();
        }

    }

    @Test
    public void autoCloseGeneratedKeysProxy() throws Throwable {
        Connection conn = this.jdbcDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        final AtomicReference<QueryExecutionContext> listenerReceivedExecutionContext = new AtomicReference<QueryExecutionContext>();
        ProxyDataSourceListener listener = new ProxyDataSourceListenerAdapter() {
            @Override
            public void afterQuery(QueryExecutionContext executionContext) {
                ResultSet generatedKeys = executionContext.getGeneratedKeys();
                boolean isClosed = true;
                try {
                    isClosed = generatedKeys.isClosed();
                } catch (SQLException ex) {
                    fail("Failed to call generatedKeys.isClosed() message=" + ex.getMessage());
                }
                assertThat(isClosed).isFalse();
                listenerReceivedExecutionContext.set(executionContext);
            }
        };

        // autoCloseGeneratedKeys=false
        JdbcProxyFactory proxyFactory = new JdkJdbcProxyFactory();
        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .listener(listener)
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(false)
                .build();
        Statement proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.RETURN_GENERATED_KEYS);

        QueryExecutionContext info = listenerReceivedExecutionContext.get();
        ResultSet generatedKeys = info.getGeneratedKeys();
        assertThat(generatedKeys.isClosed()).isFalse();

        try {
            generatedKeys.close();
        } catch (SQLException ex) {
            fail("closing non closed ResultSet should success. message=" + ex.getMessage());
        }

        listenerReceivedExecutionContext.set(null);

        // autoCloseGeneratedKeys=true
        proxyConfig = ProxyConfig.Builder.create()
                .listener(listener)
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(true)
                .build();
        proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('QUX');", Statement.RETURN_GENERATED_KEYS);

        info = listenerReceivedExecutionContext.get();
        generatedKeys = info.getGeneratedKeys();
        assertThat(generatedKeys.isClosed()).isTrue();

    }

    @Test
    public void autoRetrieveGeneratedKeysWithGeneratedKeysProxy() throws Throwable {
        Connection conn = this.jdbcDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        final AtomicReference<QueryExecutionContext> listenerReceivedExecutionContext = new AtomicReference<QueryExecutionContext>();
        ProxyDataSourceListener listener = new ProxyDataSourceListenerAdapter() {
            @Override
            public void afterQuery(QueryExecutionContext executionContext) {
                listenerReceivedExecutionContext.set(executionContext);
            }
        };

        // specify autoRetrieveGeneratedKeys and proxy factory
        JdbcProxyFactory proxyFactory = new JdkJdbcProxyFactory();
        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .listener(listener)
                .autoRetrieveGeneratedKeys(true)
                .generatedKeysProxyLogicFactory(new SimpleResultSetProxyLogicFactory())
                .autoCloseGeneratedKeys(false)
                .build();

        Statement proxySt = proxyFactory.createStatement(st, new ConnectionInfo(), conn, proxyConfig);

        proxySt.executeUpdate("insert into emp_with_auto_id ( name ) values ('BAZ');", Statement.RETURN_GENERATED_KEYS);

        QueryExecutionContext info = listenerReceivedExecutionContext.get();
        assertThat(info).isNotNull();
        assertThat(info.getGeneratedKeys()).isInstanceOf(ResultSet.class);

        ResultSet generatedKeys = info.getGeneratedKeys();
        assertThat(Proxy.isProxyClass(generatedKeys.getClass())).isTrue();
        assertThat(Proxy.getInvocationHandler(generatedKeys)).isExactlyInstanceOf(ResultSetInvocationHandler.class);

        generatedKeys.next();
        int generatedId = generatedKeys.getInt(1);
        assertThat(generatedId).as("generated ID").isEqualTo(3);  // sequence starts from 1. (two data rows inserted as initial data)

    }

}