package net.ttddyy.dsproxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tadaya Tsuyukubo
 * @since 1.3
 */
public class ExecutionInfoBuilder {
    private String dataSourceName;
    private Method method;
    private Object[] methodArgs;
    private Object result;
    private long elapsedTime;
    private Throwable throwable;
    private StatementType statementType;
    private boolean success;
    private boolean batch;
    private int batchSize;
    private String connectionId;
    private List<QueryInfo> queries = new ArrayList<>();
    private long threadId;
    private String threadName;

    public static ExecutionInfoBuilder create() {
        return new ExecutionInfoBuilder();
    }

    public ExecutionInfoBuilder dataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        return this;
    }

    public ExecutionInfoBuilder method(Method method) {
        this.method = method;
        return this;
    }

    public ExecutionInfoBuilder methodArgs(Object[] methodArgs) {
        this.methodArgs = methodArgs;
        return this;
    }

    public ExecutionInfoBuilder result(Object result) {
        this.result = result;
        return this;
    }

    public ExecutionInfoBuilder elapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
        return this;
    }

    public ExecutionInfoBuilder throwable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public ExecutionInfoBuilder statementType(StatementType statementType) {
        this.statementType = statementType;
        return this;
    }

    public ExecutionInfoBuilder success(boolean success) {
        this.success = success;
        return this;
    }

    public ExecutionInfoBuilder batch(boolean batch) {
        this.batch = batch;
        return this;
    }

    public ExecutionInfoBuilder batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public ExecutionInfoBuilder connectionId(String connectionId) {
        this.connectionId = connectionId;
        return this;
    }

    public ExecutionInfoBuilder queries(List<QueryInfo> queries) {
        this.queries = queries;
        return this;
    }

    /**
     * @since 2.0
     */
    public ExecutionInfoBuilder threadId(long threadId) {
        this.threadId = threadId;
        return this;
    }

    /**
     * @since 2.0
     */
    public ExecutionInfoBuilder threadName(String threadName) {
        this.threadName = threadName;
        return this;
    }

    public ExecutionInfo build() {
        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.setDataSourceName(dataSourceName);
        executionInfo.setMethod(method);
        executionInfo.setMethodArgs(methodArgs);
        executionInfo.setResult(result);
        executionInfo.setElapsedTime(elapsedTime);
        executionInfo.setThrowable(throwable);
        executionInfo.setStatementType(statementType);
        executionInfo.setSuccess(success);
        executionInfo.setBatch(batch);
        executionInfo.setBatchSize(batchSize);
        executionInfo.setConnectionId(this.connectionId);
        executionInfo.setQueries(this.queries);
        executionInfo.setThreadId(this.threadId);
        executionInfo.setThreadName(this.threadName);
        return executionInfo;
    }
}
