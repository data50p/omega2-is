package com.femtioprocent.omega.media.video;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.canvas.BaseCanvas;
import com.femtioprocent.omega.lesson.canvas.MsgItem;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

public class FxMoviePlayer {

    Scene scene;
    Group root;
    static JFrame frame;
    JComponent jcomp;
    MediaPlayer player = null;
    boolean initDone = false;
    boolean stopped = false;
    JFXPanel fxPanel = null;

    public int mediaW;
    public int mediaH;

    private static final String MEDIA_FN = OmegaContext.getMediaFile("feedback/film1/feedback1.mp4");
    private static final String MEDIA_FN2 = OmegaContext.getMediaFile("feedback/film1/feedback2.mp4");

    boolean ready = false;
    int winW;
    int winH;
    public boolean messageShown;


    FxMoviePlayer(int winW, int winH) {
        this.winW = winW;
        this.winH = winH;
    }

    private JFXPanel initGUI() {
        stopped = false;
        initDone = false;
        // This method is invoked on the EDT thread
        JFrame frame = new JFrame("Swing and JavaFX");
        JComponent jcomp = new JPanel();
        jcomp.setLayout(new BorderLayout(0, 0));
        frame.add(jcomp, BorderLayout.CENTER);//setContentPane(jcomp);
        frame.setSize(800, 600);
        frame.setVisible(true);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        return initGUI(jcomp, MEDIA_FN);
    }

