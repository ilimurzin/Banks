package ru.ilimurzin.banks

import org.json.JSONArray
import java.net.URL

object Banks {
    fun fetch(): List<Bank> {
        val jsonArray = JSONArray(URL("https://banks.ilimurzin.ru/v1/banks.json").readText())

        return List(jsonArray.length()) { i ->
            val bankObject = jsonArray.getJSONObject(i)

            Bank(
                bic = bankObject.getString("bic"),
                name = bankObject.getString("name"),
                nameInEnglish = bankObject.optString("nameInEnglish"),
                registryNumber = bankObject.optString("registryNumber"),
                addressCombined = bankObject.optString("addressCombined"),
            )
        }
    }
}
