import static java.lang.Math.abs;
import javax.swing.JTextArea;
import java.io.StringReader;

/*Json Library: https://github.com/google/gson*/
import com.google.gson.*;
import com.google.gson.stream.JsonReader;



/**
 *
 * @author Leon Bardenheuer
 */

/**
 *  Sources:
 * 
 *  https://github.com/google/gson
 */
public class SingleSampleSubwindow extends javax.swing.JFrame {
    
    
  /*ATTRIBUTES*/
    
    Sample s;
    boolean isParsed;
    
    /**
     * Checks if current displayed HLV and SLV reports are equal
     *
     */
    public void checkResultMatch(){
        
        /*if HLV pane on Dynamic Analysis tab*/    
        if(tbHLV.getSelectedIndex()==0){
            
            /*if SLV pane on Dynamic Analysis tab*/
            if(tbSLV.getSelectedIndex()==0){
                
                /*compare HLV dynamic to SLV dynamic; set labels accordingly*/
                if(taDynamicHLV.getText().equals(taDynamicSLV.getText())) setResult(true); else setResult(false);
                
            }else{
                /*SLV pane on Network Analysis tab*/
                
                /*compare HLV dynamic to SLV network; set labels accordingly*/
                if(taDynamicHLV.getText().equals(taNetworkSLV.getText())) setResult(true); else setResult(false);
                
            }
            
        }else{
            /*HLV pane on Network Analysis tab*/
            
            /*if SLV pane on Dynamic Analysis tab*/
            if(tbSLV.getSelectedIndex()==0){
                
                /*compare HLV network to SLV dynamic; set labels accordingly*/
                if(taNetworkHLV.getText().equals(taDynamicSLV.getText())) setResult(true); else setResult(false);
                
            }else{
                /*SLV pane on Network Analysis tab*/
                
                /*compare HLV network to SLV network; set labels accordingly*/
                if(taNetworkHLV.getText().equals(taNetworkSLV.getText())) setResult(true); else setResult(false);
                
            }
        }
    }
    
    /**
     * enables/disables the "reports match" or "reports do not match" label according to checkResultMatch() method
     *
     * @param b true if "reports match" should be visible;   false if "reports do not match" should be visible
     */
    public void setResult(boolean b){
        if(b){
            lbRepMatch.setVisible(true);
            lbRepNoMatch.setVisible(false);
            return;
        }
        lbRepMatch.setVisible(false);
        lbRepNoMatch.setVisible(true);
    }
    
    /**
     * parses Dynamic_analysis JSON (with gson) and displays in given textarea
     *
     * @param ta textPane, to display results to
     */
    public void showParsedD(JTextArea ta) {
        
        ta.setLineWrap(false);
        
        Gson gson = new Gson();
        int syscallIndex=0;
        
        /*Read json from ta and parse to JsonObject (root object)*/
        JsonReader r = new JsonReader(new StringReader(ta.getText()));
        r.setLenient(true);
        JsonObject root = gson.fromJson(r, JsonObject.class);
        ta.setText("");
        
        JsonArray jA;
        
        /*Read "syscalls" from Json and parse to JsonArray (JsonObject[])*/
        ta.append("Syscalls:\n");
        jA = root.getAsJsonArray("syscalls");
        for(JsonElement jE : jA){
            ta.append("("+syscallIndex+")"+jE.getAsJsonObject().toString()+"\n");
            syscallIndex++;
        }
        /*Read "open_files" from Json and parse to JsonArray (JsonObject[])*/
        ta.append("\nOpened Files:\n");
        jA = root.getAsJsonArray("open_files");
        for(JsonElement jE : jA){
            ta.append(jE.toString()+"\n");
        }
        /*Read "processes" from Json and parse to JsonArray (JsonObject[])*/
        ta.append("\nCreated Processes:\n");
        jA = root.getAsJsonArray("processes");
        for(JsonElement jE : jA){
            ta.append(jE.getAsJsonObject().toString()+"\n");
        }
    }
    
