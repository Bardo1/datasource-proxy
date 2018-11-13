package net.ttddyy.dsproxy.proxy;

import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.StatementType;
import net.ttddyy.dsproxy.listener.LastExecutionAwareListener;
import net.ttddyy.dsproxy.listener.MethodExecutionContext;
import net.ttddyy.dsproxy.listener.ProxyDataSourceListener;
import net.ttddyy.dsproxy.proxy.jdk.ResultSetInvocationHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tadaya Tsuyukubo
 */
public class StatementProxyLogicForPreparedStatementMockTest {

    private static final String DS_NAME = "myDS";

    @Test
    public void testExecuteQuery() throws Throwable {
        final String query = "select * from emp where id = ?";

        PreparedStatement stat = mock(PreparedStatement.class);
        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);

        StatementProxyLogic logic = getProxyLogic(stat, query, listener, null);


        Array array = mock(Array.class);
        InputStream inputStream = mock(InputStream.class);
        BigDecimal bigDecimal = mock(BigDecimal.class);
        InputStream binaryStream = mock(InputStream.class);
        Blob blob = mock(Blob.class);
        boolean booleanValue = true;
        Reader reader = mock(Reader.class);
        Clob clob = mock(Clob.class);
        Date date = mock(Date.class);
        double doubleValue = 10.0d;
        Float floatValue = 20f;
        Integer intvalue = 30;
        Long longValue = 40L;
        Object object = mock(Object.class);
        Ref ref = mock(Ref.class);
        Short shortValue = 50;
        String stringValue = "str";
        Time time = mock(Time.class);
        Timestamp timestamp = mock(Timestamp.class);
        URL url = new URL("http://foo.com");

        Method setArray = PreparedStatement.class.getMethod("setArray", int.class, Array.class);
        Method setAsciiStream = PreparedStatement.class.getMethod("setAsciiStream", int.class, InputStream.class);
        Method setBigDecimal = PreparedStatement.class.getMethod("setBigDecimal", int.class, BigDecimal.class);
        Method setBinaryStream = PreparedStatement.class.getMethod("setBinaryStream", int.class, InputStream.class);
        Method setBlob = PreparedStatement.class.getMethod("setBlob", int.class, Blob.class);
        Method setBoolean = PreparedStatement.class.getMethod("setBoolean", int.class, boolean.class);
        Method setCharacterStream = PreparedStatement.class.getMethod("setCharacterStream", int.class, Reader.class);
        Method setClob = PreparedStatement.class.getMethod("setClob", int.class, Clob.class);
        Method setDate = PreparedStatement.class.getMethod("setDate", int.class, Date.class);
        Method setDouble = PreparedStatement.class.getMethod("setDouble", int.class, double.class);
        Method setFloat = PreparedStatement.class.getMethod("setFloat", int.class, float.class);
        Method setInt = PreparedStatement.class.getMethod("setInt", int.class, int.class);
        Method setLong = PreparedStatement.class.getMethod("setLong", int.class, long.class);
        Method setNull = PreparedStatement.class.getMethod("setNull", int.class, int.class);
        Method setObject = PreparedStatement.class.getMethod("setObject", int.class, Object.class);
        Method setRef = PreparedStatement.class.getMethod("setRef", int.class, Ref.class);
        Method setShort = PreparedStatement.class.getMethod("setShort", int.class, short.class);
        Method setString = PreparedStatement.class.getMethod("setString", int.class, String.class);
        Method setTime = PreparedStatement.class.getMethod("setTime", int.class, Time.class);
        Method setTimestamp = PreparedStatement.class.getMethod("setTimestamp", int.class, Timestamp.class);
        Method setURL = PreparedStatement.class.getMethod("setURL", int.class, URL.class);
        Method executeQuery = PreparedStatement.class.getMethod("executeQuery");

