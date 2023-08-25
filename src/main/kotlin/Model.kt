import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty

data class ListingResponseModel(
    @BsonId
    val id: String,
    val name: String,
    @BsonProperty("listing_url")
    val listingUrl: String,
    val address: String
)

data class ListingInfo(
    @BsonId
    val id: String,


    //TODO: Does this annotation also help in when callable reference is called for serialization.
    @BsonProperty(value = "listing_url")
    val listingUrl: String,

    val name: String,
    val summary: String,
    val address: Address,
    val accommodates: Int
)

data class Address(
    val country: String,
    @BsonProperty("country_code")
    val countryCode: String?
)




