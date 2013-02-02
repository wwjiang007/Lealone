/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.codefollower.yourbase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import com.codefollower.yourbase.constant.Constants;
import com.codefollower.yourbase.jdbc.JdbcConnection;
import com.codefollower.yourbase.message.DbException;
import com.codefollower.yourbase.message.TraceSystem;

/*## Java 1.7 ##
import java.util.logging.Logger;
//*/

/**
 * The database driver. An application should not use this class directly. The
 * only thing the application needs to do is load the driver. This can be done
 * using Class.forName. To load the driver and open a database connection, use
 * the following code:
 *
 * <pre>
 * Class.forName(&quot;com.codefollower.yourbase.Driver&quot;);
 * Connection conn = DriverManager.getConnection(
 *      &quot;jdbc:yourbase:&tilde;/test&quot;, &quot;sa&quot;, &quot;sa&quot;);
 * </pre>
 */
public class Driver implements java.sql.Driver {

    private static final Driver INSTANCE = new Driver();
    private static final String DEFAULT_URL = "jdbc:default:connection";
    //private static final String DEFAULT_HBASE_URL = "jdbc:yourbase:hbase:";
    //private static final String DEFAULT_MEM_URL = "jdbc:yourbase:mem:";
    private static final ThreadLocal<Connection> DEFAULT_CONNECTION = new ThreadLocal<Connection>();

    private static volatile boolean registered;

    static {
        load();
    }

    /**
     * Open a database connection.
     * This method should not be called by an application.
     * Instead, the method DriverManager.getConnection should be used.
     *
     * @param url the database URL
     * @param info the connection properties
     * @return the new connection or null if the URL is not supported
     */
    public Connection connect(String url, Properties info) throws SQLException {
        try {
            if (info == null) {
                info = new Properties();
            }
            if (!acceptsURL(url)) {
                return null;
            }
            if (url.equals(DEFAULT_URL)) {
                return DEFAULT_CONNECTION.get();
            }

            return new JdbcConnection(url, info);
        } catch (Exception e) {
            throw DbException.toSQLException(e);
        }
    }

    /**
     * Check if the driver understands this URL.
     * This method should not be called by an application.
     *
     * @param url the database URL
     * @return if the driver understands the URL
     */
    public boolean acceptsURL(String url) {
        if (url != null) {
            if (url.startsWith(Constants.START_URL)) {
                return true;
            } else if (url.equals(DEFAULT_URL)) {
                return DEFAULT_CONNECTION.get() != null;
            }
        }
        return false;
    }

    /**
     * Get the major version number of the driver.
     * This method should not be called by an application.
     *
     * @return the major version number
     */
    public int getMajorVersion() {
        return Constants.VERSION_MAJOR;
    }

    /**
     * Get the minor version number of the driver.
     * This method should not be called by an application.
     *
     * @return the minor version number
     */
    public int getMinorVersion() {
        return Constants.VERSION_MINOR;
    }

    /**
     * Get the list of supported properties.
     * This method should not be called by an application.
     *
     * @param url the database URL
     * @param info the connection properties
     * @return a zero length array
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        return new DriverPropertyInfo[0];
    }

    /**
     * Check if this driver is compliant to the JDBC specification.
     * This method should not be called by an application.
     *
     * @return true
     */
    public boolean jdbcCompliant() {
        return true;
    }

    /**
     * [Not supported]
     */
/*## Java 1.7 ##
    public Logger getParentLogger() {
        return null;
    }
//*/

    /**
     * INTERNAL
     */
    public static synchronized Driver load() {
        try {
            if (!registered) {
                registered = true;
                DriverManager.registerDriver(INSTANCE);
            }
        } catch (SQLException e) {
            TraceSystem.traceThrowable(e);
        }
        return INSTANCE;
    }

    /**
     * INTERNAL
     */
    public static synchronized void unload() {
        try {
            if (registered) {
                registered = false;
                DriverManager.deregisterDriver(INSTANCE);
            }
        } catch (SQLException e) {
            TraceSystem.traceThrowable(e);
        }
    }

    /**
     * INTERNAL
     */
    public static void setDefaultConnection(Connection c) {
        DEFAULT_CONNECTION.set(c);
    }

    /**
     * INTERNAL
     */
    public static void setThreadContextClassLoader(Thread thread) {
        // Apache Tomcat: use the classloader of the driver to avoid the
        // following log message:
        // org.apache.catalina.loader.WebappClassLoader clearReferencesThreads
        // SEVERE: The web application appears to have started a thread named
        // ... but has failed to stop it.
        // This is very likely to create a memory leak.
        try {
            thread.setContextClassLoader(Driver.class.getClassLoader());
        } catch (Throwable t) {
            // ignore
        }
    }

}
