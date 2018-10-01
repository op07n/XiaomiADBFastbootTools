package tools;

public class Device {

    String serial;
    String codename;
    boolean bootloader;
    int anti;
    boolean auth;
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
        }
        if (op.contains("unlocked: false")) {
            bootloader = false;
        }
        op = comm.exec("fastboot getvar anti", true);
        op = op.substring(0, op.indexOf(System.lineSeparator()));
        if (op.length() != 7)
            anti = -1;
        else anti = Integer.parseInt(op.substring(6));
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
        anti = -1;
        op = comm.exec("adb shell getprop ro.boot.flash.locked", false);
        if (op.contains("0")) {
            bootloader = true;
        }
        if (op.contains("1")) {
            bootloader = false;
        }
        return true;
    }
}
