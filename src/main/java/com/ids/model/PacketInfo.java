package com.ids.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class PacketInfo {
    private Long id;
    private String sourceIp;
    private int sourcePort;
    private String destinationIp;
    private int destinationPort;
    private String protocol;
    private long packetLength;
    private String packetType;
    private LocalDateTime timestamp;
    private String flags;
    private long totalBytes;
    private int retransmissionCount;
    private int connectionCount;
    private Duration flowDuration;

    public PacketInfo(String sourceIp, int sourcePort, String destinationIp,
            int destinationPort, String protocol, long packetLength,
            String packetType, LocalDateTime timestamp,
            String flags, long totalBytes, int retransmissionCount,
            int connectionCount, Duration flowDuration) {
        this.sourceIp = sourceIp;
        this.sourcePort = sourcePort;
        this.destinationIp = destinationIp;
        this.destinationPort = destinationPort;
        this.protocol = protocol;
        this.packetLength = packetLength;
        this.packetType = packetType;
        this.timestamp = timestamp;
        this.flags = flags;
        this.totalBytes = totalBytes;
        this.retransmissionCount = retransmissionCount;
        this.connectionCount = connectionCount;
        this.flowDuration = flowDuration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public long getPacketLength() {
        return packetLength;
    }

    public void setPacketLength(long packetLength) {
        this.packetLength = packetLength;
    }

    public String getPacketType() {
        return packetType;
    }

    public void setPacketType(String packetType) {
        this.packetType = packetType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public int getRetransmissionCount() {
        return retransmissionCount;
    }

    public void setRetransmissionCount(int retransmissionCount) {
        this.retransmissionCount = retransmissionCount;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public Duration getFlowDuration() {
        return flowDuration;
    }

    public void setFlowDuration(Duration flowDuration) {
        this.flowDuration = flowDuration;
    }

    @Override
    public String toString() {
        return "PacketInfo{" +
                "id=" + id +
                ", sourceIp='" + sourceIp + '\'' +
                ", sourcePort=" + sourcePort +
                ", destinationIp='" + destinationIp + '\'' +
                ", destinationPort=" + destinationPort +
                ", protocol='" + protocol + '\'' +
                ", packetLength=" + packetLength +
                ", packetType='" + packetType + '\'' +
                ", timestamp=" + timestamp +
                ", flags=" + flags +
                ", totalBytes=" + totalBytes +
                ", retransmissionCount=" + retransmissionCount +
                ", connectionCount=" + connectionCount +
                ", flowDuration=" + flowDuration +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PacketInfo that = (PacketInfo) o;
        return sourcePort == that.sourcePort &&
                destinationPort == that.destinationPort &&
                packetLength == that.packetLength &&
                flags == that.flags &&
                totalBytes == that.totalBytes &&
                retransmissionCount == that.retransmissionCount &&
                connectionCount == that.connectionCount &&
                Objects.equals(id, that.id) &&
                Objects.equals(sourceIp, that.sourceIp) &&
                Objects.equals(destinationIp, that.destinationIp) &&
                Objects.equals(protocol, that.protocol) &&
                Objects.equals(packetType, that.packetType) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(flowDuration, that.flowDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourceIp, sourcePort, destinationIp, destinationPort,
                protocol, packetLength, packetType, timestamp, flags, totalBytes,
                retransmissionCount, connectionCount, flowDuration);
    }
}