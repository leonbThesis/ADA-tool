
/**
 *
 * @author Leon Bardenheuer
 */
public class Sample {

    /*Attributes*/
    
    
    String name;
    String namePretty;
    long sizeH=0;
    long sizeS=0;
    int score=0;
    String result="no result"; // result string may not contain commas to ensure correct csv creation
    String resultShort="no result";
    /*See full analysis > syscallAnalysis*/
    String uniqueSyscalls="";
    
    /**
    * report data
    * 
    * [0] = HLV dynamicAnalysis
    * [1] = SLV dynamicAnalysis
    * [2] = HLV networkAnalysis
    * [3] = SLV networkAnalysis
    * 
    */
    String[] reportData = new String[4];
    
    
    /*Constructor*/
    
    /*isHLV -> HLV  ;  !isHLV -> SLV*/
    /*reportDataHalf includes reportData from HLV (isHLV) or from SLV (!isHLV)  -  gets saved accordingly*/
    
    public Sample(boolean isHLV, String name, long size, String[] reportDataHalf) {
        this.name = name;
        /*remove .json at the end*/
        this.namePretty = name.substring(0, name.length()-5);
        
        if(isHLV){
            this.sizeH = size;
            this.reportData[0] = reportDataHalf[0];
            this.reportData[2] = reportDataHalf[1];
        }else{
            this.sizeS = size;
            this.reportData[1] = reportDataHalf[0];
            this.reportData[3] = reportDataHalf[1];
        }
        
    }
    
    
    /*Methods*/
    
    /** Checks, if both, hlv and slv, data is present, or only one (indicating an error on only on variant, which might indicate anti-vm)
     *
     * @return true if hlv and slv data is present; false if only one
     */
    public boolean isFull(){
        return !( sizeH == 0 || sizeS == 0 );
    }
    
    
    
}
