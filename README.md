# Network Traffic Monitor with ML-based Anomaly Detection

A Java-based network traffic monitoring application that captures and analyzes network packets in real-time, featuring machine learning-based anomaly detection for potential security threats.

## Features

- Real-time network packet capture
- Detailed packet analysis and display
- Protocol-based filtering
- Content-based search
- Interactive GUI with JavaFX

## Prerequisites

- Java 11 or higher
- Maven
- Npcap (Windows) or libpcap (Linux/Mac)
- Administrator/root privileges for packet capture

## Dependencies

- JavaFX - GUI framework
- Pcap4J - Packet capture library
- SQLite - Local database storage

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
│   │           │   └── PacketCaptureController.java
│   │           ├── model
│   │           │   ├── NetworkInterfaceInfo.java
│   │           │   └── PacketInfo.java
│   │           ├── repository
│   │           │   └── PacketInfoRepository.java
│   │           └── service
│   │               └── NetworkCaptureService.java
│   └── resources
│       └── com
│           └── ids
│               ├── application.css
│               ├── common.css
│               ├── interface-selection.css
│               ├── interface-selection.fxml
│               └── packet-capture.fxml
```

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
- JavaFX for the GUI framework