    /**
     * parses Network_analysis JSON (with gson) and displays in given textarea
     *
     * @param ta textPane, to display results to
     */
    public void showParsedN(JTextArea ta) {
        
        ta.setLineWrap(false);
        
        /*create root JSONObject */
        Gson gson = new Gson();
        
        /*Read json from ta and parse to JsonObject (root object)*/
        JsonReader r = new JsonReader(new StringReader(ta.getText()));
        r.setLenient(true);
        JsonObject root = gson.fromJson(r, JsonObject.class);
        ta.setText("");
        
        /*Reusable*/
        JsonArray jA;
        JsonObject jO;
        
        
        /*Read "Anomalies" from Json and parse to JsonArray (JsonObject[])*/
        ta.append("Anomalies:\n");
        jA = root.getAsJsonArray("anomalies");
        for(JsonElement jE : jA){
            ta.append(jE.getAsJsonObject().toString()+"\n");
        }
        
        
        /*Read "DNS Questions" from Json and parse to JsonArray (JsonObject[])*/
        ta.append("\nDNS Questions:\n");
        jA = root.getAsJsonArray("dns_questions");
        for(JsonElement jE : jA){
            ta.append(jE.getAsJsonObject().toString()+"\n");
        }
        
        
        /*Get "Port Statistics" from Json and parse to JsonObject*/
        ta.append("\nPort Statistics:\n");
        JsonObject jOPorts = root.getAsJsonObject("port_statistics");
        
            /*Read "TCP" from Json#PortStatistics and parse to JsonObject*/
            ta.append("    TCP:\n");
            jO = jOPorts.getAsJsonObject("TCP");
            ta.append("    "+jO.toString()+"\n");
            
            /*Read "UDP" from Json#PortStatistics and parse to JsonObject*/
            ta.append("    UDP:\n");
            jO = jOPorts.getAsJsonObject("UDP");
            ta.append("    "+jO.toString()+"\n");
            
            
        /*Read "Endpoints" from Json and parse to JsonArray (JsonObject[])*/
        ta.append("\nEndpoints:\n");
        jA = root.getAsJsonArray("endpoints");
        for(JsonElement jE : jA){
            ta.append(jE.getAsJsonObject().toString()+"\n");
        }
        
    }
    
    
    /*CONSTRUCTORS*/
    
    
    public SingleSampleSubwindow() {
        initComponents();
        /*see other constructor*/
    }
    
