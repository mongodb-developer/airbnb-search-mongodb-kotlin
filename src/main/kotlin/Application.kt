import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import spark.kotlin.Http


fun main() {
    setupKoin()
    Application()
}


class Application : KoinComponent {

    private val http: Http by inject()

    init {
        ApiRoutes(http).setupRoutes()
    }
}


fun setupKoin() {
    startKoin {
        modules(appModule)
    }
}

