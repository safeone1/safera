package com.ids.service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.TcpPacket.TcpHeader;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.util.NifSelector;

import com.ids.model.PacketInfo;
import com.ids.repository.PacketInfoRepository;

public class NetworkCaptureService {

    // Add these fields at class level
    private Map<String, Long> lastSeqNumbers = new HashMap<>();
    private Map<String, LocalDateTime> flowStartTimes = new HashMap<>();
    private Map<String, Long> totalBytes = new HashMap<>();
    private Map<String, Integer> connectionCount = new HashMap<>();

    private PcapNetworkInterface nif;
    private PcapHandle handle;
    private final PacketInfoRepository repository;
    private boolean isCapturing = false;
    private ExecutorService executorService;

    public NetworkCaptureService() {
        this.repository = new PacketInfoRepository();
        // Initialize packet_info table
        repository.initializeTable();
    }

    public void selectNetworkInterface() throws IOException, PcapNativeException {
        nif = new NifSelector().selectNetworkInterface();
        if (nif == null) {
            throw new RuntimeException("No network interface selected");
        }
    }

    public void startCapture(PcapNetworkInterface networkInterface) throws PcapNativeException {
        repository.truncatePacketInfoTable();

        if (networkInterface == null) {
            throw new IllegalArgumentException("Network interface cannot be null");
        }

        // Open the network interface for capturing
        handle = networkInterface.openLive(
                65536, // Max capture size
                PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, // Promiscuous mode
                10 // Timeout in milliseconds
        );

        // Set up packet listener
        PacketListener listener = packet -> {
            if (packet != null) {
                processPacket(packet);
            }
        };

        // Start capturing in a separate thread
        executorService = Executors.newSingleThreadExecutor();
        isCapturing = true;

        executorService.submit(() -> {
            try {
                // Capture packets indefinitely
                while (isCapturing) {
                    handle.loop(-1, listener); // -1 means infinite loop
                }
            } catch (InterruptedException | NotOpenException | PcapNativeException e) {
                e.printStackTrace();
            } finally {
                stopCapture();
            }
        });
    }

    public void startCapture() throws PcapNativeException {
        if (nif == null) {
            throw new IllegalStateException("Network interface not selected");
        }

        // Open the network interface for capturing
        handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);

        // Set up packet listener
        PacketListener listener = packet -> {
            if (packet != null) {
                processPacket(packet);
            }
        };

        // Start capturing in a separate thread
        executorService = Executors.newSingleThreadExecutor();
        isCapturing = true;

