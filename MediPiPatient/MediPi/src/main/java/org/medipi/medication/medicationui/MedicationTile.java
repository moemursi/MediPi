package org.medipi.medication.medicationui;

import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.medipi.medication.Schedule;
import org.medipi.ui.Tile;

import java.util.HashMap;
import java.util.Random;

public class MedicationTile extends Tile {

    Label titleLabel;
    Label purposeLabel;
    Label dueStatusLabel;
    AdherenceBar adherenceBar;
    public enum DisplayType {ADHERENCE, DUESTATUS};
    public enum DueStatus {TAKENOW, ASNEEDED, NOTNOW};
    public static final HashMap<DueStatus, String> dueStatusStyles = new HashMap<>();
    public static final HashMap<DueStatus, String> dueStatusStrings = new HashMap<>();
    static {
        dueStatusStyles.put(DueStatus.TAKENOW, "button-advised");
        dueStatusStyles.put(DueStatus.ASNEEDED, "");
        dueStatusStyles.put(DueStatus.NOTNOW, "button-caution");
        dueStatusStrings.put(DueStatus.TAKENOW, "Take Now");
        dueStatusStrings.put(DueStatus.ASNEEDED, "Take as Needed");
        dueStatusStrings.put(DueStatus.NOTNOW, "Not Due");
    }

    public DueStatus getDueStatus() {
        return dueStatus;
    }

    public void setDueStatus(DueStatus dueStatus) {
        if (this.dueStatus != null) {
            content.getStyleClass().remove(dueStatusStyles.get(dueStatus));
            dueStatusLabel.setText("");
        }
        this.dueStatus = dueStatus;
        content.getStyleClass().add(dueStatusStyles.get(dueStatus));
        dueStatusLabel.setText(dueStatusStrings.get(dueStatus));
    }

    private DueStatus dueStatus = null;

    public DisplayType getDisplayType() {
        return displayType;
    }

    public void setDisplayType(DisplayType displayType) {
        this.displayType = displayType;
        updateLayout();
    }

    DisplayType displayType = DisplayType.ADHERENCE;

    public Schedule getMedicationSchedule() {
        return medicationSchedule;
    }

    Schedule medicationSchedule;

    public MedicationTile(BooleanProperty bprop, int widthUnits, int heightUnits, Schedule medicationSchedule) {
        super(bprop, widthUnits, heightUnits);

        content.setId("mainwindow-dashboard-component");
        content.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));

        content.setPadding(new Insets(5, 5, 5, 5));

        VBox vbox = new VBox();
        vbox.setFillWidth(true);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(0, 10, 0, 10));
        content.setTop(vbox);
        content.setAlignment(vbox, Pos.CENTER_LEFT);

        titleLabel = new Label(medicationSchedule.determineDisplayName());
        purposeLabel = new Label(medicationSchedule.getPurposeStatement());
        dueStatusLabel = new Label();


        //titleLabel.setPrefSize(content.getWidth(), 30);
        titleLabel.setId("mainwindow-dashboard-component-title");
        adherenceBar = new AdherenceBar();
        adherenceBar.setWidth(200);
        vbox.getChildren().add(titleLabel);
        vbox.getChildren().add(purposeLabel);
        updateLayout();

        setMedicationSchedule(medicationSchedule);
        setShowInfoOnClick();
    }

    private void updateLayout() {
        content.setCenter(null);
        content.setBottom(null);
        if (displayType == DisplayType.ADHERENCE) {
            setAdherenceView();
        } else if (displayType == DisplayType.DUESTATUS) {
            setDoseStatusView();
            content.setBottom(dueStatusLabel);
            content.setAlignment(dueStatusLabel, Pos.CENTER);
            dueStatusLabel.setPadding(new Insets(5));
        }
    }

    private void setAdherenceView() {

        content.setCenter(adherenceBar);
        content.setAlignment(adherenceBar, Pos.CENTER);
    }

    private void setDoseStatusView() {
        purposeLabel = new Label("No purpose set");
        //purposeLabel.setPrefSize(content.getWidth(), 30);

    }

    private void setDoseStatus(DueStatus status) {
        dueStatus = status;
    }

    private DueStatus getDoseStatus() {
        return dueStatus;
    }

    public void setMedicationSchedule(Schedule schedule) {
        adherenceBar.setProgress(new Random().nextDouble() % 1);
        adherenceBar.setStreakLength((int) ((new Random().nextDouble() % 1)*20));
        this.medicationSchedule = schedule;
    }

    public void setShowInfoOnClick() {
        this.setOnTileClick((MouseEvent event) -> {
            Stage popupWindow = new Stage();
            MedicationInformation popupContents = new MedicationInformation(medicationSchedule);
            Scene scene = new Scene(popupContents);
            scene.getStylesheets().addAll(content.getScene().getStylesheets());
            popupWindow.setScene(scene);
            popupWindow.setTitle("Medication Information");
            popupWindow.initModality(Modality.WINDOW_MODAL);
            popupWindow.initOwner(content.getScene().getWindow());
            popupWindow.showAndWait();
        });
    }

}
