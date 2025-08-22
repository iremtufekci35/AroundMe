package com.example.aroundme.utils

object Translations {

    val amenityMap = mapOf(
        "restaurant" to "Restoran",
        "cafe" to "Kafe",
        "bar" to "Bar",
        "school" to "Okul",
        "hospital" to "Hastane",
        "bank" to "Banka",
        "atm" to "ATM",
        "parking" to "Otopark",
        "toilets" to "Tuvalet"
    )

    val shopMap = mapOf(
        "supermarket" to "Süpermarket",
        "convenience" to "Bakkal",
        "clothes" to "Giyim",
        "bakery" to "Fırın",
        "butcher" to "Kasap",
        "florist" to "Çiçekçi",
        "chemist" to "Eczane"
    )

    val cuisineMap = mapOf(
        "turkish" to "Türk Mutfağı",
        "italian" to "İtalyan Mutfağı",
        "chinese" to "Çin Mutfağı",
        "japanese" to "Japon Mutfağı",
        "kebab" to "Kebap",
        "pizza" to "Pizza"
    )

    val healthcareMap = mapOf(
        "hospital" to "Hastane",
        "clinic" to "Klinik",
        "dentist" to "Diş Hekimi",
        "pharmacy" to "Eczane",
        "doctor" to "Doktor"
    )
    val historicMap = mapOf(
        "castle" to "Kale",
        "monument" to "Anıt",
        "memorial" to "Anıt / Hatıra",
        "ruins" to "Harabe",
        "archaeological_site" to "Arkeolojik Alan",
        "church" to "Kilise",
        "mosque" to "Cami",
        "temple" to "Tapınak",
        "battlefield" to "Savaş Alanı",
        "fort" to "Hisar"
    )
    val railwayMap = mapOf(
        "subway" to "Metro",
        "light_rail" to "Hafif Raylı",
        "tram" to "Tramvay",
        "rail" to "Demiryolu",
        "monorail" to "Monoray",
        "highspeed" to "Hızlı Tren",
        "regional" to "Bölgesel Tren"
    )

    val tourismMap = mapOf(
        "museum" to "Müze",
        "hotel" to "Otel",
        "hostel" to "Hostel",
        "motel" to "Motel",
        "gallery" to "Galeri",
        "viewpoint" to "Seyir Noktası"
    )

    val placeMap = mapOf(
        "city" to "Şehir",
        "town" to "Kasaba",
        "village" to "Köy",
        "hamlet" to "Mezra",
        "suburb" to "Semt",
        "neighbourhood" to "Mahalle"
    )

    val publicTransportMap = mapOf(
        "stop_position" to "Durak",
        "station" to "İstasyon",
        "platform" to "Peron",
        "stop_area" to "Durak Alanı",
        "halt" to "Durak Noktası"
    )

    fun translate(map: Map<String, String>, key: String?): String? {
        return key?.let { map[it] ?: it }
    }
}
