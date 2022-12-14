package de.akquinet.ccsp.monitoring.jdbc.wrapper

import de.akquinet.ccsp.monitoring.*
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.Timer
import java.sql.ResultSet
import java.sql.Statement
import java.util.function.BooleanSupplier
import java.util.function.IntSupplier
import java.util.function.LongSupplier
import java.util.function.Supplier

class JDBCStatement(
	private val jdbcMetrics: JDBCMetrics,
	private val statement: Statement
) : Statement by statement {
	private val instanceCounter = jdbcMetrics.registerCounter(JDBC_STATEMENTS, Tags.empty(), UNIT_INSTANCES)
	private val batches = ArrayList<String>()

	init {
		instanceCounter.increment()
	}

	override fun execute(sql: String) = timer(sql).record(BooleanSupplier { statement.execute(sql) })

	override fun execute(sql: String, columnIndexes: IntArray) =
		timer(sql).record(BooleanSupplier { statement.execute(sql, columnIndexes) })

	override fun execute(sql: String, columnNames: Array<String>) =
		timer(sql).record(BooleanSupplier { statement.execute(sql, columnNames) })

	override fun execute(sql: String, autoGeneratedKeys: Int) = timer(sql)
		.record(BooleanSupplier { statement.execute(sql, autoGeneratedKeys) })

	override fun executeUpdate(sql: String) = timer(sql).record(IntSupplier { statement.executeUpdate(sql) })

	override fun executeUpdate(sql: String, autoGeneratedKeys: Int) =
		timer(sql).record(IntSupplier { statement.executeUpdate(sql, autoGeneratedKeys) })

	override fun executeUpdate(sql: String, columnIndexes: IntArray) =
		timer(sql).record(IntSupplier { statement.executeUpdate(sql, columnIndexes) })

	override fun executeUpdate(sql: String, columnNames: Array<out String>) =
		timer(sql).record(IntSupplier { statement.executeUpdate(sql, columnNames) })

	override fun executeQuery(sql: String): ResultSet = timer(sql).record(Supplier { statement.executeQuery(sql) })!!

	override fun addBatch(sql: String) {
		batches.add(sql)
		statement.addBatch(sql)
	}

	override fun executeBatch(): IntArray {
		val sql = batches.joinToString("\n")
		batches.clear()
		return timer(sql).record(Supplier { statement.executeBatch() })!!
	}

	override fun executeLargeUpdate(sql: String) =
		timer(sql).record(LongSupplier { statement.executeLargeUpdate(sql) })

	override fun executeLargeUpdate(sql: String, autoGeneratedKeys: Int) =
		timer(sql).record(LongSupplier { statement.executeLargeUpdate(sql, autoGeneratedKeys) })

	override fun executeLargeUpdate(sql: String, columnIndexes: IntArray) =
		timer(sql).record(LongSupplier { statement.executeLargeUpdate(sql, columnIndexes) })

	override fun executeLargeUpdate(sql: String, columnNames: Array<out String>) =
		timer(sql).record(LongSupplier { statement.executeLargeUpdate(sql, columnNames) })

	private fun timer(sql: String): Timer {
		jdbcMetrics.registerCounter(JDBC_STATEMENTS_EXECUTE, Tags.of(TAG_STATEMENT_CREATION, sql), "calls").increment()

		return jdbcMetrics.registerTimer(JDBC_STATEMENT_TIMER, Tags.of(TAG_STATEMENT_EXECUTION, sql))
	}
}