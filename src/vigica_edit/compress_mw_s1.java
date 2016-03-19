import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;



public class Compress_mw_s1 {
    
	public ArrayList<Service> services = new ArrayList();
	
    /**
     *
     * @param chemin
     */
    public void compress(String chemin) throws Exception {
        byte[] satservices;
        
        
        
        services = read_bdd();
        
        for (Service service : services) {
          if (service.getS_flag() == false) {
        	  System.arraycopy(service.getS_line(), 0, satservices, satservices.length, service.getS_line().length());
          }
          else {
    	  	byte[] sdata = StringToHex(service.getS_ppr());
			int rcdlen = sdata.length;
			byte [] prefba = getppr(service.getS_ppr());
			System.arraycopy(prefba, 0, sdata, rcdlen - 10, 2);
			
			sdata[ rcdlen - 8 ] = (byte) 0x01;
			int newl = service.getS_name().length();
			int rcdnamel = Integer.valueOf(sdata[1]);
			byte[] filler = Arrays.copyOfRange(sdata, 2, 2+3);
			byte[] payload = Arrays.copyOfRange(sdata, rcdnamel+5, sdata.length);
			byte[] newrec = {(byte)0x01, (byte) newl};
			System.arraycopy(filler, 0, newrec, newrec.length, filler.length);
			byte[] newn = service.getS_name().getBytes("UTF-8");
			System.arraycopy(newn, 0, newrec, newrec.length, newn.length);
			System.arraycopy(payload, 0, newrec, newrec.length, payload.length);
			
			System.arraycopy(newrec, 0, satservices, satservices.length, newrec.length);
			
          }
        }
        
        byte[] ffbytes = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0C};
        
        byte[] snbytes = int2ba(services.size());
        
        int fl = satservices.length + 12; // 4 crc + 4 type + 4 service count + all service records
        byte[] flbytes = int2ba(fl);
        
        
    }
    	  
    private byte[] int2ba (int integer) {
        byte[] result = new byte[4];
      
        result[0] = (byte)((integer & 0xFF000000) >> 24);
        result[1] = (byte)((integer & 0x00FF0000) >> 16);
        result[2] = (byte)((integer & 0x0000FF00) >> 8);
        result[3] = (byte)(integer & 0x000000FF);
      
        return result;
    }
    
    private byte[] getppr(String preference) {
    	byte[] ppr = {(byte)0x00, (byte)0x00};
    	
    	
    	for (String perf : preference.split("-")) {
    		Double pos = Math.pow(2, Integer.valueOf(perf));
    		if (Integer.valueOf(perf) < 8) {
    			ppr[1] = (byte) (ppr[1] | pos.byteValue());
    		}
    		else {
    			ppr[1] = (byte) (ppr[0] | pos.byteValue());
    		}
    	}
    	return ppr;
    }
   
    private ArrayList read_bdd() {
    	
    }
    
}
