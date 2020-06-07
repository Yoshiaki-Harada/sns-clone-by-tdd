package com.harada

import com.harada.driver.dao.UserDao
import com.harada.gateway.UserPostgresDB
import com.harada.gateway.UserStore
import com.harada.usecase.IUserCreateUseCase
import com.harada.usecase.UserCreateUseCase
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.postgresql.ds.PGSimpleDataSource

object Injector {
    val useCaseModule = Kodein.Module("useCase") {
        bind<IUserCreateUseCase>() with singleton { UserCreateUseCase(instance()) }
    }

    val gatewayModule = Kodein.Module("gateway") {
        bind<UserStore>() with singleton { UserPostgresDB(instance(),instance()) }

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
            Database.connect(dataSource)
        }
    }
    val kodein = Kodein {
        importAll(useCaseModule, gatewayModule, driverModule, dbModule)
    }
}