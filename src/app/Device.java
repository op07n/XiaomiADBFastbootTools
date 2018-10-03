package app;

public class Device {

    String serial;
    String codename;
    boolean bootloader;
    int anti;
    boolean auth;
    int dpi;
    Command comm = new Command();

    public boolean readFastboot() {
        String op = comm.exec("fastboot devices", false);
        if (op.length() < 1) {
            return false;
        }
        String cn = comm.exec("fastboot getvar product", true);
        serial = op.substring(0, op.indexOf("fa")).trim();
        codename = cn.substring(9, cn.indexOf(System.lineSeparator())).trim();
        op = comm.exec("fastboot oem device-info", true);
        if (op.contains("unlocked: true")) {
            bootloader = true;
        } else bootloader = false;
        op = comm.exec("fastboot getvar anti", true);
        op = op.substring(0, op.indexOf(System.lineSeparator()));
        if (op.length() != 7)
            anti = -1;
        else anti = Integer.parseInt(op.substring(6));
        dpi = -1;
        return true;
    }

    public boolean readADB() {
        String op = comm.exec("adb get-serialno", true);
        auth = false;
        if (op.contains("no devices")) {
            return false;
        }
        if (op.contains("unauthorized")) {
            auth = true;
            return false;
        }
        serial = comm.exec("adb get-serialno", false).trim();
        codename = comm.exec("adb shell getprop ro.build.product", false).trim();
        op = comm.exec("adb shell getprop ro.boot.flash.locked", false);
        if (op.contains("0")) {
            bootloader = true;
        } else bootloader = false;
        anti = -1;
        op = comm.exec("adb shell wm density");
        dpi = Integer.parseInt(op.substring(op.lastIndexOf(":") + 2).trim());
        return true;
    }
}
