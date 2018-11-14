package net.ttddyy.dsproxy.listener;

import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;

/**
 * Update database access information.
 *
 * <p>Default implementation uses {@link ThreadQueryCountHolder} strategy that uses thread local to keep
 * {@link net.ttddyy.dsproxy.QueryCount}. {@link QueryCount} can be retrieved by {@link net.ttddyy.dsproxy.QueryCountHolder#get(String)}.
 *
 * <p>Alternatively, {@link SingleQueryCountHolder} strategy can be used. This strategy uses single instance to keep
 * {@link QueryCount}; therefore, {@link QueryCount} holds accumulated total values from any threads until values are cleared.
 *
 * <p>In web application lifecycle, one http request is handled by one thread.
 * Storing database access information into a thread local value provides metrics
 * information per http request.
 * On the other hand, using single instance to store database access information allows you to retrieve total accumulated
 * numbers since application has started.
 *
 * <p>{@link net.ttddyy.dsproxy.QueryCount} holds following data:
 * <ul>
 * <li> datasource name
 * <li> number of database call
 * <li> total query execution time
 * <li> number of queries by type
 * </ul>
 *
 * @author Tadaya Tsuyukubo
 * @see net.ttddyy.dsproxy.QueryCount
 * @see net.ttddyy.dsproxy.QueryCountHolder
 * @see net.ttddyy.dsproxy.listener.QueryCountStrategy
 */
public class DataSourceQueryCountListener implements ProxyDataSourceListener {

    // uses per thread implementation in default
    private QueryCountStrategy queryCountStrategy = new ThreadQueryCountHolder();

    @Override
    public void afterQuery(QueryExecutionContext executionContext) {
        String dataSourceName = executionContext.getDataSourceName();

        QueryCount count = this.queryCountStrategy.getOrCreateQueryCount(dataSourceName);

        // increment db call
        count.incrementTotal();
        if (executionContext.isSuccess()) {
            count.incrementSuccess();
        } else {
            count.incrementFailure();
        }

        // increment elapsed time
        long elapsedTime = executionContext.getElapsedTime();
        count.incrementTime(elapsedTime);

        // increment statement type
        count.increment(executionContext.getStatementType());

        // increment query count
        for (QueryInfo queryInfo : executionContext.getQueries()) {
            String query = queryInfo.getQuery();
            QueryType type = QueryUtils.getQueryType(query);
            count.increment(type);
        }

    }

    /**
     * @since 1.4.2
     */
    public QueryCountStrategy getQueryCountStrategy() {
        return queryCountStrategy;
    }

    /**
     * @since 1.4.2
     */
    public void setQueryCountStrategy(QueryCountStrategy queryCountStrategy) {
        this.queryCountStrategy = queryCountStrategy;
    }

}
