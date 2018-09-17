package app.manu.whatsoncrypto.classes.news

import android.graphics.Bitmap

class News {

    constructor(headline: String, body: String, lead: String? = null, image: Bitmap? = null, url: String? = null) {
        this.headline = headline
        this.lead = lead
        this.picture = image
        this.body = body
        this.url = url
    }

    private var headline: String
        get() = this.headline
        set(value: String) {
            headline = value
        }
    private var lead: String?
        get() = this.lead
        set(value: String?) {
            lead = value
        }
    private var body: String
        get() = this.body
        set(value: String) {
            body = value
        }
    private var picture: Bitmap?
        get() = this.picture
        set(value: Bitmap?) {
            picture = value
        }
    private var url: String?
        get() = this.url
        set(value: String?) {
            url = value
        }
}