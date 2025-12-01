package com.vpr42.marketplacemessangerapi.repository

import com.vpr42.marketplace.jooq.tables.pojos.Jobs
import com.vpr42.marketplace.jooq.tables.references.JOBS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JobsRepository(
    private val dsl: DSLContext
) {

    fun findById(id: UUID) = dsl
        .selectFrom(JOBS)
        .where(JOBS.ID.eq(id))
        .fetchOneInto(Jobs::class.java)
}