        logic.invoke(null, setArray, new Object[]{1, array});
        logic.invoke(null, setAsciiStream, new Object[]{2, inputStream});
        logic.invoke(null, setBigDecimal, new Object[]{3, bigDecimal});
        logic.invoke(null, setBinaryStream, new Object[]{4, binaryStream});
        logic.invoke(null, setBlob, new Object[]{5, blob});
        logic.invoke(null, setBoolean, new Object[]{6, booleanValue});
        logic.invoke(null, setCharacterStream, new Object[]{7, reader});
        logic.invoke(null, setClob, new Object[]{8, clob});
        logic.invoke(null, setDate, new Object[]{9, date});
        logic.invoke(null, setDouble, new Object[]{10, doubleValue});
        logic.invoke(null, setFloat, new Object[]{11, floatValue});
        logic.invoke(null, setInt, new Object[]{12, intvalue});
        logic.invoke(null, setLong, new Object[]{13, longValue});
        logic.invoke(null, setNull, new Object[]{14, Types.VARCHAR});
        logic.invoke(null, setObject, new Object[]{15, object});
        logic.invoke(null, setRef, new Object[]{16, ref});
        logic.invoke(null, setShort, new Object[]{17, shortValue});
        logic.invoke(null, setString, new Object[]{18, stringValue});
        logic.invoke(null, setTime, new Object[]{19, time});
        logic.invoke(null, setTimestamp, new Object[]{20, timestamp});
        logic.invoke(null, setURL, new Object[]{21, url});

        logic.invoke(null, executeQuery, null);

        verify(stat).setArray(1, array);
        verify(stat).setAsciiStream(2, inputStream);
        verify(stat).setBigDecimal(3, bigDecimal);
        verify(stat).setBinaryStream(4, binaryStream);
        verify(stat).setBlob(5, blob);
        verify(stat).setBoolean(6, booleanValue);
        verify(stat).setCharacterStream(7, reader);
        verify(stat).setClob(8, clob);
        verify(stat).setDate(9, date);
        verify(stat).setDouble(10, doubleValue);
        verify(stat).setFloat(11, floatValue);
        verify(stat).setInt(12, intvalue);
        verify(stat).setLong(13, longValue);
        verify(stat).setNull(14, Types.VARCHAR);
        verify(stat).setObject(15, object);
        verify(stat).setRef(16, ref);
        verify(stat).setShort(17, shortValue);
        verify(stat).setString(18, stringValue);
        verify(stat).setTime(19, time);
        verify(stat).setTimestamp(20, timestamp);
        verify(stat).setURL(21, url);
        verify(stat).executeQuery();

        Map<String, Object> expectedQueryArgas = new LinkedHashMap<String, Object>();
        expectedQueryArgas.put("1", array);
        expectedQueryArgas.put("2", inputStream);
        expectedQueryArgas.put("3", bigDecimal);
        expectedQueryArgas.put("4", binaryStream);
        expectedQueryArgas.put("5", blob);
        expectedQueryArgas.put("6", booleanValue);
        expectedQueryArgas.put("7", reader);
        expectedQueryArgas.put("8", clob);
        expectedQueryArgas.put("9", date);
        expectedQueryArgas.put("10", doubleValue);
        expectedQueryArgas.put("11", floatValue);
        expectedQueryArgas.put("12", intvalue);
        expectedQueryArgas.put("13", longValue);
        expectedQueryArgas.put("14", Types.VARCHAR);
        expectedQueryArgas.put("15", object);
        expectedQueryArgas.put("16", ref);
        expectedQueryArgas.put("17", shortValue);
        expectedQueryArgas.put("18", stringValue);
        expectedQueryArgas.put("19", time);
        expectedQueryArgas.put("20", timestamp);
        expectedQueryArgas.put("21", url);

