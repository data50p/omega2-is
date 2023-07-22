package com.femtioprocent.omega.lesson.canvas;


import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.OmegaVersion;
import com.femtioprocent.omega.adm.assets.TargetCombinations;
import com.femtioprocent.omega.lesson.LessonContext;
import com.femtioprocent.omega.lesson.appl.ApplContext;
import com.femtioprocent.omega.lesson.helper.PathHelper;
import com.femtioprocent.omega.swing.TableSorter;
import com.femtioprocent.omega.swing.filechooser.ChooseDir;
import com.femtioprocent.omega.swing.filechooser.ChooseOmegaBundleFile;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Files;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.value.Value;
import com.femtioprocent.omega.xml.Element;
import org.hs.jfc.FormPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class OmegaAssetsProperty extends Property_B {

    static final int COL_MEDIA = 0;
    static final int COL_FOOBAR = 1;
    static final int COL_ACT1 = 2;
    static final int COL_ACT2 = 3;
    public static final String OMEGA_BUNDLE_MANIFEST = "bundle.omega_manifest";

    HashMap<String,JComponent> guimap = new HashMap<>();
    LessonContext l_ctxt;
    JFrame owner;

    JTable table;
    JButton set_act_b;
    JButton set_sgn_b;

    JRadioButton rb_def, rb_act;
    JRadioButton rb_defSign, rb_actSign;

    int[][] tmm;

    OmAssProp_TableModel tmod;

    TargetCombinations latestTargetCombinations;
    static TargetCombinations.Builder targetCombinationsBuilder = new TargetCombinations.Builder();
    private JButton oaBundleJB;
    private JButton imBundle;
    private JButton scanBundle;
    private JTextField infoTF;

    OmegaAssetsProperty(JFrame owner, LessonContext l_ctxt) {
        super(owner, T.t("Omega - Assets Bundle Dialog"));
        this.owner = owner;
        this.l_ctxt = l_ctxt;
        build(getContentPane());
        pack();
        setVisible(true);
    }

    void destroy() {
    }

    public void refresh() {
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Dimension d2 = new Dimension((int) d.getWidth() + 100, (int) d.getHeight());
        return d2;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        return d;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        return d;
    }


    class myActionListener implements ActionListener {
        void set(String what, String value) {
            JTextField tf = (JTextField) guimap.get(what);
            if (value == null) {
                String def = tf.getText();
                value = l_ctxt.getLessonCanvas().askForOneTarget(OmegaAssetsProperty.this, def);
                if (value == null)
                    value = def;

                // what to do ?
            }
        }

        public void actionPerformed(ActionEvent ev) {
            String s = ev.getActionCommand();

            if (s.equals("save bundle")) {
                ChooseOmegaBundleFile choose_f = new ChooseOmegaBundleFile();

                String url_s = null;
                int rv = choose_f.showDialog(ApplContext.top_frame, T.t("Save"));
                OmegaContext.sout_log.getLogger().info(":--: " + "choose file -> " + rv);
                if (rv == JFileChooser.APPROVE_OPTION) {
                    File file = choose_f.getSelectedFile();
                    if (!file.getName().endsWith(OmegaConfig.OMEGA_BUNDLE_EXTENSION))
                        file = new File(file.getPath() + OmegaConfig.OMEGA_BUNDLE_EXTENSION);
                    try {
                        java.util.List<String> manifest = new ArrayList<>();
                        for (TargetCombinations.TCItem s2 : targetCombinationsBuilder.asOne().getSrc_set()) {
                            String manifestInfo = manifestInfo(s2.getFn());
                            manifest.add(manifestInfo);
                        }
                        for (TargetCombinations.TCItem s2 : targetCombinationsBuilder.asOne().getDep_set()) {
                            String manifestInfo = manifestInfo(s2.getFn());
                            manifest.add(manifestInfo);
                        }

                        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
                        StringBuilder sb = new StringBuilder();
                        sb.append("type: omega-assets\n");
                        sb.append("version: " + OmegaVersion.getVersion() + "\n");
                        sb.append("saved: " + new Date() + "\n");
                        sb.append("user: " + System.getProperty("user.name") + "\n");
                        sb.append("info: " + infoTF.getText() + "\n");
                        for (String man : manifest)
                            sb.append(man + "\n");
                        putData(out, OMEGA_BUNDLE_MANIFEST, sb.toString());

                        for (TargetCombinations.TCItem s2 : targetCombinationsBuilder.asOne().getSrc_set()) {
                            put(out, s2.getFn());
                        }
                        for (TargetCombinations.TCItem s2 : targetCombinationsBuilder.asOne().getDep_set()) {
                            put(out, s2.getFn());
                        }

                        out.close();

                    } catch (IOException e) {
                    }
                }
            }

            if (s.equals("new bundle")) {
                targetCombinationsBuilder = new TargetCombinations.Builder();
//		targetCombinationsBuilder.add(latestTargetCombinations);
                latestTargetCombinations = targetCombinationsBuilder.asOne();
                tmod.update(latestTargetCombinations);
                oaBundleJB.setText(T.t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize());
            }

            if (s.equals("add bundle")) {
                targetCombinationsBuilder.add(latestTargetCombinations);
                latestTargetCombinations = targetCombinationsBuilder.asOne();
                tmod.update(latestTargetCombinations);
                oaBundleJB.setText(T.t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize());
            }

            if (s.equals("import bundle")) {
                importOmegaAssetsBundle(true);
                tmod.update(latestTargetCombinations);
                oaBundleJB.setText(T.t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize());
            }

            if (s.equals("view bundle")) {
                importOmegaAssetsBundle(false);
                tmod.update(latestTargetCombinations);
            }

            if (s.equals("scan add bundle")) {
                scanAddOmegaAssetsBundle();
                tmod.update(latestTargetCombinations);
                oaBundleJB.setText(T.t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize());
            }

            if (s.equals("close")) {
                setVisible(false);
            }
        }
    }

    private void scanAddOmegaAssetsBundle() {
        ChooseDir choose_f = new ChooseDir();

        int rv = choose_f.showDialog(ApplContext.top_frame, T.t("Scan"));
        OmegaContext.sout_log.getLogger().info(":--: " + "choose file -> " + rv);
        if (rv == JFileChooser.APPROVE_OPTION) {
            File dir = choose_f.getSelectedFile();

            java.util.List<File> list = new ArrayList<>();
            scanOmegaLessons(dir, list);

            Thread th = new Thread(() -> {
                try {
                    imBundle.setEnabled(false);
                    scanBundle.setEnabled(false);
                    for (File file : list) {
                        String url_s = Files.toURL(file);
                        String fn = Files.mkRelFname1(url_s);
                        OmegaContext.serr_log.getLogger().info("scanned: " + fn);
                        l_ctxt.getLesson().sendMsgWait("load", (String) fn);
                        SundryUtils.m_sleep(200);
                        latestTargetCombinations = l_ctxt.getLessonCanvas().getAllTargetCombinationsEx2(false);
                        latestTargetCombinations.getSrc_set().add(new TargetCombinations.TCItem(l_ctxt.getLesson().getLoadedFName()));

                        targetCombinationsBuilder.add(latestTargetCombinations);
                        latestTargetCombinations = targetCombinationsBuilder.asOne();
                        tmod.update(latestTargetCombinations);
                        oaBundleJB.setText(T.t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize());
                    }
                } finally {
                    imBundle.setEnabled(true);
                    scanBundle.setEnabled(true);
                }
            });
            th.start();
        }
    }

    private void scanOmegaLessons(File file, java.util.List<File> list) {
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory())
                scanOmegaLessons(f, list);
            if (f.getName().endsWith(".omega_lesson"))
                list.add(f);
        }
    }

    private String manifestInfo(String fName) {
        return manifestInfo(fName, null);
    }

    private String manifestInfo(String fName, Boolean exist) {
        File f = new File(OmegaContext.omegaAssets(fName));
        if (exist == null)
            if (f.exists() && f.canRead())
                return "entry: " + f.length() + ", " + fName;
            else
                return "entry: -1, " + fName;
        else
            return "entry: " + f.length() + ", " + fName;
    }

    private void importOmegaAssetsBundle(boolean unpack) {
        latestTargetCombinations = latestTargetCombinations == null || unpack ? new TargetCombinations() : latestTargetCombinations;

        ChooseOmegaBundleFile choose_f = new ChooseOmegaBundleFile();

        String url_s = null;
        int rv = choose_f.showDialog(ApplContext.top_frame, T.t(unpack ? "Import" : "List"));
        OmegaContext.sout_log.getLogger().info(":--: " + "choose file -> " + rv);
        if (rv == JFileChooser.APPROVE_OPTION) {
            File file = choose_f.getSelectedFile();
            if (!file.getName().endsWith(".omega_bundle"))

                // add dialog

                return;
            try {
                ZipInputStream in = new ZipInputStream(new FileInputStream(file));
                for (; ; ) {
                    ZipEntry zent = in.getNextEntry();
                    if (zent == null)
                        break;

                    String name = zent.getName();
                    if (zent.isDirectory()) {
                        File dir = new File(OmegaContext.omegaAssets(name));
                        if (unpack) {
                            if (dir.mkdirs()) {
                                OmegaContext.serr_log.getLogger().info("Created dir: T " + dir);
                            } else {
                                OmegaContext.serr_log.getLogger().info("Created dir: f " + dir);
                            }
                        }
                    } else {
                        try {
                            OmegaContext.serr_log.getLogger().info("Got: " + name + ' ' + OmegaContext.omegaAssets("."));
                            FileOutputStream output = null;
                            boolean obManifest = OMEGA_BUNDLE_MANIFEST.equals(name);
                            if (obManifest)
                                name = rmExt(file.getName()) + "-" + name;
                            File entFile = new File(OmegaContext.omegaAssets(name));
                            long time = zent.getTime();
                            try {
                                if (unpack) {
                                    if (!entFile.getParentFile().exists())
                                        entFile.getParentFile().mkdirs();
                                    if (entFile.exists()) {
                                        OmegaContext.serr_log.getLogger().info("Overwrite: exist " + entFile);
//                                    continue;
                                    }
                                }
                                output = unpack ? new FileOutputStream(entFile) : null;
                                int len = 0;
                                byte[] buffer = new byte[4096];
                                while ((len = in.read(buffer)) > 0) {
                                    if (unpack)
                                        output.write(buffer, 0, len);
                                    // hack
                                    if (obManifest) {
                                        String infoText = new String(buffer, 0, len);
                                        String sa[] = infoText.split("\n");
                                        for (String s : sa) {
                                            if (s.startsWith("info: "))
                                                infoTF.setText(s.substring(6));
                                        }
                                    }
                                }
                                if (name.endsWith(".omega_lesson")) {
                                    latestTargetCombinations.getSrc_set().add(new TargetCombinations.TCItem(name));
                                } else {
                                    if (!obManifest)
                                        latestTargetCombinations.getDep_set().add(new TargetCombinations.TCItem(name));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (output != null)
                                    output.close();
                            }
                            if (unpack)
                                entFile.setLastModified(time);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                in.close();

            } catch (IOException e) {
            }
        }
    }

    private String rmExt(String name) {
        int ix = name.lastIndexOf(".");
        if (ix != -1)
            return name.substring(0, ix);
        return name;
    }

    private void print(PrintWriter pw, String prfx, String s2) {
        File f = new File(OmegaContext.omegaAssets(s2));
        String stat = f.exists() && f.canRead() ? "OK" : "??";
        pw.println(prfx + ", " + stat + ", " + s2);
    }

    private void put(ZipOutputStream out, String s2) throws IOException {
        byte[] data = fileAsBytaArray(s2);
        if (data != null) {
            ZipEntry e = new ZipEntry(s2);
            File f = new File(OmegaContext.omegaAssets(s2));
            e.setTime(f.lastModified());
            out.putNextEntry(e);

            out.write(data, 0, data.length);
            out.closeEntry();
        }
    }

    private void putData(ZipOutputStream out, String name, String text) throws IOException {
        ZipEntry e = new ZipEntry(name);
        out.putNextEntry(e);
        out.write(text.getBytes("utf-8"));
        out.closeEntry();
    }


    private byte[] fileAsBytaArray(String fn) {
        File f = new File(OmegaContext.omegaAssets(fn));
        try {
            long fileSize = f.length();
            if (fileSize > Integer.MAX_VALUE) {
                fileSize = Integer.MAX_VALUE;
            }
            InputStream is = new FileInputStream(f);
            byte[] data = new byte[(int) fileSize];
            byte[] buf = new byte[1024];
            int pos = 0;
            for (; ; ) {
                int n = is.read(buf);
                if (n == -1)
                    break;
                System.arraycopy(buf, 0, data, pos, n);
                pos += n;
            }
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    OmegaAssetsProperty.myActionListener myactl = new OmegaAssetsProperty.myActionListener();

    // when item in table selected
    class MyListSelectionModel extends DefaultListSelectionModel implements ListSelectionListener {
        MyListSelectionModel() {
            addListSelectionListener(this);
        }

        public void valueChanged(ListSelectionEvent ev) {
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "" + ev);
            if (ev.getValueIsAdjusting() == false) {
                OmegaAssetsProperty.MyListSelectionModel lselmod_ = (OmegaAssetsProperty.MyListSelectionModel) ev.getSource();
                int ix = lselmod_.getMinSelectionIndex();
                if (ix >= 0) {
                    TableModel tmod = (TableModel) table.getModel();
                    String s = (String) tmod.getValueAt(ix, COL_ACT1);
                }
            }
        }
    }

    OmegaAssetsProperty.MyListSelectionModel lselmod = new OmegaAssetsProperty.MyListSelectionModel();


    class CloseAction extends AbstractAction {
        CloseAction() {
            super(T.t("Close"));
        }

        public void actionPerformed(ActionEvent ev) {
            setVisible(false);
        }
    }

    ;

    void build(Container con) {
        FormPanel fpan = new FormPanel(5, 5, 7, 15);

        //	JPanel pan1 = new JPanel();
        con.setLayout(new BorderLayout());

        JLabel jl;
        JTextField tf;
        JComboBox cb;
        JCheckBox ch;
        JButton jb;

        int Y = 0;
        int X = 0;

// 	fpan.add(new JLabel(T.t("Parameter:   ")), gbcf.createL(X++, Y, 1));
// 	fpan.add(new JLabel(T.t("Value:          ")),  gbcf.createL(X++, Y, 1));

// 	Y++;
// 	X = 0;
        fpan.add(jl = new JLabel(T.t("Info")), tf = new JTextField("", 40), Y, ++X);
        infoTF = tf;
        infoTF.setEditable(true);

        guimap.put("info", tf);
        tf.getDocument().addDocumentListener(mydocl);
        tf.setEnabled(true);

        fpan.add(new JLabel(""), jb = new JButton(T.t("New Omega Bundle")), Y, ++X);
        jb.setActionCommand("new bundle");
        jb.addActionListener(myactl);

        fpan.add(new JLabel(""), jb = new JButton(T.t("Add Omega Assets to Bundle") + " " + targetCombinationsBuilder.srcSize()), Y, ++X);
        jb.setActionCommand("add bundle");
        jb.addActionListener(myactl);
        oaBundleJB = jb;

        fpan.add(new JLabel(""), jb = new JButton(T.t("Scan and Add to Bundle")), Y, ++X);
        jb.setActionCommand("scan add bundle");
        jb.addActionListener(myactl);
        scanBundle = jb;

        Y++;
        X = 0;
        fpan.add(new JLabel(""), jb = new JButton(T.t("Save Omega Assets Bundle")), Y, ++X);
        jb.setActionCommand("save bundle");
        jb.addActionListener(myactl);

        fpan.add(new JLabel(""), jb = new JButton(T.t("View & Add Omega Bundle")), Y, ++X);
        jb.setActionCommand("view bundle");
        jb.addActionListener(myactl);

        fpan.add(new JLabel(""), jb = new JButton(T.t("Import Omega Assets Bundle")), Y, ++X);
        jb.setActionCommand("import bundle");
        jb.addActionListener(myactl);
        imBundle = jb;

        Y++;
        X = 0;
        fpan.add(new JLabel(""), new JLabel(T.t("(Shift) Click on the table header to (reverse) sort")), Y, ++X);

        Y++;
        X = 0;

        latestTargetCombinations = l_ctxt.getLessonCanvas().getAllTargetCombinationsEx2(false);
        latestTargetCombinations.getSrc_set().add(new TargetCombinations.TCItem(l_ctxt.getLesson().getLoadedFName()));
        tmod = new OmAssProp_TableModel(this, latestTargetCombinations, tmm);

        TableSorter tsort = new TableSorter(tmod);

        table = new JTable(tsort);
        tsort.addMouseListenerToHeaderInTable(table);

        JScrollPane jscr = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            TableColumn tcol = table.getColumnModel().getColumn(i);
            tcol.setPreferredWidth(i == 0 ? 350 :
                    i == 1 ? 350 :
                            i == 2 ? 60 :
                                    60);
        }
        try {
            table.setAutoResizeMode(table.AUTO_RESIZE_OFF);
            table.setSelectionModel(lselmod);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowSelectionInterval(0, 0);
            table.setPreferredScrollableViewportSize(new Dimension(830, 300));
        } catch (Exception ex) {
        }

        con.add(fpan, BorderLayout.NORTH);
        con.add(jscr, BorderLayout.CENTER);
        JPanel jpa = new JPanel();
        jpa.add(jb = new JButton(new OmegaAssetsProperty.CloseAction()));
        con.add(jpa, BorderLayout.SOUTH);

        oaBundleJB.setText(T.t("Import Omega Bundle") + " " + targetCombinationsBuilder.srcSize());
    }

    void updTrigger(Document doc) {
        Iterator it = guimap.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object o = guimap.get(key);
            if (o instanceof JTextField) {
                JTextField tf = (JTextField) o;
                if (doc == tf.getDocument()) {
                    String txt = tf.getText();
                    fireValueChanged(new Value(key, txt));
                }
            }
        }
    }

    void setLabel(String id, String txt) {
        Iterator it = guimap.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.equals(id)) {
                Object o = guimap.get(key);
                if (o instanceof JLabel) {
                    JLabel jl = (JLabel) o;
                    jl.setText(txt);
                }
            }
        }
    }

    void updTrigger(JComboBox cb) {
        try {
            JComboBox cbg;

//  	    cbg = (JComboBox)guimap.get("type");
//  	    if ( cb == cbg ) {
//  		String s = (String)cb.getSelectedItem();
//  		OmegaContext.sout_log.getLogger().info(":--: " + "CB type " + cb);
//  		if ( s.equals("action") )
//  		    setLabel("Llid", "Path id");
//  		if ( s.equals("actor") )
//    		    setLabel("Llid", "Path id");
//  		else
//  		    setLabel("Llid", "-");
//  	    }

            cbg = (JComboBox) guimap.get("Slid");
            if (cb == cbg) {
                JTextField tf = (JTextField) guimap.get("lid");
                updTF(tf, cbg);
            }

        } catch (ClassCastException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "CCE " + ex);
        }
    }

    void updTrigger(JCheckBox ch) {
        try {
            JCheckBox chg;

            chg = (JCheckBox) guimap.get("Slid");
        } catch (ClassCastException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "CCE " + ex);
        }
    }

    public Element getElement() {
        Element el = new Element("test_prop");

        Element pel = new Element("test");
        pel.addAttr("kind", "pre");
        pel.addAttr("ord", "1");
        pel.addAttr("text", ((JTextField) (guimap.get("pret1"))).getText());
        el.add(pel);

        pel = new Element("test");
        pel.addAttr("kind", "pre");
        pel.addAttr("ord", "2");
        pel.addAttr("text", ((JTextField) (guimap.get("pret2"))).getText());
        el.add(pel);

        pel = new Element("test");
        pel.addAttr("kind", "post");
        pel.addAttr("ord", "1");
        pel.addAttr("text", ((JTextField) (guimap.get("postt1"))).getText());
        el.add(pel);

        pel = new Element("test");
        pel.addAttr("kind", "post");
        pel.addAttr("ord", "2");
        pel.addAttr("text", ((JTextField) (guimap.get("postt2"))).getText());
        el.add(pel);

        return el;
    }
}
