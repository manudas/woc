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

    public var body: String

    public var picture: Bitmap?

    public var imageURL: String?

    public var url: String?

    public var timestamp: Long?

}