
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.logging.*;
import javax.swing.text.*;



/**
 *
 * @author Leon Bardenheuer
 */

/**
 *  Sources:
 * 
 *  https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
 *  https://docs.oracle.com/javase/7/docs/api/javax/swing/text/StyledDocument.html
 *  https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
 *  https://docs.oracle.com/javase/tutorial/essential/io/index.html
 */
public class main extends javax.swing.JFrame {
    
    
  /*ATTRIBUTES*/
    
    
    /*Colors*/
    Color colorGreen = new Color(0,179,30);
    Color colorRed = new Color(179,0,0);
    Color colorBlack = new Color(150,150,150);
    
    /*TextPane*/
    StyledDocument taStatusDoc;
    Style taStatusStyle;
    
    /*Sample Lists*/
    
    Sample[] samplesArr;
    
    /*Folder Paths*/
    String folderPath;
    File folder;
    File folderH;
    File folderS;
    
    /*Other Subwindows*/
    AllSamplesSubwindow allSamplesSubwindow;
    SingleSampleSubwindow singleSampleSubwindow;
    SettingsSubwindow settingsSubwindow;
    AboutSubwindow aboutSubwindow;
    
    
    /*Other*/
    boolean isAnalyzed = false; //current analysis status; to prevent displaying analysis results before analysis
    boolean disabled = false; // when fatal error occurred, certain functions (like most buttons) are disabled
    Settings settings;        // settings object
    String staticAnalysis;  // per file ; saved here because this String can be very big; to avoid copying over and over again when using in different functions
    

    
    
