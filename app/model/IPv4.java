package model;

import java.util.ArrayList;
import java.util.List;

public class IPv4 {
    int baseIPnumeric;
    int netmaskNumeric;

    public IPv4(String IPinCIDRFormat) throws NumberFormatException {

        String[] st = IPinCIDRFormat.split("\\/");
        if (st.length != 2)
            throw new NumberFormatException("Invalid CIDR format '"
                    + IPinCIDRFormat + "', should be: xx.xx.xx.xx/xx");

        String symbolicIP = st[0];
        String symbolicCIDR = st[1];
        System.out.println("IP :: "+ symbolicIP);
        System.out.println("IP :: "+ symbolicCIDR);
        
        Integer numericCIDR = new Integer(symbolicCIDR);
        if (numericCIDR > 32)
            throw new NumberFormatException("CIDR can not be greater than 32");
        /* IP */
        st = symbolicIP.split("\\.");
        if (st.length != 4)
            throw new NumberFormatException("Invalid IP address: " + symbolicIP);
        int i = 24;
        baseIPnumeric = 0;
        for (int n = 0; n < st.length; n++) {
            int value = Integer.parseInt(st[n]);
            if (value != (value & 0xff)) {
                throw new NumberFormatException("Invalid IP address: " + symbolicIP);
            }
            baseIPnumeric += value << i;
            i -= 8;
        }

        /* netmask from CIDR */
        if (numericCIDR < 8)
            throw new NumberFormatException("Netmask CIDR can not be less than 8");
        netmaskNumeric = 0xffffffff;
        netmaskNumeric = netmaskNumeric << (32 - numericCIDR);
    }

    public String getIP() {
        return convertNumericIpToSymbolic(baseIPnumeric);
    }

    private String convertNumericIpToSymbolic(Integer ip) {
        StringBuffer sb = new StringBuffer(15);

        for (int shift = 24; shift > 0; shift -= 8) {
            // process 3 bytes, from high order byte down.
            sb.append(Integer.toString((ip >>> shift) & 0xff));

            sb.append('.');
        }
        sb.append(Integer.toString(ip & 0xff));

        return sb.toString();
    }

    public List<String> getAvailableIPs() {

        ArrayList<String> result = new ArrayList<String>();
        int numberOfBits;

        for (numberOfBits = 0; numberOfBits < 32; numberOfBits++) {

            if ((netmaskNumeric << numberOfBits) == 0)
                break;

        }
        Integer numberOfIPs = 0;
        for (int n = 0; n < (32 - numberOfBits); n++) {

            numberOfIPs = numberOfIPs << 1;
            numberOfIPs = numberOfIPs | 0x01;

        }

        Integer baseIP = baseIPnumeric & netmaskNumeric;

        for (int i = 1; i < (numberOfIPs); i++) {

            Integer ourIP = baseIP + i;

            String ip = convertNumericIpToSymbolic(ourIP);

            result.add(ip);
        }
        return result;
    }

    public Long getNumberOfHosts() {
        int numberOfBits;

        for (numberOfBits = 0; numberOfBits < 32; numberOfBits++) {

            if ((netmaskNumeric << numberOfBits) == 0)
                break;

        }

        Double x = Math.pow(2, (32 - numberOfBits));

        if (x == -1)
            x = 1D;

        return x.longValue();
    }

    public boolean contains(String IPaddress) {
        Integer checkingIP = 0;
        String[] st = IPaddress.split("\\.");
        if (st.length != 4)
        	throw new NumberFormatException("Invalid IP address: " + IPaddress);

        int i = 24;
        for (int n = 0; n < st.length; n++) {
            int value = Integer.parseInt(st[n]);
            if (value != (value & 0xff)) {
                throw new NumberFormatException("Invalid IP address: "
                        + IPaddress);
            }
            checkingIP += value << i;
            i -= 8;
        }

        if ((baseIPnumeric & netmaskNumeric) == (checkingIP & netmaskNumeric))
            return true;
        else
            return false;
    }

    public boolean contains(IPv4 child) {
        Integer subnetID = child.baseIPnumeric;
        Integer subnetMask = child.netmaskNumeric;
        if ((subnetID & this.netmaskNumeric) == (this.baseIPnumeric & this.netmaskNumeric)) {
            if ((this.netmaskNumeric < subnetMask) == true
                    && this.baseIPnumeric <= subnetID) {
                return true;
            }
        }
        return false;
    }
}