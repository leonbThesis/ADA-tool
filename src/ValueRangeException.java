/**
 *
 * @author Leon Bardenheuer
 */

/**
 *  Sources:
 * 
 *  https://docs.oracle.com/javase/7/docs/api/java/lang/Exception.html
 */
public class ValueRangeException extends IllegalArgumentException{
    public ValueRangeException(){
        
    }
    
    public ValueRangeException(String m){
        super(m);
    }
}
