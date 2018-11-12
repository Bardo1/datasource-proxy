package net.ttddyy.dsproxy.proxy;

import net.ttddyy.dsproxy.listener.LastExecutionAwareListener;
import net.ttddyy.dsproxy.listener.MethodExecutionContext;
import net.ttddyy.dsproxy.listener.ProxyDataSourceListener;
import net.ttddyy.dsproxy.proxy.jdk.ConnectionInvocationHandler;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tadaya Tsuyukubo
 */
public class DataSourceProxyLogicMockTest {

    private static final String DS_NAME = "myDS";

    @Test
    public void testGetConnection() throws Throwable {
        DataSource ds = mock(DataSource.class);
        DataSourceProxyLogic logic = getProxyLogic(ds);

        Method method = DataSource.class.getMethod("getConnection");
        Object result = logic.invoke(null, method, null);

        assertThat(result).isInstanceOf(Connection.class);
        verifyConnection((Connection) result);
        verify(ds).getConnection();
    }

    private DataSourceProxyLogic getProxyLogic(DataSource ds) {
        ProxyDataSourceListener listener = mock(ProxyDataSourceListener.class);

        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .dataSourceName(DS_NAME)
                .listener(listener)
                .build();
        return new DataSourceProxyLogic(ds, proxyConfig);
    }

    private void verifyConnection(Connection conn) {
        assertThat(conn).isNotNull();

        assertThat(Proxy.isProxyClass(conn.getClass())).isTrue();
        InvocationHandler handler = Proxy.getInvocationHandler(conn);
        assertThat(handler).isInstanceOf(ConnectionInvocationHandler.class);

        assertThat(conn).isInstanceOf(ProxyJdbcObject.class);
    }

    @Test
    public void testGetTarget() throws Throwable {
        DataSource ds = mock(DataSource.class);
        DataSourceProxyLogic logic = getProxyLogic(ds);

        Method method = ProxyJdbcObject.class.getMethod("getTarget");
        Object result = logic.invoke(null, method, null);

        assertThat(result).isInstanceOf(DataSource.class);
        DataSource resultDS = (DataSource) result;
        assertThat(resultDS).isSameAs(ds);
    }

    @Test
    public void testUnwrap() throws Throwable {
        DataSource ds = mock(DataSource.class);
        when(ds.unwrap(String.class)).thenReturn("called");

        DataSourceProxyLogic logic = getProxyLogic(ds);

        Method method = Connection.class.getMethod("unwrap", Class.class);
        Object result = logic.invoke(null, method, new Object[]{String.class});

        verify(ds).unwrap(String.class);
        assertThat(result).isEqualTo("called");
    }

    @Test
    public void testIsWrapperFor() throws Throwable {
        DataSource ds = mock(DataSource.class);
        when(ds.isWrapperFor(String.class)).thenReturn(true);

        DataSourceProxyLogic logic = getProxyLogic(ds);

        Method method = Connection.class.getMethod("isWrapperFor", Class.class);
        Object result = logic.invoke(null, method, new Object[]{String.class});

        verify(ds).isWrapperFor(String.class);
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void testToString() throws Throwable {
        DataSource ds = mock(DataSource.class);

        when(ds.toString()).thenReturn("my ds");
        DataSourceProxyLogic logic = getProxyLogic(ds);

        Method method = Object.class.getMethod("toString");
        Object result = logic.invoke(null, method, null);

        assertThat(result).isEqualTo(ds.getClass().getSimpleName() + " [my ds]");
    }

    @Test
    public void testHashCode() throws Throwable {
        DataSource ds = mock(DataSource.class);
        DataSourceProxyLogic logic = getProxyLogic(ds);

        Method method = Object.class.getMethod("hashCode");
        Object result = logic.invoke(null, method, null);

        assertThat(result).isEqualTo(ds.hashCode());
    }

    @Test
    public void testEquals() throws Throwable {
        DataSource ds = mock(DataSource.class);
        DataSourceProxyLogic logic = getProxyLogic(ds);

        Method method = Object.class.getMethod("equals", Object.class);

        // equals(null)
        Object result = logic.invoke(null, method, new Object[]{null});
        assertThat(result).isEqualTo(false);

        // equals(true)
        result = logic.invoke(null, method, new Object[]{ds});
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void methodExecutionListener() throws Throwable {
        LastExecutionAwareListener listener = new LastExecutionAwareListener();

        DataSource ds = mock(DataSource.class);

        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .dataSourceName(DS_NAME)
                .listener(listener)
                .build();
        DataSourceProxyLogic logic = new DataSourceProxyLogic(ds, proxyConfig);


        Method method = DataSource.class.getMethod("getConnection");
        logic.invoke(null, method, new Object[]{});

        assertTrue(listener.isBeforeMethodCalled());
        assertTrue(listener.isAfterMethodCalled());

        MethodExecutionContext executionContext = listener.getAfterMethodContext();
        assertSame(DataSource.class, executionContext.getMethod().getDeclaringClass(), "method should come from interface");
        assertSame("getConnection", executionContext.getMethod().getName());
        assertSame(ds, executionContext.getTarget());
        assertNotNull(executionContext.getConnectionInfo());
    }

}
