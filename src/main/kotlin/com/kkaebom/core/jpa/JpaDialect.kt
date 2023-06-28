package com.kkaebom.core.jpa

import org.hibernate.boot.model.FunctionContributions
import org.hibernate.dialect.MySQLDialect
import org.hibernate.type.StandardBasicTypes

class JpaDialect: MySQLDialect() {

    override fun initializeFunctionRegistry(functionContributions: FunctionContributions) {
        val basicTypeRegistry = functionContributions.typeConfiguration.basicTypeRegistry

        val sqmFunctionRegistry = functionContributions.functionRegistry
        sqmFunctionRegistry.registerPattern(
                "match",
                "match(?1) against (?2)",
                basicTypeRegistry.resolve(StandardBasicTypes.DOUBLE)
        )
        super.initializeFunctionRegistry(functionContributions)
    }
}