        executorService.submit(() -> {
            // Capture packets indefinitely
            while (isCapturing) {
                try {
                    handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
                } catch (PcapNativeException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void processPacket(Packet packet) {
        try {
            IpV4Packet ipPacket = packet.get(IpV4Packet.class);
            if (ipPacket == null) {
                System.out.println("Packet is not an IPv4 packet: " + packet);
                return;
            }

            String sourceIp = ipPacket.getHeader().getSrcAddr().getHostAddress();
            String destIp = ipPacket.getHeader().getDstAddr().getHostAddress();
            String protocol = determineProtocol(packet);
            int sourcePort = extractSourcePort(packet);
            int destPort = extractDestinationPort(packet);

            int flagsInt = 0;
            int retransmissionCount = 0;
            TcpPacket tcpPacket = packet.get(TcpPacket.class);
            if (tcpPacket != null) {
                TcpHeader tcpHeader = tcpPacket.getHeader();
                flagsInt = (tcpHeader.getSyn() ? 0x02 : 0) |
                        (tcpHeader.getFin() ? 0x01 : 0) |
                        (tcpHeader.getRst() ? 0x04 : 0) |
                        (tcpHeader.getPsh() ? 0x08 : 0) |
                        (tcpHeader.getAck() ? 0x10 : 0) |
                        (tcpHeader.getUrg() ? 0x20 : 0);

                if (lastSeqNumbers.containsKey(sourceIp + destIp) &&
                        lastSeqNumbers.get(sourceIp + destIp) == tcpHeader.getSequenceNumber()) {
                    retransmissionCount = 1;
                }
                lastSeqNumbers.put(sourceIp + destIp, (long) tcpHeader.getSequenceNumber());
            }

            String flagsStr = getTcpFlags(flagsInt);
            String flowKey = sourceIp + ":" + sourcePort + "-" + destIp + ":" + destPort;
            LocalDateTime flowStart = flowStartTimes.computeIfAbsent(flowKey, k -> LocalDateTime.now());
            Duration flowDuration = Duration.between(flowStart, LocalDateTime.now());

            PacketInfo packetInfo = new PacketInfo(
                    sourceIp,
                    sourcePort,
                    destIp,
                    destPort,
                    protocol,
                    packet.length(),
                    packet.getClass().getSimpleName(),
                    LocalDateTime.now(),
                    flagsStr,
                    totalBytes.getOrDefault(flowKey, 0L) + packet.length(),
                    retransmissionCount,
                    connectionCount.getOrDefault(flowKey, 1),
                    flowDuration);

            System.out.println("TCP Flags: " + flagsStr);
            System.out.println(packetInfo);

            totalBytes.put(flowKey, packetInfo.getTotalBytes());
            if ((flagsInt & 0x02) != 0) {
                connectionCount.merge(flowKey, 1, Integer::sum);
            }

            repository.savePacketInfo(packetInfo);

            repository.savePacketInfo(packetInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTcpFlags(int flags) {
        StringBuilder flagStr = new StringBuilder();
        if ((flags & 0x02) != 0)
            flagStr.append("SYN ");
        if ((flags & 0x01) != 0)
            flagStr.append("FIN ");
        if ((flags & 0x04) != 0)
            flagStr.append("RST ");
        if ((flags & 0x08) != 0)
            flagStr.append("PSH ");
        if ((flags & 0x10) != 0)
            flagStr.append("ACK ");
        if ((flags & 0x20) != 0)
            flagStr.append("URG ");
        return flagStr.toString().trim();
    }

    public String determineProtocol(Packet packet) {
        // Check if the packet is an EthernetPacket
        if (packet instanceof EthernetPacket) {
            EthernetPacket ethernetPacket = (EthernetPacket) packet;
            Packet payload = ethernetPacket.getPayload();

            // Check if the payload is an IpV4Packet
            if (payload instanceof IpV4Packet) {
                IpV4Packet ipPacket = (IpV4Packet) payload;
                IpNumber protocol = ipPacket.getHeader().getProtocol();

                // Determine protocol based on IP number
                switch (protocol.valueAsString()) {
                    case "6":
                        return "TCP";
                    case "17":
                        return "UDP";
                    case "1":
                        return "ICMP";
                    case "2":
                        return "IGMP"; // Internet Group Management Protocol
                    case "89":
                        return "OSPF"; // Open Shortest Path First
                    case "50":
                        return "ESP"; // Encapsulating Security Payload
                    case "51":
                        return "AH"; // Authentication Header
                    case "41":
                        return "IPv6"; // IPv6 encapsulation
                    case "4":
                        return "IP-in-IP"; // IP in IP encapsulation
                    case "132":
                        return "SCTP"; // Stream Control Transmission Protocol
                    case "94":
                        return "IPIP"; // IP-within-IP Encapsulation Protocol
                    case "58":
                        return "ICMPv6"; // ICMP for IPv6
                    case "47":
                        return "GRE"; // Generic Routing Encapsulation
                    case "115":
                        return "L2TP"; // Layer Two Tunneling Protocol
                    default:
                        return "Unknown (" + protocol.valueAsString() + ")";
                }
            } else {
                System.out.println("Payload is not an IpV4Packet.");
            }
        } else {
            System.out.println("Packet is not an EthernetPacket.");
        }
        return "Unknown";
    }

    private int extractSourcePort(Packet packet) {
        try {
            if (packet.contains(TcpPacket.class)) {
                return packet.get(TcpPacket.class).getHeader().getSrcPort().valueAsInt();
            }
            if (packet.contains(UdpPacket.class)) {
                return packet.get(UdpPacket.class).getHeader().getSrcPort().valueAsInt();
            }
        } catch (Exception e) {
            // Ignore if port extraction fails
        }
        return -1;
    }

    private int extractDestinationPort(Packet packet) {
        try {
            if (packet.contains(TcpPacket.class)) {
                return packet.get(TcpPacket.class).getHeader().getDstPort().valueAsInt();
            }
            if (packet.contains(UdpPacket.class)) {
                return packet.get(UdpPacket.class).getHeader().getDstPort().valueAsInt();
            }
        } catch (Exception e) {
            // Ignore if port extraction fails
        }
        return -1;
    }

    public void stopCapture() {
        isCapturing = false;

        if (handle != null) {
            try {
                handle.breakLoop();
            } catch (NotOpenException e) {
                System.err.println("Handle is not open, cannot break loop: " + e.getMessage());
            }

            handle.close();
        }

        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private String extractHexStream(String rawData) {
        int hexStartIndex = rawData.indexOf("Hex stream:");
        if (hexStartIndex == -1) {
            return "";
        }

        return rawData.substring(hexStartIndex + 11).trim(); // Extract everything after "Hex stream:"
    }

    private String decodeHexToString(String hexStream) {
        if (hexStream == null || hexStream.isEmpty()) {
            return "";
        }

        StringBuilder decoded = new StringBuilder();
        String[] hexBytes = hexStream.split("\\s+"); // Split hex by spaces

        for (String hexByte : hexBytes) {
            try {
                int charCode = Integer.parseInt(hexByte, 16);
                if (charCode >= 32 && charCode <= 126) { // Only include printable ASCII characters
                    decoded.append((char) charCode);
                } else {
                    decoded.append('.'); // Non-printable characters as "."
                }
            } catch (NumberFormatException e) {
                decoded.append('.'); // Invalid hex byte
            }
        }

        return decoded.toString();
    }

}