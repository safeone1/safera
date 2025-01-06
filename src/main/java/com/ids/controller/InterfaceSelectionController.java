package com.ids.controller;

import com.ids.model.NetworkInterfaceInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class InterfaceSelectionController {
    @FXML
    private ListView<NetworkInterfaceInfo> interfaceListView;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        loadNetworkInterfaces();
        setupInterfaceSelection();
    }

    private void loadNetworkInterfaces() {
        try {
            List<NetworkInterfaceInfo> interfaces = Pcaps.findAllDevs().stream()
                    .map(NetworkInterfaceInfo::new)
                    .collect(Collectors.toList());

            interfaceListView.getItems().addAll(interfaces);
        } catch (PcapNativeException e) {
            statusLabel.setText("Error loading network interfaces: " + e.getMessage());
        }
    }

    private void setupInterfaceSelection() {
        interfaceListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                NetworkInterfaceInfo selectedInterface = interfaceListView.getSelectionModel().getSelectedItem();
                if (selectedInterface != null) {
                    openPacketCaptureView(selectedInterface);
                }
            }
        });
    }

    private void openPacketCaptureView(NetworkInterfaceInfo networkInterface) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ids/packet-capture.fxml"));
            Parent root = loader.load();

            PacketCaptureController controller = loader.getController();
            controller.setNetworkInterface(networkInterface);

            Stage stage = (Stage) interfaceListView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            statusLabel.setText("Error opening packet capture view: " + e.getMessage());
        }
    }
}