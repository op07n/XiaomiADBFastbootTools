package app

class Device {

    lateinit var serial: String
    lateinit var codename: String
    var bootloader = false
    var anti = 0
    var auth = false
    var dpi = 0
    var comm = Command()

    fun readFastboot(): Boolean {
        var op = comm.exec("fastboot devices", false)
        if (op.isEmpty())
            return false
        val cn = comm.exec("fastboot getvar product", true)
        serial = op.substring(0, op.indexOf("fa")).trim()
        codename = cn.substring(9, cn.indexOf(System.lineSeparator())).trim()
        op = comm.exec("fastboot oem device-info", true)
        bootloader = op.contains("unlocked: true")
        op = comm.exec("fastboot getvar anti", true)
        op = op.substring(0, op.indexOf(System.lineSeparator()))
        if (op.length != 7)
            anti = -1
        else
            anti = Integer.parseInt(op.substring(6))
        dpi = -1
        return true
    }

    fun readADB(): Boolean {
        var op = comm.exec("adb get-serialno", true)
        auth = false
        if (op.contains("no devices"))
            return false
        if (op.contains("unauthorized")) {
            auth = true
            return false
        }
        serial = comm.exec("adb get-serialno", false).trim()
        codename = comm.exec("adb shell getprop ro.build.product", false).trim()
        bootloader = comm.exec("adb shell getprop ro.boot.flash.locked", false).contains("0") or comm.exec("adb shell getprop ro.secureboot.lockstate", false).contains("unlocked")
        anti = -1
        op = comm.exec("adb shell wm density")
        dpi = Integer.parseInt(op.substring(op.lastIndexOf(":") + 2).trim())
        return true
    }
}
