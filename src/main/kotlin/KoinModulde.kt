import com.mongodb.kotlin.client.coroutine.MongoClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import spark.kotlin.ignite

val appModule = module {

    single {
        val client = MongoClient.create(connectionString = CONNECTION_STRING)
        client.getDatabase(databaseName = DATABASE_NAME)
    }

    single {
        ignite().run {
            port(1234)
        }
    }

    singleOf(::ApiRoutes)

    singleOf(::SearchService)

}