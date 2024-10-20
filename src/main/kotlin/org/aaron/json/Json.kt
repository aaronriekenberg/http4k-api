package org.aaron.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.http4k.format.*
import se.ansman.kotshi.KotshiJsonAdapterFactory

//val jsonFormat = Moshi

// Build the kotshi adapter
@KotshiJsonAdapterFactory
private object MyJsonAdapterFactory : JsonAdapter.Factory by
KotshiMyJsonAdapterFactory // this class will be generated during compile

val jsonFormat = ConfigurableMoshi(
    com.squareup.moshi.Moshi.Builder()
        .addLast(MyJsonAdapterFactory)
        .addLast(EventAdapter)
        .addLast(ThrowableAdapter)
        .addLast(ListAdapter)
        .addLast(SetAdapter)
        .addLast(MapAdapter)
        .addLast(MoshiNodeAdapter)
        .asConfigurable(KotlinJsonAdapterFactory())
        .withStandardMappings()
        .done()
)