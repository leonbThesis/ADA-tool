import java.io.StringReader;
import java.util.ArrayList;

/*Json Library: https://github.com/google/gson*/
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import static java.lang.Math.abs;
import java.util.Collections;



/**
 *
 * @author Leon Bardenheuer
 */

/**
 *  Sources:
 * 
 *  https://docs.oracle.com/javase/7/docs/api/java/util/Collections.html
 */
public class FullAnalysisProcessor {

    Sample sam;
    Settings set;
    Gson gson = new Gson();
    
    JsonObject rootHLVDyn;
    JsonObject rootSLVDyn;
    JsonObject rootHLVNet;
    JsonObject rootSLVNet;
    
    String[] suspiciousFiles = new String[]{"\"/proc/scsi/scsi\"",
                                            "\"/proc/scsi/sg/device_strs\"",
                                            "\"/sys/class/net/eth0/address\"",
                                            "\"/sys/class/dmi/id/sys_vendor\"",
                                            "\"/sys/class/dmi/id/board_vendor\"",
                                            "\"/sys/class/dmi/id/product_name\"",
                                            "\"/sys/class/dmi/id/bios_vendor\"",
                                            "\"/sys/block/sda/device/model\"",
                                            "\"/proc/cpuinfo\"",
                                            };
    
    
    
    /**
     *
     * @param s to be analyzed sample
     */
    public FullAnalysisProcessor(Sample sam, Settings set) {
        this.sam=sam;
        this.set=set;
        
        /*create Json root objects for each HLV/SLV Dynamic/Network analysis data*/
        JsonReader rHLVDyn = new JsonReader(new StringReader(sam.reportData[0]));
        JsonReader rSLVDyn = new JsonReader(new StringReader(sam.reportData[1]));
        JsonReader rHLVNet = new JsonReader(new StringReader(sam.reportData[2]));
        JsonReader rSLVNet = new JsonReader(new StringReader(sam.reportData[3]));
        rHLVDyn.setLenient(true);
        rSLVDyn.setLenient(true);
        rHLVNet.setLenient(true);
        rSLVNet.setLenient(true);
        rootHLVDyn = gson.fromJson(rHLVDyn, JsonObject.class);
        rootSLVDyn = gson.fromJson(rSLVDyn, JsonObject.class);
        rootHLVNet = gson.fromJson(rHLVNet, JsonObject.class);
        rootSLVNet = gson.fromJson(rSLVNet, JsonObject.class);
    }

    /** 
     * Checks the samples opened files for a predefined set of suspicious files
     * 
     * @return Object[] returned values: [0]: (String) resultShort; [1]: (String) result; [2]: (int) score change
     */
    public Object[] suspiciousFiles(){       
        
        JsonArray jArrHLV;
        JsonArray jArrSLV;
        ArrayList<String> resultHLV = new ArrayList<>();
        ArrayList<String> resultSLV = new ArrayList<>();

        
        /*Read "open_files" from Json and parse to JsonArray (JsonObject[])*/
        jArrHLV = rootHLVDyn.getAsJsonArray("open_files");
        jArrSLV = rootSLVDyn.getAsJsonArray("open_files");
        
        
        /*Check if HLV/SLV opened files contain a suspicious file from the default set of suspiciousFiles*/
        
        /*Iterate over suspicious files*/
        for(String sF : suspiciousFiles){
            
            /*Iterate over opened files HLV...*/
            for(JsonElement jE : jArrHLV){
                if(jE.toString().equals(sF)){
                    /*if match > add to result list, if not already on the list*/
                    if(!resultHLV.contains(sF)) resultHLV.add(sF);
                }
                //System.out.println(jE.toString()+" | "+sF);
                
                
            }

            /*Iterate over opened files SLV...*/
            for(JsonElement jE : jArrSLV){
                if(jE.toString().equals(sF)){
                    /*if match > add to result list*/
                    if(!resultSLV.contains(sF)) resultSLV.add(sF);
                }
            }
            
        }
        /*Sort for increased comparability*/
        Collections.sort(resultHLV);
        Collections.sort(resultSLV);
        
        /*Evaluation*/
        
        /*if no suspicious file was opened...*/
        if(resultHLV.isEmpty() && resultSLV.isEmpty()){
            /*...message + no score change*/
            return new Object[]{"No suspicious files","No suspicious files opened",0};
        }
        
        
        /*if same suspicious files were opened...*/
        if(resultHLV.equals(resultSLV)){
            if(resultHLV.size()<=1){
                /*...message + smaller score change*/
                /*Also replace ',' in List with ';' because ',' is bad for csv*/
                return new Object[]{"Same suspicious file!","One suspicious file opened on both variants! ("+resultHLV.toString().replaceAll(",",";")+")",15};
            }else{
                /*...message + small score change*/
                return new Object[]{"Same suspicious files!","Suspicious files opened on both variants! ("+resultHLV.toString().replaceAll(",",";")+")",20};
            }
            
        }
        
        /*if suspicious files were opened, differing between both variants; indicating abort...*/
        /*...message + larger score change*/
        return new Object[]{"Different suspicious files!","Different suspicious files were opened on both variants! (HLV: "+resultHLV.toString().replaceAll(",",";")+"    SLV: "+resultSLV.toString().replaceAll(",",";")+")",50};
       
    }
    