    public SingleSampleSubwindow(Sample sample) {
        initComponents();
        s = sample;
        isParsed = false;
        
        this.setTitle("Analysis result of "+s.namePretty);
        
        lbName.setText(s.namePretty);
        lbSizeH.setText(Long.toString(s.sizeH));
        lbSizeS.setText(Long.toString(s.sizeS));
        lbSizeDiff.setText(Long.toString(abs(s.sizeH-s.sizeS)));
        
        /**
        * report data
        * 
        * [0] = HLV dynamicAnalysis
        * [1] = SLV dynamicAnalysis
        * [2] = HLV networkAnalysis
        * [3] = SLV networkAnalysis
        * 
        */
        
        taDynamicHLV.setLineWrap(true);
        taDynamicSLV.setLineWrap(true);
        taNetworkHLV.setLineWrap(true);
        taNetworkSLV.setLineWrap(true);
        
        taDynamicHLV.setText(s.reportData[0]);
        taDynamicSLV.setText(s.reportData[1]);
        taNetworkHLV.setText(s.reportData[2]);
        taNetworkSLV.setText(s.reportData[3]);
        
        taResult.setText(s.result);
        taSyscalls.setText(s.uniqueSyscalls);
        
        checkResultMatch();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        lbName = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        lbSizeH = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lbSizeS = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lbSizeDiff = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        taResult = new javax.swing.JTextArea();
        tbHLV = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        taDynamicHLV = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        taNetworkHLV = new javax.swing.JTextArea();
        tbSLV = new javax.swing.JTabbedPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        taDynamicSLV = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        taNetworkSLV = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lbRepNoMatch = new javax.swing.JLabel();
        lbRepMatch = new javax.swing.JLabel();
        rbNoParsed = new javax.swing.JRadioButton();
        rbParsed = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        taSyscalls = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(1200, 680));
        setMinimumSize(new java.awt.Dimension(1200, 680));
        setPreferredSize(new java.awt.Dimension(1200, 680));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lbName.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lbName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbName.setText("placeholder");
        getContentPane().add(lbName, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 1200, 30));
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 1300, 20));

        lbSizeH.setText("0");
        getContentPane().add(lbSizeH, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 150, 70, -1));

        jLabel2.setText("Size of HLV:");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, -1));

        lbSizeS.setText("0");
        getContentPane().add(lbSizeS, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 170, 70, -1));

        jLabel4.setText("Size of SLV:");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        lbSizeDiff.setText("0");
        getContentPane().add(lbSizeDiff, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 190, 70, -1));

        jLabel6.setText("Difference:");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, -1, -1));
        getContentPane().add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 660, 1200, 20));

        taResult.setEditable(false);
        taResult.setColumns(20);
        taResult.setLineWrap(true);
        taResult.setRows(5);
        taResult.setText("no result");
        jScrollPane1.setViewportView(taResult);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 390, 780, 250));

        tbHLV.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tbHLVStateChanged(evt);
            }
        });

        taDynamicHLV.setEditable(false);
        taDynamicHLV.setColumns(20);
        taDynamicHLV.setRows(5);
        jScrollPane3.setViewportView(taDynamicHLV);

        tbHLV.addTab("Dynamic Analysis", jScrollPane3);

        taNetworkHLV.setEditable(false);
        taNetworkHLV.setColumns(20);
        taNetworkHLV.setRows(5);
        jScrollPane4.setViewportView(taNetworkHLV);

        tbHLV.addTab("NetworkAnalysis", jScrollPane4);

        getContentPane().add(tbHLV, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 110, 500, 240));

        tbSLV.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tbSLVStateChanged(evt);
            }
        });

        taDynamicSLV.setEditable(false);
        taDynamicSLV.setColumns(20);
        taDynamicSLV.setRows(5);
        jScrollPane5.setViewportView(taDynamicSLV);

        tbSLV.addTab("Dynamic Analysis", jScrollPane5);

        taNetworkSLV.setEditable(false);
        taNetworkSLV.setColumns(20);
        taNetworkSLV.setRows(5);
        jScrollPane2.setViewportView(taNetworkSLV);

        tbSLV.addTab("Network Analysis", jScrollPane2);

        getContentPane().add(tbSLV, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 110, 500, 240));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("Result (full):");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 390, -1, 30));

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("SLV");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 80, 500, 30));

        lbRepNoMatch.setForeground(new java.awt.Color(179, 0, 0));
        lbRepNoMatch.setText("reports do not match");
        getContentPane().add(lbRepNoMatch, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 360, -1, -1));

        lbRepMatch.setForeground(new java.awt.Color(0, 179, 30));
        lbRepMatch.setText("reports match");
        getContentPane().add(lbRepMatch, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 360, -1, -1));

        buttonGroup1.add(rbNoParsed);
        rbNoParsed.setSelected(true);
        rbNoParsed.setText("Raw Data");
        rbNoParsed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbNoParsedActionPerformed(evt);
            }
        });
        getContentPane().add(rbNoParsed, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 70, -1, -1));

        buttonGroup1.add(rbParsed);
        rbParsed.setText("Parsed");
        rbParsed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbParsedActionPerformed(evt);
            }
        });
        getContentPane().add(rbParsed, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 90, -1, -1));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("HLV");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 80, 60, 30));

        taSyscalls.setColumns(20);
        taSyscalls.setRows(5);
        taSyscalls.setText("no variant-unique syscalls");
        jScrollPane6.setViewportView(taSyscalls);

        getContentPane().add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 420, 220, 220));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("variant-unique syscalls (before divergence)");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(947, 390, 240, -1));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void tbHLVStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tbHLVStateChanged
        /*When the State of the HLV tabbed pane changed (changed to different tab), checkResultMatch()*/
        
        checkResultMatch();
    }//GEN-LAST:event_tbHLVStateChanged

    private void tbSLVStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tbSLVStateChanged
        /*When the State of the SLV tabbed pane changed (changed to different tab), checkResultMatch()*/
        
        checkResultMatch();
    }//GEN-LAST:event_tbSLVStateChanged

    private void rbParsedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbParsedActionPerformed
        /*dont parse again, if already parsed*/
        if(isParsed){
            return;
        }
        
        showParsedD(taDynamicHLV);
        showParsedD(taDynamicSLV);
        showParsedN(taNetworkHLV);
        showParsedN(taNetworkSLV);
        
        isParsed=true;
    }//GEN-LAST:event_rbParsedActionPerformed

    private void rbNoParsedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbNoParsedActionPerformed
        taDynamicHLV.setLineWrap(true);
        taDynamicSLV.setLineWrap(true);
        taNetworkHLV.setLineWrap(true);
        taNetworkSLV.setLineWrap(true);
        
        taDynamicHLV.setText(s.reportData[0]);
        taDynamicSLV.setText(s.reportData[1]);
        taNetworkHLV.setText(s.reportData[2]);
        taNetworkSLV.setText(s.reportData[3]);
        
        isParsed=false;
    }//GEN-LAST:event_rbNoParsedActionPerformed

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
            java.util.logging.Logger.getLogger(SingleSampleSubwindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SingleSampleSubwindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SingleSampleSubwindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SingleSampleSubwindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SingleSampleSubwindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lbName;
    private javax.swing.JLabel lbRepMatch;
    private javax.swing.JLabel lbRepNoMatch;
    private javax.swing.JLabel lbSizeDiff;
    private javax.swing.JLabel lbSizeH;
    private javax.swing.JLabel lbSizeS;
    private javax.swing.JRadioButton rbNoParsed;
    private javax.swing.JRadioButton rbParsed;
    private javax.swing.JTextArea taDynamicHLV;
    private javax.swing.JTextArea taDynamicSLV;
    private javax.swing.JTextArea taNetworkHLV;
    private javax.swing.JTextArea taNetworkSLV;
    private javax.swing.JTextArea taResult;
    private javax.swing.JTextArea taSyscalls;
    private javax.swing.JTabbedPane tbHLV;
    private javax.swing.JTabbedPane tbSLV;
    // End of variables declaration//GEN-END:variables

}
