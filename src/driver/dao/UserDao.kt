package com.harada.driver.dao

import com.harada.driver.entity.UserEntity
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime
import java.util.*

object Users : UUIDTable() {
    val name = varchar("name", 100)
    val mail = varchar("mail", 100)
    val birthday = date("birthday")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

class UserDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDao>(Users) {
        fun create(entity: UserEntity) = UserDao.new {
            UserDao.new(entity.id) {
                this.name = entity.name
                this.mail = entity.mail
                this.birthday = entity.birthday
                this.createdAt = entity.createdAt
                this.updatedAt = entity.updatedAt
            }
        }.id.value
    }

    var name by Users.name
    var mail by Users.mail
    var birthday by Users.birthday
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt
}