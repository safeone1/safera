package com.ids.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.ids.component.Toast;
import com.ids.model.NetworkInterfaceInfo;
import com.ids.model.PacketInfo;
import com.ids.repository.PacketInfoRepository;
import com.ids.service.ApiService;
import com.ids.service.NetworkCaptureService;
import com.ids.util.JsonUtils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class PacketCaptureController {
    @FXML
    private Label selectedInterfaceLabel;
    @FXML
    private Button startCaptureButton;
    @FXML
    private Button stopCaptureButton;
    @FXML
    private TableView<PacketInfo> packetTableView;
    @FXML
    private TableColumn<PacketInfo, String> sourceIpColumn;
    @FXML
    private TableColumn<PacketInfo, String> lengthColumn;
    @FXML
    private TableColumn<PacketInfo, String> destIpColumn;
    @FXML
    private TableColumn<PacketInfo, String> protocolColumn;
    @FXML
    private TableColumn<PacketInfo, LocalDateTime> timestampColumn;
    @FXML
    private TableColumn<PacketInfo, String> flagsColumn;
    @FXML
    private TableColumn<PacketInfo, String> totalBytesColumn;
    @FXML
    private TableColumn<PacketInfo, String> retransmissionColumn;
    @FXML
    private TableColumn<PacketInfo, String> connectionCountColumn;
    @FXML
    private TableColumn<PacketInfo, String> flowDurationColumn;

    @FXML
    private TextArea packetDetailsArea;

    @FXML
    private TextField protocolSearchField;
    @FXML
    private TextField wordSearchField;

    private NetworkInterfaceInfo networkInterface;
    private NetworkCaptureService captureService;
    private ApiService apiService;
    private PacketInfoRepository repository;
    private List<PacketInfo> capturedPackets;

    public void setNetworkInterface(NetworkInterfaceInfo networkInterface) {
        this.networkInterface = networkInterface;
        this.selectedInterfaceLabel.setText("Selected Interface: " + networkInterface.toString());
        this.captureService = new NetworkCaptureService();
        this.apiService = new ApiService();
    }

    @FXML
    public void initialize() {
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        sourceIpColumn.setCellValueFactory(new PropertyValueFactory<>("sourceIp"));
        destIpColumn.setCellValueFactory(new PropertyValueFactory<>("destinationIp"));
        protocolColumn.setCellValueFactory(new PropertyValueFactory<>("protocol"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("packetLength"));
        flagsColumn.setCellValueFactory(new PropertyValueFactory<>("flags"));
        totalBytesColumn.setCellValueFactory(new PropertyValueFactory<>("totalBytes"));
        retransmissionColumn.setCellValueFactory(new PropertyValueFactory<>("retransmissionCount"));
        connectionCountColumn.setCellValueFactory(new PropertyValueFactory<>("connectionCount"));
        flowDurationColumn.setCellValueFactory(new PropertyValueFactory<>("flowDuration"));

        packetTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                packetDetailsArea.setText(
                        String.format("Flags: %d\nTotal Bytes: %d\nRetransmissions: %d\n" +
                                "Connections: %d\nFlow Duration: %s",
                                newSelection.getFlags(),
                                newSelection.getTotalBytes(),
                                newSelection.getRetransmissionCount(),
                                newSelection.getConnectionCount(),
                                newSelection.getFlowDuration()));
            }
        });

        packetTableView.setRowFactory(tv -> new TableRow<>());
    }

    @FXML
    public void startCapture() {

        try {
            captureService.startCapture(networkInterface.getNetworkInterface());
            startCaptureButton.setDisable(true);
            stopCaptureButton.setDisable(false);
        } catch (Exception e) {
            showError("Failed to start capture: " + e.getMessage());
        }
    }

    @FXML
    public void searchByProtocol() {
        if (repository == null) {
            repository = new PacketInfoRepository();
        }

        String protocol = protocolSearchField.getText().trim();
        String searchWord = wordSearchField.getText().trim();

        if (protocol.isEmpty()) {
            Toast.showToast((StackPane) startCaptureButton.getScene().getRoot(),
                    "Please enter a protocol to search.", 3000);
            return;
        }

        try {
            List<PacketInfo> filteredPackets;

            if (!searchWord.isEmpty()) {
                filteredPackets = repository.getPacketsByProtocolAndData(protocol, searchWord);
            } else {
                filteredPackets = repository.getPacketsByProtocol(protocol);
            }

            Platform.runLater(() -> {
                packetTableView.getItems().clear();
                packetTableView.getItems().addAll(filteredPackets);
            });

            if (filteredPackets.isEmpty()) {
                Toast.showToast((StackPane) startCaptureButton.getScene().getRoot(),
                        "No packets found for protocol: " + protocol +
                                (searchWord.isEmpty() ? "" : " containing word: " + searchWord),
                        3000);
            }
        } catch (Exception e) {
            Toast.showToast((StackPane) startCaptureButton.getScene().getRoot(),
                    "Error fetching packets: " + e.getMessage(), 3000);
        }
    }

    @FXML
    public void stopCapture() throws Exception {
        try {
            if (captureService != null) {
                captureService.stopCapture();
            }

            if (repository == null) {
                repository = new PacketInfoRepository();
            }

            // Fetch and store captured packets
            capturedPackets = repository.getAllPackets();

            String capturedPacketsJson = JsonUtils.toPrettyJson(capturedPackets);

            apiService.sendPostRequest("http://172.20.57.147:3000", capturedPacketsJson)
                    .thenAcceptAsync(response -> {

                        // Handle the API response if needed
                        System.out.println("API Response: " + response);

                        Platform.runLater(() -> {
                            packetTableView.getItems().clear();
                            packetTableView.getItems().addAll(capturedPackets);
                            startCaptureButton.setDisable(false);
                            stopCaptureButton.setDisable(true);
                        });
                    }).exceptionally(throwable -> {
                        Platform.runLater(() -> showError("Failed to send data to API: " + throwable.getMessage()));
                        return null;
                    });

        } catch (Exception e) {
            System.out.println(e.getMessage());
            showError("Failed to stop capture and retrieve packets: " + e.getMessage());
        }
    }

    @FXML
    public void goBack() {
        try {
            if (captureService != null) {
                captureService.stopCapture();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ids/interface-selection.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) startCaptureButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Error returning to interface selection: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }
}