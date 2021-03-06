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

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import vigica_edit.view.Error_Msg;
import vigica_edit.model.Service;

/**
 * Util class for file decomposition
 * 
 * @author nabillo
 */
public class Decompress_mw_s1 {
    
    private final byte[] endpatt = {(byte) 0x00, (byte)0x00, (byte)0x3f, (byte)0xff};
    static private Error_Msg error_msg = new Error_Msg();
    private Service_BDD bdd = new Service_BDD();
    public DecompressTask decompressTask;
    public DuplicateTask duplicateTask;
    
    
    public ArrayList<Service> services = new ArrayList();

    public ArrayList<Service> getServices() {
        return services;
    }
    
    public Decompress_mw_s1 () {
        decompressTask = new DecompressTask();
        duplicateTask = new DuplicateTask();
    }

    /**
     *
     * @param chemin
     * @throws java.lang.Exception
     */
    public void decompress(String chemin) throws Exception {
        byte[] bindata;
        services.clear();
        
        try {
            Path binfile = Paths.get(chemin);
            bindata = Files.readAllBytes(binfile);
            int binl = bindata.length;
            byte[] givl_s = Arrays.copyOfRange(bindata, 0, 4);
            int givl_d = ByteBuffer.wrap(givl_s).getInt() + 4;
            if (binl - givl_d != 0)
                error_msg.Error_diag("length of input binary file \\n differs from length given in that file!");

            byte[] recd_nmbr_s = Arrays.copyOfRange(bindata, 12, 16);
            int recd_nmbr_d = ByteBuffer.wrap(recd_nmbr_s).getInt();
            int recd_idx = 1;
            int bind_idx = 16;
            while (recd_idx <= recd_nmbr_d) {
                int nxt_idx = find_end(bindata, bind_idx);
                byte[] binrcd = Arrays.copyOfRange(bindata, bind_idx, nxt_idx);
                int rcdlen = nxt_idx - bind_idx;
                // create record file name
                byte rcdnamel = binrcd[1];
                byte[] rcdname = Arrays.copyOfRange(binrcd, 5, Byte.toUnsignedInt(rcdnamel) + 5);
//            for (int idx=0; idx < Byte.toUnsignedInt(rcdnamel); idx++) {
//                if (rcdname[idx] < 0x21)  // non printable codes or spaces
//                    rcdname[idx] = 0x5f;    // an underscore instead
//                if (rcdname[idx] == 0x7f || rcdname[idx] == 0x3c || rcdname[idx] == 0x3a || rcdname[idx] == 0x22 || 
//                    rcdname[idx] == 0x2f || rcdname[idx] == 0x5c || rcdname[idx] == 0x7c || rcdname[idx] == 0x3f ||
//                    rcdname[idx] == 0x2a
//                    )  // control chars < > : " / \ | ? * space are not allowed in file names; replace by %
//                    rcdname[idx] = 0x25;
//            }

                // start the filename with R | TV | ? to indicate the radio/TV/unknown service type
                String stype = "U"; // unknown
                if (binrcd[rcdlen - 17] == 0x00) // this byte in fixed distance 17 back from next record has TV/Radio
                    stype = "TV";
                else if (binrcd[rcdlen - 17] == 0x01)
                    stype = "R";

                byte[] nid_s = Arrays.copyOfRange(binrcd, rcdlen - 26, rcdlen - 24); // also fixed distance back from end
                int nid_d = getInt(nid_s); // make the two bytes into an integer
                byte[] ppr = Arrays.copyOfRange(binrcd, rcdlen - 10, rcdlen - 8); // preference setting
                String ppr_s = getPreference(ppr);

                // add the network number and preference setting to the end of the file name
                String rcdname_s = new String(rcdname, "UTF-8");
                String binrcd_s = bytesToHexString(binrcd);
//            String asciiname = stype + "~" + recd_idx + "~" + rcdname_s + "~E0~" + "N" + nid_d + "~" + "P" + ppr_s;
                services.add(new Service(stype, recd_idx, rcdname_s, nid_d, ppr_s,binrcd_s, false, ""));
                recd_idx++;
                bind_idx = nxt_idx;
            }
        }catch (Exception e) {
            throw new Exception(e.getCause().getMessage());
        }
    }

