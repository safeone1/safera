# Network Traffic Monitor with ML-based Anomaly Detection

A Java-based network traffic monitoring application that captures and analyzes network packets in real-time, featuring machine learning-based anomaly detection for potential security threats.

## Features

- Real-time network packet capture
- Detailed packet analysis and display
- Protocol-based filtering
- Content-based search
- Machine learning-based anomaly detection
- Interactive GUI with JavaFX
- Packet hex stream visualization
- Traffic pattern analysis

## Prerequisites

- Java 11 or higher
- Maven
- Npcap (Windows) or libpcap (Linux/Mac)
- Administrator/root privileges for packet capture

## Dependencies

- JavaFX - GUI framework
- Pcap4J - Packet capture library
- Weka - Machine learning library
- SQLite - Local database storage
- Apache Commons CSV - Data export functionality

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/network-traffic-monitor.git
```

2. Install Npcap (Windows) or libpcap (Linux/Mac)
   - Windows: Download and install Npcap from [npcap.org](https://npcap.org/)
   - Linux: `sudo apt-get install libpcap-dev`
   - Mac: `brew install libpcap`

3. Build the project:

```bash
mvn clean install
```

## Running the Application

1. Run with Maven:

```bash
mvn javafx:run
```

## Usage

1. **Interface Selection**
   - Launch the application
   - Select a network interface from the list

2. **Packet Capture**
   - Click "Start Capture" to begin monitoring
   - View real-time packet information in the table
   - Click "Stop Capture" to end monitoring

3. **Search and Filter**
   - Use the protocol search field to filter by protocol (e.g., TCP, UDP)
   - Use the word search field to filter by packet content

4. **Anomaly Detection**
   - Capture normal traffic patterns
   - Click "Train Model" to train the ML model
   - Click "Analyze Traffic" to detect anomalies
   - Suspicious packets will be highlighted in red

## Project Structure

```
java-ids/
├── src
├── main
│   ├── java
│   │   └── com
│   │       └── ids
│   │           ├── App.java
│   │           ├── component
│   │           │   └── Toast.java
│   │           ├── controller
│   │           │   ├── InterfaceSelectionController.java
│   │           │   ├── PacketCaptureController.java
│   │           │   └── TrafficAnalysisController.java
│   │           ├── model
│   │           │   ├── ConnectionInfo.java
│   │           │   ├── IpTrafficInfo.java
│   │           │   ├── NetworkInterfaceInfo.java
│   │           │   ├── PacketFeatures.java
│   │           │   ├── PacketInfo.java
│   │           │   ├── TrafficStatistics.java
│   │           │   └── network_traffic_structure.arff
│   │           ├── repository
│   │           │   └── PacketInfoRepository.java
│   │           └── service
│   │               ├── AnomalyDetector.java
│   │               ├── NetworkCaptureService.java
│   │               └── PacketPreprocessor.java
│   └── resources
│       └── com
│           └── ids
│               ├── application.css
│               ├── common.css
│               ├── interface-selection.css
│               ├── interface-selection.fxml
│               ├── packet-capture.fxml
│               └── traffic-analysis.fxml
|
└── 15 directories, 22 files
```

## Machine Learning Features

The application uses the Weka machine learning library to:

- Process network traffic patterns
- Train a Random Forest classifier
- Detect anomalous network behavior
- Highlight potential security threats

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Pcap4J for packet capture capabilities
- Weka for machine learning functionality
- JavaFX for the GUI framework