package com.harada

import com.harada.driver.dao.UserDao
import com.harada.gateway.UserWritePostgresDB
import com.harada.gateway.UserWriteStore
import com.harada.usecase.IUserCreateUseCase
import com.harada.usecase.IUserUpdateUseCase
import com.harada.usecase.UserCreateUseCase
import com.harada.usecase.UserUpdateUseCase
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.postgresql.ds.PGSimpleDataSource

object Injector {
    val useCaseModule = Kodein.Module("useCase") {
        bind<IUserCreateUseCase>() with singleton { UserCreateUseCase(instance()) }
        bind<IUserUpdateUseCase>() with singleton { UserUpdateUseCase(instance()) }
    }

    val gatewayModule = Kodein.Module("gateway") {
        bind<UserWriteStore>() with singleton { UserWritePostgresDB(instance(), instance()) }
    }

    val driverModule = Kodein.Module("driver") {
        bind<UserDao.Companion>() with singleton { UserDao }
    }

    val dbModule = Kodein.Module("dataSource") {
        bind<Database>() with singleton {
            val dataSource = PGSimpleDataSource()
            dataSource.user = "developer"
            dataSource.password = "developer"
            dataSource.setURL("jdbc:postgresql://localhost:5432/sns_db")
            dataSource.loggerLevel = "DEBUG"
            Database.connect(dataSource)
        }
    }
    val kodein = Kodein {
        importAll(useCaseModule, gatewayModule, driverModule, dbModule)
    }
}