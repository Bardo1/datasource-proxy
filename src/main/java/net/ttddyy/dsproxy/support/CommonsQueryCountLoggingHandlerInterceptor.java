package net.ttddyy.dsproxy.support;

import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import net.ttddyy.dsproxy.listener.CommonsLogLevel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * Spring {@link org.springframework.web.servlet.HandlerInterceptor} to log the query metrics during a http request
 * lifecycle using Apache Commons Logging.
 *
 * @author Tadaya Tsuyukubo
 * @see CommonsQueryCountLoggingFilter
 * @see CommonsQueryCountLoggingRequestListener
 */
public class CommonsQueryCountLoggingHandlerInterceptor extends AbstractQueryCountLoggingHandlerInterceptor {

    private Log log = LogFactory.getLog(CommonsQueryCountLoggingHandlerInterceptor.class);
    private CommonsLogLevel logLevel = CommonsLogLevel.DEBUG;

    public CommonsQueryCountLoggingHandlerInterceptor() {
    }

    public CommonsQueryCountLoggingHandlerInterceptor(CommonsLogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    protected void writeLog(String logEntry) {
        CommonsLogUtils.writeLog(log, logLevel, logEntry);
    }

    public void setLogLevel(CommonsLogLevel logLevel) {
        this.logLevel = logLevel;
    }
}