        verifyListener(listener, "executeQuery", query, expectedQueryArgas);

    }

    private StatementProxyLogic getProxyLogic(PreparedStatement ps, String query, ProxyDataSourceListener listener, Connection proxyConnection) {
        return getProxyLogic(ps, query, listener, proxyConnection, false, false);
    }

    private StatementProxyLogic getProxyLogic(PreparedStatement ps, String query,
                                              ProxyDataSourceListener listener, Connection proxyConnection,
                                              boolean createResultSetProxy, boolean createGenerateKeysProxy) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setDataSourceName(DS_NAME);

        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .listener(listener)
                .resultSetProxyLogicFactory(createResultSetProxy ? new SimpleResultSetProxyLogicFactory() : null)
                .generatedKeysProxyLogicFactory(createGenerateKeysProxy ? new SimpleResultSetProxyLogicFactory() : null)
                .build();

        return StatementProxyLogic.Builder.create()
                .statement(ps, StatementType.PREPARED)
                .query(query)
                .connectionInfo(connectionInfo)
                .proxyConnection(proxyConnection)
                .proxyConfig(proxyConfig)
                .build();
    }

    @SuppressWarnings("unchecked")
    private void verifyListener(ProxyDataSourceListener listener, String methodName, String query, Map<String, Object> expectedQueryArgs) {
        ArgumentCaptor<ExecutionInfo> executionInfoCaptor = ArgumentCaptor.forClass(ExecutionInfo.class);

        verify(listener).afterQuery(executionInfoCaptor.capture());

        ExecutionInfo execInfo = executionInfoCaptor.getValue();
        assertThat(execInfo.getMethod()).isNotNull();
        assertThat(execInfo.getMethod().getName()).isEqualTo(methodName);

        assertThat(execInfo.getMethodArgs()).isNull();
        assertThat(execInfo.getDataSourceName()).isEqualTo(DS_NAME);
        assertThat(execInfo.getThrowable()).isNull();
        assertThat(execInfo.isBatch()).isFalse();
        assertThat(execInfo.getBatchSize()).isEqualTo(0);

        List<QueryInfo> queryInfoList = execInfo.getQueries();
        assertThat(queryInfoList).hasSize(1);
        QueryInfo queryInfo = queryInfoList.get(0);
        assertThat(queryInfo.getQuery()).isEqualTo(query);

        List<ParameterSetOperations> parameterSetOperations = queryInfo.getParameterSetOperations();
        assertThat(parameterSetOperations).hasSize(1).as("non-batch query size is always 1");
        List<ParameterSetOperation> operations = parameterSetOperations.get(0).getOperations();

        Map<String, Object> queryArgs = new HashMap<String, Object>();
        for (ParameterSetOperation operation : operations) {
            Object[] args = operation.getArgs();
            queryArgs.put(args[0].toString(), args[1]);
        }

        assertThat(queryArgs).hasSize(expectedQueryArgs.size());
        for (Map.Entry<String, Object> entry : expectedQueryArgs.entrySet()) {
            assertThat(queryArgs).containsEntry(entry.getKey(), entry.getValue());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBatch() throws Throwable {
        final String query = "update emp set name = ? where id = ?";

        PreparedStatement stat = mock(PreparedStatement.class);
        when(stat.executeBatch()).thenReturn(new int[]{1, 1, 1});

        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);

        StatementProxyLogic logic = getProxyLogic(stat, query, listener, null);

        Method setString = PreparedStatement.class.getMethod("setString", int.class, String.class);
        Method setInt = PreparedStatement.class.getMethod("setInt", int.class, int.class);
        Method addBatch = PreparedStatement.class.getMethod("addBatch");
        Method executeBatch = PreparedStatement.class.getMethod("executeBatch");

        logic.invoke(null, setString, new Object[]{1, "foo"});
        logic.invoke(null, setInt, new Object[]{2, 10});
        logic.invoke(null, addBatch, null);

        logic.invoke(null, setString, new Object[]{1, "bar"});
        logic.invoke(null, setInt, new Object[]{2, 20});
        logic.invoke(null, addBatch, null);

        logic.invoke(null, setString, new Object[]{1, "baz"});
        logic.invoke(null, setInt, new Object[]{2, 30});
        logic.invoke(null, addBatch, null);

        Object result = logic.invoke(null, executeBatch, null);

        assertThat(result).isInstanceOf(int[].class);
        assertThat(((int[]) result).length).isEqualTo(3);
        assertThat(((int[]) result)[0]).isEqualTo(1);
        assertThat(((int[]) result)[1]).isEqualTo(1);
        assertThat(((int[]) result)[2]).isEqualTo(1);

        verify(stat).setString(1, "foo");
        verify(stat).setInt(2, 10);
        verify(stat).setString(1, "bar");
        verify(stat).setInt(2, 20);
        verify(stat).setString(1, "baz");
        verify(stat).setInt(2, 30);
        verify(stat, times(3)).addBatch();

        Map<String, Object> expectedArgs1 = new LinkedHashMap<String, Object>();
        expectedArgs1.put("1", "foo");
        expectedArgs1.put("2", 10);

        Map<String, Object> expectedArgs2 = new LinkedHashMap<String, Object>();
        expectedArgs2.put("1", "bar");
        expectedArgs2.put("2", 20);

        Map<String, Object> expectedArgs3 = new LinkedHashMap<String, Object>();
        expectedArgs3.put("1", "baz");
        expectedArgs3.put("2", 30);

        MockTestUtils.verifyListenerForBatch(listener, DS_NAME, query, expectedArgs1, expectedArgs2, expectedArgs3);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBatchWithClearBatch() throws Throwable {
        final String query = "update emp set name = ? where id = ?";

        PreparedStatement stat = mock(PreparedStatement.class);
        when(stat.executeBatch()).thenReturn(new int[]{1});

        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);

        StatementProxyLogic logic = getProxyLogic(stat, query, listener, null);

        Method setString = PreparedStatement.class.getMethod("setString", int.class, String.class);
        Method setInt = PreparedStatement.class.getMethod("setInt", int.class, int.class);
        Method addBatch = PreparedStatement.class.getMethod("addBatch");
        Method clearBatch = PreparedStatement.class.getMethod("clearBatch");
        Method executeBatch = PreparedStatement.class.getMethod("executeBatch");

        logic.invoke(null, setString, new Object[]{1, "foo"});
        logic.invoke(null, setInt, new Object[]{2, 10});
        logic.invoke(null, addBatch, null);

        logic.invoke(null, clearBatch, null);

        logic.invoke(null, setString, new Object[]{1, "FOO"});
        logic.invoke(null, setInt, new Object[]{2, 20});
        logic.invoke(null, addBatch, null);

        Object result = logic.invoke(null, executeBatch, null);

        assertThat(result).isInstanceOf(int[].class);

        assertThat(((int[]) result).length).isEqualTo(1);
        assertThat(((int[]) result)[0]).isEqualTo(1);

        verify(stat).setString(1, "foo");
        verify(stat).setInt(2, 10);
        verify(stat).clearBatch();
        verify(stat).setString(1, "FOO");
        verify(stat).setInt(2, 20);
        verify(stat, times(2)).addBatch();

        Map<String, Object> expectedArgs = new LinkedHashMap<String, Object>();
        expectedArgs.put("1", "FOO");
        expectedArgs.put("2", 20);

        MockTestUtils.verifyListenerForBatch(listener, DS_NAME, query, expectedArgs);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBatchWithClearParameters() throws Throwable {
        final String query = "update emp set name = ? where id = ?";

        PreparedStatement stat = mock(PreparedStatement.class);
        when(stat.executeBatch()).thenReturn(new int[]{1});

        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);

        StatementProxyLogic logic = getProxyLogic(stat, query, listener, null);

        Method setString = PreparedStatement.class.getMethod("setString", int.class, String.class);
        Method setInt = PreparedStatement.class.getMethod("setInt", int.class, int.class);
        Method addBatch = PreparedStatement.class.getMethod("addBatch");
        Method clearParametes = PreparedStatement.class.getMethod("clearParameters");
        Method executeBatch = PreparedStatement.class.getMethod("executeBatch");

        logic.invoke(null, setString, new Object[]{1, "foo"});
        logic.invoke(null, clearParametes, null);
        logic.invoke(null, setString, new Object[]{1, "FOO"});
        logic.invoke(null, setInt, new Object[]{2, 10});
        logic.invoke(null, addBatch, null);

        Object result = logic.invoke(null, executeBatch, null);

        assertThat(result).isInstanceOf(int[].class);

        assertThat(((int[]) result).length).isEqualTo(1);
        assertThat(((int[]) result)[0]).isEqualTo(1);

        verify(stat).setString(1, "foo");
        verify(stat).clearParameters();
        verify(stat).setString(1, "FOO");
        verify(stat).setInt(2, 10);
        verify(stat).addBatch();

        Map<String, Object> expectedArgs = new LinkedHashMap<String, Object>();
        expectedArgs.put("1", "FOO");
        expectedArgs.put("2", 10);

        MockTestUtils.verifyListenerForBatch(listener, DS_NAME, query, expectedArgs);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testClearParameters() throws Throwable {
        final String query = "update emp set name = ? where id = ?";

        PreparedStatement stat = mock(PreparedStatement.class);

        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);

        StatementProxyLogic logic = getProxyLogic(stat, query, listener, null);

        Method setString = PreparedStatement.class.getMethod("setString", int.class, String.class);
        Method setInt = PreparedStatement.class.getMethod("setInt", int.class, int.class);
        Method addBatch = PreparedStatement.class.getMethod("addBatch");
        Method clearParametes = PreparedStatement.class.getMethod("clearParameters");
        Method executeBatch = PreparedStatement.class.getMethod("executeBatch");

        logic.invoke(null, setString, new Object[]{1, "foo"});
        logic.invoke(null, setInt, new Object[]{2, 10});
        logic.invoke(null, clearParametes, null);
        logic.invoke(null, addBatch, null);

        logic.invoke(null, executeBatch, null);

        verify(stat).setString(1, "foo");
        verify(stat).setInt(2, 10);
        verify(stat).clearParameters();
        verify(stat).addBatch();

        ArgumentCaptor<ExecutionInfo> executionInfoCaptor = ArgumentCaptor.forClass(ExecutionInfo.class);

        verify(listener).afterQuery(executionInfoCaptor.capture());

        ExecutionInfo execInfo = executionInfoCaptor.getValue();
        List<QueryInfo> queryInfoList = execInfo.getQueries();
        assertThat(queryInfoList).hasSize(1);

        assertThat(queryInfoList.get(0).getParameterSetOperations()).hasSize(1);
        assertThat(queryInfoList.get(0).getParameterSetOperations().get(0).getOperations()).as("Args should be empty").isEmpty();


    }


    @Test
    public void testGetTarget() throws Throwable {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);
        StatementProxyLogic logic = getProxyLogic(stmt, null, listener, null);

        Method method = ProxyJdbcObject.class.getMethod("getTarget");
        Object result = logic.invoke(null, method, null);

        assertThat(result).isInstanceOf(PreparedStatement.class).isSameAs(stmt);
    }

    @Test
    public void testUnwrap() throws Throwable {
        PreparedStatement mock = mock(PreparedStatement.class);
        when(mock.unwrap(String.class)).thenReturn("called");
        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);

        StatementProxyLogic logic = getProxyLogic(mock, null, listener, null);

        Method method = PreparedStatement.class.getMethod("unwrap", Class.class);
        Object result = logic.invoke(null, method, new Object[]{String.class});

        verify(mock).unwrap(String.class);
        assertThat(result).isInstanceOf(String.class).isEqualTo("called");
    }

    @Test
    public void testIsWrapperFor() throws Throwable {
        PreparedStatement mock = mock(PreparedStatement.class);
        when(mock.isWrapperFor(String.class)).thenReturn(true);
        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);

        StatementProxyLogic logic = getProxyLogic(mock, null, listener, null);

        Method method = PreparedStatement.class.getMethod("isWrapperFor", Class.class);
        Object result = logic.invoke(null, method, new Object[]{String.class});

        verify(mock).isWrapperFor(String.class);
        assertThat(result).isInstanceOf(Boolean.class).isEqualTo(true);
    }

    @Test
    public void testGetConnection() throws Throwable {
        Connection conn = mock(Connection.class);
        PreparedStatement stat = mock(PreparedStatement.class);
        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);

        when(stat.getConnection()).thenReturn(conn);
        StatementProxyLogic logic = getProxyLogic(stat, null, listener, conn);

        Method method = PreparedStatement.class.getMethod("getConnection");
        Object result = logic.invoke(null, method, null);

        assertThat(result).isSameAs(conn);
    }

    @Test
    public void testToString() throws Throwable {
        PreparedStatement stat = mock(PreparedStatement.class);
        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);

        when(stat.toString()).thenReturn("my ps");
        StatementProxyLogic logic = getProxyLogic(stat, null, listener, null);

        Method method = Object.class.getMethod("toString");
        Object result = logic.invoke(null, method, null);

        assertThat(result).isEqualTo(stat.getClass().getSimpleName() + " [my ps]");
    }

    @Test
    public void testHashCode() throws Throwable {
        PreparedStatement stat = mock(PreparedStatement.class);
        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);

        StatementProxyLogic logic = getProxyLogic(stat, null, listener, null);

        Method method = Object.class.getMethod("hashCode");
        Object result = logic.invoke(null, method, null);

        assertThat(result).isInstanceOf(Integer.class).isEqualTo(stat.hashCode());
    }

    @Test
    public void testEquals() throws Throwable {
        PreparedStatement stat = mock(PreparedStatement.class);
        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);
        StatementProxyLogic logic = getProxyLogic(stat, null, listener, null);

        Method method = Object.class.getMethod("equals", Object.class);

        // equals(null)
        Object result = logic.invoke(null, method, new Object[]{null});
        assertThat(result).isInstanceOf(Boolean.class).isEqualTo(false);

        // equals(true)
        result = logic.invoke(null, method, new Object[]{stat});
        assertThat(result).isInstanceOf(Boolean.class).isEqualTo(true);
    }

    @Test
    public void proxyResultSet() throws Throwable {

        final AtomicReference<Object> listenerReceivedResult = new AtomicReference<>();
        ProxyDataSourceListener listener = new ProxyDataSourceListener() {
            @Override
            public void afterQuery(ExecutionInfo execInfo) {
                listenerReceivedResult.set(execInfo.getResult());
            }
        };


        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getMetaData()).thenReturn(metaData);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(resultSet);
        when(ps.getGeneratedKeys()).thenReturn(resultSet);
        StatementProxyLogic logic = getProxyLogic(ps, "", listener, null, true, false);


        // "executeQuery" with no args
        Method executeQueryMethod = PreparedStatement.class.getMethod("executeQuery");
        Method getGeneratedKeysMethod = Statement.class.getMethod("getGeneratedKeys");
        Object result;

        // check "executeQuery"
        result = logic.invoke(null, executeQueryMethod, new Object[]{});
        assertThat(result).isInstanceOf(ResultSet.class);
        assertTrue(Proxy.isProxyClass(result.getClass()));
        assertTrue(Proxy.getInvocationHandler(result).getClass().equals(ResultSetInvocationHandler.class));
        assertThat(listenerReceivedResult.get()).as("listener should receive proxied resultset").isSameAs(result);

        listenerReceivedResult.set(null);

        // check "getGeneratedKeys". generated keys has separate configuration
        result = logic.invoke(null, getGeneratedKeysMethod, null);
        assertThat(result).isInstanceOf(ResultSet.class);
        assertFalse(Proxy.isProxyClass(result.getClass()));

    }

    @Test
    public void proxyGeneratedKeysResultSet() throws Throwable {

        final AtomicReference<Object> listenerReceivedResult = new AtomicReference<>();
        ProxyDataSourceListener listener = new ProxyDataSourceListener() {
            @Override
            public void afterQuery(ExecutionInfo execInfo) {
                listenerReceivedResult.set(execInfo.getResult());
            }
        };


        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getMetaData()).thenReturn(metaData);


        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(resultSet);
        when(ps.getGeneratedKeys()).thenReturn(resultSet);
        when(ps.getResultSet()).thenReturn(resultSet);
        StatementProxyLogic logic = getProxyLogic(ps, "", listener, null, false, true);


        // "executeQuery", "getGeneratedKeys", "getResultSet"
        Method getGeneratedKeysMethod = PreparedStatement.class.getMethod("getGeneratedKeys");
        Method executeQueryMethod = PreparedStatement.class.getMethod("executeQuery");
        Method getResultSetMethod = PreparedStatement.class.getMethod("getResultSet");
        Object result;


        // check "getGeneratedKeys"
        result = logic.invoke(null, getGeneratedKeysMethod, new Object[]{});
        assertThat(result).isInstanceOf(ResultSet.class);
        assertTrue(Proxy.isProxyClass(result.getClass()));
        assertTrue(Proxy.getInvocationHandler(result).getClass().equals(ResultSetInvocationHandler.class));
        assertThat(listenerReceivedResult).as("listener should not be called").hasValue(null);

        listenerReceivedResult.set(null);

        // check "executeQuery"
        result = logic.invoke(null, executeQueryMethod, null);
        assertThat(result).isInstanceOf(ResultSet.class);
        assertFalse(Proxy.isProxyClass(result.getClass()));

        // check "getResultSet"
        result = logic.invoke(null, getResultSetMethod, null);
        assertThat(result).isInstanceOf(ResultSet.class);
        assertFalse(Proxy.isProxyClass(result.getClass()));

    }

    @Test
    public void autoCloseGeneratedKeys() throws Throwable {

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.isClosed()).thenReturn(false);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.getGeneratedKeys()).thenReturn(resultSet);

        // autoCloseGeneratedKeys=true
        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(true)
                .build();

        StatementProxyLogic logic = StatementProxyLogic.Builder.create()
                .statement(ps, StatementType.PREPARED)
                .connectionInfo(new ConnectionInfo())
                .proxyConfig(proxyConfig)
                .generateKey(true)
                .build();


        // executeUpdate
        Method executeUpdate = PreparedStatement.class.getMethod("executeUpdate");
        logic.invoke(null, executeUpdate, null);

        verify(resultSet).close();


        reset(resultSet);

        // autoCloseGeneratedKeys=false
        proxyConfig = ProxyConfig.Builder.create()
                .autoRetrieveGeneratedKeys(true)
                .autoCloseGeneratedKeys(false)
                .build();

        logic = StatementProxyLogic.Builder.create()
                .statement(ps, StatementType.PREPARED)
                .connectionInfo(new ConnectionInfo())
                .proxyConfig(proxyConfig)
                .build();


        // "executeQuery
        logic.invoke(null, executeUpdate, null);

        verify(resultSet, never()).close();

    }


    @Test
    public void methodExecutionListener() throws Throwable {
        LastExecutionAwareListener listener = new LastExecutionAwareListener();
        ProxyConfig proxyConfig = ProxyConfig.Builder.create().listener(listener).build();

        PreparedStatement ps = mock(PreparedStatement.class);

        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setDataSourceName(DS_NAME);

        StatementProxyLogic logic = new StatementProxyLogic.Builder()
                .statement(ps, StatementType.PREPARED)
                .connectionInfo(connectionInfo)
                .proxyConfig(proxyConfig)
                .build();

        Method method = PreparedStatement.class.getMethod("executeQuery");
        logic.invoke(null, method, new Object[]{});

        assertTrue(listener.isBeforeMethodCalled());
        assertTrue(listener.isAfterMethodCalled());

        MethodExecutionContext executionContext = listener.getAfterMethodContext();
        assertSame(PreparedStatement.class, executionContext.getMethod().getDeclaringClass(), "method should come from interface");
        assertSame("executeQuery", executionContext.getMethod().getName());
        assertSame(ps, executionContext.getTarget());
        assertSame(connectionInfo, executionContext.getConnectionInfo());
    }

}
