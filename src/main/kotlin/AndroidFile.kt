data class AndroidFile(var dir: Boolean, var name: String, var size: Int, var time: String) {

    private fun shorten(num: Float): String {
        val str = num.toString()
        if (str.substringAfter('.').length > 2)
            return str.substring(0, str.indexOf('.') + 3)
        if (str.endsWith(".0"))
            return str.substringBefore('.')
        return str
    }

    fun getSize(): String {
        return if (size > 1024) {
            var siz = size / 1024.0f
            if (siz > 1024.0f) {
                siz /= 1024.0f
                if (siz > 1024.0f) {
                    siz /= 1024.0f
                    "${shorten(siz)} GB"
                } else "${shorten(siz)} MB"
            } else "${shorten(siz)} KB"
        } else "$size B"
    }
}