    /** 
     * Checks the samples network activities for differences between both variants
     * 
     * @return Object[] returned values: [0]: (String) resultShort; [1]: (String) result; [2]: (int) score change
     */
    public Object[] checkNetwork(){       
        
        
        
        if(sam.reportData[2].equals(sam.reportData[3])){
            /*message + no score change*/
            return new Object[]{"Equal network traffic","Equal network traffic",0};
        }
        
        //System.out.println(s.reportData[2].length()+","+s.reportData[3].length());
        
        /*small hack; sometimes, LiSa lists a local UDP packet with {"ip":"10.0.2.2","ports":["67"],"blacklisted":false,"data_in":0,"data_out":300}*/
        /*the apperance of the package can vary from execution to execution, and thereby also from variant to variant*/
        /*the total length in the report is 95 bytes/chars; the hack accounts for this inconsistency by not increasing the score*/
        if(abs(sam.reportData[2].length()-sam.reportData[3].length())==95){
            /*message + no score change*/
            return new Object[]{"Equal network traffic?","Unequal network traffic but probably only bugged internal UDP package",0};
        }
        
        /*if only a bit unequal: message + small score change (probably uninteresting difference e.g. malware tried to send 512 TCP-syn packages on HLV and 461 on SLV)*/
        if(abs(sam.reportData[2].length()-sam.reportData[3].length())<10){
            return new Object[]{"Network traffic unequal!","Small unequalness in network traffic!",5};
        }
        /*otherwise bigger score change*/
        return new Object[]{"Network traffic unequal!","Network traffic unequal!",10};
       
    }
    
        /** 
     * Checks the samples network activities for differences between both variants
     * 
     * @return Object[] returned values: [0]: (String) resultShort; [1]: (String) result; [2]: (int) score change
     */
    public Object[] fileSize(){       
        
        
        
        int sizeDiff = abs(sam.reportData[0].length()-sam.reportData[1].length());
        
        if(sizeDiff==0) return new Object[]{"Size equal","Dynamic report sizes are equal",0};
        
        if(sizeDiff<=set.minSizeSimple) return new Object[]{"Accepted small size difference","Dynamic report sizes are almost equal (still within set limit)",0};
        
        return new Object[]{"Different report sizes!","Dynamic report sizes differ significantly ( "+sizeDiff+" ) Bytes",15};
        
       
    }
    
