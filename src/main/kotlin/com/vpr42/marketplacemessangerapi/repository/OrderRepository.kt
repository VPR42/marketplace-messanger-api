package com.vpr42.marketplacemessangerapi.repository

import com.vpr42.marketplace.jooq.tables.pojos.Jobs
import com.vpr42.marketplace.jooq.tables.references.JOBS
import com.vpr42.marketplace.jooq.tables.references.ORDERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class OrderRepository(
    private val dsl: DSLContext
) {

    fun findJobByOrderId(orderId: Long) = dsl
        .select(JOBS.asterisk())
        .from(JOBS)
        .join(ORDERS)
        .on(ORDERS.JOB_ID.eq(JOBS.ID))
        .where(ORDERS.ID.eq(orderId))
        .fetchOneInto(Jobs::class.java)
}