    public JFXPanel initGUI(JComponent jcomp, String fn) {
        Log.getLogger().info("enter initGUI " + Platform.isFxApplicationThread());
        // This method is invoked on the EDT thread
        this.jcomp = jcomp;
        boolean snd = true;

        if (fxPanel == null) {
            fxPanel = new JFXPanel();
            fxPanel.setSize(291, 251);
            fxPanel.setLocation(62, 72);
            jcomp.add(fxPanel);//, BorderLayout.CENTER);
            snd = false;
            //jcomp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        final boolean snd_ = snd;

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.getLogger().info("runLater: 100");
                    if (snd_) {
                        initFX2(fxPanel, fn);
                    } else {
                        initFX(fxPanel, fn);
                        initFX2(fxPanel, fn);
                    }
                    Log.getLogger().info("runLater: play()...");
                    player.play();
                    Log.getLogger().info("runLater: ...play()");
                } catch (MediaException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                initDone = true;
            }
        });

        Log.getLogger().info("leave initGUI");
        return fxPanel;
    }

    public void waitReady() {
        while (!ready)
            SundryUtils.m_sleep(100);

    }

    private void initFX(JFXPanel fxPanel, String fn) throws URISyntaxException {
        Log.getLogger().info("enter initFX FxAppThread => " + Platform.isFxApplicationThread());
        // This method is invoked on the JavaFX thread
        this.root = new Group();
        Scene scene = new Scene(this.root, winW, winH, new Color(0.24, 0.44, 0.84, 0.184));
        this.scene = scene;
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                Log.getLogger().info("Mouse pressed");
                messageShown = false;
            }
        });

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                Log.getLogger().info("Key Pressed: " + ke.getText() + ' ' + ke.getCode());
                //reset();
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                Log.getLogger().info("Key Released: " + ke.getText() + ' ' + ke.getCode());
            }
        });
        fxPanel.setScene(scene);
    }

    private void initFX2(JFXPanel fxPanel, String fn_) throws URISyntaxException {
        String fn = OmegaContext.omegaAssets(fn_);
        File file = new File(fn);
        String uu = file.toURI().toString();
        Log.getLogger().info("UU is " + uu);

        Class<? extends FxMoviePlayer> aClass = getClass();
        Log.getLogger().info("aClass is " + aClass);
        URL resource = null;//aClass.getResource(MEDIA0_URL);
        try {
            resource = file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.getLogger().info("resource is " + resource);
        String u = resource.toURI().toString();
        Log.getLogger().info("U is " + u);

        player = new MediaPlayer(new Media(uu));
        MediaView mediaView = new MediaView(player);
        mediaView.setX(0);
        mediaView.setY(0);
        //root.getChildren().clear();
        root.getChildren().add(mediaView);

        player.setOnReady(new Runnable() {
            @Override
            public void run() {
                mediaW = player.getMedia().getWidth();
                mediaH = player.getMedia().getHeight();

                double scal = 1.6;
                double xx = (winW - mediaW) / 2.0;
                double yy = (winH - mediaH) / 2.0;
                mediaView.setScaleX(scal);
                mediaView.setScaleY(scal);
                mediaView.setTranslateX(xx);
                mediaView.setTranslateY(yy);

                Log.getLogger().info("---++-- win: " + winW + ' ' + winH + " media: " + mediaW + ' ' + mediaH + " translate: " + xx + ' ' + yy);
                Log.getLogger().info("VP " + mediaView.getX());
                ready = true;
//		player.play();
            }
        });

        player.setOnEndOfMedia(() -> {
            Log.getLogger().info("EOF ");
            stopped = true;
//            player.dispose();
        });

        Log.getLogger().info("leave initFX");
    }

    public void play() {
        if (true)
            return;
        for (int i = 0; i < 100; i++)
            if (initDone)
                break;
            else
                SundryUtils.m_sleep(100);

        if (player != null) {
            Log.getLogger().info("Play the movie...");
            player.play();
        }
    }

    public void reset() {
        if (player != null) {
            stopped = false;
            player.seek(player.getStartTime());
            player.play();
        }
    }

    public void dispose() {
        if (player != null) {
            MediaPlayer mp = player;
//	    Platform.runLater(() -> {
//	        mp.stop();
//	        mp.dispose();
//	    });
            player = null;
        }
    }

    public void wait4done() {
        while (stopped == false)
            SundryUtils.m_sleep(200);
    }


    public void start(Stage primaryStage) {
        Log.getLogger().info("Java Home: " + System.getProperty("java.home"));
        Log.getLogger().info("User Home: " + System.getProperty("user.home"));
        Log.getLogger().info("User dir: " + System.getProperty("user.dir"));

        (new Thread(() -> initGUI())).start();
        while (stopped == false)
            SundryUtils.m_sleep(200);

        (new Thread(() -> initGUI(jcomp, MEDIA_FN2))).start();
        while (stopped == false)
            SundryUtils.m_sleep(200);

        dispose();
    }

    public static void main(String[] args) {
        FxMoviePlayer fxp = new FxMoviePlayer(800, 600);
        fxp.start(null);
    }

    private Color getColor(HashMap<String, BaseCanvas.ColorColors> colors, String key, java.awt.Color def) {
        java.awt.Color c = colors != null ? colors.get(key).color() : def;
        return Color.rgb(c.getRed(), c.getGreen(), c.getBlue());
    }

    public void hideMsg(boolean hide) {
        msgItems.hide(hide);
    }

    private static class MsgItems {
        Text text;
        Rectangle rect;

        public void hide(boolean hide) {
            if ( text != null ) {
                text.setVisible(!hide);
            }
            if ( rect != null ) {
                rect.setVisible(!hide);
            }
        }
    }

    MsgItems msgItems = new MsgItems();

    public void showMsg(MsgItem mi, int width, int height, HashMap<String, BaseCanvas.ColorColors> colors) {
        Platform.runLater(() -> {
            double fontH = height * 0.037;
            msgItems.text = new Text(mi.text);
            msgItems.text.setFont(new Font(fontH));
            double textW = msgItems.text.getLayoutBounds().getWidth();
            double textH = msgItems.text.getLayoutBounds().getHeight();

            double w_ = textW + 10 + width * 0.03;
            double x = width * 0.5 - w_ / 2;
            double y = height * 0.88;
            double w = w_;
            double h = height * 0.06;
            double r = width * 0.02;

            double tx = x + w / 2 - textW / 2;
            double ty = y + h - (2 * fontH) / 5;
            double sw = height / 200.0;

            DropShadow ds = new DropShadow();
            ds.setOffsetX(sw);
            ds.setOffsetY(sw);
            ds.setColor(Color.GRAY);

            msgItems.rect = new Rectangle(x, y, w, h);
            msgItems.rect.setEffect(ds);
            msgItems.rect.setArcWidth(r);
            msgItems.rect.setArcHeight(r);
            msgItems.rect.setStrokeWidth(sw);
            msgItems.rect.setStroke(getColor(colors, "sn_fr", java.awt.Color.black));
            msgItems.rect.setFill(getColor(colors, "sn_bg", java.awt.Color.white));

            msgItems.text.setX(tx);
            msgItems.text.setY(ty);
            msgItems.text.setFill(getColor(colors, "sn_tx", java.awt.Color.black));

            root.getChildren().addAll(msgItems.rect, msgItems.text);

            if (OmegaConfig.isDebug()) {
                plot(root, x, y, w, h, Color.RED);
                plot(root, tx, ty, textW, -textH, Color.ORANGE);
                plot(root, x-30, y, sw, sw, Color.BLUE);
            }
        });
    }

    private void plot(Group g, double x, double y, double w, double h, Color col) {
        g.getChildren().add(plotXY(x, y, col));
        g.getChildren().add(plotWH(x, y, w, h, col));
    }

    private Node plotXY(double x, double y, Color col) {
        return plotIt(x, y,
                -10, -10,
                10, 10,
                col);
    }

    private Node plotWH(double x, double y, double w, double h, Color col) {
        return plotIt(x + w, y + h,
                -20 * Math.signum(w), -20 * Math.signum(h),
                0 * Math.signum(w), 0 * Math.signum(h),
                col);
    }

    private Node plotIt(double x, double y, double ax, double ay, double a2x, double a2y, Color col) {
        Line n1 = new Line(x+ax, y, x+a2x, y);
        Line n2 = new Line(x, y+ay, x, y+a2y);
        Line n3 = new Line(x+ax, y, x+a2x, y);
        Line n4 = new Line(x, y+ay, x, y+a2y);
        n1.setStroke(col);
        n2.setStroke(col);
        n1.setStrokeWidth(1.0);
        n2.setStrokeWidth(1.0);
        n3.setStroke(Color.BLACK);
        n4.setStroke(Color.BLACK);
        n3.setStrokeWidth(3.0);
        n4.setStrokeWidth(3.0);
        return new Group(n3, n4, n1, n2);
    }
}
