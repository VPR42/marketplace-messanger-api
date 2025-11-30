package com.vpr42.marketplacemessangerapi.repository

import com.vpr42.marketplace.jooq.tables.pojos.Users
import com.vpr42.marketplace.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository(
    private val dsl: DSLContext
) {

    fun findById(id: UUID) = dsl
        .selectFrom(USERS)
        .where(USERS.ID.eq(id))
        .fetchOneInto(Users::class.java)
}
