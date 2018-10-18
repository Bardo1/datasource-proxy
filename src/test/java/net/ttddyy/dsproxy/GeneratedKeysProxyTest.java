package net.ttddyy.dsproxy;

import net.ttddyy.dsproxy.listener.ProxyDataSourceListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 21/11/17.
 *
 * @author Reda.Housni-Alaoui
 */
public class GeneratedKeysProxyTest {

    @Test
    public void checkThatResultSetCanBeConsumedMoreThanOnce() throws Exception {
        JDBCDataSource dataSourceWithData = dataSourceWithData();

        GeneratedKeysProxyTest.LoggingExecutionListener listener = new GeneratedKeysProxyTest.LoggingExecutionListener();
        ProxyDataSource proxyDataSource = ProxyDataSourceBuilder.create(dataSourceWithData)
                .listener(listener)
                .autoRetrieveGeneratedKeysWithRepeatableReadProxy(false)
                .build();

        checkThatResultSetCanBeConsumedViaTheProxyDataSource(proxyDataSource);
        checkThatTheResultSetWasAlsoConsumedInTheListener(listener);
    }

    private void checkThatTheResultSetWasAlsoConsumedInTheListener(GeneratedKeysProxyTest.LoggingExecutionListener listener) {
        assertThat(listener.generatedKeys).containsExactly(2);
    }

    private JDBCDataSource dataSourceWithData() throws SQLException {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setDatabase("jdbc:hsqldb:mem:test");
        Connection connection = dataSource.getConnection();
        connection.createStatement().execute("CREATE TABLE GeneratedKeysProxyTest(id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY)");
        connection.prepareStatement("INSERT INTO GeneratedKeysProxyTest(id) VALUES(default)").executeUpdate();
        connection.prepareStatement("INSERT INTO GeneratedKeysProxyTest(id) VALUES(default)").executeUpdate();
        return dataSource;
    }

    private void checkThatResultSetCanBeConsumedViaTheProxyDataSource(ProxyDataSource proxyDataSource) throws SQLException {
        Connection connection = proxyDataSource.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO GeneratedKeysProxyTest(id) VALUES(default)", Statement.RETURN_GENERATED_KEYS);
        preparedStatement.executeUpdate();
        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();

        generatedKeys.next();
        assertThat(generatedKeys.getInt(1)).isEqualTo(2);

        generatedKeys.close();
    }

    private static class LoggingExecutionListener implements ProxyDataSourceListener {

        private List<Integer> generatedKeys = new ArrayList<>();

        @Override
        public void afterQuery(ExecutionInfo execInfo) {
            try {
                ResultSet resultSet = execInfo.getGeneratedKeys();
                if (resultSet == null) {
                    return;
                }
                while (resultSet.next()) {
                    generatedKeys.add(resultSet.getInt(1));
                }
                resultSet.beforeFirst();
            } catch (SQLException e) {
                throw new IllegalStateException("Could not extract generated keys", e);
            }
        }

    }

}