import com.google.gson.Gson
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.inject
import spark.kotlin.Http

class ApiRoutes(private val httpClient: Http) {

    private val searchService: SearchService by inject(SearchService::class.java)

    fun setupRoutes() {

        httpClient.get("/healthCheck") {
            val response = runBlocking {
                return@runBlocking searchService.getAnyListing().toList()
            }
            Gson().toJson(response)
        }

        httpClient.get("/getListingByCounties/:countryCode") {
            val countryCode = request.params(":countryCode")

            if (countryCode.isNullOrBlank()) {
                response.status(400)
            } else {
                runBlocking {
                    val items = searchService.getListingByCountry(countryCode).toList()
                    Gson().toJson(items)
                }
            }
        }

        httpClient.get("/getCountriesWithMostListing") {
            runBlocking {
                val items = searchService.getListingCountByCountry().toList()
                Gson().toJson(items)
            }
        }

        httpClient.get("/autocompleteLocation/:keyword") {
            val keyword = request.params(":keyword")
            runBlocking {
                val items = searchService.autocompleteLocationName(keyword).toList()
                Gson().toJson(items)
            }
        }

        httpClient.get("/getListing/:countryCode/:range/:persons") {
            val countryCode = request.params(":countryCode")
            val range = request.params(":range").toInt()
            val guest = request.params(":persons").toInt()
            runBlocking {
                val items = searchService.getListing(countryCode, range, guest).toList()
                Gson().toJson(items)
            }
        }

    }
}