    private static String bytesToHexString(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private int find_end(byte[] bindata, int strt) {
        int sidx;
        for (sidx = strt; sidx <strt+1000; sidx++)
        {   
            if (Arrays.equals(Arrays.copyOfRange(bindata, sidx, sidx+4), endpatt))
                break;
        }
        return sidx+4;
    }
    
    private int getInt(byte[] in) {
        int val = ((in[0] & 0xff) << 8) | (in[1] & 0xff);;
        return val;
    }

    private String getPreference(byte[] ppr) {
        String ppr_s ="";
        Boolean isFirst = true;
        
        for(int i=0; i<8; i++) {
            if (((ppr[1] >> i) & 1) == 1) {
                if (isFirst) {
                    ppr_s += (i+1);
                    isFirst = false;
                }
                else
                    ppr_s += "-" + (i+1);
            }
        }
        for(int i=9; i<=10; i++) {
            if (((ppr[0] >> (i-9)) & 1) == 1) {
                if (i<10)
                    if (isFirst) {
                        ppr_s += (i);
                        isFirst = false;
                    }
                    else
                        ppr_s += "-" + (i);
                else
                    ppr_s += (i);
            }
        }
        return ppr_s;
    }
    
    public static Boolean isPreferenceOn(String ppr_s, int index) {
        Boolean isOk = false;
        for (String ppr: ppr_s.split("-")){
            if (ppr.equals(String.valueOf(index))) {
                isOk = true;
            }
        }
        return isOk;
    }
    
    public static String add_ppr(String old_ppr, int new_Value) {
        String new_ppr = "";
        Boolean isFirst = true;
        Boolean isAdded = false;
        
        // not preference yet
        if (old_ppr.length() == 0)
            return old_ppr += new_Value;
        for (String ppr: old_ppr.split("-")){
            // still not in position
            if ((Integer.valueOf(ppr) < new_Value)) {
                if (isFirst) {
                    new_ppr += ppr;
                    isFirst = false;
                }
                else
                    new_ppr += "-" + ppr;
            }
            // here we are
            else if ((Integer.valueOf(ppr) > new_Value) && !isAdded) {
                // expect for 1 to not have a -
                if (new_Value == 1)
                    new_ppr += new_Value + "-" + ppr ;
                else
                    new_ppr += "-" + new_Value + "-" + ppr ;
                isAdded = true;
            }
            // the rest of the line
            else {
                new_ppr += "-" + ppr ;
            }
        }
        // if new value is the last one we added it manualy
        if (!isAdded)
            new_ppr += "-" + new_Value;
        return new_ppr;
    }
    
    public static String remove_ppr(String old_ppr, int new_Value) {
        String new_ppr = "";
        Boolean isFirst = true;
        
        for (String ppr: old_ppr.split("-")){
            if (Integer.valueOf(ppr) == new_Value) {
                continue;
            }
            else {
                if (isFirst) {
                    new_ppr += ppr;
                    isFirst = false;
                }
                else
                    new_ppr += "-" + ppr;
            }
        }
        return new_ppr;
    }
    
    public class DecompressTask extends Task<ArrayList<Service>> {

        private String chemin;
        
        public String getChemin() {
            return this.chemin;
        }

        public void setChemin(String chemin) {
            this.chemin = chemin;
        }

        @Override
        protected ArrayList<Service> call() throws Exception {
            ArrayList<Service> services;
            int count = 0;

            updateProgress(-1, 0);
            decompress(chemin);
            services = getServices();

            // Add to database
            bdd.truncate_bdd();
            for(Service service : services){
                count++;
                updateProgress(count, services.size());
                bdd.save_bdd(service);
            }

            return services;
        };
    }
    
    public class DuplicateTask extends Task<ArrayList<Service>> {

        ObservableList<Service> services = FXCollections.observableArrayList();
        
        public ObservableList <Service> getServices() {
            return this.services;
        }

        public void setServices(ObservableList <Service> services) {
            this.services = services;
        }

        @Override
        protected ArrayList<Service> call() throws Exception {
            ArrayList <Service> servicesUnique = new ArrayList();
            int count = 0;

            updateProgress(-1, 0);
            ArrayList<String> uniqueId = new ArrayList<>();
            
            int i=1;

            for (Service service :this.services) {
                if (!uniqueId.contains(service.getS_name()) || !service.getS_ppr().equals("")) {
                    service.setS_idx(i);
                    servicesUnique.add(service);
                    i++;
                    uniqueId.add(service.getS_name());
                }
            }

            // Add to database
                bdd.truncate_bdd();
                bdd.save_bdd(servicesUnique);
            
            return servicesUnique;
        };
    }
}
