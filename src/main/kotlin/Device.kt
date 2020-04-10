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

    suspend fun checkADB() = Command.exec(mutableListOf("adb", "devices")).let {
        serial in it && "recovery" !in it
    }

    suspend fun checkRecovery() = Command.exec(mutableListOf("adb", "devices")).let {
        serial in it && "recovery" in it
    }

    suspend fun readADB() {
        Command.exec(mutableListOf("adb", "shell", "getprop")).let { propString ->
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
                        if ("recovery" in Command.exec(mutableListOf("adb", "devices")))
                            Mode.RECOVERY
                        else {
                            reinstaller =
                                Command.exec(mutableListOf("adb", "shell", "cmd", "package", "install-existing xaft"))
                                    .let {
                                        !("not found" in it || "Unknown command" in it)
                                    }
                            disabler = "enabled" in Command.exec(
                                mutableListOf(
                                    "adb",
                                    "shell",
                                    "pm",
                                    "enable",
                                    "com.android.settings"
                                )
                            )
                            dpi = try {
                                Command.exec(mutableListOf("adb", "shell", "wm", "density")).substringAfterLast(':')
                                    .trim().toInt()
                            } catch (e: Exception) {
                                -1
                            }
                            Command.exec(mutableListOf("adb", "shell", "wm", "size")).let {
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

    suspend fun checkFastboot() =
        serial in Command.exec(mutableListOf("fastboot", "devices"), redirectErrorStream = false)

    suspend fun readFastboot() {
        if (Command.exec(mutableListOf("fastboot", "devices"), redirectErrorStream = false).isNotEmpty()) {
            props.clear()
            Command.exec(mutableListOf("fastboot", "getvar", "all")).trim().lines().forEach {
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
