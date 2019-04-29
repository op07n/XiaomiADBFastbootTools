class Device {

    var serial = ""
    var codename = ""
    var bootloader = false
    var camera2 = false
    var anti = 0
    var dpi = -1
    var width = -1
    var height = -1

    var mode = 0
    var auth = false
    var recovery = false
    var reinstaller = true

    private val comm = Command()

    private fun String.getProp(prop: String): String {
        for (line in this.lines())
            if (prop in line)
                return line.substringAfterLast(':').trim().trim('[', ']')
        return ""
    }

    fun readADB(): Boolean {
        val props = comm.exec("adb shell getprop")
        auth = false
        if ("no devices" in props) {
            mode = 0
            return false
        }
        if ("unauthorized" in props) {
            mode = 0
            auth = true
            return false
        }
        recovery = comm.exec("adb devices").contains("recovery")
        if (mode == 1 && serial == props.getProp("ro.serialno") && dpi != -1)
            return true
        serial = props.getProp("ro.serialno")
        codename = props.getProp("ro.build.product")
        bootloader = props.getProp("ro.boot.flash.locked").contains("0")
        camera2 = props.getProp("persist.sys.camera.camera2").contains("true")
        if (!recovery) {
            dpi = try {
                comm.exec("adb shell wm density").substringAfterLast(':').trim().toInt()
            } catch (e: Exception) {
                -1
            }
            val size = comm.exec("adb shell wm size")
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
        mode = 1
        return true
    }

    fun readFastboot(): Boolean {
        val status = comm.exec("fastboot devices", false)
        if (status.isEmpty()) {
            mode = 0
            return false
        }
        if (mode == 2 && status.contains(serial))
            return true
        recovery = false
        val props = comm.exec("fastboot getvar all")
        serial = props.getProp("serial")
        codename = props.getProp("product")
        bootloader = props.getProp("unlocked").contains("yes")
        anti = try {
            props.getProp("anti").toInt()
        } catch (e: Exception) {
            0
        }
        mode = 2
        return true
    }
}
