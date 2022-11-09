
import java.awt.Color;
import java.io.*;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.*;

/**
 *
 * @author Leon Bardenheuer
 */

/**
 *  Sources:
 * 
 *  https://docs.oracle.com/javase/tutorial/essential/io/index.html
 */
public class SettingsSubwindow extends javax.swing.JFrame {
    
    
  /*ATTRIBUTES*/
    
    
    /*Colors*/
    Color colorGreen = new Color(0,179,30);
    Color colorRed = new Color(179,0,0);
    Color colorBlack = new Color(150,150,150);
    
    /*TextPane*/
    StyledDocument taStatusSettingsDoc;
    Style taStatusSettingsStyle;
    
     
    
  /*METHODS*/
    
    
     /**
     * Appends a message with a given color to the "taStatusSettings" textPane
     * 
     * @param c color of to be printed message
     * @param m message
     */
    public void appendStatusSettings(Color c, String m){
        
        try{
            /*set color*/
            StyleConstants.setForeground(taStatusSettingsStyle, c);
            
            /*append message to taStatusSettings*/
            taStatusSettingsDoc.insertString(taStatusSettingsDoc.getLength(), m, taStatusSettingsStyle);
            
        /*generated catch*/    
        } catch (BadLocationException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Processes all updates on the settings.txt files; writes updated values to the file
     * 
     * @return true if everything is written correctly, false if loading/parsing/writing/etc failed
     * @throws java.io.IOException
     */
    public boolean processNewSettingsFile() throws IOException {
        
        /*result: -1 = not set; 0 = false; 1 = true*/
        int result=-1;
        
        String currentLine;
        int currentSettingIndex = 0;
        
        /*files*/
        Path w = Paths.get(System.getProperty("user.dir")+"\\settingsTEMP.txt");
        Path r = Paths.get(System.getProperty("user.dir")+"\\settings.txt");
        
        /*Load reader (settings.txt) and writers (settingsTEMP.txt)*/
        try (FileReader fr = new FileReader(r.toString());
             FileWriter fw = new FileWriter(w.toString());
             BufferedReader br = new BufferedReader(fr);
             BufferedWriter bw = new BufferedWriter(fw)) {
            
            /*read first line*/
            currentLine = br.readLine();
            
            /*read lines until end*/
            while(currentLine!=null){
                
                /*copy line, if it starts with '*' (comment)*/
                if(currentLine.startsWith("*")){
                    fw.write(currentLine+"\n");
                    currentLine = br.readLine();
                    continue;
                }
                
                /*when arriving at this point > actual settings value (no comment) > has to be updated*/
                
                try{
                    switch(currentSettingIndex){
                        case 0: try{
                                    int number = Integer.parseInt(txMinSizeSimple.getText());
                                    if(number<=0) throw new ValueRangeException();
                                    fw.write(Integer.toString(number)+"\n");
                                }catch(NumberFormatException e){
                                    appendStatusSettings(colorRed, "* ERROR: invalid type of field "+(currentSettingIndex+1)+"\n");
                                    result=0;
                                }catch(ValueRangeException e){
                                    appendStatusSettings(colorRed, "* ERROR: invalid value range of field "+(currentSettingIndex+1)+"; value must be greater than 0\n");
                                    result=0;
                                }
                                break;
                        case 1: try{
                                    int number = Integer.parseInt(txSysCallDepth.getText());
                                    if(number<=0) throw new ValueRangeException();
                                    fw.write(Integer.toString(number)+"\n");
                                }catch(NumberFormatException e){
                                    appendStatusSettings(colorRed, "* ERROR: invalid type of field "+(currentSettingIndex+1)+"\n");
                                    result=0;
                                }catch(ValueRangeException e){
                                    appendStatusSettings(colorRed, "* ERROR: invalid value range of field "+(currentSettingIndex+1)+"; value must be greater than 0\n");
                                    result=0;
                                }
                                break;
                        case 2: try{
                                    int number = Integer.parseInt(txMaxDivSysCallsPercentage.getText());
                                    if(number<=0 || number>100) throw new ValueRangeException();
                                    fw.write(Integer.toString(number)+"\n");
                                }catch(NumberFormatException e){
                                    appendStatusSettings(colorRed, "* ERROR: invalid type of field "+(currentSettingIndex+1)+"\n");
                                    result=0;
                                }catch(ValueRangeException e){
                                    appendStatusSettings(colorRed, "* ERROR: invalid value range of field "+(currentSettingIndex+1)+"; value must be greater than 0 and smaller or equal to 100\n");
                                    result=0;
                                }
                                break;
                               

                        default: result = 0;
                    }
                }catch(Exception e){
                    appendStatusSettings(colorRed, "* ERROR: during parsing of settings! Please verify inputs! Settings have NOT been saved!\n");
                    result = 0;
                }
                
                currentSettingIndex++;
                
                
                currentLine = br.readLine();
            }
            
            /*if all settings have been parsed correctly, and there was no error (would be indicated by result=0), success*/
            if(currentSettingIndex==3 && result!=0){
                result = 1;
            }
        }
        
        /*failed, if result did not set to 1 (if 0, then already 0)*/
        if(result==-1){
            result=0;
        }
        
        /*overwrite real file with tmp file if all successful*/
        if(result == 1){
            Files.move(w,r, StandardCopyOption.REPLACE_EXISTING);
        }
        /*if failed; delete tmp file*/
        if(result == 0){
            Files.delete(w);
        }
        
        return result==1;
        
    }
    
    /**
     * Creates new form SettingsSubwindow
     */
    public SettingsSubwindow() {
        initComponents();
        /*see other constructor*/
    }
    
    public SettingsSubwindow(Settings settings){
        initComponents();
        
        taStatusSettingsDoc = taStatusSettings.getStyledDocument();
        taStatusSettingsStyle = taStatusSettings.addStyle("style2", null);
        
        /*only changes the settings.txt file, not the internal settings object; this means program has to be restarted for changes to take effect*/
        
        txMinSizeSimple.setText(Integer.toString(settings.minSizeSimple));
        txSysCallDepth.setText(Integer.toString(settings.sysCallDepth));
        txMaxDivSysCallsPercentage.setText(Integer.toString(settings.maxDivSysCallsPercentage));
        
        
        appendStatusSettings(colorGreen, "* loading of settings successful\n");
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        btSave = new javax.swing.JButton();
        txMinSizeSimple = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane3 = new javax.swing.JScrollPane();
        taStatusSettings = new javax.swing.JTextPane();
        jSeparator2 = new javax.swing.JSeparator();
        txSysCallDepth = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txMaxDivSysCallsPercentage = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Settings");
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Simple and full analysis  -  Minimum size difference (bytes) needed, to recognize anti-vm (default = 100)");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 60, -1, 20));

        btSave.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btSave.setText("SAVE SETTINGS");
        btSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSaveActionPerformed(evt);
            }
        });
        getContentPane().add(btSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 190, -1, 90));

        txMinSizeSimple.setText("100");
        getContentPane().add(txMinSizeSimple, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 90, -1));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("The values, present below, are the current values of the loaded settings! For changed settings to take effect, new values have to be saved and the program has to be restarted! ");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 1020, -1));
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 1020, 20));

        taStatusSettings.setEditable(false);
        jScrollPane3.setViewportView(taStatusSettings);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 750, 90));
        getContentPane().add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 300, 1020, 20));

        txSysCallDepth.setText("3");
        getContentPane().add(txSysCallDepth, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, 90, -1));

        jLabel3.setText("Full analysis  -  Search depth for syscall analysis (default = 3)");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 90, -1, 20));

        txMaxDivSysCallsPercentage.setText("20");
        getContentPane().add(txMaxDivSysCallsPercentage, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, 90, -1));

        jLabel4.setText("Full analysis  -  Percentage of syscalls that can be different before total divergence is declared (default = 20)");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 120, -1, 20));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSaveActionPerformed
        
        try {
            /*if successful...*/
            if(processNewSettingsFile()){
                appendStatusSettings(colorGreen, "* write successful; settings have been saved!\n");
                appendStatusSettings(colorGreen, "* please restart the program for changes to take effect!\n");
            }else{
                appendStatusSettings(colorRed, "* ERROR: during parsing of settings! Please verify inputs! Settings have NOT been saved!\n");
            }
        } catch (IOException ex) {
            appendStatusSettings(colorRed, "* ERROR: while trying to save settings.txt file\n");
            Logger.getLogger(SettingsSubwindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_btSaveActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SettingsSubwindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SettingsSubwindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SettingsSubwindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SettingsSubwindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SettingsSubwindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextPane taStatusSettings;
    private javax.swing.JTextField txMaxDivSysCallsPercentage;
    private javax.swing.JTextField txMinSizeSimple;
    private javax.swing.JTextField txSysCallDepth;
    // End of variables declaration//GEN-END:variables
}
