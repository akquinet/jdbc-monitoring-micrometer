package de.akquinet.ccsp.monitoring.jdbc.wrapper

import de.akquinet.ccsp.monitoring.*
import java.sql.Connection

class JDBCConnection(
	private val jdbcMetrics: JDBCMetrics,
	private val connection: Connection
) : Connection by connection {
	init {
		jdbcMetrics.counter(JDBC_CONNECTIONS_OPENED).increment()
		jdbcMetrics.gaugeCounter(JDBC_CONNECTIONS_ACTIVE).increment()
	}

	override fun close() {
		jdbcMetrics.counter(JDBC_CONNECTIONS_CLOSED).increment()
		jdbcMetrics.gaugeCounter(JDBC_CONNECTIONS_ACTIVE).decrement()

		connection.close()
	}

	override fun createStatement() = JDBCStatement(jdbcMetrics, connection.createStatement())

	override fun createStatement(resultSetType: Int, resultSetConcurrency: Int) =
		JDBCStatement(jdbcMetrics, connection.createStatement(resultSetType, resultSetConcurrency))

	override fun createStatement(resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int) =
		JDBCStatement(jdbcMetrics, connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability))

	override fun prepareStatement(sql: String) =
		JDBCPreparedStatement(jdbcMetrics, sql, connection.prepareStatement(sql))

	override fun prepareStatement(sql: String, autoGeneratedKeys: Int) =
		JDBCPreparedStatement(jdbcMetrics, sql, connection.prepareStatement(sql, autoGeneratedKeys))

	override fun prepareStatement(sql: String, columnIndexes: IntArray) =
		JDBCPreparedStatement(jdbcMetrics, sql, connection.prepareStatement(sql, columnIndexes))

	override fun prepareStatement(sql: String, columnNames: Array<String>) =
		JDBCPreparedStatement(jdbcMetrics, sql, connection.prepareStatement(sql, columnNames))

	override fun prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int) =
		JDBCPreparedStatement(jdbcMetrics, sql, connection.prepareStatement(sql, resultSetType, resultSetConcurrency))

	override fun prepareStatement(
		sql: String,
		resultSetType: Int,
		resultSetConcurrency: Int,
		resultSetHoldability: Int
	) = JDBCPreparedStatement(
		jdbcMetrics, sql, connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability)
	)
}