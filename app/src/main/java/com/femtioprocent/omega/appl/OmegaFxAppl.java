package com.femtioprocent.omega.appl;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;
import de.codecentric.centerdevice.MenuToolkit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class OmegaFxAppl extends Application {

    static String[] args;

    public OmegaFxAppl() {
    }

    public static void main(String[] args) {
        OmegaFxAppl.args = args;
        launch(args);
    }

    public void start2(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(event -> Log.getLogger().info("Hello World!"));
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    @Override
    public void start(Stage stage) {
        Platform.setImplicitExit(false);

        MenuToolkit tk = MenuToolkit.toolkit();

        MenuBar bar = new MenuBar();

        String appName = "Omega IS";
        // Application Menu
        // TBD: services menu
        Menu appMenu = new Menu(appName); // Name for appMenu can't be dep_set at
        // LessonRuntimeAppl
        MenuItem aboutItem = tk.createAboutMenuItem(appName);
        MenuItem prefsItem = new MenuItem("Preferences...");
        prefsItem.setOnAction(event -> Log.getLogger().info("prefs clicked"));
        appMenu.getItems().addAll(aboutItem, new SeparatorMenuItem(), prefsItem, new SeparatorMenuItem(),
                tk.createHideMenuItem(appName), tk.createHideOthersMenuItem(), tk.createUnhideAllMenuItem(),
                new SeparatorMenuItem(), tk.createQuitMenuItem(appName));

        // File Menu (items TBD)
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New...");
        fileMenu.getItems().addAll(newItem, new SeparatorMenuItem(), tk.createCloseWindowMenuItem(),
                new SeparatorMenuItem(), new MenuItem("TBD"));

        // Edit (items TBD)
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(new MenuItem("TBD"));

        // Format (items TBD)
        Menu formatMenu = new Menu("Format");
        formatMenu.getItems().addAll(new MenuItem("TBD"));

        // View Menu (items TBD)
        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(new MenuItem("TBD"));

        // Window Menu
        // TBD standard window menu items
        Menu windowMenu = new Menu("Window");
        windowMenu.getItems().addAll(tk.createMinimizeMenuItem(), tk.createZoomMenuItem(), tk.createCycleWindowsItem(),
                new SeparatorMenuItem(), tk.createBringAllToFrontItem());

        // Help Menu (items TBD)
        Menu helpMenu = new Menu("Help");
        helpMenu.getItems().addAll(new MenuItem("TBD"));

        bar.getMenus().addAll(appMenu, fileMenu, editMenu, formatMenu, viewMenu, windowMenu, helpMenu);

        tk.autoAddWindowMenuItems(windowMenu);
        tk.setGlobalMenuBar(bar);
        tk.setGlobalMenuBar(bar);

        Platform.runLater(() -> bar.setUseSystemMenuBar(true));

        int scW = 1000;
        int scH = 1000;
        int ww = 700;
        int hh = 330 + 22 * 3;
        int xx = (scW - ww) / 2;
        int yy = (scH - hh) / 2;

        Circle circ = new Circle(40, 40, 30);
        Group root = new Group(circ);
        Scene scene = new Scene(root, 700, 330 + 22 * 3);
        stage.setTitle("Omega IS");

        String aImname = OmegaContext.getMediaFile("default/omega_splash.gif");
        Image im = new Image("file:" + aImname);
        ImageView imView = new ImageView();
        imView.setImage(im);
        imView.setX(4);
        imView.setY(4);
        root.getChildren().addAll(imView);
        root.getChildren().add(bar);

        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.show();
        Log.getLogger().info("started");
        new Thread(() -> {
            SundryUtils.m_sleep(500);
            Platform.runLater(() -> {
//	        stage.setIconified(true);
                stage.hide();
                new Thread(() -> {
                    SundryUtils.m_sleep(500);
                    LessonEditorAppl.main(args);
                    Platform.runLater(() -> stage.show());
                    SundryUtils.m_sleep(500);
                }).start();
            });
        }).start();

    }

    public void stop() {
        Log.getLogger().info("stop");
//	Platform.exit();
    }

//    public void paint(Graphics g) {
//	g.drawImage(im, 0, 0, null);
//	g.setColor(Color.yellow);
//	g.drawString(OmegaVersion.getVersion(), 5, 12);
//	g.drawString(OmegaVersion.getCWD(), 5, 322 + 20 * 0);
//	g.drawString(OmegaVersion.getJavaVersion(), 5, 322 + 20 * 1);
//	g.drawString(OmegaVersion.getJavaVendor(), 5, 322 + 20 * 2);
//	g.drawString(OmegaVersion.getJavaHome(), 5, 322 + 20 * 3);
//    }
}
