package com.kkaebom.core.jpa

import com.p6spy.engine.logging.Category
import com.p6spy.engine.spy.appender.MessageFormattingStrategy
import org.hibernate.engine.jdbc.internal.FormatStyle
import java.util.*

class CustomP6spySqlFormat : MessageFormattingStrategy {

    override fun formatMessage(connectionId: Int, now: String, elapsed: Long, category: String, prepared: String, sql: String?, url: String): String? {
        val sql = formatSql(connectionId, elapsed, category, sql)

        sql?.run {
            return if (sql.contains("BATCH_")) {
                "BATCH SQL"
            } else now + "|" + elapsed + "ms|" + category + "|connection " + connectionId + "|" + sql
        }
        return null;
    }

    private fun formatSql(connectionId: Int, elapsed: Long, category: String, sql: String?): String? {
        var sql: String? = sql
        if (sql == null || sql.trim { it <= ' ' } == "") return sql

        // Only format Statement, distinguish DDL And DML
        if (Category.STATEMENT.name == category) {
            val tmpsql = sql.trim { it <= ' ' }.lowercase()
            sql = if (tmpsql.startsWith("create") || tmpsql.startsWith("alter") || tmpsql.startsWith("comment")) {
                FormatStyle.DDL.formatter.format(sql)
            } else {
                FormatStyle.BASIC.formatter.format(sql)
            }
        }
        return sql + createStack(connectionId, elapsed)
    }

    private fun createStack(connectionId: Int, elapsed: Long): String {
        val callStack = Stack<String>()
        val stackTrace = Throwable().stackTrace
        for (stackTraceElement in stackTrace) {
            val trace = stackTraceElement.toString()

            val allowFilter = "com.kkaebom"
            if (trace.startsWith(allowFilter) && !deniedFilter(trace)) {
                callStack.push(trace)
            }
        }
        val sb = StringBuilder()
        var order = 1
        while (callStack.size != 0) {
            sb.append("\n\t\t").append(order++).append(".").append(callStack.pop())
        }
        return "\n\n\tConnection ID:" + connectionId +
                " | Excution Time:" + elapsed + " ms\n" +
                "\n\tExcution Time:" + elapsed + " ms\n" +
                "\n\tCall Stack :" + sb + "\n" +
                "\n--------------------------------------"
    }

    private fun deniedFilter(trace: String): Boolean {
        return trace.contains("jpa") ||
                trace.contains("mapstruct") ||
                trace.contains("CGLIB")
    }
}