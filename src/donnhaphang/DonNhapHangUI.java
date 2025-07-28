package donnhaphang;

import javax.swing.*;
import javax.swing.table.*;
import databaseconnector.DatabaseConnector;
import chitietnhaphang.ChiTietNhapHangUI;
import khohang.KhoHangUI;
import khohang.NhapHangCart;
import mainui.MainUI;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DonNhapHangUI extends JFrame {
    private static final Font FONT_BASE = new Font("Arial", Font.PLAIN, 15);
    private static final Font FONT_BOLD = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 24);
    private static final Font FONT_TABLE_HEADER = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_TABLE_CELL = new Font("Arial", Font.PLAIN, 15);
    private static final Color COLOR_BACKGROUND = new Color(248, 248, 248);
    private static final Color COLOR_HEADER_BG = Color.WHITE;
    private static final Color COLOR_HEADER_TEXT = new Color(34, 34, 34);
    private static final Color COLOR_BORDER = new Color(224, 224, 224);
    private static final Color COLOR_PRIMARY_TEXT = new Color(51, 51, 51);
    private static final Color COLOR_SECONDARY_TEXT = new Color(102, 102, 102);
    private static final Color COLOR_SELECTION = new Color(220, 235, 250);
    private static final Color COLOR_ARCHIVED_ROW_BG = new Color(245, 245, 245);
    private static final Color COLOR_ARCHIVED_ROW_FG = new Color(150, 150, 150);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTable table;
    private DefaultTableModel tableModel;
    private Connection conn;
    private MainUI mainUiRef;
    private KhoHangUI khoHangUiRef;

    public DonNhapHangUI(MainUI mainUi) {
        this(mainUi, null);
    }
    
    public DonNhapHangUI(MainUI mainUi, KhoHangUI khoHangUi) {
        this.mainUiRef = mainUi;
        this.khoHangUiRef = khoHangUi;
        conn = DatabaseConnector.getConnection();
        
        setTitle("Quản Lý Đơn Nhập Hàng");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                try { if (conn != null && !conn.isClosed()) conn.close(); }
                catch (SQLException ex) { ex.printStackTrace(); }
            }
        });
        initUI();
        refreshData();
    }

    private void initUI() {
        Container contentPane = getContentPane();
        contentPane.setBackground(COLOR_BACKGROUND);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);
        contentPane.add(createMainContentPanel(), BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        JLabel titleLabel = new JLabel("DANH SÁCH ĐƠN NHẬP HÀNG");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_HEADER_TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton btnTaoTuDanhSach = createStyledButton("Tạo đơn từ DS chờ");
        btnTaoTuDanhSach.addActionListener(e -> taoDonTuDanhSachCho());
        
        JButton btnThemDon = createStyledButton("Tạo đơn thủ công");
        btnThemDon.setBackground(COLOR_HEADER_TEXT);
        btnThemDon.setForeground(Color.WHITE);
        btnThemDon.addActionListener(e -> taoDonNhapMoiTuDong());
        
        JButton btnXoaDon = createStyledButton("Xóa đơn đã chọn");
        btnXoaDon.addActionListener(e -> xoaDonNhapDaChon());
        JButton btnBack = createStyledButton("Quay về");
        btnBack.addActionListener(e -> dispose());
        
        buttonPanel.add(btnTaoTuDanhSach);
        buttonPanel.add(btnThemDon);
        buttonPanel.add(btnXoaDon);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(btnBack);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));
        
        JLabel instructionLabel = new JLabel("Double-click vào một dòng để xem hoặc chỉnh sửa chi tiết đơn hàng. Các đơn màu xám đã được quyết toán.");
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        instructionLabel.setForeground(COLOR_SECONDARY_TEXT);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 0));
        
        mainContentPanel.add(instructionLabel, BorderLayout.NORTH);
        mainContentPanel.add(createTablePanel(), BorderLayout.CENTER);
        
        return mainContentPanel;
    }
    
    private JScrollPane createTablePanel() {
        String[] columns = {"Mã Nhập", "Ngày Nhập", "Tổng Tiền", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        String maNhap = tableModel.getValueAt(selectedRow, 0).toString();
                        new ChiTietNhapHangUI(mainUiRef, DonNhapHangUI.this, maNhap).setVisible(true);
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        return scrollPane;
    }
    
    private void styleTable() {
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(40);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(COLOR_PRIMARY_TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(COLOR_HEADER_BG);
        header.setForeground(COLOR_SECONDARY_TEXT);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        table.setDefaultRenderer(Object.class, new ArchivedRowRenderer());
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(COLOR_HEADER_BG);
        button.setForeground(COLOR_PRIMARY_TEXT);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), 
            BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        return button;
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        String sql = "SELECT ma_nhap, ngay_nhap, tong_tien, ma_ky FROM donnhaphang ORDER BY ngay_nhap DESC, ma_nhap DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String trangThai = (rs.getString("ma_ky") == null) ? "Chưa quyết toán" : "Đã quyết toán";
                tableModel.addRow(new Object[]{
                        rs.getString("ma_nhap"), 
                        rs.getDate("ngay_nhap"), 
                        rs.getLong("tong_tien"),
                        trangThai
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu đơn nhập hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void taoDonNhapMoiTuDong() {
        String maNhapMoi = "";
        try {
            String sqlGetLastId = "SELECT ma_nhap FROM donnhaphang ORDER BY CAST(SUBSTRING(ma_nhap, 3) AS INTEGER) DESC LIMIT 1";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlGetLastId);
            int soThuTu = 1;
            if (rs.next()) {
                String lastId = rs.getString("ma_nhap");
                if (lastId != null && lastId.matches("NH\\d+")) {
                    try { soThuTu = Integer.parseInt(lastId.substring(2)) + 1; } 
                    catch (NumberFormatException ex) { soThuTu = 1; }
                }
            }
            maNhapMoi = String.format("NH%03d", soThuTu);
            String sqlInsert = "INSERT INTO donnhaphang (ma_nhap, ngay_nhap, tong_tien) VALUES (?, ?, 0)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                pstmt.setString(1, maNhapMoi);
                pstmt.setDate(2, Date.valueOf(java.time.LocalDate.now()));
                pstmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Đã tạo đơn hàng mới '" + maNhapMoi + "'.\nBây giờ hãy thêm sản phẩm vào đơn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            
            refreshData();
            new ChiTietNhapHangUI(mainUiRef, this, maNhapMoi).setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo đơn hàng mới: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaDonNhapDaChon() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn hàng để xóa.", "Chưa chọn đơn hàng", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maNhapCanXoa = table.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(
            this, "Bạn có chắc chắn muốn xóa đơn hàng '" + maNhapCanXoa + "' không?\nThao tác này sẽ tự động hoàn trả số lượng đã nhập.",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) { return; }
        
        String sql = "DELETE FROM donnhaphang WHERE ma_nhap = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhapCanXoa);
            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0) {
                 JOptionPane.showMessageDialog(this, "Đã xóa thành công đơn hàng '" + maNhapCanXoa + "'.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                 if (mainUiRef != null) mainUiRef.refreshKpis();
                 refreshData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa đơn hàng: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void taoDonTuDanhSachCho() {
        NhapHangCart cart = NhapHangCart.getInstance();
        if (cart.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Danh sách chờ nhập hàng đang trống.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Tạo đơn nhập hàng mới với " + cart.getItemCount() + " sản phẩm từ danh sách chờ?",
            "Xác nhận tạo đơn", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            String maNhapMoi;
            String sqlGetLastId = "SELECT ma_nhap FROM donnhaphang ORDER BY CAST(SUBSTRING(ma_nhap, 3) AS INTEGER) DESC LIMIT 1";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlGetLastId)) {
                int soThuTu = 1;
                if (rs.next()) {
                    String lastId = rs.getString("ma_nhap");
                    if (lastId != null && lastId.matches("NH\\d+")) {
                        soThuTu = Integer.parseInt(lastId.substring(2)) + 1;
                    }
                }
                maNhapMoi = String.format("NH%03d", soThuTu);
            }

            conn.setAutoCommit(false);
            
            String sqlInsertDon = "INSERT INTO donnhaphang (ma_nhap, ngay_nhap, tong_tien) VALUES (?, ?, 0)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertDon)) {
                pstmt.setString(1, maNhapMoi);
                pstmt.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now()));
                pstmt.executeUpdate();
            }

            String sqlInsertChiTiet = "INSERT INTO chitietnhaphang (ma_nhap, ma_san_pham, so_luong, don_gia_nhap) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertChiTiet)) {
                for (NhapHangCart.CartItem item : cart.getItems().values()) {
                    pstmt.setString(1, maNhapMoi);
                    pstmt.setString(2, item.maSP);
                    pstmt.setInt(3, item.soLuong);
                    pstmt.setInt(4, item.giaNhap);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            
            conn.commit();

            JOptionPane.showMessageDialog(this, "Đã tạo thành công đơn hàng '" + maNhapMoi + "' với " + cart.getItemCount() + " sản phẩm.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            cart.clearCart();
            refreshData();
            if (mainUiRef != null) mainUiRef.refreshKpis();

            // *** THAY ĐỔI: Gọi refreshAllData() trên KhoHangUI nếu tồn tại
            if (khoHangUiRef != null) {
                khoHangUiRef.refreshAllData();
            }

            new ChiTietNhapHangUI(mainUiRef, this, maNhapMoi).setVisible(true);

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo đơn hàng từ danh sách chờ: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    class ArchivedRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(SwingConstants.CENTER);
            if (column == 2) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Number) {
                    setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(value));
                }
            } else if (value instanceof Date) {
                 setText(((Date)value).toLocalDate().format(DATE_FORMATTER));
            } else {
                 setText(value.toString());
            }

            String trangThai = (String) table.getModel().getValueAt(row, 3);
            if ("Đã quyết toán".equals(trangThai)) {
                if (isSelected) {
                    setBackground(COLOR_SELECTION.darker());
                    setForeground(COLOR_ARCHIVED_ROW_FG.brighter());
                } else {
                    setBackground(COLOR_ARCHIVED_ROW_BG);
                    setForeground(COLOR_ARCHIVED_ROW_FG);
                }
            } else {
                if (isSelected) {
                    setBackground(COLOR_SELECTION);
                    setForeground(COLOR_PRIMARY_TEXT);
                } else {
                    setBackground(row % 2 == 0 ? COLOR_HEADER_BG : COLOR_BACKGROUND);
                    setForeground(COLOR_PRIMARY_TEXT);
                }
            }
            
            return this;
        }
    }
}