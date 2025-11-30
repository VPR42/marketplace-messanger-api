package com.vpr42.marketplacemessangerapi.config

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class JooqConfig {

    @Bean
    fun jooqSettings(): Settings = Settings()
        .withRenderQuotedNames(RenderQuotedNames.NEVER)
        .withRenderNameCase(RenderNameCase.LOWER)

    @Bean
    fun dsl(dataSource: DataSource, settings: Settings): DSLContext =
        DSL.using(dataSource, SQLDialect.POSTGRES, settings)
}
