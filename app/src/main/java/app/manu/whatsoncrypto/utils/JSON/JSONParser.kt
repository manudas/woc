package app.manu.whatsoncrypto.utils.JSON

import android.util.Log

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


// constructor
class JSONParser {

    fun getJSONFromUrl(url: String): JSONObject? {
        val _url: URL
        val urlConnection: HttpURLConnection

        try {
            _url = URL(url)
            urlConnection = _url.openConnection() as HttpURLConnection
        } catch (e: MalformedURLException) {
            Log.e("JSON Parser", "Error due to a malformed URL " + e.toString())
            return null
        } catch (e: IOException) {
            Log.e("JSON Parser", "IO error " + e.toString())
            return null
        }

        try {
            `is` = BufferedInputStream(urlConnection.inputStream)
            val reader = BufferedReader(InputStreamReader(`is`!!))
            val total = StringBuilder(`is`!!.available())
            var line: String? = null
            while ({ line = reader.readLine(); line }() != null) {
                total.append(line).append('\n')
            }
            output = total.toString()
        } catch (e: IOException) {
            Log.e("JSON Parser", "IO error " + e.toString())
            return null
        } finally {
            urlConnection.disconnect()
        }

        try {
            json = JSONObject(output)
        } catch (e: JSONException) {
            Log.e("JSON Parser", "Error parsing data " + e.toString())
        }

        return json
    }

    companion object {

        internal var `is`: InputStream? = null
        internal var json: JSONObject? = null
        internal var output = ""
    }
}