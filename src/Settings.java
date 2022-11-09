/**
 *
 * @author Leon Bardenheuer
 */
public class Settings {
    int minSizeSimple=-1;
    int sysCallDepth=-1;
    int maxDivSysCallsPercentage=-1;

    public Settings() {
        
    }
    
    /**
     * Loads the settings.txt file
     * 
     * @return true if everything is loaded correctly, false if something is not initialized correctly
     */
    public boolean verify(){
        return minSizeSimple != -1 && sysCallDepth != -1 && maxDivSysCallsPercentage !=-1;
    }
}
