package app.manu.whatsoncrypto.classes.news

import android.graphics.Bitmap

class News {
    private var headline: String
        get() = this.headline
        set(value: String) {
            headline = value
        }
    private var lead: String
        get() = this.lead
        set(value: String) {
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
    private var url: String
        get() = this.url
        set(value: String) {
            url = value
        }
}