      /** 
     * Analyzes and compares the variants' executed syscalls
     * 
     * @return Object[] returned values: [0]: (String) resultShort; [1]: (String) result; [2]: (int) score change; [3]: (String) uniqueSyscalls
     */
    public Object[] syscallAnalysis(){       
        
        JsonArray jArrHLV;
        JsonArray jArrSLV;
        ArrayList<String> resultHLV = new ArrayList<>();
        ArrayList<String> resultSLV = new ArrayList<>();
        ArrayList<String> resultTotal = new ArrayList<>();
        
        String uniqueSyscalls="";
        
        
        jArrHLV = rootHLVDyn.getAsJsonArray("syscalls");
        jArrSLV = rootSLVDyn.getAsJsonArray("syscalls");
        
        /*if there are no syscalls probably LiSa error during analysis */
        if(jArrHLV.isEmpty() || jArrSLV.isEmpty()){
            return new Object[]{"Missing Syscalls!","No syscalls found; Re-analyzing the sample with LiSa can in some cases fix this problem!",-1,""};
        }
        
        /*fill HLV/SLV result Lists with all executed system calls*/
        
        /*Iterate over HLV*/
        for(JsonElement jE : jArrHLV){
            /*find index of syscall name*/
            int startIndex = jE.toString().indexOf("\"name\"")+8;
            
            //System.out.println(jE.toString().substring(startIndex, jE.toString().indexOf('\"', startIndex)));
            
            /*add name to result list*/
            resultHLV.add(jE.toString().substring(startIndex, jE.toString().indexOf('\"', startIndex)));
        }

        /*Iterate over SLV*/
        for(JsonElement jE : jArrSLV){
            /*find index of syscall name*/
            int startIndex = jE.toString().indexOf("\"name\"")+8;
            
            /*add name to result list*/
            resultSLV.add(jE.toString().substring(startIndex, jE.toString().indexOf('\"', startIndex)));
        }
           
        
        /*Compare the lists element-by-element; if they do not match: do a complex search (described in paper) to find differing syscalls*/
        
        /*individual indices of HLV and SLV*/
        int iH=0;
        int iS=0;
        /*for searching new index after no-match*/
        int newIndex=0;
        /**/
        boolean flag=false;
        try{
            /*for loop with [larger result lists element count] iterations*/
            for(int i = 0; i < Math.max(resultHLV.size(), resultSLV.size());i++, iH++, iS++, newIndex++){
                //System.out.println(Math.max(resultHLV.size(), resultSLV.size())+" "+i);
                try{
                    /*if current element is equal (HLV=SLV) then continue with next element*/
                    if(resultHLV.get(iH).equals(resultSLV.get(iS))){
                        System.out.println("Matched "+resultHLV.get(iH)+"("+iH+") - "+resultSLV.get(iS)+"("+iS+")");
                        continue;
                    }
                    
                }catch(IndexOutOfBoundsException e){
                    /*if here, then smaller result list ended abruptly (which is ok)*/
                }
                
            
                /*if elements are not equal > some extra syscall , syscall missing , etc.; try to save it if only some data extra*/
                
                
                /*search HLVs element in SLV; for the next [settings - sysCallDepth] occurences*/
                newIndex=iS;
                for(int j=0; j<set.sysCallDepth; j++){
                    //System.out.println(resultSLV.subList(newIndex, resultSLV.size()));
                    /*find next occurence*/
                    newIndex = resultSLV.subList(newIndex, resultSLV.size()-1).indexOf(resultHLV.get(iH))+newIndex;
                    /*check for next 5 elements, if back on track; cannot do loop because of break*/
                    if(resultHLV.get(iH).equals(resultSLV.get(newIndex))){
                        /*if already at the end > do not check more elements*/
                        if(resultHLV.get(iH).equals("exit_group")){
                            flag=true;
                            break;
                        }
                        if(resultHLV.get(iH+1).equals(resultSLV.get(newIndex+1))){
                            if(resultHLV.get(iH+1).equals("exit_group")){
                                flag=true;
                                break;
                            }
                            if(resultHLV.get(iH+2).equals(resultSLV.get(newIndex+2))){
                                if(resultHLV.get(iH+2).equals("exit_group")){
                                    flag=true;
                                    break;
                                }
                                if(resultHLV.get(iH+3).equals(resultSLV.get(newIndex+3))){
                                    if(resultHLV.get(iH+3).equals("exit_group")){
                                        flag=true;
                                        break;
                                    }
                                    if(resultHLV.get(iH+4).equals(resultSLV.get(newIndex+4))){
                                        flag=true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    newIndex++;
                }
                /*if the search in SLV was successful > do some things and continue with next element*/
                if(flag){
                    flag=false;
                    i--;
                    for(int j=0; j<newIndex-iH;j++){
                        System.out.println("Skipped SLVs "+resultSLV.get(iS+j)+"("+(iS+j)+")");
                        uniqueSyscalls+="SLV:  "+resultSLV.get(iS+j)+"("+(iS+j)+")\n";
                        resultTotal.add("SLV_"+resultSLV.get(iS+j)+"("+(iS+j)+")");
                        i++;
                    }
                    iS = newIndex;
                    /*Repeat the current element-iteration; should now be a direct match*/
                    iS--;
                    iH--;
                    continue;
                }
                
                /*search SLVs element in HLV; for the next [settings - sysCallDepth] occurences*/
                newIndex=iH;
                for(int j=0; j<set.sysCallDepth; j++){
                    //System.out.println(resultHLV.subList(newIndex, resultHLV.size()));
                    /*find next occurence*/
                    newIndex = resultHLV.subList(newIndex, resultHLV.size()-1).indexOf(resultSLV.get(iS))+newIndex;
                    /*check for next 5 elements, if back on track; cannot do loop because of break*/
                    if(resultSLV.get(iS).equals(resultHLV.get(newIndex))){
                        if(resultSLV.get(iS).equals("exit_group")){
                            flag=true;
                            break;
                        }
                        if(resultSLV.get(iS+1).equals(resultHLV.get(newIndex+1))){
                            if(resultSLV.get(iS+1).equals("exit_group")){
                                flag=true;
                                break;
                            }
                            if(resultSLV.get(iS+2).equals(resultHLV.get(newIndex+2))){
                                if(resultSLV.get(iS+2).equals("exit_group")){
                                    flag=true;
                                    break;
                                }
                                if(resultSLV.get(iS+3).equals(resultHLV.get(newIndex+3))){
                                    if(resultSLV.get(iS+3).equals("exit_group")){
                                        flag=true;
                                        break;
                                    }
                                    if(resultSLV.get(iS+4).equals(resultHLV.get(newIndex+4))){
                                        flag=true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    newIndex++;
                }
                
                /*if the search in HLV was successful > do some things and continue with next element*/
                if(flag){
                    flag=false;
                    i--;
                    for(int j=0; j<newIndex-iS; j++){
                        System.out.println("Skipped HLVs "+resultHLV.get(iH+j)+"("+(iH+j)+")");
                        uniqueSyscalls+="HLV:  "+resultHLV.get(iH+j)+"("+(iH+j)+")\n";
                        resultTotal.add("HLV_"+resultHLV.get(iH+j)+"("+(iH+j)+")");
                        i++;
                    }
                    iH = newIndex;
                    /*Repeat the current element-iteration; should now be a direct match*/
                    iS--;
                    iH--;
                    continue;
                }
                
                /*no new index could be found > execution diverged significantly*/
                /*abort search at this point*/
                
                
                System.out.println("Divergence");
                return new Object[]{"Syscall divergence (search failed)!","Syscall divergence begin: [ HLV_"+resultHLV.get(iH)+"("+iH+")  SLV_"+resultSLV.get(iS)+"("+iS+") ]",50,uniqueSyscalls};
            }
        }catch(Exception e){
            /*If this happens, its ok*/
        }
        
        /*if everything matched*/
        /*NOTE: if the syscall sequences are unequal in length, but are equal until the shorter one ended, result will nonetheless be "identical syscalls"*/
        /*Reason being that one variant executed more of the sample. If execution aborted on one variant (e.g. because of vm detect), last syscall will be exit_group*/
        /*This exit_group call will then be a detected difference. */
        if(resultTotal.isEmpty()){
            return new Object[]{"Identical Syscalls","Identical Syscalls",0,uniqueSyscalls};
        }
        /*Check if total of syscalls is over [settings - maxDivSysCallsPercentage] percent of all sysCalls; if so then declare divergence*/
        double percent = (double)resultTotal.size()/(double)Math.max(resultHLV.size(), resultSLV.size());
        if(percent > (double)set.maxDivSysCallsPercentage/100){
            return new Object[]{"Syscall divergence (limit exceeded)!","Syscall divergence begins at: [ "+resultTotal.get(0)+" ] for total overview increase maxDivSysCallsPercentage",30,uniqueSyscalls};
        }
        
        return new Object[]{"Syscall Mismatch!","Syscalls, that are variant-unique: "+resultTotal.toString(),30,uniqueSyscalls};
       
    }
}