  /*METHODS*/
    
    
    /**
     * Appends a message with a given color to the "taStatus" textPane
     * 
     * @param c color of to be printed message
     * @param m message
     */
    public void appendStatus(Color c, String m){
        
        try{
            /*set color*/
            StyleConstants.setForeground(taStatusStyle, c);
            
            /*append message to taStatus*/
            taStatusDoc.insertString(taStatusDoc.getLength(), m, taStatusStyle);
            
        /*generated catch*/    
        } catch (BadLocationException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Tests the specified folder for syntactical correctness
     * 
     * @return boolean : result of the test
     */
    public boolean testFolder(){
        
        File[] files = folder.listFiles();
        if(files.length!=2){
            appendStatus(colorRed, "* INVALID FOLDER: invalid number of items; folder should only contain hlv and slv subfolders\n");
            return false;
        }
        
        if(!files[0].getName().equals("hlv") || !files[1].getName().equals("slv")){
            appendStatus(colorRed, "* INVALID FOLDER: folder does not contain hlv and slv subfolders; check correct spelling\n");
            return false;
        }
        
        appendStatus(colorGreen, "* Folder read successful\n");
        
        return true;
    }
    
    /**
     * extract the real filename / sample name (SHA-256) from the composed filename (sample name + UUID)
     * 
     * @param filename the composed filename
     * @return String : the real filename / sample name (SHA-256)
     */
    public String getRealName(String filename){
        if(filename.contains("-"))
            /*search for first occurence of '-' (marks start of UUID) and take everything before that (sample name / SHA-256); concat .json for file management*/
            return filename.substring(0, filename.indexOf('-')).concat(".json");
        return "";
    }
    
    /**
     * Reads in all sample reports located in specified folder; creates Sample objects for them
     * 
     */
    public void readSamples(){
        
        ArrayList<Sample> samplesArrList = new ArrayList<Sample>();
        
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
        
        
        /*clear list*/
        lsSamples.setListData(new String[]{});
        
        /*get subfolders*/
        folderH = new File(folderPath+"\\hlv\\");
        folderS = new File(folderPath+"\\slv\\");
        
        
        /*add all files of hlv to the list*/
        for(File f: folderH.listFiles()){
            
            /*add sample to list*/
            try{
                samplesArrList.add(new Sample(true, getRealName(f.getName()), f.length(), getDataFromFile(f)));
                
                //appendStatus(colorBlack, "* Added hlv sample "+getRealName(f.getName())+" with "+f.length()+" bytes to the list\n");
            }catch(IOException e){
                appendStatus(colorRed, "* ERROR reading files");
            }
        }
        
        /*add all files, that are not already on the list, to the list; otherwise update entry*/
        for(File f: folderS.listFiles()){
            readSamplesInner(f, samplesArrList);
        }
        
        /*convert samplesArrList (ArrayList) to samplesArr (Array) for easier handling; List capabilities are not needed anymore*/
        samplesArr = new Sample[samplesArrList.size()];
        samplesArr = samplesArrList.toArray(samplesArr);
        
        /*add list to jList*/
        String[] forJList = new String[samplesArr.length];
        for(int i=0;i<samplesArr.length;i++){
            forJList[i]=i+": "+samplesArr[i].namePretty;
        }
        lsSamples.setListData(forJList);
        
        /*display total sample count*/
        lbSampleCount.setText(Integer.toString(samplesArr.length));
        
        
        appendStatus(colorGreen, "* successfully retrieved "+ lbSampleCount.getText() +" samples\n");
    }
    
    /**
     * Iterates through list and adds the current file, if it is not already on the list, to the list; otherwise update entry
     * 
     * @param f Current file to read
     * @param samplesArrList list of all samples (read and write in this method)
     */
    public void readSamplesInner(File f, ArrayList<Sample> samplesArrList){
        
        
        try{
            /**
            * report data
            * 
            * [0] = SLV dynamicAnalysis
            * [1] = SLV networkAnalysis
            * 
            */
            String[] slvData = getDataFromFile(f);
            
            
            /*look through list, if sample is already present; if so, update*/
            for(Sample s: samplesArrList){
                if(s.name.equals(getRealName(f.getName()))){
                    s.sizeS = f.length();

                    s.reportData[1]=slvData[0];
                    s.reportData[3]=slvData[1];
                    //appendStatus(colorBlack, "* Updated hlv sample "+getRealName(f.getName())+" with added "+f.length()+" bytes from slv\n");
                    return;
                }
            }
            /*if not already present, create new entry */
            samplesArrList.add(new Sample(false, getRealName(f.getName()), f.length(), slvData));
            //appendStatus(colorBlack, "* Added slv new sample "+getRealName(f.getName())+" with "+f.length()+" bytes to the list\n");
        }catch(IOException e){
            appendStatus(colorRed, "* ERROR reading files");
        }
    }
    
    /**
     * reads a file and returns its data in a structured way
     * 
     * @param f Current file to read
     * @return String[2] with [0] being dynamic analysis data from the file and [1] being network analysis data from the file
     * @throws java.io.IOException
     */
    public String[] getDataFromFile(File f) throws IOException{
        
        String input;
        int startDyn;
        int startNet;
        
        
        /*try-with-resources block (see sources) to read file*/
        try (FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr)) {
            
            /*File contains only one line*/
            input = br.readLine();
            
            /*after keywords: offset +19*/
            /*before keywords: offset -3*/
            startDyn = input.indexOf("dynamic_analysis");
            startNet = input.indexOf("network_analysis");
        }
            
        
        //appendStatus(colorBlack, "dynamic: "+(input.substring(startDyn+19, startNet-3))+"\n");
        //appendStatus(colorBlack, "nework: "+(input.substring(startNet+19))+"\n");
        
        
        
        String[] ret = new String[2];
        
        ret[0] = input.substring(startDyn+19, startNet-3);
        ret[1] = input.substring(startNet+19);
        
        return ret;
    }
    
    /**
     * Loads the settings.txt file
     * 
     * @return true if everything is loaded correctly, false if loading/parsing/etc failed
     * @throws java.io.IOException
     */
    public boolean loadSettings() throws IOException {
        
        /*result: -1 = not set (false); 0 = false; 1 = true*/
        int result=-1;
        
        String currentLine;
        int currentSettingIndex = 0;
        settings = new Settings();
        
        /*try-with-resources block (see sources) to read settings.txt file, contained in main program directory*/
        try (FileReader fr = new FileReader(System.getProperty("user.dir")+"\\settings.txt");
            BufferedReader br = new BufferedReader(fr)) {
            
            /*read first line*/
            currentLine = br.readLine();
            
            /*read lines until end*/
            while(currentLine!=null){
                
                /*skip line, if it starts with '*' (comment)*/
                if(currentLine.startsWith("*")){
                    currentLine = br.readLine();
                    continue;
                }
                
                /*when arriving at this point > actual settings value (no comment)*/
                
                try{
                    switch(currentSettingIndex){
                        case 0: settings.minSizeSimple=Integer.parseInt(currentLine); // this should not fail, except when manually editing the settings.txt file
                                break;
                        case 1: settings.sysCallDepth=Integer.parseInt(currentLine); // this should not fail, except when manually editing the settings.txt file
                                break;
                        case 2: settings.maxDivSysCallsPercentage=Integer.parseInt(currentLine); // this should not fail, except when manually editing the settings.txt file
                                break;
                                
                                
                        /*more settings can be initialized here*/
                                
                        default: result = 0;
                    }
                }catch(Exception e){
                    appendStatus(colorRed, "* ERROR: during parsing of settings.txt file! Please verify file and restart the program\n");
                    result = 0;
                }
                
                currentSettingIndex++;
                
                
                currentLine = br.readLine();
            }
            
            /*if all settings have been parsed correctly (!!! currentSettingIndex = number of settings !!!), and there was no error (would be indicated by result=0), success*/
            if(currentSettingIndex==3 && result!=0){
                result = 1;
            }
        }
        
        /*failed, if result did not set to 1 (if 0, then already 0)*/
        if(result==-1){
            result=0;
        }
        return result==1;
    }
    
    /**
     * Writes analysis data to a csv file
     * 
     * @param path Path (including filename) for file to be written to
     * @return true if everything is written correctly, false if loading/parsing/writing/etc failed
     * @throws java.io.IOException
     */
    public boolean saveToCSV(String path) throws IOException {
        
        
        /*result: -1 = not set; 0 = false; 1 = true*/
        int result=-1;
        
        /*Load writer*/
        try (FileWriter fw = new FileWriter(path);
             BufferedWriter bw = new BufferedWriter(fw)) {
            
            bw.write("index,name/SHA-256,score,resultShort,result\n");
            
            String message;
            for(int i=0; i<samplesArr.length; i++){
                message="";
                message += Integer.toString(i)+",";
                message += samplesArr[i].namePretty+",";
                message += samplesArr[i].score+",";
                message += samplesArr[i].resultShort+",";
                message += samplesArr[i].result;
                bw.write(message+"\n");
            }
            result = 1;
        }
        
        /*failed, if result did not set to 1 (if 0, then already 0)*/
        if(result==-1){
            result=0;
        }
        
        return result==1;
        
    }
    
    /**
     * Increase the score of a sample until 100
     * 
     * @param oldScore to be increased score
     * @param increase amount that shall be added
     * @return int new score
     */
    public int increaseScore(int oldScore, int increase){
        
        if(oldScore+increase>100) return 100;
        if(increase==-1) return -1;
        return oldScore+increase;
    }
    
    /**
     * Executes everything Window and program-startup related, e.g. initializes variables 
     */   
    public main() {
        initComponents();
        
        folderPath="";
        
        taStatusDoc = taStatus.getStyledDocument();
        taStatusStyle = taStatus.addStyle("style1", null);
        
        txLocation.setText(System.getProperty("user.home") + "\\Desktop\\");
        
        lsSamples.setListData(new String[]{"no samples yet"});
        
        
        lbSampleCount.setText("");
        
        /*load settings.txt file*/
        try {
            /*load settings.txt file; if cannot be loaded, disable further usage of program*/
            if(!loadSettings()){
                appendStatus(colorRed, "* ERROR: with settings.txt file! Please verify file and restart the program\n");
                disabled=true;
            }
        } catch (IOException ex) {
            appendStatus(colorRed, "* ERROR: with settings.txt file! Please verify file and restart the program\n");
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
            disabled=true;
        }
        
        /*verify loading*/
        if(settings==null || !settings.verify()){
            appendStatus(colorRed, "* ERROR: with parsing of settings.txt file! Please verify file and restart the program\n");
            disabled=true;
        }
        
        appendStatus(colorGreen, "* all components successfully initialized\n");
        //appendStatus(colorRed, "* example error\n");
        //appendStatus(colorBlack, "* normal message\n");
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
        jSeparator2 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        lbSampleCount = new javax.swing.JLabel();
        btAbout = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        taStatus = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        lsSamples = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        txLocation = new javax.swing.JTextField();
        btFolder = new javax.swing.JButton();
        btDownloadResults = new javax.swing.JButton();
        btSimpleAnalysis = new javax.swing.JButton();
        btSingleResult = new javax.swing.JButton();
        btAllResults = new javax.swing.JButton();
        btFullAnalysis = new javax.swing.JButton();
        btSettings = new javax.swing.JButton();
        rbProgramFolder = new javax.swing.JRadioButton();
        rbDesktop = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Automated Differential Analysis (ADA) tool");
        setMaximumSize(new java.awt.Dimension(1300, 740));
        setMinimumSize(new java.awt.Dimension(1300, 740));
        setPreferredSize(new java.awt.Dimension(1330, 740));
        setResizable(false);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 710, 1300, 10));

