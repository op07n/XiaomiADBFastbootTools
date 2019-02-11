data class AndroidFile(var dir: Boolean, var name: String, var size: Int, var time: String) {

    private fun shorten(num: Float): String {
        if (num.toString().substringAfter('.').length > 2)
            return num.toString().substring(0, num.toString().indexOf('.') + 3)
        if (num.toString().endsWith(".0"))
            return num.toString().substringBefore('.')
        return num.toString()
    }

    fun getSize(): String {
        if (size > 1024) {
            var siz = size / 1024.0f
            if (siz > 1024.0f) {
                siz /= 1024.0f
                if (siz > 1024.0f) {
                    siz /= 1024.0f
                    return "${shorten(siz)} GB"
                } else return "${shorten(siz)} MB"
            } else return "${shorten(siz)} KB"
        } else return "$size B"
    }
}