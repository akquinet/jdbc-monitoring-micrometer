@file:Suppress("SqlResolve")

package de.akquinet.ccsp.monitoring.test

import de.akquinet.ccsp.monitoring.JDBC_CONNECTIONS_ACTIVE
import de.akquinet.ccsp.monitoring.JDBC_CONNECTIONS_CLOSED
import de.akquinet.ccsp.monitoring.JDBC_CONNECTIONS_OPENED
import de.akquinet.ccsp.monitoring.JDBC_PREPARED_STATEMENTS
import de.akquinet.ccsp.monitoring.jdbc.JDBCDataSourceMetrics
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConnectionCounterTest : AbstractJDBCTest() {
	private val dataSourceMetrics = JDBCDataSourceMetrics(dataSource).apply { bindTo(registry) }

	@Test
	fun `Count connections`() {
		checkMeters(0.0, 0.0, 0.0)

		dataSourceMetrics.connection.use {
			it.createStatement().execute(SQL_CREATE)
			checkMeters(1.0, 0.0, 1.0)
		}

		dataSourceMetrics.connection.use {
			it.prepareStatement(SQL_INSERT).apply {
				setInt(1, 1)
				setString(2, "akquinet")
			}.executeUpdate()
			checkMeters(2.0, 1.0, 1.0)
		}

		val counters = dataSourceMetrics.registry().get(JDBC_PREPARED_STATEMENTS).counters()
		assertThat(counters.size).isEqualTo(1)

		checkMeters(2.0, 2.0, 0.0)
	}

	private fun checkMeters(opened: Double, closed: Double, active: Double) {
		assertThat(dataSourceMetrics.counter(JDBC_CONNECTIONS_OPENED).count()).`as`(JDBC_CONNECTIONS_OPENED).isEqualTo(opened)
		assertThat(dataSourceMetrics.counter(JDBC_CONNECTIONS_CLOSED).count()).`as`(JDBC_CONNECTIONS_CLOSED).isEqualTo(closed)
		assertThat(dataSourceMetrics.gauge(JDBC_CONNECTIONS_ACTIVE).value()).`as`(JDBC_CONNECTIONS_ACTIVE).isEqualTo(active)
	}
}