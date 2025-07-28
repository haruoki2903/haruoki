package loinhuan;

import databaseconnector.DatabaseConnector;
import mainui.MainUI;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LoiNhuanUI extends JFrame {
    // --- Bảng màu và Font ---
    private static final Color COLOR_BACKGROUND = new Color(248, 248, 248);
    private static final Color COLOR_HEADER_BG = Color.WHITE;
    private static final Color COLOR_HEADER_TEXT = new Color(34, 34, 34);
    private static final Color COLOR_BORDER = new Color(224, 224, 224);
    private static final Color COLOR_PRIMARY_TEXT = new Color(51, 51, 51);
    private static final Color COLOR_SECONDARY_TEXT = new Color(102, 102, 102);
    private static final Color COLOR_SELECTION = new Color(220, 235, 250);
    private static final Color COLOR_PROFIT = new Color(3, 140, 68);
    private static final Color COLOR_LOSS = new Color(217, 30, 24);

    private static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 24);
    private static final Font FONT_TABLE_HEADER = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_TABLE_CELL = new Font("Arial", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_TAB = new Font("Arial", Font.BOLD, 16);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // --- Các thành phần giao diện và dữ liệu ---
    private JTable tableKyHienTai, tableLichSu;
    private DefaultTableModel modelKyHienTai, modelLichSu;
    private JLabel lblLoiNhuanTong;
    private JButton btnQuyetToan;
    private Connection conn;
    private MainUI mainUiRef;
    private long currentProfit = 0;

    public LoiNhuanUI(MainUI mainUi) {
        this.mainUiRef = mainUi;
        conn = DatabaseConnector.getConnection();
        
        setTitle("Báo Cáo & Quyết Toán Lợi Nhuận");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try { if (conn != null && !conn.isClosed()) conn.close(); } 
                catch (SQLException ex) { ex.printStackTrace(); }
            }
        });
        
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                refreshAllData();
            }
        });

        initUI();
    }
    
    private void initUI() {
        Container contentPane = getContentPane();
        contentPane.setBackground(COLOR_BACKGROUND);
        contentPane.setLayout(new BorderLayout());

        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_TAB);
        tabbedPane.setOpaque(false);
        
        tabbedPane.addTab("  Báo Cáo Kỳ Hiện Tại  ", createKyHienTaiPanel());
        tabbedPane.addTab("  Lịch Sử Quyết Toán  ", createLichSuPanel());
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 25, 25, 25));
        wrapper.setOpaque(false);
        wrapper.add(tabbedPane);

        contentPane.add(wrapper, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        JLabel titleLabel = new JLabel("BÁO CÁO LỢI NHUẬN");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_HEADER_TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton btnQuayVe = createStyledButton("Quay về");
        btnQuayVe.addActionListener(e -> dispose());
        headerPanel.add(btnQuayVe, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createKyHienTaiPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        
        JPanel topToolbar = new JPanel(new BorderLayout());
        topToolbar.setOpaque(false);
        topToolbar.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 5));
        
        lblLoiNhuanTong = new JLabel("Tổng lợi nhuận kỳ này: 0 đ");
        lblLoiNhuanTong.setFont(new Font("Arial", Font.BOLD, 20));
        topToolbar.add(lblLoiNhuanTong, BorderLayout.WEST);
        
        btnQuyetToan = createStyledButton("Quyết Toán Kỳ Này");
        btnQuyetToan.setBackground(COLOR_PROFIT);
        btnQuyetToan.setForeground(Color.WHITE);
        btnQuyetToan.addActionListener(e -> quyetToanKy());
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(btnQuyetToan);
        
        topToolbar.add(buttonWrapper, BorderLayout.EAST);
        
        panel.add(topToolbar, BorderLayout.NORTH);

        modelKyHienTai = new DefaultTableModel(new Object[]{"Ngày", "Tổng Tiền Nhập", "Tổng Tiền Xuất", "Lợi Nhuận Trong Ngày"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableKyHienTai = new JTable(modelKyHienTai);
        styleTable(tableKyHienTai, false);
        
        JScrollPane scrollPane = new JScrollPane(tableKyHienTai);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLichSuPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        JLabel instruction = new JLabel("Double-click vào một dòng để xem chi tiết các hóa đơn trong kỳ.");
        instruction.setFont(new Font("Arial", Font.ITALIC, 14));
        instruction.setForeground(COLOR_SECONDARY_TEXT);
        instruction.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        panel.add(instruction, BorderLayout.NORTH);
        
        modelLichSu = new DefaultTableModel(new Object[]{"Mã Kỳ", "Ngày Quyết Toán", "Thời Gian Kỳ", "Lợi Nhuận Kỳ"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableLichSu = new JTable(modelLichSu);
        styleTable(tableLichSu, false);
        
        tableLichSu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tableLichSu.getSelectedRow() != -1) {
                    String maKy = modelLichSu.getValueAt(tableLichSu.getSelectedRow(), 0).toString();
                    hienThiChiTietKy(maKy);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableLichSu);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void styleTable(JTable table, boolean isArchived) {
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(40);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(COLOR_PRIMARY_TEXT);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(COLOR_HEADER_BG);
        header.setForeground(COLOR_SECONDARY_TEXT);
        header.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        table.setDefaultRenderer(Object.class, new ProfitTableCellRenderer(isArchived));
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
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        return button;
    }

    public void refreshAllData() {
        taiDuLieuKyHienTai();
        taiDuLieuLichSu();
    }
    
    private void taiDuLieuKyHienTai() {
        modelKyHienTai.setRowCount(0);
        String sql = 
            "SELECT ngay, SUM(tong_nhap) AS tong_nhap_ngay, SUM(tong_xuat) AS tong_xuat_ngay FROM (" +
            "    SELECT ngay_nhap AS ngay, tong_tien AS tong_nhap, 0 AS tong_xuat FROM donnhaphang WHERE ma_ky IS NULL" +
            "    UNION ALL" +
            "    SELECT ngay_xuat AS ngay, 0 AS tong_nhap, tong_tien AS tong_xuat FROM donxuat WHERE ma_ky IS NULL" +
            ") AS daily_transactions " +
            "WHERE ngay IS NOT NULL GROUP BY ngay ORDER BY ngay DESC";
        
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            long tongLoiNhuanKy = 0;
            while (rs.next()) {
                Date ngay = rs.getDate("ngay");
                int nhap = rs.getInt("tong_nhap_ngay");
                int xuat = rs.getInt("tong_xuat_ngay");
                int loiNhuan = xuat - nhap;
                tongLoiNhuanKy += loiNhuan;
                modelKyHienTai.addRow(new Object[]{ngay, nhap, xuat, loiNhuan});
            }
            this.currentProfit = tongLoiNhuanKy;
            lblLoiNhuanTong.setText("Tổng lợi nhuận kỳ này: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(tongLoiNhuanKy));
            lblLoiNhuanTong.setForeground(tongLoiNhuanKy >= 0 ? COLOR_PROFIT : COLOR_LOSS);
            btnQuyetToan.setEnabled(modelKyHienTai.getRowCount() > 0);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu kỳ hiện tại: " + e.getMessage());
        }
    }
    
    private void taiDuLieuLichSu() {
        modelLichSu.setRowCount(0);
        String sql = "SELECT ma_ky, ngay_quyet_toan, tong_loi_nhuan, ngay_bat_dau, ngay_ket_thuc FROM quyet_toan_ky ORDER BY ngay_quyet_toan DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date ngayBatDau = rs.getDate("ngay_bat_dau");
                Date ngayKetThuc = rs.getDate("ngay_ket_thuc");
                String thoiGianKy = "N/A";
                if (ngayBatDau != null && ngayKetThuc != null) {
                    thoiGianKy = ngayBatDau.toLocalDate().format(DATE_FORMATTER) + " - " + ngayKetThuc.toLocalDate().format(DATE_FORMATTER);
                }
                
                modelLichSu.addRow(new Object[]{
                    rs.getString("ma_ky"), rs.getDate("ngay_quyet_toan"), thoiGianKy, rs.getInt("tong_loi_nhuan")
                });
            }
        } catch (SQLException e) {
             JOptionPane.showMessageDialog(this, "Lỗi tải lịch sử quyết toán: " + e.getMessage());
        }
    }

 // === PHIÊN BẢN MỚI SỬ DỤNG STORED PROCEDURE ===
    private void quyetToanKy() {
        if (modelKyHienTai.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu mới để quyết toán.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn quyết toán lợi nhuận cho kỳ này?", "Xác nhận Quyết toán", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 1. Lấy ngày bắt đầu và ngày kết thúc từ bảng trên giao diện
        Date ngayBatDau = (Date) modelKyHienTai.getValueAt(modelKyHienTai.getRowCount() - 1, 0);
        Date ngayKetThuc = (Date) modelKyHienTai.getValueAt(0, 0);

        // 2. Chuẩn bị câu lệnh để gọi Stored Procedure
        // Sử dụng cú pháp {call sp_thuc_hien_quyet_toan(?, ?)} hoặc SELECT
        String sqlCallProcedure = "SELECT public.sp_thuc_hien_quyet_toan(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sqlCallProcedure)) {
            // 3. Gán các tham số đầu vào cho procedure
            ps.setDate(1, ngayBatDau);
            ps.setDate(2, ngayKetThuc);

            // 4. Thực thi procedure và nhận kết quả trả về
            // Dùng executeQuery() vì procedure của chúng ta có trả về giá trị (mã kỳ)
            ResultSet rs = ps.executeQuery();

            // 5. Xử lý kết quả
            if (rs.next()) {
                String maKyMoi = rs.getString(1); // Lấy giá trị trả về từ cột đầu tiên
                JOptionPane.showMessageDialog(this, "Đã quyết toán thành công kỳ " + maKyMoi, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                // 6. Tải lại dữ liệu để cập nhật giao diện
                refreshAllData();
            }

        } catch (SQLException e) {
            // 7. Bắt lỗi từ CSDL (bao gồm cả các lỗi RAISE EXCEPTION từ procedure)
            // Ví dụ: "Không có dữ liệu giao dịch mới..."
            String errorMessage = e.getMessage();
            // Lỗi từ PostgreSQL thường có dạng "ERROR: <thông báo của bạn> \n  Where: PL/pgSQL function..."
            // Chúng ta có thể làm cho nó thân thiện hơn
            if (errorMessage.contains("ERROR:")) {
               errorMessage = errorMessage.substring(errorMessage.indexOf("ERROR:") + 6).split("\n")[0].trim();
            }
            
            JOptionPane.showMessageDialog(this, "Lỗi khi quyết toán: " + errorMessage, "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    private void hienThiChiTietKy(String maKy) {
        JDialog dialog = new JDialog(this, "Chi Tiết Quyết Toán Kỳ: " + maKy, true);
        dialog.setSize(1200, 800);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(COLOR_BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));
        dialog.add(mainPanel);

        // --- Panel Tóm Tắt (Summary Panel) ---
        JPanel summaryPanel = new JPanel(new BorderLayout(0, 15));
        summaryPanel.setOpaque(false);
        
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 20, 0)); // 1 hàng, 3 cột, khoảng cách 20
        kpiPanel.setOpaque(false);

        long totalNhap = 0, totalXuat = 0, tongLoiNhuan = 0;
        
        // Lấy thông tin tổng lợi nhuận, ngày bắt đầu, ngày kết thúc
        String sqlKy = "SELECT * FROM quyet_toan_ky WHERE ma_ky = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlKy)) {
            ps.setString(1, maKy);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tongLoiNhuan = rs.getLong("tong_loi_nhuan");
                Date ngayBatDau = rs.getDate("ngay_bat_dau");
                Date ngayKetThuc = rs.getDate("ngay_ket_thuc");
                
                JLabel dateRangeLabel = new JLabel(
                    "Kỳ quyết toán từ " + ngayBatDau.toLocalDate().format(DATE_FORMATTER) + 
                    " đến " + ngayKetThuc.toLocalDate().format(DATE_FORMATTER),
                    SwingConstants.CENTER
                );
                dateRangeLabel.setFont(new Font("Arial", Font.ITALIC, 15));
                dateRangeLabel.setForeground(COLOR_SECONDARY_TEXT);
                summaryPanel.add(dateRangeLabel, BorderLayout.SOUTH);
            }
        } catch (SQLException e) {
            summaryPanel.add(new JLabel("Không thể tải thông tin kỳ."), BorderLayout.NORTH);
        }
        
        // --- Bảng Đơn Nhập ---
        DefaultTableModel modelNhap = new DefaultTableModel(new String[]{"Mã Nhập", "Ngày Nhập", "Tổng Tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        String sqlNhap = "SELECT ma_nhap, ngay_nhap, tong_tien FROM donnhaphang WHERE ma_ky = ? ORDER BY ngay_nhap";
        try(PreparedStatement ps = conn.prepareStatement(sqlNhap)) {
            ps.setString(1, maKy);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int tongTienNhap = rs.getInt("tong_tien");
                totalNhap += tongTienNhap;
                modelNhap.addRow(new Object[]{rs.getString("ma_nhap"), rs.getDate("ngay_nhap"), tongTienNhap});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        // --- Bảng Đơn Xuất ---
        DefaultTableModel modelXuat = new DefaultTableModel(new String[]{"Mã Xuất", "Ngày Xuất", "Khách Hàng", "Tổng Tiền"}, 0){
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        String sqlXuat = "SELECT dx.ma_xuat, dx.ngay_xuat, kh.ten, dx.tong_tien FROM donxuat dx LEFT JOIN khachhang kh ON dx.ma_khach = kh.ma_khach WHERE dx.ma_ky = ? ORDER BY dx.ngay_xuat";
        try(PreparedStatement ps = conn.prepareStatement(sqlXuat)) {
            ps.setString(1, maKy);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int tongTienXuat = rs.getInt("tong_tien");
                totalXuat += tongTienXuat;
                modelXuat.addRow(new Object[]{rs.getString("ma_xuat"), rs.getDate("ngay_xuat"), rs.getString("ten"), tongTienXuat});
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Thêm các thẻ KPI vào panel
        kpiPanel.add(createKpiCard("Tổng Tiền Nhập", totalNhap, new Color(211, 84, 0))); // Màu cam
        kpiPanel.add(createKpiCard("Tổng Tiền Xuất", totalXuat, new Color(41, 128, 185))); // Màu xanh dương
        kpiPanel.add(createKpiCard("Lợi Nhuận Ròng", tongLoiNhuan, tongLoiNhuan >= 0 ? COLOR_PROFIT : COLOR_LOSS));
        
        summaryPanel.add(kpiPanel, BorderLayout.CENTER);
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        
        // --- Tabbed Pane ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_TAB);

        JTable tableNhap = new JTable(modelNhap);
        styleTable(tableNhap, true);
        tabbedPane.addTab("Đơn Nhập Trong Kỳ (" + modelNhap.getRowCount() + ")", new JScrollPane(tableNhap));
        
        JTable tableXuat = new JTable(modelXuat);
        styleTable(tableXuat, true);
        tabbedPane.addTab("Đơn Xuất Trong Kỳ (" + modelXuat.getRowCount() + ")", new JScrollPane(tableXuat));

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    /**
     * Phương thức pomocniczy để tạo một "thẻ" hiển thị chỉ số KPI (Key Performance Indicator).
     * @param title Tiêu đề của chỉ số (ví dụ: "Tổng Tiền Nhập").
     * @param value Giá trị của chỉ số.
     * @param valueColor Màu sắc cho giá trị.
     * @return một JPanel đã được định dạng.
     */
    private JPanel createKpiCard(String title, long value, Color valueColor) {
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 4, 0, valueColor), // Đường gạch chân màu
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        cardPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setForeground(COLOR_SECONDARY_TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(value));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        cardPanel.add(valueLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        cardPanel.add(titleLabel);

        return cardPanel;
    }
    class ProfitTableCellRenderer extends DefaultTableCellRenderer {
        private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        private final boolean isArchived;

        public ProfitTableCellRenderer(boolean isArchived) {
            this.isArchived = isArchived;
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Number) {
                setText(currencyFormatter.format(((Number) value).longValue()));
                setHorizontalAlignment(SwingConstants.CENTER);
            } else if (value instanceof Date) {
                setText(((Date) value).toLocalDate().format(DATE_FORMATTER));
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setText(value != null ? value.toString() : "");
                if (table.getModel().getColumnCount() > 2 && table.getModel().getColumnName(2).equals("Khách Hàng")) {
                     setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }
            }

            setForeground(isArchived ? COLOR_SECONDARY_TEXT : COLOR_PRIMARY_TEXT);
            setFont(FONT_TABLE_CELL);

            int modelColumn = table.convertColumnIndexToModel(column);
            if ( (table.getModel() == modelKyHienTai && modelColumn == 3) || 
                 (table.getModel() == modelLichSu && modelColumn == 3) ) {
                if (value instanceof Number) {
                    long loiNhuan = ((Number) value).longValue();
                    setForeground(loiNhuan >= 0 ? COLOR_PROFIT : COLOR_LOSS);
                    setFont(new Font("Arial", Font.BOLD, 15));
                }
            }
            
            if (isSelected) {
                 setForeground(table.getSelectionForeground());
                 setBackground(table.getSelectionBackground());
            } else {
                 setBackground(row % 2 == 0 ? COLOR_HEADER_BG : COLOR_BACKGROUND);
            }
            return this;
        }
    }
}