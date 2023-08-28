import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.bson.Document
import org.bson.conversions.Bson
import org.eclipse.jetty.server.handler.ContextHandler.Availability

class SearchService(private val db: MongoDatabase) {

    private val collection = db.getCollection<ListingInfo>(collectionName = COLLECTION_NAME)

    fun getAnyListing(): Flow<ListingInfo> {
        return collection.find().limit(1)
    }

    fun getListingByCountry(countryCode: String): Flow<ListingResponseModel> {

        val fieldSet = Projections.computed("address", "\$address.street")

        val projection = Projections
            .include(
                listOf(
                    "_id",
                    "listing_url",
                    "name"
                )
            )

        val filters = Filters.eq("address.country_code", countryCode)

        return collection.find<ListingResponseModel>(filter = filters)
            .projection(projection = Projections.fields(listOf(projection, fieldSet)))
            .limit(5)
    }

    suspend fun getListingByCountryWithAggregation(countryCode: String) {

        val filters = Filters.eq("address.country_code", countryCode)

        val fieldSet = Projections.computed("countryName", "\$address.country")
        val fieldIncluded = Projections.include(
            listOf(
                "_id",
                "listing_url",
                "name"
            )
        )

        val aggregationPipeline = listOf<Bson>(
            Aggregates.match(filters),
            Aggregates.project(
                Projections.fields(
                    fieldIncluded,
                    fieldSet
                )
            ),
            Aggregates.limit(5)
        )


        collection.aggregate<ListingResponseModel>(aggregationPipeline).collect {
            println(it)
        }
    }

    fun getListingCountByCountry(): Flow<Document> {
        val groupByCountry = Aggregates.group("\$address.country", Accumulators.sum("count", 1))
        val sortByListingCount = Aggregates.sort(Sorts.descending("count"))
        val limit = Aggregates.limit(5)

        val aggregationPipeline = listOf(
            groupByCountry,
            sortByListingCount,
            limit
        )

        return collection.aggregate<Document>(aggregationPipeline).map {
            it
        }
    }

    fun autocompleteLocationName(keyword: String): Flow<String> {
        val fieldSet = listOf(
            Projections.computed("location", "\$address.street"),
            Projections.excludeId()
        )

        val search = Document(
            "\$search",
            Document("index", "default").append(
                "autocomplete",
                Document("query", keyword).append("path", "address.street")
            )
        )

        val groupByLocationName = Aggregates.group("\$location")

        /*Aggregates.search(
            SearchOperator.autocomplete(
                SearchPath.fieldPath("address.country"),
                listOf("United")
            )
        )*/


        return collection
            .aggregate<Document>(
                listOf(
                    search,
                    Aggregates.project(
                        Projections.fields(
                            fieldSet
                        ),
                    ),
                    groupByLocationName,
                    Aggregates.limit(5),
                )
            )
            .map {
                it.getString("_id")
            }
    }

    fun getListing(countryCode: String, availabilityInDay: Int, guestCount: Int): Flow<ListingResponseModel> {

        val range = when {
            availabilityInDay <= 30 -> "availability.availability_30"
            availabilityInDay <= 60 -> "availability.availability_60"
            availabilityInDay <= 90 -> "availability.availability_90"
            else -> "availability.availability_365"
        }

        val filters = Filters.and(
            Filters.eq("address.country_code", countryCode),
            Filters.gt(range, 1),
            Filters.gte("accommodates", guestCount)
        )

        val fieldSet = Projections.computed("address", "\$address.street")
        val projection = Projections.include("_id", "name", "listing_url")

       return collection.aggregate<ListingResponseModel>(
            listOf(
                Aggregates.match(filters),
                Aggregates.project(Projections.fields(projection, fieldSet)),
                Aggregates.limit(5)
            )
        )
    }
}