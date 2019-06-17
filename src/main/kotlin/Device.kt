class Device {

    companion object {
        var serial = ""
        var codename = ""
        var bootloader = false
        var camera2 = false
        var anti = -1
        var dpi = -1
        var width = -1
        var height = -1
        var props = HashMap<String, String>()

        var mode = Mode.NONE
        var reinstaller = true
        var disabler = true

        fun readADB(): Boolean {
            val propstring = Command.exec("adb shell getprop")
            when {
                "no devices" in props -> {
                    mode = Mode.NONE
                    return false
                }
                "unauthorized" in props -> {
                    mode = Mode.AUTH
                    return false
                }
            }
            mode = if ("recovery" in Command.exec("adb devices"))
                Mode.RECOVERY
            else Mode.ADB
            if (mode == Mode.ADB && serial in propstring && dpi != -1 && width != -1 && height != -1)
                return true
            props.clear()
            propstring.lines().forEach {
                val parts = it.split("]: [")
                props[parts[0].trimStart('[')] = parts[1].trimEnd(']')
            }
            if (props["ro.serialno"].isNullOrEmpty() || props["ro.product.build"].isNullOrEmpty()) {
                mode = Mode.ERROR
                return false
            }
            serial = props["ro.serialno"] ?: ""
            codename = props["ro.build.product"] ?: ""
            bootloader = props["ro.boot.flash.locked"]?.contains("0") ?: false
            camera2 = props["persist.sys.camera.camera2"]?.contains("true") ?: false
            if (mode == Mode.ADB) {
                dpi = try {
                    Command.exec("adb shell wm density").substringAfterLast(':').trim().toInt()
                } catch (e: Exception) {
                    -1
                }
                val size = Command.exec("adb shell wm size")
                width = try {
                    size.substringAfterLast(':').substringBefore('x').trim().toInt()
                } catch (e: Exception) {
                    -1
                }
                height = try {
                    size.substringAfterLast('x').trim().toInt()
                } catch (e: Exception) {
                    -1
                }
            }
            return true
        }

        fun readFastboot(): Boolean {
            val status = Command.exec("fastboot devices", err = false)
            when {
                status.isEmpty() -> {
                    mode = Mode.NONE
                    return false
                }
                mode == Mode.FASTBOOT && serial in status -> return true
            }
            props.clear()
            Command.exec("fastboot getvar all").lines().forEach {
                val parts = it.split(' ', limit = 3)
                props[parts[1].trimEnd(':')] = parts[2]
            }
            serial = props["serial"] ?: ""
            codename = props["product"] ?: ""
            bootloader = props["unlocked"]?.contains("yes") ?: false
            anti = try {
                props["anti"]!!.toInt()
            } catch (e: Exception) {
                -1
            }
            mode = Mode.FASTBOOT
            return true
        }
    }
}
