/*
 * Copyright (C) 2016 bnabi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vigica_edit;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;
import vigica_edit.view.Error_Msg;

/**
 *
 * @author nabillo
 */
public class Decompress_mw_s1 {
    
    private final byte[] endpatt = {(byte) 0x00, (byte)0x00, (byte)0x3f, (byte)0xff};
    private byte[] bindata;
    private Error_Msg error_msg;
    
    /**
     *
     * @param chemin
     */
    public void decompress(String chemin) {
        try {
            Path binfile = Paths.get(chemin);
            bindata = Files.readAllBytes(binfile);
            Integer binl = bindata.length;
            byte[] givl_s = Arrays.copyOfRange(bindata, 0, 4);
            Integer givl_d = ByteBuffer.wrap(givl_s).getInt() + 4;
            if (binl - givl_d != 0)
                error_msg.Error_diag("length of input binary file \\n differs from length given in that file!");
            
            byte[] recd_nmbr_s = Arrays.copyOfRange(bindata, 12, 16);
            Integer recd_nmbr_d = ByteBuffer.wrap(recd_nmbr_s).getInt();
            Integer recd_idx = 1;
            Integer bind_idx = 16;
            while (recd_idx <= recd_nmbr_d) {
                Integer nxt_idx = find_end(bind_idx);
                byte[] binrcd = Arrays.copyOfRange(bindata, bind_idx, nxt_idx);
                Integer rcdlen = nxt_idx - bind_idx;
                // create record file name
                byte rcdnamel = binrcd[1];
                byte[] rcdname = Arrays.copyOfRange(binrcd, 5, Byte.toUnsignedInt(rcdnamel) + 5);
                for (int idx=0; idx < Byte.toUnsignedInt(rcdnamel); idx++) {
                    if (rcdname[idx] < 0x21)  // non printable codes or spaces
                        rcdname[idx] = 0x5f;    // an underscore instead
                    if (rcdname[idx] == 0x7f || rcdname[idx] == 0x3c || rcdname[idx] == 0x3a || rcdname[idx] == 0x22 || 
                        rcdname[idx] == 0x2f || rcdname[idx] == 0x5c || rcdname[idx] == 0x7c || rcdname[idx] == 0x3f ||
                        rcdname[idx] == 0x2a
                        )  // control chars < > : " / \ | ? * space are not allowed in file names; replace by %
                        rcdname[idx] = 0x25;
                }
                
                // start the filename with R | TV | ? to indicate the radio/TV/unknown service type
                String stype = "U"; // unknown
                if (binrcd[rcdlen - 17] == 0x00) // this byte in fixed distance 17 back from next record has TV/Radio
                    stype = "TV";
                else if (binrcd[rcdlen - 17] == 0x01)
                    stype = "R";
                
                byte[] nid_s = Arrays.copyOfRange(binrcd, rcdlen - 26, rcdlen - 24); // also fixed distance back from end
                Integer nid_d = getInt(nid_s); // make the two bytes into an integer
                byte[] ppr_s = Arrays.copyOfRange(binrcd, rcdlen - 10, rcdlen - 8); // preference setting
                Integer ppr_d = getInt(ppr_s);
                
                // add the network number and preference setting to the end of the file name
                String rcdname_s = new String(rcdname, "cp860");
                String asciiname = stype + "~" + recd_idx + "~" + rcdname_s + "~E0~" + "N" + nid_d + "~" + "P" + ppr_d;
                recd_idx++;
                bind_idx = nxt_idx;

            }
        }
        catch (IOException e)
        {
            error_msg.Error_diag("File open error");
        }
        catch (NumberFormatException e)
        {
            System.out.println(e.getCause());
            error_msg.Error_diag("Incorect header");
        }
    }
    
    private Integer find_end(Integer strt) {
        Integer sidx;
        for (sidx = strt; sidx <strt+300; sidx++)
        {   
            if (Arrays.equals(Arrays.copyOfRange(bindata, sidx, sidx+4), endpatt))
                break;
        }
        return sidx+4;
    }
    
    private Integer getInt(byte[] in) {
        int val = ((in[0] & 0xff) << 8) | (in[1] & 0xff);;
        return val;
    }
}
