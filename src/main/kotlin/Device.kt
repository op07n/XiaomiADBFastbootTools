object Device {

    var serial = ""
    var codename = ""
    var bootloader = false
    var camera2 = false
    var anti = -1
    var dpi = -1
    var width = -1
    var height = -1
    private var props = mutableMapOf<String, String>()
    var mode = Mode.NONE
    var reinstaller = true
    var disabler = true
    private val command = Command()

    fun checkADB() = command.exec("adb devices").let {
        serial in it && "recovery" !in it
    }

    fun checkRecovery() = command.exec("adb devices").let {
        serial in it && "recovery" in it
    }

    fun readADB() {
        command.exec("adb shell getprop").let { propString ->
            when {
                "unauthorized" in propString -> mode = Mode.AUTH
                "no devices" !in propString -> {
                    props.clear()
                    propString.trim().lines().forEach {
                        val parts = it.split("]: [")
                        if (parts.size == 2)
                            props[parts[0].trimStart('[')] = parts[1].trimEnd(']')
                    }
                    mode = if (props["ro.serialno"].isNullOrEmpty() || props["ro.build.product"].isNullOrEmpty())
                        Mode.ERROR
                    else {
                        serial = props["ro.serialno"] ?: ""
                        codename = props["ro.build.product"] ?: ""
                        bootloader = props["ro.boot.flash.locked"]?.contains("0") ?: false
                        camera2 = props["persist.sys.camera.camera2"]?.contains("true") ?: false
                        if ("recovery" in command.exec("adb devices"))
                            Mode.RECOVERY
                        else {
                            reinstaller = command.exec("adb shell cmd package install-existing xaft").let {
                                !("not found" in it || "Unknown command" in it)
                            }
                            disabler = "enabled" in command.exec("adb shell pm enable com.android.settings")
                            dpi = try {
                                command.exec("adb shell wm density").substringAfterLast(':').trim().toInt()
                            } catch (e: Exception) {
                                -1
                            }
                            command.exec("adb shell wm size").let {
                                width = try {
                                    it.substringAfterLast(':').substringBefore('x').trim().toInt()
                                } catch (e: Exception) {
                                    -1
                                }
                                height = try {
                                    it.substringAfterLast('x').trim().toInt()
                                } catch (e: Exception) {
                                    -1
                                }
                            }
                            Mode.ADB
                        }
                    }
                }
            }
        }
    }

    fun checkFastboot() = serial in command.exec("fastboot devices", err = false)

    fun readFastboot() {
        if (command.exec("fastboot devices", err = false).isNotEmpty()) {
            props.clear()
            command.exec("fastboot getvar all").trim().lines().forEach {
                if (it[0] == '(')
                    props[it.substringAfter(')').substringBeforeLast(':').trim()] = it.substringAfterLast(':').trim()
            }
            if (props["product"].isNullOrEmpty() || (props["serialno"].isNullOrEmpty() && props["serial"].isNullOrEmpty()))
                mode = Mode.ERROR
            else {
                serial = props["serialno"] ?: props["serial"] ?: ""
                codename = props["product"] ?: ""
                bootloader = props["unlocked"]?.contains("yes") ?: false
                anti = props["anti"]?.toInt() ?: -1
                mode = Mode.FASTBOOT
            }
        }
    }
}
