package app.manu.whatsoncrypto.classes.news

import android.graphics.Bitmap

class News {

    constructor(
            headline: String,
            body: String,

            image: Bitmap? = null,
            imageURL: String? = null,
            url: String? = null,
            timestamp: Long? = null) {

        this.headline = headline

        this.picture = image
        this.imageURL = imageURL
        this.body = body
        this.url = url
        this.timestamp = timestamp
    }

    public var headline: String
        get() = this.headline
        set(value: String) {
            headline = value
        }

    public var body: String
        get() = this.body
        set(value: String) {
            body = value
        }
    public var picture: Bitmap?
        get() = this.picture
        set(value: Bitmap?) {
            picture = value
        }
    public var imageURL: String?
        get() = this.imageURL
        set(value: String?) {
            imageURL = value
        }
    public var url: String?
        get() = this.url
        set(value: String?) {
            url = value
        }
    public var timestamp: Long?
        get() = this.timestamp
        set(value: Long?) {
            timestamp = value
        }
}