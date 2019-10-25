package de.ikas.iotrec.network.json

data class WeatherDataJson(
    var weather: List<Weather>,
    var main: Main
)

data class Weather(
    var id: Int,
    var main: String,
    var description: String,
    var icon: String
)

data class Main(
    var temp: Float,
    var pressure: Int,
    var humidity: Int,
    var temp_min: Float,
    var temp_max: Float
)