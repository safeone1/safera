package com.ids.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.ids.model.PacketInfo;

public class PacketInfoRepository {
    private static final String DB_URL = "jdbc:sqlite:network_monitor.db";

    public void initializeTable() {
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS packet_info (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "source_ip TEXT NOT NULL, " +
                "source_port INTEGER NOT NULL, " +
                "destination_ip TEXT NOT NULL, " +
                "destination_port INTEGER NOT NULL, " +
                "protocol TEXT, " +
                "packet_length LONG, " +
                "packet_type TEXT, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "flags TEXT, " +
                "total_bytes LONG, " +
                "retransmission_count INTEGER, " +
                "connection_count INTEGER, " +
                "flow_duration_seconds LONG" +
                ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCreateTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePacketInfo(PacketInfo packetInfo) {
        String sql = "INSERT INTO packet_info " +
                "(source_ip, source_port, destination_ip, destination_port, " +
                "protocol, packet_length, packet_type, timestamp, " +
                "flags, total_bytes, retransmission_count, connection_count, flow_duration_seconds) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, packetInfo.getSourceIp());
            pstmt.setInt(2, packetInfo.getSourcePort());
            pstmt.setString(3, packetInfo.getDestinationIp());
            pstmt.setInt(4, packetInfo.getDestinationPort());
            pstmt.setString(5, packetInfo.getProtocol());
            pstmt.setLong(6, packetInfo.getPacketLength());
            pstmt.setString(7, packetInfo.getPacketType());
            pstmt.setTimestamp(8, Timestamp.valueOf(packetInfo.getTimestamp()));
            pstmt.setString(9, packetInfo.getFlags());
            pstmt.setLong(10, packetInfo.getTotalBytes());
            pstmt.setInt(11, packetInfo.getRetransmissionCount());
            pstmt.setInt(12, packetInfo.getConnectionCount());
            pstmt.setLong(13, packetInfo.getFlowDuration().getSeconds());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PacketInfo> getPacketsByProtocol(String protocol) {
        List<PacketInfo> packets = new ArrayList<>();
        String sql = "SELECT * FROM packet_info WHERE protocol = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, protocol);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    packets.add(createPacketInfoFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return packets;
    }

    public List<PacketInfo> getAllPackets() {
        List<PacketInfo> packets = new ArrayList<>();
        String sql = "SELECT * FROM packet_info ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                packets.add(createPacketInfoFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return packets;
    }

    public List<PacketInfo> getPacketsByProtocolAndData(String protocol, String searchWord) {
        List<PacketInfo> packets = new ArrayList<>();
        String sql = "SELECT * FROM packet_info WHERE protocol = ? AND decoded_content LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, protocol);
            pstmt.setString(2, "%" + searchWord + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    packets.add(new PacketInfo(
                            rs.getString("source_ip"),
                            rs.getInt("source_port"),
                            rs.getString("destination_ip"),
                            rs.getInt("destination_port"),
                            rs.getString("protocol"),
                            rs.getLong("packet_length"),
                            rs.getString("packet_type"),
                            rs.getTimestamp("timestamp").toLocalDateTime(),
                            rs.getString("flags"),
                            rs.getLong("total_bytes"),
                            rs.getInt("retransmission_count"),
                            rs.getInt("connection_count"),
                            Duration.ofSeconds(rs.getLong("flow_duration_seconds"))));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return packets;
    }

    private PacketInfo createPacketInfoFromResultSet(ResultSet rs) throws SQLException {
        return new PacketInfo(
                rs.getString("source_ip"),
                rs.getInt("source_port"),
                rs.getString("destination_ip"),
                rs.getInt("destination_port"),
                rs.getString("protocol"),
                rs.getLong("packet_length"),
                rs.getString("packet_type"),
                rs.getTimestamp("timestamp").toLocalDateTime(),
                rs.getString("flags"),
                rs.getLong("total_bytes"),
                rs.getInt("retransmission_count"),
                rs.getInt("connection_count"),
                Duration.ofSeconds(rs.getLong("flow_duration_seconds")));
    }

    public void truncatePacketInfoTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM packet_info");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='packet_info'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}