        jLabel1.setText("Total Samples:");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 480, -1, -1));

        lbSampleCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbSampleCount.setText(" ");
        lbSampleCount.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        getContentPane().add(lbSampleCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 480, 60, -1));

        btAbout.setText("about");
        btAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAboutActionPerformed(evt);
            }
        });
        getContentPane().add(btAbout, new org.netbeans.lib.awtextra.AbsoluteConstraints(1220, 660, 70, 30));

        taStatus.setEditable(false);
        jScrollPane3.setViewportView(taStatus);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 540, 1190, 150));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Automated Differential Analysis (ADA) tool");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 1400, -1));
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 1300, 10));

        lsSamples.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        lsSamples.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lsSamplesMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(lsSamples);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, 520, 360));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setText("Folder Location:");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 110, -1, 30));

        txLocation.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txLocation.setPreferredSize(new java.awt.Dimension(98, 17));
        getContentPane().add(txLocation, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 110, 620, 30));

        btFolder.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btFolder.setText("Read folder with sample reports");
        btFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFolderActionPerformed(evt);
            }
        });
        getContentPane().add(btFolder, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 150, 730, 60));

        btDownloadResults.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btDownloadResults.setText("Save analysis results to csv file");
        btDownloadResults.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btDownloadResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDownloadResultsActionPerformed(evt);
            }
        });
        getContentPane().add(btDownloadResults, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 420, 300, 40));

        btSimpleAnalysis.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btSimpleAnalysis.setText("Simple Analysis (only file size)");
        btSimpleAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSimpleAnalysisActionPerformed(evt);
            }
        });
        getContentPane().add(btSimpleAnalysis, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 220, 350, 60));

        btSingleResult.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btSingleResult.setText("Show detailed analysis result of selected sample");
        btSingleResult.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btSingleResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSingleResultActionPerformed(evt);
            }
        });
        getContentPane().add(btSingleResult, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 320, 470, 40));

        btAllResults.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btAllResults.setText("Show analysis results of all sample");
        btAllResults.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btAllResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAllResultsActionPerformed(evt);
            }
        });
        getContentPane().add(btAllResults, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 370, 470, 40));

        btFullAnalysis.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btFullAnalysis.setText("Full Analysis");
        btFullAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFullAnalysisActionPerformed(evt);
            }
        });
        getContentPane().add(btFullAnalysis, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 220, 350, 60));

        btSettings.setText("settings");
        btSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSettingsActionPerformed(evt);
            }
        });
        getContentPane().add(btSettings, new org.netbeans.lib.awtextra.AbsoluteConstraints(1220, 620, -1, 30));

        buttonGroup1.add(rbProgramFolder);
        rbProgramFolder.setSelected(true);
        rbProgramFolder.setText("save to Program folder");
        getContentPane().add(rbProgramFolder, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 420, -1, -1));

        buttonGroup1.add(rbDesktop);
        rbDesktop.setText("save to Desktop");
        getContentPane().add(rbDesktop, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 440, -1, -1));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAboutActionPerformed
       
        /*pop-up new settings subwindow (but only one at the time)*/
        if(aboutSubwindow != null){
            aboutSubwindow.dispose();
        }
        aboutSubwindow = new AboutSubwindow();
        aboutSubwindow.setVisible(true);
    }//GEN-LAST:event_btAboutActionPerformed

    private void btFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFolderActionPerformed

        /*if the program is disabled, e.g. settings file could not be loaded, prevent further use of program*/
        if(disabled){
            appendStatus(colorRed, "* ERROR: please restart the program and check file integrity\n");
            return;
        }
        
        /*check if folderPath already set*/
        if(!folderPath.equals("")){
            appendStatus(colorRed, "* INVALID ACTION: folder already read; restart program to choose another folder\n");
            return;
        }
        
        folderPath = txLocation.getText();

        try{

            folder = new File(folderPath);
            
            /*when testFolder successful: continue*/
            if(testFolder()){
                
                readSamples();
            }else{
                folderPath="";
                appendStatus(colorRed, "* folder path not set\n");
            }

        }catch(NullPointerException e){
            appendStatus(colorRed, "* ERROR: cannot read folder path\n");
            folderPath="";
        }

    }//GEN-LAST:event_btFolderActionPerformed

    private void btDownloadResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDownloadResultsActionPerformed
        /*if the program is disabled, e.g. settings file could not be loaded, prevent further use of program*/
        if(disabled){
            appendStatus(colorRed, "* ERROR: please restart the program and check file integrity\n");
            return;
        }
        
        if(!isAnalyzed){
            appendStatus(colorRed, "* INVALID ACTION: analyze samples first\n");
            return;
        }
        
        boolean success = false;
        try{
            /*if save to Program folder*/
            if(rbProgramFolder.isSelected()){
                success = saveToCSV(System.getProperty("user.dir")+"\\results.csv");
            }else{
                /*if save to Desktop*/
                success = saveToCSV(System.getProperty("user.home") + "\\Desktop\\results.csv");
            }
            
        }catch(IOException ex){
            success = false;
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(success) appendStatus(colorGreen, "* File successfully written\n");
        else appendStatus(colorRed, "* ERROR: during file creation; try the other save path\n");
        
        
    }//GEN-LAST:event_btDownloadResultsActionPerformed

    private void btSimpleAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSimpleAnalysisActionPerformed
        /*if the program is disabled, e.g. settings file could not be loaded, prevent further use of program*/
        if(disabled){
            appendStatus(colorRed, "* ERROR: please restart the program and check file integrity\n");
            return;
        }
        
        if(folderPath.equals("")){
            appendStatus(colorRed, "* INVALID ACTION: read folder first\n");
            return;
        }
        
        try{
            for(Sample s: samplesArr){
                s.resultShort = "";
                s.result = "";
                s.score = 0;
                
                if(!s.isFull()){
                    if(s.sizeH==0)s.result = s.resultShort = "Only SLV report is available; possible error on HLV version; this might indicate anti-vm";
                    else s.result = s.resultShort = "Only HLV report is available; possible error on SLV version; this might indicate anti-vm";
                    s.score=-1;
                    continue;
                }
                if(s.sizeH == s.sizeS){
                    s.result = s.resultShort = "Both reports have the same size of "+s.sizeH+" bytes";
                    s.score=0;
                    continue;
                }
                if(s.sizeH > s.sizeS){
                    s.result = s.resultShort = "The size of HLV is "+(s.sizeH - s.sizeS)+" bytes bigger than SLV ( "+s.sizeH+" - "+s.sizeS+" )";
                    
                    if((s.sizeH - s.sizeS) >= settings.minSizeSimple) s.score=100;
                    else s.score=0;
                    
                    
                    
                    continue;
                }
                s.result = s.resultShort = "The size of SLV is "+(s.sizeS - s.sizeH)+" bytes bigger than HLV ( "+s.sizeS+" - "+s.sizeH+" )";
                
                if((s.sizeS - s.sizeH) >= settings.minSizeSimple) s.score=100;
                else s.score=0;
            }
            
            appendStatus(colorGreen, "* Simple analysis finished successfully\n");
            isAnalyzed=true;
            
            
        }catch(Exception e){
            appendStatus(colorRed, "* ERROR: during simple analysis\n");
        }
        
        
    }//GEN-LAST:event_btSimpleAnalysisActionPerformed

    private void btSingleResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSingleResultActionPerformed
        /*if the program is disabled, e.g. settings file could not be loaded, prevent further use of program*/
        if(disabled){
            appendStatus(colorRed, "* ERROR: please restart the program and check file integrity\n");
            return;
        }
        
        if(lsSamples.getSelectedIndex()==-1){
            appendStatus(colorRed, "* INVALID ACTION: select a sample from the list first\n");
            return;
        }
        
        /*pop-up new subwindow (but only one at the time)*/
        if(singleSampleSubwindow != null){
            singleSampleSubwindow.dispose();
        }
        singleSampleSubwindow = new SingleSampleSubwindow(samplesArr[lsSamples.getSelectedIndex()]);
        singleSampleSubwindow.setVisible(true);
        
        
        
        
    }//GEN-LAST:event_btSingleResultActionPerformed

    private void btAllResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAllResultsActionPerformed
        /*if the program is disabled, e.g. settings file could not be loaded, prevent further use of program*/
        if(disabled){
            appendStatus(colorRed, "* ERROR: please restart the program and check file integrity\n");
            return;
        }
        
        if(!isAnalyzed){
            appendStatus(colorRed, "* INVALID ACTION: analyze samples first\n");
            return;
        }
        
        /*pop-up new subwindow (but only one at the time)*/
        if(allSamplesSubwindow != null){
            allSamplesSubwindow.dispose();
        }
        allSamplesSubwindow = new AllSamplesSubwindow(samplesArr);
        allSamplesSubwindow.setVisible(true);
        
        

    }//GEN-LAST:event_btAllResultsActionPerformed

    private void btFullAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFullAnalysisActionPerformed
        /*if the program is disabled, e.g. settings file could not be loaded, prevent further use of program*/
        if(disabled){
            appendStatus(colorRed, "* ERROR: please restart the program and check file integrity\n");
            return;
        }
        
        if(folderPath.equals("")){
            appendStatus(colorRed, "* INVALID ACTION: read folder first\n");
            return;
        }
        
        FullAnalysisProcessor proc;
        
        try{
            for(Sample s: samplesArr){
                
                /*Check if only HLV or SLV report is available; if so: set result and score and continue with next sample*/
                if(!s.isFull()){
                    if(s.sizeH==0)s.result = s.resultShort = "Only SLV report is available; possible error on HLV version; this might indicate anti-vm";
                    else s.result = s.resultShort = "Only HLV report is available; possible error on SLV version; this might indicate anti-vm";
                    s.score=-1;
                    continue;
                }
                
                proc = new FullAnalysisProcessor(s, settings);
                s.resultShort = "";
                s.result = "";
                s.score = 0;
                
                /* Object[] result[0]: (String) s.resultShort; ...[1]: (String) s.result; ...[2]: (double) s.score change; ...[3]: (String) s.uniqueSyscalls*/
                /*Check for suspicious files*/
                Object[] result = proc.suspiciousFiles(); 
                s.resultShort = (String) result[0];
                s.result = (String) result[1];
                s.score = increaseScore(s.score, (int) result[2]);
                
                /*Check for network analysis differences*/
                result = proc.checkNetwork(); 
                s.resultShort = s.resultShort +" | "+(String) result[0];
                s.result = s.result +" | "+(String) result[1];
                s.score = increaseScore(s.score, (int) result[2]);
                
                /*Check for file size differences*/
                result = proc.fileSize(); 
                s.resultShort = s.resultShort +" | "+(String) result[0];
                s.result = s.result +" | "+(String) result[1];
                s.score = increaseScore(s.score, (int) result[2]);
                
                /*Check for syscall analysis differences*/
                /*Detailed syscall analysis log is available in console*/
                System.out.println("\nSyscall Analysis of: "+s.namePretty);
                result = proc.syscallAnalysis(); 
                s.resultShort = s.resultShort +" | "+(String) result[0];
                s.result = s.result +" | "+(String) result[1];
                s.score = increaseScore(s.score, (int) result[2]);
                s.uniqueSyscalls = (String) result[3];
            }
            
            appendStatus(colorGreen, "* Full analysis finished successfully\n");
            isAnalyzed=true;
            
            
        }catch(Exception e){
            appendStatus(colorRed, "* ERROR: during full analysis\n");
        }
        
        
    }//GEN-LAST:event_btFullAnalysisActionPerformed

    private void btSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSettingsActionPerformed
        /*if the program is disabled, e.g. settings file could not be loaded, prevent further use of program*/
        if(disabled){
            appendStatus(colorRed, "* ERROR: please restart the program and check file integrity\n");
            return;
        }
        
        /*pop-up new settings subwindow (but only one at the time)*/
        if(settingsSubwindow != null){
            settingsSubwindow.dispose();
        }
        settingsSubwindow = new SettingsSubwindow(settings);
        settingsSubwindow.setVisible(true);
        
    }//GEN-LAST:event_btSettingsActionPerformed

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        
        /*if focus is gained (> subwindow was open, and clicked on main window again) close settings and about subwindow to avoid them being enabled in background*/
        
        if(aboutSubwindow != null){
            aboutSubwindow.dispose();
        }
        if(settingsSubwindow != null){
            settingsSubwindow.dispose();
        }
    }//GEN-LAST:event_formWindowGainedFocus

    private void lsSamplesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lsSamplesMouseClicked
        
        /*if the program is disabled, e.g. settings file could not be loaded, prevent further use of program*/
        if(disabled){
            appendStatus(colorRed, "* ERROR: please restart the program and check file integrity\n");
            return;
        }
        
        /*if double click on table entry (except the placeholder "no samples yet")*/
        if(evt.getClickCount()==2 && lsSamples.getSelectedIndex()!=-1 && !lsSamples.getSelectedValue().equals("no samples yet")){
            
            /*pop-up new subwindow (but only one at the time)*/
            if(singleSampleSubwindow != null){
                singleSampleSubwindow.dispose();
            }
            singleSampleSubwindow = new SingleSampleSubwindow(samplesArr[lsSamples.getSelectedIndex()]);
            singleSampleSubwindow.setVisible(true);
        }
    }//GEN-LAST:event_lsSamplesMouseClicked

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
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAbout;
    private javax.swing.JButton btAllResults;
    private javax.swing.JButton btDownloadResults;
    private javax.swing.JButton btFolder;
    private javax.swing.JButton btFullAnalysis;
    private javax.swing.JButton btSettings;
    private javax.swing.JButton btSimpleAnalysis;
    private javax.swing.JButton btSingleResult;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lbSampleCount;
    private javax.swing.JList<String> lsSamples;
    private javax.swing.JRadioButton rbDesktop;
    private javax.swing.JRadioButton rbProgramFolder;
    private javax.swing.JTextPane taStatus;
    private javax.swing.JTextField txLocation;
    // End of variables declaration//GEN-END:variables
}
